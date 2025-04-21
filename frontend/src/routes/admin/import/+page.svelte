<script>
  import { onMount } from "svelte";
  import { goto } from "$app/navigation";
  import { isAdmin } from "$lib/stores/user_stores";
  import ImportModal from "$lib/modals/ImportModal.svelte";
  import ZipImport from "./zipImport.svelte";
  import Icon from '@iconify/svelte';
  import { modals } from "svelte-modals";


  let file;
  let fileInput;
  let errorMessage = "";
  let dragging = false;
  let showModal = false;

  function handleDrop(event) {
    event.preventDefault();
    dragging = false;
    if (event.dataTransfer.files.length) {
      file = event.dataTransfer.files[0];
      if (!isValidExcelFile(file)) {
        errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
        file = null;
      } else {
        errorMessage = "";
      }
    }
  }

  function handleDragOver(event) {
    event.preventDefault();
    dragging = true;
  }

  function handleDragLeave() {
    dragging = false;
  }

  function handleFileSelection(event) {
    if (event.target.files.length) {
      file = event.target.files[0];
      if (!isValidExcelFile(file)) {
        errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
        file = null;
      } else {
        errorMessage = "";
      }
    }
  }

  function handleImport(event) {
    event.preventDefault();
    if (!file || !isValidExcelFile(file)) {
      errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
    } else {
      errorMessage = "";
      showModal = true;
    }
  }

  function removeFile() {
    file = null;
    errorMessage = "";
  }

  function isValidExcelFile(file) {
    const validExtensions = [".xlsx", ".xls"];
    return validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));
  }

  onMount(() => {
    if (!isAdmin) {
      goto("/unauthorized");
    }
  });
</script>

<svelte:head>
  <title>Importation de fichiers</title>
</svelte:head>

<main class="w-full flex flex-col items-center">
  <h1 class="text-2xl font-bold mt-6">Importation de fichiers Excel</h1>

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
      <p class="text-gray-600">Glissez et déposez un fichier Excel ici pour l'importer</p>
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
      Choisir un fichier
    </button>
    {#if file}
      <button
        class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-2 ml-2"
        type="submit"
      >
        Importer
      </button>
    {/if}
  </form>

  {#if showModal}
    <ImportModal {file} on:close={() => {
      showModal = false;
      file = null;
    }} />
  {/if}


  <ZipImport />
</main>
