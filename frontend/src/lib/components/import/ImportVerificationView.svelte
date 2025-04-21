<script>
  export let jsonData = [];
  export let requiredColumns = [];
  export let columnMapping = {};
  export let selectedOption = "";
  export let showAddCharacteristicModal = false;

  import { createEventDispatcher } from "svelte";
  const dispatch = createEventDispatcher();

  function handleImportClick() {
    dispatch("import");
  }

  function handleBackClick() {
    dispatch("back");
  }

  /** Update the column mapping, or remove if value is empty */
  function updateColumnMapping(index, value) {
    if (!value || value.trim() === "") {
      delete columnMapping[index];
    } else {
      columnMapping[index] = value;
    }
  }
</script>

<!-- Header and back button -->
<div class="flex items-center mb-4">
  <button class="text-gray-700 mr-4" onclick={handleBackClick}>←</button>
  <h2 class="text-2xl font-bold">Vérification des Colonnes</h2>
</div>

<p class="text-gray-700 mb-4">Voici un aperçu du fichier Excel importé :</p>

<!-- Table preview of Excel content -->
<div class="overflow-auto" style="max-height: 60vh; max-width: 100%;">
  {#if jsonData && jsonData.length > 0}
    <table class="border-collapse border border-gray-400 w-full text-sm">
      <thead>
        <tr>
          {#each jsonData[0] as header, index}
            <th class="border border-gray-400 p-2 bg-gray-200">
              {#if selectedOption === "Alternatives"}
                <input
                  class="w-full bg-gray-100 border-none"
                  value={columnMapping[index]}
                  readonly
                />
              {:else}
                <select
                  bind:value={columnMapping[index]}
                  class="w-full"
                  style="min-width: {Math.max(80, (columnMapping[index]?.length || 4) * 10)}px"
                  onchange={(e) => {
                    const val = e.target.value;
                    if (val === "__add_new__") {
                      showAddCharacteristicModal = true;
                      e.target.value = "";
                    } else {
                      updateColumnMapping(index, val);
                    }
                  }}
                >
                  <option value="">vide</option>
                  {#each requiredColumns.filter(col => !Object.values(columnMapping).includes(col) || columnMapping[index] === col) as column}
                    <option value={column}>{column}</option>
                  {/each}
                  {#if selectedOption === "SubGroup"}
                    <option value="__add_new__">➕ Autre...</option>
                  {/if}
                </select>
              {/if}
            </th>
          {/each}
        </tr>
      </thead>
      <tbody>
        {#each jsonData.slice(1) as row}
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

<!-- Import button -->
<div class="flex justify-end mt-4">
  <button class="bg-blue-600 text-white py-2 px-4 rounded" onclick={handleImportClick}>
    Importer
  </button>
</div>
