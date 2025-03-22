<script>
  import { fetchGroups, fetchCharacteristics, sendExcelToBackend, fetchSuppliers} from "../../../api.js";
  import { isAdmin } from "$lib/stores/user_stores";
  import { onMount } from "svelte";
  import * as XLSX from "xlsx";
  import { goto } from "$app/navigation";

  // Variable declarations
  // Declaring various variables used for drag & drop, file selection, modal handling, and state tracking.
  let dragging = false;
  let file;
  let fileInput;
  let errorMessage = "";
  let showModal = false;
  let showJsonModal = false;
  let modalPosition = { x: 100, y: 100 };
  let offset = { x: 0, y: 0 };
  let isDraggingModal = false;
  let currentView = "main";
  let viewHistory = [];
  let selectedGroup = "";
  let selectedSubGroup = "";
  let isNextEnabled = false;
  let selectedOption = "";
  let missingColumns = [];
  let presentColumns = [];
  let jsonData = null;
  let isLoading = false;
  let loadingProgress = 0;
  let groups = [];
  let subGroups = {};
  let requiredColumns = []; 
  let columnMapping = {};
  let suppliers = [];
  let selectedSupplier = "";
  let isSupplierModalOpen = false;
  let isImporting = false;

  /**
   * Fetches the list of suppliers from the backend.
   */
  async function loadSuppliers() {
    try {
      suppliers = await fetchSuppliers();
    } catch (error) {
      console.error("Error while retrieving suppliers:", error);
    }
  }

  /**
   * Handles the drop event for the file drag and drop.
   * @param {DragEvent} event The drag event.
   */
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
      }
    }
  };

  /** 
   * Loads groups and their respective sub-groups from the backend.
   * @returns {Promise<{groups: string[], subGroups: object}>} A promise resolving to groups and subGroups.
   */
  async function loadGroups() {
      try {
          const data = await fetchGroups(); // Fetch data from API
          if (!Array.isArray(data)) {
              throw new Error("Invalid response format: Expected an array but got " + typeof data);
          }

          // Extracting group names
          groups = data.map(group => group.name);

          // Mapping each group to its respective sub-groups
          subGroups = Object.fromEntries(
              data.map(group => [group.name, (group.subGroups || []).map(sub => sub.name)])
          );

          return { groups, subGroups };
      } catch (error) {
          console.error("Error while retrieving groups:", error);
          return { groups: [], subGroups: {} };
      }
  }


  /**
   * Sets the required columns based on the selected option.
   */
  const setRequiredColumns = async () => {
    if (selectedOption === "NonCategorized") {
      requiredColumns = [
        "reference", 
        "supplier_name", 
        "sold_by_md", 
        "closed", 
        "supplier_description", 
        "price", 
        "obsolete"
      ];
    } 
    else if (selectedOption === "Catalogue") {
      requiredColumns = [
        "reference", 
        "sold_by_md", 
        "closed", 
        "supplier_description", 
        "price", 
        "obsolete"
      ];
    } 
    else if (selectedOption === "Crossref") {
      try {
        const supplierList = await fetchSuppliers();
        requiredColumns = supplierList.map(supplier => supplier.name);
      } catch (error) {
        console.error("Error loading suppliers:", error);
        requiredColumns = []; 
      }
    }
  };

  /**
   * Fetches characteristics for the selected subgroup.
   * @param {string} selectedSubGroup The selected subgroup.
   * @returns {Promise<string[]>} A promise resolving to the list of required columns.
   */
   async function loadCharacteristics(selectedSubGroup) {
    try {
      const characteristics = await fetchCharacteristics(selectedSubGroup);

      if (!Array.isArray(characteristics)) {
        console.error("Unexpected format: expected an array but got", typeof characteristics);
        return [];
      }

      requiredColumns = [
        "reference",
        "supplier_name",
        "sold_by_md",
        "closed",
        "group_name",
        "supplier_description",
        "price",
        "obsolete",
        ...characteristics,  
        ...characteristics.map(char => `abbreviation_${char}`),
      ];

      return requiredColumns;
    } catch (error) {
      console.error("Error while retrieving characteristics:", error);
      return [];
    }
  }
  /**
   * Normalizes a column header (lowercase, trims spaces, replaces spaces with underscores).
   * @param {string} header 
   * @returns {string}
   */
  function normalizeHeader(header) {
    return header
      .normalize("NFD")
      .replace(/\p{Diacritic}/gu, "")
      .trim()
      .toLowerCase()
      .replace(/\s+/g, "_");
  }


  /**
   * Handles the drag-over event to allow a file to be dropped.
   * @param {DragEvent} event The drag event.
   */
  const handleDragOver = (event) => {
    event.preventDefault();
    dragging = true;
  };

  /**
   * Handles when the drag leaves the drop area.
   */
  const handleDragLeave = () => {
    dragging = false;
  };

  /**
   * Handles file selection through the file input element.
   * @param {Event} event The change event from the file input.
   */
  const handleFileSelection = (event) => {
    if (event.target.files.length) {
      file = event.target.files[0];
      if (!isValidExcelFile(file)) {
        errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
        file = null;
      } else {
        errorMessage = "";
      }
    }
  };

  /**
   * Removes the selected file.
   */
  const removeFile = () => {
    file = null;
    errorMessage = "";
  };

  /**
   * Handles the import button click and opens the modal if the file is valid.
   * @param {Event} event The event from the button click.
   */
  const handleImport = (event) => {
    event.preventDefault();
    if (!file || !isValidExcelFile(file)) {
      errorMessage = "Erreur : Le fichier sélectionné n'est pas un fichier Excel valide.";
    } else {
      errorMessage = "";
      showModal = true;
      currentView = "main";
      isNextEnabled = false;
      selectedOption = "";
    }
  };

  /**
   * Checks if the selected file is a valid Excel file based on its extension.
   * @param {File} file The file to be checked.
   * @returns {boolean} True if the file is valid, otherwise false.
   */
  const isValidExcelFile = (file) => {
    const validExtensions = [".xlsx", ".xls"];
    return validExtensions.some((ext) => file.name.toLowerCase().endsWith(ext));
  };

  /**
   * Handles the mouse down event to begin dragging the modal.
   * @param {MouseEvent} event The mouse event.
   */
  const handleMouseDown = (event) => {
    isDraggingModal = true;
    offset = {
      x: event.clientX - modalPosition.x,
      y: event.clientY - modalPosition.y,
    };
  };

  /**
   * Handles mouse movement to reposition the modal if it is being dragged.
   * @param {MouseEvent} event The mouse event.
   */
  const handleMouseMove = (event) => {
    if (isDraggingModal) {
      modalPosition = {
        x: event.clientX - offset.x,
        y: event.clientY - offset.y,
      };
    }
  };

  /**
   * Handles mouse up event to stop dragging the modal.
   */
  const handleMouseUp = () => {
    isDraggingModal = false;
  };

  /**
   * Handles selecting an option in the modal for importing.
   * @param {string} option The selected option.
   */
  const handleSelectGroup = (option) => {
    selectedOption = option;
    isNextEnabled = true;
  };

  /**
   * Handles finishing the import process.
   */
  const handleFinishImport = () => {
    showModal = false;
  };

  /**
   * Handles the "Next" button click to move to the next view in the modal.
   */
  const handleNext = async () => {
    viewHistory.push(currentView); 
    if (currentView === "main" && isNextEnabled) {
      if (selectedOption === "Catalogue") {
        currentView = "Supplier";
        isNextEnabled = false;
        selectedSupplier = "";

      } else if (selectedOption === "Crossref") {
        isLoading = true;
        loadingProgress = 0;
        await simulateLoadingProgress();
        isLoading = false;

        currentView = "verification";
        extractExcelDataToJson();
        setRequiredColumns();
      }else if (selectedOption === "NonCategorized") {
        isLoading = true;
        loadingProgress = 0;
        await simulateLoadingProgress();
        isLoading = false;
        
        currentView = "verification";
        verifyColumns();
        extractExcelDataToJson();
        setRequiredColumns();
      } else {
        currentView = "SubGroup";
        isNextEnabled = false;
        selectedSubGroup = "";
      }
    }  else if (currentView === "Supplier" && isNextEnabled) {
      isLoading = true;
      loadingProgress = 0;
      await simulateLoadingProgress();
      isLoading = false;

      currentView = "verification";
      verifyColumns();
      extractExcelDataToJson();
    }else if (currentView === "SubGroup" && isNextEnabled) {
      isLoading = true;
      loadingProgress = 0;
      await simulateLoadingProgress();
      isLoading = false;

      currentView = "verification";
      verifyColumns();
      extractExcelDataToJson();
    }
  };

  /**
   * Simulates a loading progress bar.
   * @returns {Promise<void>} A promise that resolves when loading is complete.
   */
  const simulateLoadingProgress = () => {
    return new Promise((resolve) => {
      const loadingInterval = setInterval(() => {
        if (loadingProgress >= 100) {
          clearInterval(loadingInterval);
          resolve();
        } else {
          loadingProgress += 10; // Simulating progress increment
        }
      }, 100);
    });
  };

  /**
   * Handles changing the selected group.
   * @param {Event} event The change event.
   */
  const handleGroupChange = (event) => {
    selectedGroup = event.target.value;
    selectedSubGroup = "";
    isNextEnabled = false;
  };

  /**
   * Handles changing the selected sub-group.
   * @param {Event} event The change event.
   */
  const handleSubGroupChange = (event) => {
    selectedSubGroup = event.target.value;
    isNextEnabled = selectedGroup !== "" && selectedSubGroup !== "";
    loadCharacteristics(selectedSubGroup);
  };

  /**
   * Handles changing the selected supplier.
   * @param {Event} event The change event.
   */
  const handleSupplierChange = (event) => {
    selectedSupplier = event.target.value;
    isNextEnabled = selectedSupplier !== "";
    setRequiredColumns();
  };

  /**
   * Verifies the columns in the uploaded Excel file to check if they meet the requirements.
   */
  const verifyColumns = () => {
    const uploadedColumns = ["Nom", "Quantité", "Prix"]; // Example logic for verifying columns - placeholder
    presentColumns = uploadedColumns.filter(col => requiredColumns.includes(col));
    missingColumns = requiredColumns.filter(col => !uploadedColumns.includes(col));
  };

  /**
   * Extracts the data from the Excel file and converts it to JSON format.
   */
  const extractExcelDataToJson = () => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const data = new Uint8Array(e.target.result);
      const workbook = XLSX.read(data, { type: 'array' });
      const firstSheetName = workbook.SheetNames[0];
      const worksheet = workbook.Sheets[firstSheetName];

      const tempJsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });

      if (!tempJsonData || tempJsonData.length === 0) {
        console.warn("Aucune donnée extraite de l'Excel !");
        jsonData = [];
      } else {
        jsonData = [...tempJsonData];

        // Auto-map headers to required columns
        jsonData[0].forEach((header, index) => {
          const normalized = normalizeHeader(header);
          const match = requiredColumns.find(col => normalizeHeader(col) === normalized);
          if (match) {
            columnMapping[index] = match;
          }
        });
        // Find the index of the 'reference' column
        const refIndex = Object.entries(columnMapping).find(([_, col]) => col === "reference")?.[0];

        if (refIndex !== undefined) {
          // Filter out rows without a valid 'reference' value
          const headerRow = jsonData[0];
          const validRows = jsonData.slice(1).filter(row =>
            row[refIndex] && String(row[refIndex]).trim() !== ""
          );
          jsonData = [headerRow, ...validRows];
        } else {
          console.warn("No column mapped to 'reference'. Cannot filter invalid rows.");
        }
      }

      isLoading = false;
      currentView = "verification";
      verifyColumns();
    };
    reader.readAsArrayBuffer(file);
  };


  /**
   * Handles opening the JSON modal to view the extracted data.
   */
  const handleViewJson = () => {
    showJsonModal = true;
  };

  /**
   * Handles closing the JSON modal.
   */
  const handleCloseJsonModal = () => {
    showJsonModal = false;
  };

  /**
   * Handles the final import process after verifying the columns.
   */
   const handleImportFinal = async () => {
    isImporting = true;
    try {
      await handleDataSubmission(jsonData, columnMapping, selectedOption, selectedGroup, selectedSubGroup, selectedSupplier);
    } finally {
      isImporting = false;
      showModal = false;
    }
  };

  /**
   * Closes the import modal.
   */
  const handleCloseModal = () => {
    showModal = false;
    isLoading = false;
    loadingProgress = 0;
  };

  /**
   * Updates the column mapping when a column is changed.
   * @param {number} index The index of the column.
   */
  const updateColumnMapping = (index) => {
    if (!columnMapping[index] || columnMapping[index].trim() === "") {
      delete columnMapping[index]; 
    }
  };

  /**
   * Sends the extracted Excel data to the backend for processing.
   * @param {Array} jsonData The extracted JSON data.
   * @param {Object} columnMapping The column mapping.
   * @param {string} selectedOption The selected import option.
   * @param {string} selectedGroup The selected group.
   * @param {string} selectedSubGroup The selected subgroup.
   * @param {string} selectedSupplier The selected supplier.
   */
   async function handleDataSubmission(jsonData, columnMapping, selectedOption, selectedGroup, selectedSubGroup, selectedSupplier) {
      try {
          const response = await sendExcelToBackend(jsonData, columnMapping, selectedOption, selectedGroup, selectedSubGroup, selectedSupplier);
          
          const data = await response.json();

          if (data.success) {
              alert("Success: " + (data.message || "Import completed successfully!"));
          } else {
              alert("Error: " + (data.message || "An error occurred while importing."));
          }
      } catch (error) {
          console.error("Error while sending data:", error);
          alert("Failed to send data: " + error.message);
      }
  }

  /**
   * Adds event listeners for mouse events when the component is mounted.
   */
   onMount(async () => {
      if (!isAdmin) {
          goto("/unauthorized");
      }

      window.addEventListener("mousemove", handleMouseMove);
      window.addEventListener("mouseup", handleMouseUp);

      try {
          await loadGroups();
          await loadSuppliers();
      } catch (error) {
          console.error("Error loading data:", error);
      }
  });

