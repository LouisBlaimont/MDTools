<script>
    import { fetchAlternatives, fetchTools,fetchGroups, fetchCharacteristics, fetchInstrumentsBySubGroup, fetchSupplierByName, fetchInstrumentsBySupplier,fetchCharacteristicValuesByCategory, fetchSuppliers} from "../../../api.js";
    import { onMount } from "svelte";
    import { handleExport  } from "$lib/utils/exportToExcel";
    import { goto } from "$app/navigation";
    import { isAdmin } from "$lib/stores/user_stores";
    import { _ } from "svelte-i18n";
  
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
  
    let availableLanguages = [$_('admin.export.french'), $_('admin.export.dutch'), $_('admin.export.english')];
    let selectedLanguages = [$_('admin.export.french')];
  
    let characteristics = []; 
    let selectedCharacteristics = []; 
    
    async function exportToExcel(contextName, selectedColumns) {
      await handleExport({
        mode: selectedOption,
        contextName,
        selectedColumns,
        selectedCharacteristics
      });
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
          characteristics = characteristics.map(c => c.name);
          selectedCharacteristics = [...characteristics]; 
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
    <h1 class="text-2xl font-bold mb-4">{$_('admin.export.export_file')}</h1>
  
    <div class="flex gap-6">
      
      <div class="w-1/4 border p-4">
        <h2 class="text-lg font-semibold mb-2">{$_('admin.export.question')}</h2>
        {#each ["SubGroup", "Catalogue", "Alternatives", "Crossref", "Full"] as option}
          <label class="block">
            <input type="radio" name="exportOption" value={option} on:change={() => selectOption(option)} checked={selectedOption === option} />
            {option}
          </label>
        {/each}
      </div>
  
      {#if selectedOption === "SubGroup"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">{$_('admin.export.select')}</h2>
          <select bind:value={selectedGroup} on:change={handleGroupChange} class="w-full p-2 mt-2 border">
            <option value="">-- {$_('admin.export.choose')} --</option>
            {#each groups as group}
              <option value={group}>{group}</option>
            {/each}
          </select>
  
          <h2 class="text-lg font-semibold mt-4">{$_('admin.export.select_subgroup')}</h2>
          <select bind:value={selectedSubGroup} on:change={handleSubGroupChange} class="w-full p-2 mt-2 border" disabled={!selectedGroup}>
            <option value="">-- {$_('admin.export.choose_subgroup')} --</option>
            {#each (subGroups[selectedGroup] || []) as subGroup}
              <option value={subGroup}>{subGroup}</option>
            {/each}
          </select>
        </div>
      {/if}
      {#if selectedOption === "Catalogue"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">{$_('admin.export.select_supplier')}</h2>
          <select bind:value={selectedSupplier} class="w-full p-2 mt-2 border">
            <option value="">-- {$_('admin.export.choose_supp')} --</option>
            {#each suppliers as supplier}
              <option value={supplier.name}>{supplier.name}</option>
            {/each}
          </select>
        </div>
      {/if}

      {#if selectedOption === "Crossref"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">{$_('admin.export.include')}</h2>

          <label class="block mb-2">
            <input
              type="checkbox"
              checked={exportAllSuppliers}
              on:change={() => exportAllSuppliers = !exportAllSuppliers}
            />
            {$_('admin.export.exported')}
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

      {#if selectedOption === "Alternatives"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">{$_('admin.export.language')}</h2>
          {#each availableLanguages as language}
            <label class="block">
              <input
                type="checkbox"
                checked={selectedLanguages.includes(language)}
                on:change={() => toggleLanguage(language)}
              />
              {language}
            </label>
          {/each}

          <button
            class="bg-blue-600 text-white py-2 px-4 rounded-lg mt-4 w-full"
            on:click={() => exportToExcel(null, [])}
          >
            {$_('admin.export.export_button')}
          </button>
        </div>
      {/if}


  
      {#if (selectedOption === "SubGroup" && selectedSubGroup) || (selectedOption === "Catalogue" && selectedSupplier) || selectedOption === "Full"}
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">{$_('admin.export.column')}</h2>
          {#each allColumns as column}
            <label class="block">
              <input type="checkbox" checked={selectedColumns.includes(column)} on:change={() => toggleColumn(column)} />
              {column}
            </label>
          {/each}
  
          {#if characteristics.length > 0 && selectedOption === "SubGroup"}
            <h2 class="text-lg font-semibold mt-4">{$_('admin.export.char')}</h2>
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
        selectedOption === "Crossref" ||
        (selectedOption === "Full" && selectedColumns.length > 0)
      }      
        <div class="w-1/4 border p-4">
          <h2 class="text-lg font-semibold">{$_('admin.export.column')}</h2>
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
            {$_('admin.export.export_button')}
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