<script>
  import { modals } from "svelte-modals";
  import Icon from "@iconify/svelte";
  import EditAbbreviationModal from "$lib/modals/editAbbreviationModal.svelte";
  import { apiFetch } from "$lib/utils/fetch";

  let { abbreviation, refreshAbbreviations } = $props();

   async function deleteAbbrev() {
      try {
         const response = await apiFetch(`/api/category/abbreviation/${abbreviation.value}`, {
            method: "DELETE",
         });

         if (!response.ok) {
            throw new Error("Failed to delete abbreviation");
         }

         refreshAbbreviations();
      } catch (error) {
         console.error(error);
      }
   }

</script>

<div class="flex items-center">
  <button
    class="px-5 py-2 rounded-l-lg flex items-center transform transition bg-blue-600 hover:bg-blue-700 text-white"
    onclick={() => {
      event.stopPropagation();
      modals.open(EditAbbreviationModal, { abbreviation, refreshAbbreviations});
    }}
  >
    <span>
      <Icon icon="material-symbols:edit-rounded" width="24" height="24" />
    </span>
  </button>

  <button
    onclick={() => deleteAbbrev()}
    class="bg-red-500 text-white px-5 py-2 rounded-r-lg hover:bg-red-600 flex items-center transform transition"
  >
    <span>
      <Icon icon="material-symbols:delete-outline" width="24" height="24" />
    </span>
  </button>
</div>
