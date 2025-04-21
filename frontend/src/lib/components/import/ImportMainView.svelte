<script>
    import { createEventDispatcher } from "svelte";
  
    export let selectedOption = "";
    const dispatch = createEventDispatcher();
  
    function handleSelect(option) {
      dispatch("select", option);
    }
  
    const options = [
      { label: "Groupe et sous-groupe d'instruments", value: "SubGroup" },
      { label: "Instruments non catégorisés", value: "NonCategorized" },
      { label: "Catalogue", value: "Catalogue" },
      { label: "Alternatives", value: "Alternatives" },
      { label: "Crossref", value: "Crossref" }
    ];
  </script>
  
  <h2 class="text-2xl font-bold mb-6">Options d'Importation</h2>
  
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
  