</script>
  
<main class="w-full flex flex-col items-center">
  <!-- File Drag and Drop Section -->
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

  <!-- Error Message Display -->
  {#if errorMessage}
    <p class="text-red-600 mt-2">{errorMessage}</p>
  {/if}

  <!-- File Selection Form -->
  <form class="w-3/4 mt-4" on:submit={handleImport}>
    <input id="file-upload" type="file" accept=".xlsx, .xls" class="hidden" bind:this={fileInput} on:change={handleFileSelection} />
    <button class="bg-gray-700 text-white py-2 px-4 rounded-lg mt-2" type="button" on:click={() => fileInput.click()}>Choisir un fichier</button>
    {#if file}
      <button class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-2 ml-2" type="submit">Importer</button>
    {/if}
  </form>

  <!-- Modal Section -->
  {#if showModal}
    <div class="fixed inset-0 flex items-center justify-center overflow-auto" style="background: rgba(0, 0, 0, 0.1);">
      <!-- svelte-ignore a11y_no_static_element_interactions -->
      <div class="bg-white w-3/4 p-10 rounded-lg shadow-xl relative" style="left: {modalPosition.x}px; top: {modalPosition.y}px; position: absolute; min-height: 500px;" on:mousedown={handleMouseDown}>
        <button class="absolute top-2 right-2 text-gray-600" on:click={handleCloseModal}>
          ✖
        </button>              
        {#if isLoading}
          <div class="flex flex-col items-center justify-center h-full">
            <div class="loader ease-linear rounded-full border-8 border-t-8 border-gray-200 h-32 w-32 mb-4"></div>
            <progress value={loadingProgress} max="100" class="w-full"></progress>
            <p class="mt-2">Chargement: {loadingProgress}%</p>
          </div>
        {:else}
          {#if currentView === "main"}
            <!-- Main Options View -->
            <h2 class="text-2xl font-bold mb-6">Options d'Importation</h2>
            <div class="flex flex-col gap-6">
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('SubGroup')} style="background-color: {selectedOption === 'SubGroup' ? '#4a5568' : '#2d3748'}; color: white;">
                Groupe et sous-groupe d'instruments
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('NonCategorized')} style="background-color: {selectedOption === 'NonCategorized' ? '#4a5568' : '#2d3748'}; color: white;">
                Instruments non catégorisés
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Catalogue')} style="background-color: {selectedOption === 'Catalogue' ? '#4a5568' : '#2d3748'}; color: white;">
                Catalogue
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Alternatives')} style="background-color: {selectedOption === 'Alternatives' ? '#4a5568' : '#2d3748'}; color: white;">
                Alternatives
              </button>
              <button class="option-button py-3 px-6 rounded-lg" on:click={() => handleSelectGroup('Crossref')} style="background-color: {selectedOption === 'Crossref' ? '#4a5568' : '#2d3748'}; color: white;">
                Crossref
              </button>
            </div>
          {:else if currentView === "SubGroup"}
            <!-- Sub-group Selection View -->
            <div class="flex items-center mb-4">
              <button class="text-gray-700 mr-4" on:click={() => {
                currentView = viewHistory[viewHistory.length - 1];
                viewHistory.pop(); 
              }}>
                ←
              </button>
              <h2 class="text-2xl font-bold">Sélectionnez le groupe et sous-groupe</h2>
            </div>
            <div class="mb-6">
              <label for="group-select" class="block mb-2 text-gray-700">Groupe :</label>
              <input id="group-select" type="text" bind:value={selectedGroup} class="w-full p-3 border rounded" placeholder="Entrez un groupe" on:input={handleGroupChange} list="group-options" />
              <datalist id="group-options">
                {#each groups.filter(group => group.toLowerCase().includes(selectedGroup.toLowerCase())) as group}
                  <option value={group}>{group}</option>
                {/each}
              </datalist>
            </div>
            <div class="mb-6">
              <label for="subgroup-select" class="block mb-2 text-gray-700">Sous-groupe :</label>
              <input id="subgroup-select" type="text" bind:value={selectedSubGroup} class="w-full p-3 border rounded" placeholder="Entrez un sous-groupe" on:input={handleSubGroupChange} list="subgroup-options" disabled={selectedGroup === ""} />
              <datalist id="subgroup-options">
                {#each (subGroups[selectedGroup] || []).filter(subGroup => subGroup.toLowerCase().includes(selectedSubGroup.toLowerCase())) as subGroup}
                  <option value={subGroup}>{subGroup}</option>
                {/each}
              </datalist>
            </div>
            {:else if currentView === "Supplier"}
            <!-- Sub-group Selection View -->
            <div class="flex items-center mb-4">
              <button class="text-gray-700 mr-4" on:click={() => {
                currentView = viewHistory[viewHistory.length - 1];
                viewHistory.pop();                  
              }}>
                ←
              </button>
              <h2 class="text-2xl font-bold">Sélectionnez le fournisseur</h2>
            </div>
            <div class="mb-6">
              <label for="supplier-select" class="block mb-2 text-gray-700">Fournisseur :</label>
              <input id="supplier-select" type="text" bind:value={selectedSupplier} class="w-full p-3 border rounded" placeholder="Entrez un fournisseur" on:input={handleSupplierChange} list="supplier-options" />
              <datalist id="supplier-options">
                {#each (Array.isArray(suppliers) ? suppliers : []).filter(s => s.name.toLowerCase().includes(selectedSupplier.toLowerCase())) as supplier}
                <option value={supplier.name}>{supplier.name}</option>
                {/each}
              </datalist>
            </div>
            {:else if currentView === "verification"}
              <!-- Column Verification View with Excel-like Table -->
              <div class="flex items-center mb-4">
                <button class="text-gray-700 mr-4" on:click={() => {
                  currentView = viewHistory[viewHistory.length - 1];
                  viewHistory.pop(); 
                }}>
                  ←
                </button>
                <h2 class="text-2xl font-bold mb-6">Vérification des Colonnes</h2>
              </div>
              <p class="text-gray-700 mb-4">Voici un aperçu du fichier Excel importé :</p>
              <div class="overflow-auto" style="max-height: 60vh; max-width: 100%;">
                {#if isImporting}
                  <div class="absolute inset-0 bg-white bg-opacity-80 flex flex-col justify-center items-center z-50">
                    <div class="loader ease-linear rounded-full border-8 border-t-8 border-gray-200 h-16 w-16 mb-4"></div>
                    <p class="text-gray-700 text-sm">Importation en cours...</p>
                  </div>
                {/if}
                {#if jsonData && jsonData.length > 0}
                  <table class="border-collapse border border-gray-400 w-full text-sm">
                      <thead>
                          <tr>
                              {#each jsonData[0] as header, index}
                                  <th class="border border-gray-400 p-2 bg-gray-200">
                                    <select
                                      bind:value={columnMapping[index]}
                                      class="w-full"
                                      style="min-width: {Math.max(80, (columnMapping[index]?.length || 4) * 10)}px"
                                      on:change={() => updateColumnMapping(index)}
                                    >                                                                        <option value="">vide</option>
                                          {#each requiredColumns.filter(col => !Object.values(columnMapping).includes(col) || columnMapping[index] === col) as column}
                                              <option value={column}>{column}</option>
                                          {/each}
                                      </select>
                                  </th>
                              {/each}
                          </tr>
                      </thead>
                      <tbody>
                          {#each jsonData.slice(1) as row, rowIndex}
                              <tr>
                                  {#each row as cell}
                                      <td class="border border-gray-400 p-2 text-center">{cell}</td>
                                  {/each}
                              </tr>
                          {/each}
                      </tbody>
                  </table>
                {:else}
                  <p class="text-gray-600">Aucune donnée disponible.</p>
                {/if}
              </div>
            <div class="flex justify-end mt-4">
              <button 
                class="border border-gray-500 text-gray-500 py-2 px-4 rounded-lg bg-blue-600 text-white"
                on:click={handleImportFinal}
              >
                Importer
              </button>
            </div>
          {/if}
        {/if}
        {#if currentView !== "verification"}
          <!-- Next Button for Main and Sub-group Views -->
          <button 
            class="border border-gray-500 text-gray-500 py-2 px-4 rounded-lg absolute bottom-0 right-0 mb-2 mr-2"
            style="opacity: {isNextEnabled ? 1 : 0.5}; pointer-events: {isNextEnabled ? 'auto' : 'none'}; background-color: {isNextEnabled ? '#c3dafe' : 'transparent'}; color: {isNextEnabled ? '#2d3748' : '#gray-500'};"
            on:click={handleNext}
          >
            Suivant
          </button>
        {/if}
      </div>
    </div>
  {/if}

  <!-- JSON Data Modal -->
  {#if showJsonModal}
    <div class="fixed inset-0 flex items-center justify-center overflow-auto" style="background: rgba(0, 0, 0, 0.1);">
      <!-- svelte-ignore a11y_no_static_element_interactions -->
      <div class="bg-white w-3/4 p-10 rounded-lg shadow-xl relative" style="left: {modalPosition.x}px; top: {modalPosition.y}px; position: absolute; min-height: 500px; max-height: 80vh; overflow-y: auto;" on:mousedown={handleMouseDown}>
        <button class="absolute top-2 right-2 text-gray-600" on:click={handleCloseJsonModal}>
          ✖
        </button>
        <h2 class="text-2xl font-bold mb-6">Données JSON Extraites</h2>
        <pre class="bg-gray-100 p-4 rounded overflow-x-auto text-sm">{JSON.stringify(jsonData, null, 2)}</pre>
      </div>
    </div>
  {/if}
</main>
  
<style>
  /* Styles for the main container */
  main {
    padding: 1rem;
  }
  /* Style to fix the modal position */
  .fixed {
    position: fixed;
  }
  /* Style to make the modal take up the entire viewport */
  .inset-0 {
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
  }
  /* Style to add shadow to the modal */
  .shadow-xl {
    box-shadow: 0 20px 25px rgba(0, 0, 0, 0.15);
  }
  /* Loader style */
  .loader {
    border-top-color: #3498db;
    animation: spinner 1.5s linear infinite;
  }
  @keyframes spinner {
    0% {
      transform: rotate(0deg);
    }
    100% {
      transform: rotate(360deg);
    }
  }
</style>
