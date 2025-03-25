<script>
    import { fetchTools,fetchGroups, fetchCharacteristics, fetchInstrumentsBySubGroup, fetchSupplierByName, fetchInstrumentsBySupplier,fetchCharacteristicValuesByCategory, fetchSuppliers} from "../../../api.js";
    import { onMount } from "svelte";
    import * as XLSX from "xlsx";
    import { goto } from "$app/navigation";
    import { isAdmin } from "$lib/stores/user_stores";


  
    // State variables
    let selectedOption = ""; // Un seul choix possible
    let groups = [];
    let subGroups = {};
    let selectedGroup = "";
    let selectedSubGroup = "";
    let suppliers = [];
    let selectedSupplier = "";
    let exportAllSuppliers = true; // checked by default
    let selectedCrossrefSuppliers = [];
  
    
    let allColumns = [
        "reference",
        "supplier",
        "sold_by_md",
        "closed",
        "supplierDescription",
        "price",
        "obsolete"
    ];
  
    let selectedColumns = [...allColumns]; 
  
    let availableLanguages = ["Français", "Néerlandais", "Anglais"];
    let selectedLanguages = ["Français"];
  
    let characteristics = []; 
    let selectedCharacteristics = []; 
    
    /**
     * Exports instrument data to an Excel file based on the selected export mode.
     *
     * Modes supported:
     * - "SubGroup": exports instruments of a specific subgroup with selected columns and characteristics.
     * - "Catalogue": exports instruments of a specific supplier, filtering out obsolete instruments if needed.
     * - "Crossref": builds a cross-reference matrix of categories vs suppliers, showing instrument references
     *               only for categories present in at least two different suppliers.
     *
     * @param {string} contextName - The subgroup name, supplier name, or null for "Crossref".
     * @param {string[]} selectedColumns - The list of selected columns to include in the Excel export.
     * @throws {Error} If the export fails due to missing input or API errors.
     */
    async function exportToExcel(contextName, selectedColumns) {
        const isCatalogue = selectedOption === "Catalogue";
        const isCrossref = selectedOption === "Crossref";

        // For SubGroup and Catalogue: validate inputs
        if (!isCrossref) {
            if (!contextName) {
                alert(`Please select a ${isCatalogue ? "supplier" : "subgroup"} before exporting.`);
                return;
            }

            if (!selectedColumns || selectedColumns.length === 0) {
                alert("Please select at least one column to export.");
                return;
            }
        }

        try {
            if (isCrossref) {
                // Handle Crossref export
                const instruments = await fetchTools(); // Fetch all instruments

                const suppliersSet = new Set();
                const categoriesMap = new Map();

                // Group instruments by category and supplier
                for (const instrument of instruments) {
                    const category = instrument.categoryId;
                    const supplier = instrument.supplier;
                    const reference = instrument.reference;

                    if (!category || !supplier) continue;

                    suppliersSet.add(supplier);

                    if (!categoriesMap.has(category)) {
                        categoriesMap.set(category, new Map());
                    }

                    const supplierMap = categoriesMap.get(category);
                    if (!supplierMap.has(supplier)) {
                        supplierMap.set(supplier, []);
                    }
                    supplierMap.get(supplier).push(reference);
                }

                // Keep only categories found in at least 2 different suppliers
                const filteredRows = [];
                for (const [categoryId, supplierMap] of categoriesMap.entries()) {
                    if (supplierMap.size < 2) continue;

                    const row = { Category: categoryId };
                    for (const supplier of suppliersSet) {
                        const refs = supplierMap.get(supplier);
                        row[supplier] = refs && refs.length > 0 ? refs.join(", ") : "";
                    }
                    filteredRows.push(row);
                }

                if (filteredRows.length === 0) {
                    alert("No categories found with instruments from at least two suppliers.");
                    return;
                }

                // Export Crossref data
                const worksheet = XLSX.utils.json_to_sheet(filteredRows);
                const workbook = XLSX.utils.book_new();
                XLSX.utils.book_append_sheet(workbook, worksheet, "Crossref");
                XLSX.writeFile(workbook, `export_crossref.xlsx`);
                return;
            }

            // Handle SubGroup or Catalogue export
            const instruments = isCatalogue
                ? await fetchInstrumentsBySupplier(contextName)
                : await fetchInstrumentsBySubGroup(contextName);

            if (!instruments || instruments.length === 0) {
                alert("No data available to export.");
                return;
            }

            const filteredData = [];

            for (const instrument of instruments) {
                // In Catalogue mode, skip obsolete instruments if 'obsolete' column is not selected
                if (isCatalogue && !selectedColumns.includes("obsolete") && instrument.obsolete === true) {
                    continue;
                }

                let row = {};

                // Include selected basic fields
                selectedColumns.forEach((col) => {
                    if (instrument.hasOwnProperty(col)) {
                        row[col] = instrument[col];
                    }
                });

                // Optionally fetch supplier info (closed, sold_by_md)
                if (selectedColumns.includes("closed") || selectedColumns.includes("sold_by_md")) {
                    const supplier = await fetchSupplierByName(instrument.supplier);
                    if (selectedColumns.includes("closed")) {
                        row["closed"] = supplier.closed;
                    }
                    if (selectedColumns.includes("sold_by_md")) {
                        row["sold_by_md"] = supplier.soldByMD;
                    }
                }

                // Optionally include characteristic values for SubGroup mode
                if (!isCatalogue && selectedCharacteristics.length > 0 && instrument.categoryId) {
                    const values = await fetchCharacteristicValuesByCategory(instrument.categoryId);
                    for (const characteristic of selectedCharacteristics) {
                        row[characteristic] = values[characteristic] ?? "";
                    }
                }

                filteredData.push(row);
            }

            // Export standard instrument data
            const worksheet = XLSX.utils.json_to_sheet(filteredData);
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Instruments");
            XLSX.writeFile(workbook, `export_${contextName}.xlsx`);
        } catch (error) {
            console.error("Error during export:", error);
            alert("An error occurred while exporting.");
        }
    }




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
        if (!isAdmin) {
          goto("/unauthorized");
      }
        loadGroups();
        loadSuppliers();
    });
  </script>
  
  <main class="w-full flex flex-col p-6">
    <h1 class="text-2xl font-bold mb-4">Exporter un fichier Excel</h1>
  
    <div class="flex gap-6">
      
      <div class="w-1/4 border p-4">
        <h2 class="text-lg font-semibold mb-2">Que voulez-vous exporter ?</h2>
        {#each ["SubGroup", "Catalogue", "Alternatives", "Crossref"] as option}
          <label class="block">
            <input type="radio" name="exportOption" value={option} on:change={() => selectOption(option)} checked={selectedOption === option} />
            {option}
          </label>
        {/each}
      </div>
  
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
      {#if selectedOption === "Catalogue"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">Sélectionnez un Fournisseur</h2>
          <select bind:value={selectedSupplier} class="w-full p-2 mt-2 border">
            <option value="">-- Choisir un fournisseur --</option>
            {#each suppliers as supplier}
              <option value={supplier.name}>{supplier.name}</option>
            {/each}
          </select>
        </div>
      {/if}

      {#if selectedOption === "Crossref"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">Suppliers à inclure</h2>

          <label class="block mb-2">
            <input
              type="checkbox"
              checked={exportAllSuppliers}
              on:change={() => exportAllSuppliers = !exportAllSuppliers}
            />
            Export all suppliers
          </label>

          {#if !exportAllSuppliers}
            <div class="mt-2">
              {#each suppliers as supplier}
                <label class="block">
                  <input
                    type="checkbox"
                    value={supplier.name}
                    checked={selectedCrossrefSuppliers.includes(supplier.name)}
                    on:change={(e) => {
                      const name = e.target.value;
                      if (e.target.checked) {
                        selectedCrossrefSuppliers = [...selectedCrossrefSuppliers, name];
                      } else {
                        selectedCrossrefSuppliers = selectedCrossrefSuppliers.filter(s => s !== name);
                      }
                    }}
                  />
                  {supplier.name}
                </label>
              {/each}
            </div>
          {/if}
        </div>
      {/if}

  
      {#if (selectedOption === "SubGroup" && selectedSubGroup) || (selectedOption === "Catalogue" && selectedSupplier)}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">Sélectionnez les Colonnes</h2>
          {#each allColumns as column}
            <label class="block">
              <input type="checkbox" checked={selectedColumns.includes(column)} on:change={() => toggleColumn(column)} />
              {column}
            </label>
          {/each}
  
          {#if characteristics.length > 0 && selectedOption === "SubGroup"}
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
  
      {#if
        (selectedOption === "SubGroup" && selectedSubGroup && selectedColumns.length > 0) ||
        (selectedOption === "Catalogue" && selectedSupplier && selectedColumns.length > 0) ||
        selectedOption === "Crossref"
      }      
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">Langues disponibles</h2>
          {#each availableLanguages as language}
            <label class="block">
              <input type="checkbox" checked={selectedLanguages.includes(language)} on:change={() => toggleLanguage(language)} />
              {language}
            </label>
          {/each}
  
          <button
            class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-4 w-full"
            on:click={() =>
              exportToExcel(
                selectedOption === "Crossref" ? null : (selectedOption === "Catalogue" ? selectedSupplier : selectedSubGroup),
                selectedColumns
              )
            }
          >
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