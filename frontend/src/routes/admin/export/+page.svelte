<script>
    import { fetchGroups, fetchCharacteristics, fetchSuppliers } from "../../../api.js";
    import { onMount } from "svelte";
  
    // State variables
    let showModal = false;
    let currentView = "main";
    let viewHistory = [];
    let selectedOption = "";
    let isNextEnabled = false;
  
    // Group and supplier selection
    let groups = [];
    let subGroups = {};
    let selectedGroup = "";
    let selectedSubGroup = "";
    let selectedSupplier = "";
    let suppliers = [];
  
    /**
     * Loads groups and their respective sub-groups from the backend.
     */
    async function loadGroups() {
      try {
        const data = await fetchGroups();
        groups = data.map(group => group.name);
        subGroups = Object.fromEntries(
          data.map(group => [group.name, group.subGroups.map(sub => sub.name)])
        );
      } catch (error) {
        console.error("Error while retrieving groups:", error);
      }
    }
  
    /**
     * Loads the list of suppliers.
     */
    async function loadSuppliers() {
      try {
        suppliers = await fetchSuppliers();
      } catch (error) {
        console.error("Error while retrieving suppliers:", error);
      }
    }
  
    /**
     * Handles selecting an export option.
     * @param {string} option The selected option.
     */
    const handleSelectOption = (option) => {
      selectedOption = option;
      isNextEnabled = true;
    };
  
    /**
     * Navigates to the next step.
     */
    const handleNext = async () => {
      viewHistory.push(currentView);
      
      if (currentView === "main") {
        if (selectedOption === "Catalogue") {
          currentView = "Supplier";
          isNextEnabled = false;
          selectedSupplier = "";
        } else {
          currentView = "SubGroup";
          isNextEnabled = false;
          selectedGroup = "";
          selectedSubGroup = "";
        }
      } else if (currentView === "Supplier" && isNextEnabled) {
        currentView = "confirmation";
      } else if (currentView === "SubGroup" && isNextEnabled) {
        currentView = "confirmation";
      }else if (currentView === "columns_selection") {
        currentView = "confirmation"; // Passage à la dernière étape
      }
    };
  
    /**
     * Navigates back to the previous view.
     */
    const handleBack = () => {
      currentView = viewHistory.pop();
      isNextEnabled = false;
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
    };
  
    /**
     * Handles changing the selected supplier.
     * @param {Event} event The change event.
     */
    const handleSupplierChange = (event) => {
      selectedSupplier = event.target.value;
      isNextEnabled = selectedSupplier !== "";
    };
    
    let selectedColumns = [
        "reference",
        "supplier_name",
        "sold_by_md",
        "closed",
        "supplier_description",
        "price",
        "obsolete",
        "characteristics"
    ];
    
    let availableLanguages = ["Français", "Néerlandais", "Anglais"];
    let selectedLanguages = ["Français"]; // Par défaut, une langue sélectionnée
    
    function toggleColumn(column) {
        if (selectedColumns.includes(column)) {
        selectedColumns = selectedColumns.filter(col => col !== column);
        } else {
        selectedColumns.push(column);
        }
    }
    
    function toggleLanguage(language) {
        if (selectedLanguages.includes(language)) {
        selectedLanguages = selectedLanguages.filter(lang => lang !== language);
        } else {
        selectedLanguages.push(language);
        }
    }
  
    onMount(() => {
      loadGroups();
      loadSuppliers();
    });
</script>
  
