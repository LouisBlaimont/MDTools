<script>
  import { apiFetch } from "$lib/utils/fetch";
  import { onMount, getContext } from "svelte";
  import { reload } from "$lib/stores/searches";
  import Icon from "@iconify/svelte";
  import Loading from "$lib/Loading.svelte";

  const {
    // provided by <Modals />
    isOpen,
    close,

    // your props
    category,
  } = $props();

  let file;
  let name = $state(category.name);
  let characteristics = $state([]);

  async function handleSubmit(event) {
    event.preventDefault();

    if (file) {
      try {
        const fileData = new FormData();
        fileData.append("file", file);
        const response = await apiFetch("/api/category/" + encodeURIComponent(category.id) + "/picture",
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

    if (characteristicsEdited) {
      try {
        const response = await apiFetch(
          "/api/category/" + encodeURIComponent(category.id),
          {
            method: "PATCH",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(characteristics),
          }
        );
        console.log(response);
        if (!response.ok) {
          throw new Error("Échec de la mise à jour des caractéristiques");
        }
      } catch (error) {
        console.error("Erreur:", error);
      }
    }
    reload.set(true);
    close();
  }

  async function fetchCharacteristics() {
    try {
      const response = await apiFetch(
        "/api/category/" + encodeURIComponent(category.id)
      );
      if (!response.ok) {
        throw new Error(`Failed to fetch characteristics: ${response.statusText}`);
      }
      characteristics = await response.json();
    } catch (error) {
      console.error(error);
    }
    return;
  }

  let characteristicsEdited = false;

  let promise = fetchCharacteristics();
</script>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg  lg:max-w-4xl"
        >
          <form class="max-w-4xl mx-auto" onsubmit={handleSubmit}>
            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <div class="sm:flex sm:items-start">
                <div
                  class="mx-auto flex size-12 shrink-0 items-center justify-center rounded-full bg-orange-100 sm:mx-0 sm:size-10"
                >
                  <Icon icon="material-symbols:edit" width="24" height="24" />
                </div>
                <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                  <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                    Modifier une catégorie
                  </h3>
                  {#await promise}
                    <div role="status" class="my-6 flex items-center justify-center">
                      <Loading />
                    </div>
                  {:then}
                    <div class="mt-2">
                      <label class="block my-2 text-sm font-medium text-gray-900" for="user_avatar"
                        >Nom (ne fonctionne pas !)</label
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
                      {#if category.pictureId != null}
                        <div class="mt-1 text-sm text-red-500">
                          Une image existe déjà pour ce groupe, en indiquant une image ci-dessus,
                          l'image actuelle sera supprimée.
                        </div>
                      {/if}
                    </div>
                    <div class="my-2">
                      <h3 class="text-base text-gray-900">Caractéristiques</h3>
                      <div class="grid grid-cols-2 gap-4">
                        {#each characteristics as characteristic}
                          <div>
                            <label class="text-sm font-medium text-gray-900" for="user_avatar"
                              >{characteristic.name}</label
                            >
                            <input
                              type="text"
                              bind:value={characteristic.value} onchange={() => (characteristicsEdited = true)}
                              class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
                            />
                          </div>
                        {/each}
                      </div>
                    </div>
                  {/await}
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
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
{/if}
