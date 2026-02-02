<script>
  import { getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import { selectedGroup } from "$lib/stores/searches";
  import { userId } from "$lib/stores/user_stores";
  import { apiFetch } from "$lib/utils/fetch";
  import { _ } from "svelte-i18n";
  import { createEventDispatcher } from "svelte";
  import { ZipReader, BlobReader, BlobWriter } from "@zip.js/zip.js";
  import { onMount, tick } from "svelte";

  export let isOpen = false;
  export let close;
  export let zipFile;
  export let references;
  export let missingFiles;


  const dispatch = createEventDispatcher();

  function erase() {
    referenceColumnName = "";
    mappingColumnName = "";
  }

  function cancel() {
    dispatch("cancel", { message: $_("modals.add_group.operation") });
    close();
  }

  let posX = 0,
    posY = 0,
    offsetX = 0,
    offsetY = 0,
    isDragging = false;

  function startDrag(event) {
    isDragging = true;
    offsetX = event.clientX - posX;
    offsetY = event.clientY - posY;
  }

  function drag(event) {
    if (isDragging) {
      posX = event.clientX - offsetX;
      posY = event.clientY - offsetY;
    }
  }

  function stopDrag() {
    isDragging = false;
  }

  function wait(ms) {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  // Track upload status for each reference
  let uploadStatus = [];
  let isUploading = false;
  let overallProgress = 0;
  let currentUploadIndex = -1;
  let statusContainerRef;
  let fileRefs = [];

  // Initialize upload status for each reference
  $: if (references && references.length > 0 && uploadStatus.length === 0) {
    uploadStatus = references.map(() => ({ status: "pending", message: "" }));
    fileRefs = Array(references.length).fill(null);
  }

  // Auto-scroll to the currently uploading file
  async function scrollToCurrentFile() {
    await tick(); // Wait for DOM update
    if (currentUploadIndex >= 0 && fileRefs[currentUploadIndex] && statusContainerRef) {
      const fileElement = fileRefs[currentUploadIndex];
      const container = statusContainerRef;

      // Calculate scroll position to show the current element
      const fileRect = fileElement.getBoundingClientRect();
      const containerRect = container.getBoundingClientRect();

      // Check if element is not fully visible
      if (fileRect.bottom > containerRect.bottom || fileRect.top < containerRect.top) {
        // Scroll the element into view with some padding
        const scrollTop = fileElement.offsetTop - container.offsetTop - 20;
        container.scrollTo({
          top: scrollTop,
          behavior: "smooth",
        });
      }
    }
  }
  async function uploadFile(index) {
    currentUploadIndex = index;

    // NEW reference format: [reference, filename, filePath]
    const [reference, filename, filePath] = references[index];

    uploadStatus[index] = { status: "uploading", message: $_("import_pages.svelte.uploading") };
    uploadStatus = [...uploadStatus];

    await scrollToCurrentFile();

    const formData = new FormData();
    try {
      // Extract the file as a Blob from the zip (zip.js)
      const fileExtracted = await extractZipEntryBlob(zipFile, filePath);

      const size = fileExtracted.size;
      formData.append("picture", fileExtracted, filePath); // filename is useful
      formData.append("reference", reference);

      const response = await apiFetch("/api/pictures/instruments/upload-with-reference", {
        method: "POST",
        body: formData,
      });
      /*
      if (!response.ok) {
        const contentType = response.headers.get("Content-Type") || "";
        let errorMessage;

        if (contentType.includes("application/json")) {
          const errorJson = await response.json();
          errorMessage = errorJson.message || response.statusText;
        } else {
          errorMessage = response.statusText || `Erreur ${response.status}`;
        }
      
        throw new Error(errorMessage);
      }*/
      // Update status to success
      uploadStatus[index] = {
        status: "success",
        message: $_('import_pages.svelte.success') + ` ${filename} for ${reference}`,
      };
      uploadStatus = [...uploadStatus]; // Trigger reactivity

      return { success: true, size };
    } catch (error) {
      console.error("Error uploading file:", error);
      // Update status to error
      uploadStatus[index] = {
        status: "error",
        message: $_('import_pages.svelte.upload_fail') + `: ${error.message || 'Unknown error'}`,
      };
      // uploadStatus = [...uploadStatus]; // Trigger reactivity

      return { success: false, size: 0 };
    }
  }

  // Function to retry a specific file upload
  async function retryUpload(index) {
    if (isUploading) return; // Prevent retry during active upload process

    const result = await uploadFile(index);

    if (result.success) {
      toast.push($_('import_pages.svelte.retry_sucess'), {
        theme: {
          "--toastBackground": "#10B981",
          "--toastBarBackground": "#ffffff",
        },
      });
    } else {
      toast.push($_('import_pages.svelte.fail'), {
        theme: {
          "--toastBackground": "#EF4444",
          "--toastBarBackground": "#ffffff",
        },
      });
    }
  }

  async function submitForm(event) {
    event.preventDefault();
    isUploading = true;
    let cumulativeSize = 0;
    let successCount = 0;
    let failCount = 0;

    // Ensure feedback section is open
    isFeedbackOpen = true;
    await tick();

    for (const [index, [reference, filename, filepath]] of references.entries()) {
      // Skip files that have already been successfully uploaded
      if (uploadStatus[index].status === "success") {
        successCount++;
        continue;
      }

      const result = await uploadFile(index);

      if (result.success) {
        successCount++;
        cumulativeSize += result.size;
      } else {
        failCount++;
        await wait(500);
      }

      // Update overall progress
      overallProgress = Math.round(((index + 1) / references.length) * 100);

      // If we've uploaded more than 10MB, wait a bit to avoid overwhelming the server
      if (cumulativeSize > 10000000) {
        cumulativeSize = 0;
        //   await wait(15000);
      }
    }

    // Reset current upload index when done
    currentUploadIndex = -1;

    // Show toast notification with summary
    toast.push(`Upload complete: ${successCount} successful, ${failCount} failed`, {
      theme: {
        "--toastBackground": successCount > 0 ? "#10B981" : "#EF4444",
        "--toastBarBackground": "#ffffff",
      },
    });

    isUploading = false;
  }

  // Accordion state
  let isReferencesOpen = false;
  let isErrorsOpen = true;
  let isFeedbackOpen = true;

  // Toggle accordion sections
  const toggleReferences = () => (isReferencesOpen = !isReferencesOpen);
  const toggleErrors = () => (isErrorsOpen = !isErrorsOpen);
  const toggleFeedback = () => (isFeedbackOpen = !isFeedbackOpen);

  // Update bindings when references change
  $: if (references && references.length) {
    fileRefs = Array(references.length).fill(null);
  }


  async function extractZipEntryBlob(zipFile, filePath) {
    const reader = new ZipReader(new BlobReader(zipFile));
    try {
      const entries = await reader.getEntries();
      const entry = entries.find((e) => e.filename === filePath);
      if (!entry) {
        throw new Error(`Entry not found in zip: ${filePath}`);
      }
      const blob = await entry.getData(new BlobWriter());
      return blob;
    } finally {
      await reader.close();
    }
  }

</script>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div
      class="fixed inset-0 z-10 flex items-center justify-center"
      onmousemove={drag}
      onmouseup={stopDrag}
    >
      <div
        class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
        style="transform: translate({posX}px, {posY}px);"
      >
        <div
          class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg select-none"
          style="cursor: move;"
          onmousedown={startDrag}
        >
          <h2 class="text-2xl font-bold text-teal-500 text-center">{$_("import_pages.svelte.pictures_import")}</h2>
        </div>
        <form onsubmit={submitForm} class="bg-gray-100 p-6 rounded-lg">
          <div class="w-full mx-auto space-y-2">
            <!-- References Accordion -->
            <div class="border rounded-md overflow-hidden">
              <!-- svelte-ignore a11y_click_events_have_key_events -->
              <div
                class="flex justify-between items-center p-4 bg-gray-50 cursor-pointer"
                onclick={toggleReferences}
              >
                <h3 class="font-medium">{$_("import_pages.svelte.found_files")} ({references.length})</h3>
                <svg
                  class="w-5 h-5 transition-transform duration-200 {isReferencesOpen
                    ? 'rotate-180'
                    : ''}"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  ></path>
                </svg>
              </div>

              {#if isReferencesOpen}
                <div
                  class="flex flex-row flex-wrap max-w-full mx-auto mb-4 p-4 bg-green-100 overflow-y-auto max-h-60"
                >
                  <!-- Preview all the pictures and display errors -->
                  {#each references as [reference, filename], index}
                    <div
                      class="size-1/3 flex items-center justify-between p-3 bg-white border rounded-md flex-row"
                    >
                      <div class="flex items-center text-sm">
                        <p class="text-gray-700">{reference + " " + $_("import_pages.svelte.linked_to") + " " + filename}</p>
                      </div>
                    </div>
                  {/each}
                </div>
              {/if}
            </div>

            <!-- Errors Accordion -->
            <div class="border rounded-md overflow-hidden">
              <!-- svelte-ignore a11y_click_events_have_key_events -->
              <div
                class="flex justify-between items-center p-4 bg-gray-50 cursor-pointer"
                onclick={toggleErrors}
              >
                <h3 class="font-medium">{$_("import_pages.svelte.missing_files")} ({missingFiles.length})</h3>
                <svg
                  class="w-5 h-5 transition-transform duration-200 {isErrorsOpen
                    ? 'rotate-180'
                    : ''}"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 9l-7 7-7-7"
                  ></path>
                </svg>
              </div>

              {#if isErrorsOpen}
                <div class="flex flex-col p-4 gap-4 bg-red-100 overflow-y-auto max-h-60">
                  <!-- Preview all the pictures and display errors -->
                  <ul class="list-disc list-inside">
                    {#each missingFiles as [reference, filename]}
                      <li class="text-red-700 text-sm">
                        {$_("import_pages.svelte.file") +
                          " " +
                          filename +
                          " " +
                          $_("import_pages.svelte.for") +
                          " " +
                          reference +
                          " " +
                          $_("import_pages.svelte.not_found")}
                      </li>
                    {/each}
                  </ul>
                </div>
              {/if}
            </div>

            <!-- Upload Feedback Accordion -->
            {#if isUploading || uploadStatus.some((status) => status.status !== "pending")}
              <div class="border rounded-md overflow-hidden">
                <!-- svelte-ignore a11y_click_events_have_key_events -->
                <div
                  class="flex justify-between items-center p-4 bg-gray-50 cursor-pointer"
                  onclick={toggleFeedback}
                >
                  <h3 class="font-medium">{$_("import_pages.svelte.upload_status")}</h3>
                  <svg
                    class="w-5 h-5 transition-transform duration-200 {isFeedbackOpen
                      ? 'rotate-180'
                      : ''}"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                    xmlns="http://www.w3.org/2000/svg"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M19 9l-7 7-7-7"
                    ></path>
                  </svg>
                </div>

                {#if isFeedbackOpen}
                  <div class="p-4">
                    <!-- Overall progress bar -->
                    {#if isUploading}
                      <div class="w-full bg-gray-200 rounded-full h-4 mb-10">
                        <div
                          class="bg-teal-500 h-4 rounded-full"
                          style="width: {overallProgress}%"
                        ></div>
                        <p class="text-xs text-center mt-2">
                          {$_("import_pages.svelte.progress") + ":" + overallProgress}%
                        </p>
                      </div>
                    {/if}

                    <!-- Individual file status -->
                    <div
                      class="space-y-2 mt-2 max-h-60 overflow-y-auto"
                      bind:this={statusContainerRef}
                    >
                      {#each references as [reference, filename], index}
                        {@const status = uploadStatus[index] || { status: "pending", message: "" }}
                        <div
                          class="flex items-center p-3 rounded-md text-sm"
                          class:bg-gray-100={status.status === "pending"}
                          class:bg-blue-100={status.status === "uploading"}
                          class:bg-green-100={status.status === "success"}
                          class:bg-red-100={status.status === "error"}
                          class:border-2={currentUploadIndex === index}
                          class:border-blue-500={currentUploadIndex === index}
                          bind:this={fileRefs[index]}
                        >
                          <!-- Status icon -->
                          <div class="mr-3">
                            {#if status.status === "pending"}
                              <svg
                                class="w-5 h-5 text-gray-500"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                                xmlns="http://www.w3.org/2000/svg"
                              >
                                <path
                                  stroke-linecap="round"
                                  stroke-linejoin="round"
                                  stroke-width="2"
                                  d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                                ></path>
                              </svg>
                            {:else if status.status === "uploading"}
                              <svg
                                class="w-5 h-5 text-blue-500 animate-spin"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                                xmlns="http://www.w3.org/2000/svg"
                              >
                                <path
                                  stroke-linecap="round"
                                  stroke-linejoin="round"
                                  stroke-width="2"
                                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                                ></path>
                              </svg>
                            {:else if status.status === "success"}
                              <svg
                                class="w-5 h-5 text-green-500"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                                xmlns="http://www.w3.org/2000/svg"
                              >
                                <path
                                  stroke-linecap="round"
                                  stroke-linejoin="round"
                                  stroke-width="2"
                                  d="M5 13l4 4L19 7"
                                ></path>
                              </svg>
                            {:else if status.status === "error"}
                              <svg
                                class="w-5 h-5 text-red-500"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                                xmlns="http://www.w3.org/2000/svg"
                              >
                                <path
                                  stroke-linecap="round"
                                  stroke-linejoin="round"
                                  stroke-width="2"
                                  d="M6 18L18 6M6 6l12 12"
                                ></path>
                              </svg>
                            {/if}
                          </div>

                          <!-- File info -->
                          <div class="flex-1">
                            <p class="font-medium">{reference}: {filename}</p>
                            {#if status.message}
                              <p
                                class:text-blue-600={status.status === "uploading"}
                                class:text-green-600={status.status === "success"}
                                class:text-red-600={status.status === "error"}
                              >
                                {status.message}
                              </p>
                            {/if}
                          </div>

                          <!-- Retry button for failed uploads -->
                          {#if status.status === "error"}
                            <button
                              type="button"
                              class="ml-2 text-white py-1 px-3 rounded text-xs flex items-center"
                              class:bg-gray-500={isUploading}
                              class:opacity-50={isUploading}
                              class:cursor-not-allowed={isUploading}
                              class:bg-blue-500={!isUploading}
                              class:hover:bg-blue-600={!isUploading}
                              class:disabled:bg-gray-500={isUploading}
                              class:disabled:cursor-not-allowed={isUploading}
                              class:disabled:opacity-50={isUploading}
                              class:hover:bg-blue-500={isUploading}
                              disabled={isUploading}
                              onclick={() => retryUpload(index)}
                            >
                              <svg
                                class="w-4 h-4 mr-1"
                                fill="none"
                                stroke="currentColor"
                                viewBox="0 0 24 24"
                                xmlns="http://www.w3.org/2000/svg"
                              >
                                <path
                                  stroke-linecap="round"
                                  stroke-linejoin="round"
                                  stroke-width="2"
                                  d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                                ></path>
                              </svg>
                              {$_("import_pages.svelte.retry")}
                            </button>
                          {/if}
                        </div>
                      {/each}
                    </div>
                  </div>
                {/if}
              </div>
            {/if}
          </div>

          <div class="flex justify-end gap-4 mt-4">
            <button
              type="button"
              onclick={cancel}
              class="px-4 py-2 bg-gray-500 text-white rounded"
              disabled={isUploading}
            >
              {$_("modals.add_group.cancel")}
            </button>
            <button
              type="submit"
              class="bg-teal-500 text-white px-4 py-2 rounded flex items-center"
              disabled={isUploading}
            >
              {#if isUploading}
                <svg
                  class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                {$_("import_pages.svelte.uploading")}
              {:else}
                {$_("import_pages.svelte.upload")}
              {/if}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
{/if}
