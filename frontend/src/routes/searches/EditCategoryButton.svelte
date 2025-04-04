<script>
  import { modals } from "svelte-modals";
  import editCateogoryModal from "$lib/modals/editCategoryModal.svelte";
  import Icon from "@iconify/svelte";

  export let category;

  async function editCateogoryButton() {
    event.stopPropagation();
    await modals.open(editCateogoryModal, { category });
  }
</script>

<td class="text-center border border-solid border-[black] bg-cover hover:bg-yellow-500 w-[22px]">
  <button
    class="cursor-pointer"
    onclick={() => editCateogoryButton()}
    aria-label="Edit Category"
  >
    <Icon icon="material-symbols:edit" width="20" height="20" />
  </button>
</td>
