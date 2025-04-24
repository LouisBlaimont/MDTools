<script>
    import { createEventDispatcher } from "svelte";
    import { _ } from "svelte-i18n";

  
    /** Currently selected import option */
    export let selectedOption = "";
  
    const dispatch = createEventDispatcher();
  
    /** Emits selected option to parent */
    function handleSelect(option) {
      dispatch("select", option);
    }
    
    /** Available import options */
    const options = [
      { label: $_('import_pages.main.group'), value: "SubGroup" },
      { label: $_('import_pages.main.instrument'), value: "NonCategorized" },
      { label: $_('import_pages.main.catalogue'), value: "Catalogue" },
      { label: $_('import_pages.main.alternative'), value: "Alternatives" },
      { label: $_('import_pages.main.crossref'), value: "Crossref" }
    ];
  </script>
  
  <h2 class="text-2xl font-bold mb-6">{$_('import_pages.main.option')}</h2>
  
  <!-- List of selectable import types -->
  <div class="flex flex-col gap-4">
    {#each options as { label, value }}
      <button
        class="py-3 px-6 rounded-lg text-white transition-colors"
        style="background-color: {selectedOption === value ? '#4a5568' : '#2d3748'}"
        onclick={() => handleSelect(value)}
      >
        {label}
      </button>
    {/each}
  </div>
  