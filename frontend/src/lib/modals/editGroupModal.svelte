<script>
  import { apiFetch } from "$lib/utils/fetch";
  import Icon from "@iconify/svelte";

  const {
    // provided by <Modals />
    isOpen,
    close,

    // your props
    group,
  } = $props();

  let name = $state(group.name);
  let file;

  async function handleSubmit(event) {
    event.preventDefault();

    // Check if the name has changed
    const nameChanged = name && name !== group.name;

    // Prepare the payload only if needed
    const payload = nameChanged ? { name } : null;

    // Update group name if it has changed
    if (nameChanged) {
      try {
        const response = await apiFetch("/api/groups/" + encodeURIComponent(group.name),
          {
            method: "PATCH",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
          }
        );

        if (!response.ok) {
          throw new Error("Échec de la mise à jour");
        }
      } catch (error) {
        console.error("Erreur:", error);
      }
    }

    // Update group picture if a new file is provided
    if (file) {
      try {
        const fileData = new FormData();
        fileData.append("file", file);
        const response = await apiFetch("/api/groups/" + encodeURIComponent(group.name) + "/picture",
          {
            method: "POST",
            body: fileData,
          }
        );
        if (!response.ok) {
          throw new Error("Échec de la mise à jour de l'image");
        }
      } catch (error) {
        console.error("Erreur:", error);
      }
    }
    close();
  }
</script>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
        >
          <form class="max-w-lg mx-auto" onsubmit={handleSubmit}>
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <div class="sm:flex sm:items-start">
                <div
                  class="mx-auto flex size-12 shrink-0 items-center justify-center rounded-full bg-orange-100 sm:mx-0 sm:size-10"
                >
                  <Icon icon="material-symbols:edit" width="24" height="24" />
                </div>
                <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                  <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                    Modifier un groupe
                  </h3>
                  <div class="mt-2">
                    <label class="block my-2 text-sm font-medium text-gray-900" for="user_avatar"
                      >Nom</label
                    >
                    <input
                      type="text"
                      bind:value={name}
                      class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
                    />

                    <label class="block my-2 text-sm font-medium text-gray-900" for="user_avatar"
                      >Image</label
                    >
                    <input
                      class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5"
                      type="file"
                      onchange={(e) => (file = e.target.files[0])}
                    />
                    {#if group.pictureId}
                      <div class="mt-1 text-sm text-red-500">
                        Une image existe déjà pour ce groupe, en indiquant une image ci-dessus,
                        l'image actuelle sera supprimée.
                      </div>
                    {/if}
                  </div>
                </div>
              </div>
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
              <button
                type="submit"
                class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
                >Enregistrer</button
              >
              <button
                type="button"
                class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50 sm:mt-0 sm:w-auto"
                onclick={() => close()}>Annuler</button
              >
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
{/if}
