<script>
    import { modals } from "svelte-modals";
    import EditSupplierModal from "$lib/modals/editSupplierModal.svelte";

    export let supplier;

    function editSupplier() {
      event.stopPropagation();
      modals.open(EditSupplierModal, { supplier });
    }
</script>

<div class="inline-flex rounded-md shadow-sm">
    <button
      onclick={editSupplier}
      class="bg-cyan-500 text-white px-5 py-2 rounded-lg hover:bg-cyan-600 flex items-center transform transition"
    >
      <span>
        <svg
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
          stroke-width="1.5"
          stroke="currentColor"
          class="size-6"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            d="M16.862 4.487a2.25 2.25 0 1 1 3.182 3.182L7.5 20.182l-4.5 1.5 1.5-4.5 12.362-12.695Z"
          />
        </svg>
      </span>
      <span class="pl-2">Modifier</span>
    </button>
</div>
