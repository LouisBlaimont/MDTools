<script>
  import { _ } from "svelte-i18n";
  import { createEventDispatcher } from "svelte";

  export let suppliers = [];
  export let selectedSupplier = "";

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
  <h2 class="text-2xl font-bold">{$_('import_pages.supplier.select')}</h2>
</div>

<!-- Supplier input with suggestions -->
<div class="mb-6">
  <label for="supplier-select" class="block mb-2 text-gray-700">{$_('import_pages.supplier.supplier')}</label>
  <input
    id="supplier-select"
    type="text"
    bind:value={selectedSupplier}
    class="w-full p-3 border rounded"
    placeholder={$_('import_pages.supplier.enter_supp')}
    oninput={handleInput}
    list="supplier-options"
  />
  <datalist id="supplier-options">
    {#each filteredSuppliers() as supplier}
      <option value={supplier.name}>{supplier.name}</option>
    {/each}
  </datalist>
</div>
