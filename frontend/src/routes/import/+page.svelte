<script>
    import { onMount } from "svelte";
    let dragging = false;
    let file;
    let fileInput;
    let errorMessage = "";
    let showModal = false;
    let modalPosition = { x: 100, y: 100 };
    let offset = { x: 0, y: 0 };
    let isDraggingModal = false;
    let currentView = "main";
    let selectedGroup = "Classique";
    let selectedSubGroup = "";
    let isNextEnabled = false;
    let selectedOption = "";
    const groups = ["Pinces", "Ciseaux", "Autre..."];
    let subGroups = { "Classique": ["Sous-groupe 1", "Sous-groupe 2"], "Pinces": ["Sous-groupe A", "Sous-groupe B"], "Ciseaux": ["Sous-groupe X", "Sous-groupe Y"], "Autre": [] };
  
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
        showModal = true;
        currentView = "main";
        isNextEnabled = false;
        selectedOption = "";
      }
    };
  
    const isValidExcelFile = (file) => {
      const validExtensions = [".xlsx", ".xls"];
      return validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));
    };
  
    const handleMouseDown = (event) => {
      isDraggingModal = true;
      offset = {
        x: event.clientX - modalPosition.x,
        y: event.clientY - modalPosition.y,
      };
    };
  
    const handleMouseMove = (event) => {
      if (isDraggingModal) {
        modalPosition = {
          x: event.clientX - offset.x,
          y: event.clientY - offset.y,
        };
      }
    };
  
    const handleMouseUp = () => {
      isDraggingModal = false;
    };
  
    const handleSelectGroup = (option) => {
      selectedOption = option;
      isNextEnabled = true;
    };
  
    const handleFinishImport = () => {
      showModal = false;
    };
  
    const handleNext = () => {
      if (currentView === "main" && isNextEnabled) {
        currentView = "sous-groupe";
        isNextEnabled = false;
        selectedSubGroup = "";
      }
    };
  
    const handleGroupChange = (event) => {
      selectedGroup = event.target.value;
      if (selectedGroup === "Autre") {
        selectedGroup = "";
        subGroups["Autre"] = [];
      }
      selectedSubGroup = "Classique";
      isNextEnabled = true;
    };
  
    const handleSubGroupChange = (event) => {
      selectedSubGroup = event.target.value;
    };
  
    onMount(() => {
      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("mouseup", handleMouseUp);
    });
  </script>
  
  <main class="w-full flex flex-col items-center">
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore a11y-click-events-have-key-events -->
    <!-- svelte-ignore a11y-mouse-events-have-key-events -->
    <div class="w-3/4 h-64 border-4 border-dashed border-gray-500 rounded-lg flex items-center justify-center mt-6 bg-gray-100" role="button" tabindex="0"
      on:drop={handleDrop}
      on:dragover={handleDragOver}
      on:dragleave={handleDragLeave}
      class:bg-gray-200={dragging}
    >
      {#if file}
        <div class="w-full flex items-center justify-between p-4 bg-white border rounded-md">
          <div class="flex items-center">
            <img src="https://example.com/excel-icon.png" alt="Excel Icon" role="presentation" class="w-6 h-6 mr-2" />
            <p class="text-gray-700">{file.name}</p>
          </div>
          <button class="bg-red-600 text-white rounded-full p-1 ml-4 flex items-center justify-center w-6 h-6" type="button" on:click={removeFile}>
            <span class="text-sm">✖</span>
          </button>
        </div>
      {:else}
        <p class="text-gray-600">Glissez et déposez un fichier Excel ici pour l'importer</p>
      {/if}
    </div>
  
    {#if errorMessage}
      <p class="text-red-600 mt-2">{errorMessage}</p>
    {/if}
  
    <form class="w-3/4 mt-4" on:submit={handleImport}>
      <input type="file" accept=".xlsx, .xls" class="hidden" bind:this={fileInput} on:change={handleFileSelection} />
      <button class="bg-gray-700 text-white py-2 px-4 rounded-lg mt-2" type="button" on:click={() => fileInput.click()}>Choisir un fichier</button>
      {#if file}
        <button class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-2 ml-2" type="submit">Importer</button>
      {/if}
    </form>
  
    {#if showModal}
      <div class="fixed inset-0 flex items-center justify-center" style="background: rgba(0, 0, 0, 0.1);">
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <div class="bg-white w-3/4 p-10 rounded-lg shadow-xl relative" style="left: {modalPosition.x}px; top: {modalPosition.y}px; position: absolute; min-height: 500px;" on:mousedown={handleMouseDown}>
          <button class="absolute top-2 right-2 text-gray-600" on:click={() => showModal = false}>
            ✖
          </button>
          {#if currentView === "main"}
            <h2 class="text-2xl font-bold mb-6">Options d'Importation</h2>
            <div class="flex flex-col gap-6">
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Importer un sous-groupe')} style="background-color: {selectedOption === 'Importer un sous-groupe' ? '#4a5568' : '#2d3748'}; color: white;">
                Importer un sous-groupe
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Importer des instruments non catégorisés')} style="background-color: {selectedOption === 'Importer des instruments non catégorisés' ? '#4a5568' : '#2d3748'}; color: white;">
                Importer des instruments non catégorisés
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Importer un catalogue')} style="background-color: {selectedOption === 'Importer un catalogue' ? '#4a5568' : '#2d3748'}; color: white;">
                Importer un catalogue
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Importer des alternatives')} style="background-color: {selectedOption === 'Importer des alternatives' ? '#4a5568' : '#2d3748'}; color: white;">
                Importer des alternatives
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Importer un crossref')} style="background-color: {selectedOption === 'Importer un crossref' ? '#4a5568' : '#2d3748'}; color: white;">
                Importer un crossref
              </button>
            </div>
          {:else if currentView === "sous-groupe"}
            <div class="flex items-center mb-4">
              <button class="text-gray-700 mr-4" on:click={() => currentView = "main"}>
                ←
              </button>
              <h2 class="text-2xl font-bold">Sélectionnez le groupe et sous-groupe</h2>
            </div>
            <div class="mb-6">
              <!-- svelte-ignore a11y_label_has_associated_control -->
              <label class="block mb-2 text-gray-700">Groupe :</label>
              <select bind:value={selectedGroup} class="w-full p-3 border rounded" on:change={handleGroupChange}>
                {#each groups as group}
                  <option value={group}>{group}</option>
                {/each}
                <option value="Autre">Autre...</option>
              </select>
              {#if selectedGroup === ""}
                <input type="text" class="w-full p-3 border rounded mt-2" placeholder="Entrez un nouveau groupe" bind:value={selectedGroup} />
              {/if}
            </div>
            <div class="mb-6">
              <label class="block mb-2 text-gray-700">Sous-groupe :</label>
              <select bind:value={selectedSubGroup} class="w-full p-3 border rounded" on:change={handleSubGroupChange}>
                {#each (subGroups[selectedGroup] || []) as subGroup}
                  <option value={subGroup}>{subGroup}</option>
                {/each}
                <option value="Autre">Autre...</option>
              </select>
              {#if selectedSubGroup === "Autre"}
                <input type="text" class="w-full p-3 border rounded mt-2" placeholder="Entrez un nouveau sous-groupe" bind:value={selectedSubGroup} />
              {/if}
            </div>
          {/if}
          <button 
            class="border border-gray-500 text-gray-500 py-2 px-4 rounded-lg absolute bottom-0 right-0 mb-2 mr-2"
            style="opacity: {isNextEnabled ? 1 : 0.5}; pointer-events: {isNextEnabled ? 'auto' : 'none'}; background-color: {isNextEnabled ? '#c3dafe' : 'transparent'}; color: {isNextEnabled ? '#2d3748' : '#gray-500'};"
            on:click={handleNext}
          >
            Suivant
          </button>
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
    .shadow-xl {
      box-shadow: 0 20px 25px rgba(0, 0, 0, 0.15);
    }
    .button-next {
      border: 1px solid #ccc;
      color: #888;
      transition: all 0.3s ease;
    }
    .button-next.enabled {
      border-color: #444;
      color: #444;
    }
  </style>
  