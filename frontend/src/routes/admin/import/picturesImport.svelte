<script>
  import { onMount } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { apiFetch } from "$lib/utils/fetch";
  import Icon from "@iconify/svelte";
  import { _ } from "svelte-i18n";
  import { preventDefault } from "svelte/legacy";
  import * as XLSX from "xlsx";
  import JSZip from "jszip";
  import { fromJSON } from "postcss";
  import { modals } from "svelte-modals";
  import SelectHeadersModal from "./picturesImportModals/selectHeadersModal.svelte";
  import PicturesImportModal from "./picturesImportModals/picturesImportModal.svelte";

  let files = $state([]);
  let dragging = $state(false);
  let fileInput;

  let references = $state([]);
  let filenames = $state([]);

  let responses = $state(null);

  function handleDrop(event) {
    event.preventDefault();
    dragging = false;
    if (event.dataTransfer.files.length > 0) {
      files.push(event.dataTransfer.files[0]);
    }
  }

  function triggerFileSelection() {
    fileInput.click();
  }

  function handleFileSelection(event) {
    if (event.target.files.length > 0) {
      files.push(event.target.files[0]);
    }
  }

  async function handleSubmit() {
    if (files.length === 0) {
      return;
    }

    // Get the zip file and the excel file
    const zipFile = files.find((file) => file.name.endsWith(".zip"));
    const excelFile = files.find(
      (file) => file.name.endsWith(".xlsx") || file.name.endsWith(".xls")
    );

    if (!zipFile || !excelFile) {
      toast.push($_("import_pages.svelte.missing_file"), {
        theme: {
          "--toastBackground": "#f87171",
          "--toastColor": "#fff",
        },
        duration: 2000,
      });
      return;
    }

    let columns = [];

    await getColumnsNames(excelFile)
      .then(async (headers) => {
        // Open modal to select columns
        columns = await modals.open(SelectHeadersModal, { headers: headers });
      })
      .catch((error) => {
        console.error("Error reading Excel file:", error);
      });

    if (!columns) {
      toast.push($_("import_pages.svelte.error_columns"), {
        theme: {
          "--toastBackground": "#f87171",
          "--toastColor": "#fff",
        },
        duration: 2000,
      });
      return;
    }

    if (columns === "cancel") {
      return;
    }

    console.log(columns);
    importPreview(zipFile, excelFile, columns[0], columns[1]);
  }

  async function getColumnsNames(excelFile) {
    const workbook = XLSX.read(await excelFile.arrayBuffer(), { type: "array" });
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1 });

    const headers = rows[0]; // First row = column headers
    return headers;
  }

  async function importPreview(
    zipFile,
    excelFile,
    referenceColumnName = "REFERENCE",
    mappingColumnName = "MAPPING"
  ) {
    // Extract all references from the excel file
    const workbook = XLSX.read(await excelFile.arrayBuffer(), { type: "array" });
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const rows = XLSX.utils.sheet_to_json(sheet, { header: 1 });

    const headers = rows[0]; // First row = column headers
    const dataRows = rows.slice(1); // The rest = data

    const filenameIndex = headers.indexOf(mappingColumnName);
    const referenceIndex = headers.indexOf(referenceColumnName);

    if (filenameIndex === -1 || referenceIndex === -1) {
      toast.push($_("import_pages.svelte.error_columns"), {
        theme: {
          "--toastBackground": "#f87171",
          "--toastColor": "#fff",
        },
        duration: 2000,
      });
      throw new Error("Chosen column name(s) not found in Excel headers.");
    }

    for (const row of dataRows) {
      const filename = row[filenameIndex];
      const reference = row[referenceIndex];
      if (filename && reference) {
        // Trim the '"' character from the filename and reference
        const trimmedFilename = filename.replace(/"/g, "");
        const trimmedReference = reference.replace(/"/g, "");

        references.push([trimmedReference, trimmedFilename]);
      }
    }

    // Extract all files from the zip file
    const zip = new JSZip();
    const zipContent = await zip.loadAsync(zipFile);
    const supportedExtensions = ["jpg", "jpeg", "png"];
    let missingFiles = [];
    let completeReferences = [];
    // Add to the references the file linked to the reference
    for (const [index, [reference, filename]] of references.entries()) {
      let fileFound = false;

      for (const ext of supportedExtensions) {
        const filePath = `${filename}.${ext}`;
        const file = zipContent.file(filePath);

        if (file) {
          completeReferences.push([reference, filename, file, filePath]);
          fileFound = true;
          break; // Exit inner loop once file is found
        }
      }

      if (!fileFound) {
        missingFiles.push([reference, filename]);
      }
    }
    modals.open(PicturesImportModal, {
      references: completeReferences,
      missingFiles: missingFiles,
    });
  }
</script>

<main class="w-full flex flex-col items-center mb-4">
  <h1 class="text-2xl font-bold mt-6">{$_('import_pages.svelte.pictures_import')}</h1>
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <div
    class="w-3/4 min-h-64 border-4 border-dashed border-gray-500 rounded-lg flex items-center justify-center mt-6 bg-gray-100"
    role="button"
    tabindex="0"
    ondrop={handleDrop}
    ondragover={(event) => {
      event.preventDefault();
      dragging = true;
    }}
    ondragleave={() => {
      dragging = false;
    }}
    ondragenter={() => {
      dragging = true;
    }}
    onclick={triggerFileSelection}
    class:bg-gray-200={dragging}
  >
    {#if files.length > 0}
      <div class="flex flex-col items-center w-full mx-8 p-2 overflow-y-auto h-64">
        {#each files as file, index}
          <div
            class="w-full flex items-center justify-between p-4 bg-white border rounded-md flex-row my-1"
          >
            <div class="flex items-center">
              <Icon icon="material-symbols:folder-zip" width="24" height="24" />
              <p class="text-gray-700">{file.name}</p>
            </div>
            <button
              class="bg-red-600 text-white rounded-full p-1 ml-4 flex items-center justify-center w-6 h-6"
              type="button"
              onclick={() => {
                event.stopPropagation();
                event.preventDefault();
                files.splice(index, 1);
                toast.push($_("import_pages.svelte.delete"), {
                  theme: {
                    "--toastBackground": "#f87171",
                    "--toastColor": "#fff",
                  },
                  duration: 2000,
                });
              }}
            >
              <Icon icon="material-symbols:delete-forever" width="24" height="24" />
            </button>
          </div>
        {/each}
      </div>
    {:else}
      <p class="text-gray-600">
        {$_("import_pages.svelte.click")}
      </p>
    {/if}
  </div>

  {#if files.length > 0}
    <form class="w-3/4 mt-4" onsubmit={handleSubmit}>
      <button class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-2 ml-2" type="submit">
        {$_("import_pages.svelte.continue")}</button
      >
    </form>
  {/if}

  {#if responses}
    <div class="w-3/4 mt-4 h-64 overflow-y-auto border rounded-lg p-4 bg-gray-50">
      <h2 class="text-xl font-bold">{$_("import_pages.svelte.response")}</h2>
      {#each responses as response}
        <div class="my-2 py-2 flex items-center">
          {#if response.success}
            <Icon
              icon="material-symbols:check-circle"
              width="24"
              height="24"
              class="text-green-500"
            />
            <strong>{response.fileName}</strong>: {response.message}
          {:else}
            <Icon icon="material-symbols:error" width="24" height="24" class="text-red-500" />
            <strong>{response.fileName}</strong>: {response.message}
          {/if}
        </div>
      {/each}
    </div>
  {/if}

  <!-- Input file caché -->
  <input
    type="file"
    accept=".zip, .xlsx, .xls"
    class="hidden"
    bind:this={fileInput}
    onchange={handleFileSelection}
  />
</main>
