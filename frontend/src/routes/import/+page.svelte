<script>
    import * as XLSX from "xlsx";
    let dragging = false;
    let file;
    let fileInput;
    let errorMessage = "";
    let jsonData;
    let showModal = false;
  
    const handleDrop = (event) => {
      event.preventDefault();
      dragging = false;
      if (event.dataTransfer.files.length) {
        file = event.dataTransfer.files[0];
        if (!isValidExcelFile(file)) {
          errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
          file = null;
        } else {
          errorMessage = "";
          console.log('Fichier importé:', file.name);
        }
      }
    };
  
    const handleDragOver = (event) => {
      event.preventDefault();
      dragging = true;
    };
  
    const handleDragLeave = () => {
      dragging = false;
    };
  
    const handleFileSelection = (event) => {
      if (event.target.files.length) {
        file = event.target.files[0];
        if (!isValidExcelFile(file)) {
          errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
          file = null;
        } else {
          errorMessage = "";
          console.log('Fichier sélectionné:', file.name);
        }
      }
    };
  
    const removeFile = () => {
      file = null;
      errorMessage = "";
    };
  
    const handleImport = (event) => {
      event.preventDefault();
      if (!file || !isValidExcelFile(file)) {
        errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
      } else {
        errorMessage = "";
        console.log('Importation du fichier:', file.name);
        readExcelFile(file);
      }
    };
  
    const isValidExcelFile = (file) => {
      const validExtensions = [".xlsx", ".xls"];
      return validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));
    };
  
    const readExcelFile = (file) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, { type: "array" });
  
        // Suppose que tu veux juste la première feuille
        const sheetName = workbook.SheetNames[0];
        const worksheet = workbook.Sheets[sheetName];
  
        // Convertir les données en JSON
        jsonData = XLSX.utils.sheet_to_json(worksheet);
        console.log("Données JSON :", jsonData);
        showModal = true;
      };
      reader.readAsArrayBuffer(file);
    };
  </script>
  
  <main class="w-full flex flex-col items-center">
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore a11y-click-events-have-key-events -->
    <!-- svelte-ignore a11y-mouse-events-have-key-events -->
    <div class="w-3/4 h-64 border-4 border-dashed border-teal-500 rounded-lg flex items-center justify-center mt-6 bg-gray-100" role="button" tabindex="0"
      on:drop={handleDrop}
      on:dragover={handleDragOver}
      on:dragleave={handleDragLeave}
      class:bg-teal-100={dragging}
    >
      {#if file}
        <div class="w-full flex items-center justify-between p-4 bg-white border rounded-md">
          <div class="flex items-center">
            <img src="https://example.com/excel-icon.png" alt="Excel Icon" role="presentation" class="w-6 h-6 mr-2" />
            <p class="text-teal-700">{file.name}</p>
          </div>
          <button class="bg-red-500 text-white rounded-full p-1 ml-4 flex items-center justify-center w-6 h-6" type="button" on:click={removeFile}>
            <span class="text-sm">✖</span>
          </button>
        </div>
      {:else}
        <p class="text-teal-500">Glissez et déposez un fichier Excel ici pour l'importer</p>
      {/if}
    </div>
  
    {#if errorMessage}
      <p class="text-red-500 mt-2">{errorMessage}</p>
    {/if}
  
    <form class="w-3/4 mt-4" on:submit={handleImport}>
      <input type="file" accept=".xlsx, .xls" class="hidden" bind:this={fileInput} on:change={handleFileSelection} />
      <button class="bg-teal-500 text-white py-2 px-4 rounded-lg mt-2" type="button" on:click={() => fileInput.click()}>Choisir un fichier</button>
      {#if file}
        <button class="bg-blue-500 text-white py-2 px-4 rounded-lg mt-2 ml-2" type="submit">Importer</button>
      {/if}
    </form>
  
    {#if showModal}
      <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
        <div class="bg-white w-2/3 max-h-96 p-6 rounded-lg overflow-auto relative">
          <button class="absolute top-2 right-2 text-red-500" on:click={() => showModal = false}>
            ✖
          </button>
          <h2 class="text-xl font-bold mb-4">Données du fichier Excel</h2>
          <pre class="text-sm text-gray-800">{JSON.stringify(jsonData, null, 2)}</pre>
        </div>
      </div>
    {/if}
  </main>
  
  <style>
    main {
      padding: 1rem;
    }
    .fixed {
      position: fixed;
    }
    .inset-0 {
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
    }
    .bg-opacity-50 {
      opacity: 0.5;
    }
  </style>
  