<script>
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { isAdmin } from "$lib/stores/user_stores";
  import ImportModal from "$lib/modals/ImportModal.svelte";
  import ZipImport from "./zipImport.svelte";
  import Icon from '@iconify/svelte';
  import { _ } from "svelte-i18n";
  import { modals } from "svelte-modals";
  import PicturesImport from "./picturesImport.svelte";


  let file;
  let fileInput;
  let errorMessage = "";
  let dragging = false;
  let showModal = false;

  /**
   * Handle file drop into the drop zone.
   * @param {DragEvent} event
   */
  function handleDrop(event) {
    event.preventDefault();
    dragging = false;
    if (event.dataTransfer.files.length) {
      file = event.dataTransfer.files[0];
      if (!isValidExcelFile(file)) {
        errorMessage = $_('import_pages.svelte.error');
        file = null;
      } else {
        errorMessage = "";
      }
    }
  }

  /**
   * Handle dragover to activate visual feedback.
   * @param {DragEvent} event
   */
  function handleDragOver(event) {
    event.preventDefault();
    dragging = true;
  }

  /**
   * Reset visual feedback when dragging leaves drop zone.
   */
  function handleDragLeave() {
    dragging = false;
  }

  /**
   * Handle file selection via input field.
   * @param {Event} event
   */
  function handleFileSelection(event) {
    if (event.target.files.length) {
      file = event.target.files[0];
      if (!isValidExcelFile(file)) {
        errorMessage = $_('import_pages.svelte.error');
        file = null;
      } else {
        errorMessage = "";
      }
    }
  }

  /**
   * Trigger import modal if file is valid.
   * @param {Event} event
   */
  function handleImport(event) {
    event.preventDefault();
    if (!file || !isValidExcelFile(file)) {
      errorMessage = $_('import_pages.svelte.error');
    } else {
      errorMessage = "";
      showModal = true;
    }
  }

  /**
   * Remove the currently selected file.
   */
  function removeFile() {
    file = null;
    errorMessage = "";
  }

  /**
   * Check if file has a valid Excel extension.
   * @param {File} file
   * @returns {boolean}
   */
  function isValidExcelFile(file) {
    const validExtensions = [".xlsx", ".xls"];
    return validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));
  }

  onMount(() => {
    // Redirect non-admin users to unauthorized page
    if (!isAdmin) {
      goto("/unauthorized");
    }
  });
</script>

<svelte:head>
  <title>{$_('import_pages.svelte.import2')}</title>
</svelte:head>

<main class="w-full flex flex-col items-center">
  <h1 class="text-2xl font-bold mt-6">{$_('import_pages.svelte.import')}</h1>

  <div
    class="w-3/4 h-64 border-4 border-dashed border-gray-500 rounded-lg flex items-center justify-center mt-6 bg-gray-100"
    ondrop={handleDrop}
    ondragover={handleDragOver}
    ondragleave={handleDragLeave}
    class:bg-gray-200={dragging}
    role="button"
    tabindex="0"
  >
    {#if file}
      <div class="w-full flex items-center justify-between p-4 bg-white border rounded-md">
        <div class="flex items-center">
          <img src="https://cdn-icons-png.flaticon.com/512/732/732220.png" alt="Excel Icon" class="w-6 h-6 mr-2" />
          <p class="text-gray-700">{file.name}</p>
        </div>
        <button
          class="bg-red-600 text-white rounded-full p-1 ml-4 flex items-center justify-center w-6 h-6"
          type="button"
          onclick={removeFile}
        >
          <Icon icon="material-symbols:delete-forever" width="24" height="24" />
        </button>
      </div>
    {:else}
      <p class="text-gray-600">{$_('import_pages.svelte.drag')}</p>
    {/if}
  </div>

  {#if errorMessage}
    <p class="text-red-600 mt-2">{errorMessage}</p>
  {/if}

  <form class="w-3/4 mt-4" onsubmit={handleImport}>
    <input
      id="file-upload"
      type="file"
      accept=".xlsx, .xls"
      class="hidden"
      bind:this={fileInput}
      onchange={handleFileSelection}
    />
    <button
      class="bg-gray-700 text-white py-2 px-4 rounded-lg mt-2"
      type="button"
      onclick={() => fileInput.click()}
    >
      {$_('import_pages.svelte.choose')}
    </button>
    {#if file}
      <button
        class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-2 ml-2"
        type="submit"
      >
      {$_('import_pages.svelte.import_button')}
      </button>
    {/if}
  </form>

  {#if showModal}
    <ImportModal {file} on:close={() => {
      showModal = false;
      file = null;
    }} />
  {/if}


  <!-- <ZipImport /> -->
  <PicturesImport />
</main>
