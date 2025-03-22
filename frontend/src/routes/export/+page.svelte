<script>
  import { fetchGroups, fetchCharacteristics, fetchInstrumentsBySubGroup} from "../../api.js";
  import { onMount } from "svelte";
  import * as XLSX from "xlsx";

  // State variables
  let selectedOption = ""; // Un seul choix possible
  let groups = [];
  let subGroups = {};
  let selectedGroup = "";
  let selectedSubGroup = "";

  let allColumns = [
      "reference",
      "supplier",
      "sold_by_md",
      "closed",
      "supplierDescription",
      "price",
      "obsolete"
  ];

  let selectedColumns = [...allColumns]; // Colonnes cochées par défaut

  let availableLanguages = ["Français", "Néerlandais", "Anglais"];
  let selectedLanguages = ["Français"];

  let characteristics = []; // Stockera les caractéristiques dynamiques
  let selectedCharacteristics = []; // Stockera les caractéristiques sélectionnées
  
  /**
   * Exports instrument data from a subgroup to an Excel file,
   * only including the selected columns.
   * 
   * @param {string} subGroupName - The name of the subgroup to fetch instruments from.
   * @param {Array<string>} selectedColumns - The list of columns to include in the export.
   */
  async function exportToExcel(subGroupName, selectedColumns) {
    if (!subGroupName) {
      alert("Please select a subgroup before exporting.");
      return;
    }
    
    if (!selectedColumns || selectedColumns.length === 0) {
      alert("Please select at least one column to export.");
      return;
    }


    try {
      
      // Fetch all instruments for the given subgroup
      const instruments = await fetchInstrumentsBySubGroup(subGroupName);

      if (!instruments || instruments.length === 0) {
        alert("No data available to export.");
        return;
      }

      // Filter and order data based on selected columns
      const filteredData = instruments.map(instrument => {
        let row = {};
        selectedColumns.forEach(col => {
          if (instrument.hasOwnProperty(col)) {
            row[col] = instrument[col];
          }
        });
        return row;
      });

      // Convert the data into an Excel-compatible format
      const worksheet = XLSX.utils.json_to_sheet(filteredData);

      // Create a new workbook and append the worksheet
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, "Instruments");

      // Generate and download the Excel file
      XLSX.writeFile(workbook, `export_${subGroupName}.xlsx`);
    } catch (error) {
      console.error("Error during export:", error);
      alert("An error occurred while exporting.");
    }
  }


  // Load groups and subgroups
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

  // Load characteristics for the selected sub-group
  async function loadCharacteristics() {
    if (selectedSubGroup) {
      try {
        characteristics = await fetchCharacteristics(selectedSubGroup);
        selectedCharacteristics = [...characteristics]; // Par défaut, toutes cochées
      } catch (error) {
        console.error("Error while retrieving characteristics:", error);
      }
    }
  }

  // Handle option selection (only one at a time)
  function selectOption(option) {
    selectedOption = option;
    selectedGroup = "";
    selectedSubGroup = "";
    characteristics = [];
    selectedCharacteristics = [];
  }

  // Handle group selection
  function handleGroupChange(event) {
    selectedGroup = event.target.value;
    selectedSubGroup = "";
    characteristics = [];
    selectedCharacteristics = [];
  }

  // Handle sub-group selection
  async function handleSubGroupChange(event) {
    selectedSubGroup = event.target.value;
    await loadCharacteristics();
  }

  // Toggle columns selection (but keep them visible)
  function toggleColumn(column) {
    if (selectedColumns.includes(column)) {
      selectedColumns = selectedColumns.filter(col => col !== column);
    } else {
      selectedColumns.push(column);
    }
  }

  // Toggle characteristics selection
  function toggleCharacteristic(characteristic) {
    if (selectedCharacteristics.includes(characteristic)) {
      selectedCharacteristics = selectedCharacteristics.filter(char => char !== characteristic);
    } else {
      selectedCharacteristics.push(characteristic);
    }
  }

  // Toggle languages selection
  function toggleLanguage(language) {
    if (selectedLanguages.includes(language)) {
      selectedLanguages = selectedLanguages.filter(lang => lang !== language);
    } else {
      selectedLanguages.push(language);
    }
  }

  onMount(() => {
    loadGroups();
  });
</script>

<main class="w-full flex flex-col p-6">
  <h1 class="text-2xl font-bold mb-4">Exporter un fichier Excel</h1>

  <!-- Section de sélection des options -->
  <div class="flex gap-6">
    
    <!-- Étape 1: Choix unique de l'exportation -->
    <div class="w-1/4 border p-4">
      <h2 class="text-lg font-semibold mb-2">Que voulez-vous exporter ?</h2>
      {#each ["SubGroup", "Catalogue", "Alternatives", "Crossref"] as option}
        <label class="block">
          <input type="radio" name="exportOption" value={option} on:change={() => selectOption(option)} checked={selectedOption === option} />
          {option}
        </label>
      {/each}
    </div>

    <!-- Étape 2: Sélection du groupe et sous-groupe -->
    {#if selectedOption === "SubGroup"}
      <div class="w-1/4 border p-4">
        <h2 class="text-lg font-semibold">Sélectionnez un Groupe</h2>
        <select bind:value={selectedGroup} on:change={handleGroupChange} class="w-full p-2 mt-2 border">
          <option value="">-- Choisir un groupe --</option>
          {#each groups as group}
            <option value={group}>{group}</option>
          {/each}
        </select>

        <h2 class="text-lg font-semibold mt-4">Sélectionnez un Sous-Groupe</h2>
        <select bind:value={selectedSubGroup} on:change={handleSubGroupChange} class="w-full p-2 mt-2 border" disabled={!selectedGroup}>
          <option value="">-- Choisir un sous-groupe --</option>
          {#each (subGroups[selectedGroup] || []) as subGroup}
            <option value={subGroup}>{subGroup}</option>
          {/each}
        </select>
      </div>
    {/if}

    <!-- Étape 3: Sélection des colonnes (ne disparaissent plus) -->
    {#if selectedSubGroup}
      <div class="w-1/4 border p-4">
        <h2 class="text-lg font-semibold">Sélectionnez les Colonnes</h2>
        {#each allColumns as column}
          <label class="block">
            <input type="checkbox" checked={selectedColumns.includes(column)} on:change={() => toggleColumn(column)} />
            {column}
          </label>
        {/each}

        {#if characteristics.length > 0}
          <h2 class="text-lg font-semibold mt-4">Caractéristiques</h2>
          {#each characteristics as characteristic}
            <label class="block">
              <input type="checkbox" checked={selectedCharacteristics.includes(characteristic)} on:change={() => toggleCharacteristic(characteristic)} />
              {characteristic}
            </label>
          {/each}
        {/if}
      </div>
    {/if}

    <!-- Étape 4: Bouton d'exportation -->
    {#if selectedSubGroup && selectedColumns.length > 0}
      <div class="w-1/4 border p-4">
        <h2 class="text-lg font-semibold">Langues disponibles</h2>
        {#each availableLanguages as language}
          <label class="block">
            <input type="checkbox" checked={selectedLanguages.includes(language)} on:change={() => toggleLanguage(language)} />
            {language}
          </label>
        {/each}

        <button class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-4 w-full" on:click={() => exportToExcel(selectedSubGroup, selectedColumns)}>
          Exporter
        </button>        
      </div>
    {/if}

  </div>
</main>

<style>
  select {
    background-color: white;
  }
</style>
