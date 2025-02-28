<script>
  import { modals } from "svelte-modals";
  import editCateogoryModal from "$lib/modals/editCategoryModal.svelte";

  export let category;

  async function editCateogoryButton() {
    event.stopPropagation();
    await modals.open(editCateogoryModal, { category });
  }
</script>

<td class="text-center border border-solid border-[black] bg-cover hover:bg-yellow-500">
  <button
    class="w-3.5 h-3 cursor-pointer"
    onclick={() => editCateogoryButton()}
    aria-label="Edit Category"
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      stroke="currentColor"
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="2"
      viewBox="0 0 24 24"
    >
      <path d="M12 20h9" />
      <path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L6 20l-4 1 1-4L16.5 3.5z" />
    </svg>
  </button>
</td>