<main class="w-full flex flex-col items-center">
    <button class="bg-green-600 text-white py-2 px-4 rounded-lg mt-6" on:click={() => showModal = true}>
      Exporter un fichier Excel
    </button>
  
    {#if showModal}
      <div class="fixed inset-0 flex items-center justify-center bg-black bg-opacity-10">
        <div class="bg-white w-3/4 p-10 rounded-lg shadow-xl relative">
          <button class="absolute top-2 right-2 text-gray-600" on:click={() => showModal = false}>✖</button>
  
          {#if currentView === "main"}
            <h2 class="text-2xl font-bold mb-6">Options d'Exportation</h2>
            <div class="flex flex-col gap-6">
              <button class="option-button" on:click={() => handleSelectOption('SubGroup')}>Groupe et sous-groupe</button>
              <button class="option-button" on:click={() => handleSelectOption('Catalogue')}>Catalogue</button>
              <button class="option-button" on:click={() => handleSelectOption('Alternatives')}>Alternatives</button>
              <button class="option-button" on:click={() => handleSelectOption('Crossref')}>Crossref</button>
            </div>
          {:else if currentView === "SubGroup"}
            <button class="text-gray-700" on:click={handleBack}>← Retour</button>
            <h2 class="text-2xl font-bold">Sélectionnez un Groupe</h2>
            <select bind:value={selectedGroup} on:change={handleGroupChange}>
              {#each groups as group}
                <option value={group}>{group}</option>
              {/each}
            </select>
  
            <h2 class="text-2xl font-bold mt-4">Sélectionnez un Sous-Groupe</h2>
            <select bind:value={selectedSubGroup} on:change={handleSubGroupChange} disabled={!selectedGroup}>
              {#each (subGroups[selectedGroup] || []) as subGroup}
                <option value={subGroup}>{subGroup}</option>
              {/each}
            </select>
          {:else if currentView === "Supplier"}
            <button class="text-gray-700" on:click={handleBack}>← Retour</button>
            <h2 class="text-2xl font-bold">Sélectionnez un Fournisseur</h2>
            <select bind:value={selectedSupplier} on:change={handleSupplierChange}>
              {#each suppliers as supplier}
                <option value={supplier.name}>{supplier.name}</option>
              {/each}
            </select>
          {:else if currentView === "confirmation"}
            <button class="text-gray-700" on:click={handleBack}>← Retour</button>
            <h2 class="text-2xl font-bold mb-6">Prêt à Exporter</h2>
            <p>Option sélectionnée : {selectedOption}</p>
            {#if selectedGroup}
              <p>Groupe : {selectedGroup}</p>
              <p>Sous-Groupe : {selectedSubGroup}</p>
            {/if}
            {#if selectedSupplier}
              <p>Fournisseur : {selectedSupplier}</p>
            {/if}
            <button class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-4">Exporter</button>
          {/if}
  
          {#if currentView !== "confirmation"}
            <button class="border border-gray-500 text-gray-500 py-2 px-4 rounded-lg absolute bottom-0 right-0 mb-2 mr-2"
              on:click={handleNext} disabled={!isNextEnabled}>
              Suivant
            </button>
          {/if}

          {#if currentView === "columns_selection"}
            <button class="text-gray-700" on:click={handleBack}>← Retour</button>
            <h2 class="text-2xl font-bold mb-6">Sélectionnez les Colonnes à Exporter</h2>

            <div class="mb-4">
                <h3 class="text-lg font-semibold">Colonnes disponibles :</h3>
                {#each ["reference", "supplier_name", "sold_by_md", "closed", "supplier_description", "price", "obsolete", "characteristics"] as column}
                <label class="block">
                    <input type="checkbox" 
                    checked={selectedColumns.includes(column)} 
                    on:change={() => toggleColumn(column)} 
                  />
                  {column}
                </label>
                {/each}
            </div>

            <div class="mb-4">
                <h3 class="text-lg font-semibold">Langues disponibles :</h3>
                {#each availableLanguages as language}
                    <label class="block">
                        <input type="checkbox" 
                        checked={selectedLanguages.includes(language)} 
                        on:change={() => toggleLanguage(language)} 
                        />
                        {language}
                    </label>
                {/each}
            </div>

            <button class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-4" on:click={handleNext}>
                Suivant
            </button>
          {/if}
        </div>
      </div>
    {/if}
</main>
  
  <style>
    .option-button {
      background-color: #2d3748;
      color: white;
      padding: 10px;
      border-radius: 5px;
      text-align: center;
    }
  </style>
  