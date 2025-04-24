<script>
  import { modals } from "svelte-modals";
  import { isEditing } from "$lib/stores/searches";
  import { toast } from "@zerodevx/svelte-toast";
  import Icon from "@iconify/svelte";

  function edit() {
    isEditing.update((value) => !value);
    if ($isEditing) {
      toast.push("Vous entrez en mode édition.");
      document.getElementById("edit-button").classList.add("bg-yellow-500");
    } else {
      toast.push("Vous quittez le mode édition.");
      document.getElementById("edit-button").classList.remove("bg-yellow-500");

    }
  };
</script>

<div class="flex justify-center">
  <!-- svelte-ignore a11y_consider_explicit_label -->
  <button
    class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-500 rounded-full cursor-pointer"
    id="edit-button"
    onclick={edit}
  >
  <Icon icon="material-symbols:edit" width="20" height="20" />
  </button>
</div>
