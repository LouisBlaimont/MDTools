<script>
  import { modals } from "svelte-modals";
  import editCateogoryModal from "$lib/modals/editCategoryModal.svelte";
  import { isEditing } from "$lib/stores/searches";
  import { toast } from "@zerodevx/svelte-toast";
  import Icon from "@iconify/svelte";

  function edit() {
    isEditing.update((value) => !value);
    if ($isEditing) {
      toast.push("Vous entrez en mode édition.");
    } else {
      toast.push("Vous quittez le mode édition.");
    }
  };
</script>

<div class="flex justify-center mt-8">
  <!-- svelte-ignore a11y_consider_explicit_label -->
  <button
    class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-500 rounded-full cursor-pointer"
    onclick={edit}
  >
    <Icon icon="material-symbols:edit" width="24" height="24" />
  </button>
</div>
