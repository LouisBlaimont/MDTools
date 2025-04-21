<script>
  export let suppliers = [];
  export let selectedSupplier = "";

  import { createEventDispatcher } from "svelte";
  const dispatch = createEventDispatcher();

  /** Emit selected supplier on input */
  function handleInput(e) {
    dispatch("supplierChange", e.target.value);
  }

  /** Filter suggestions based on input */
  function filteredSuppliers() {
    return suppliers.filter(s =>
      s.name.toLowerCase().includes(selectedSupplier.toLowerCase())
    );
  }
</script>

<!-- Header with back navigation -->
<div class="flex items-center mb-4">
  <button class="text-gray-700 mr-4" onclick={() => dispatch("back")}>←</button>
  <h2 class="text-2xl font-bold">Sélectionnez le fournisseur</h2>
</div>

<!-- Supplier input with suggestions -->
<div class="mb-6">
  <label for="supplier-select" class="block mb-2 text-gray-700">Fournisseur :</label>
  <input
    id="supplier-select"
    type="text"
    bind:value={selectedSupplier}
    class="w-full p-3 border rounded"
    placeholder="Entrez un fournisseur"
    oninput={handleInput}
    list="supplier-options"
  />
  <datalist id="supplier-options">
    {#each filteredSuppliers() as supplier}
      <option value={supplier.name}>{supplier.name}</option>
    {/each}
  </datalist>
</div>
