<script>
  import { PUBLIC_API_URL } from "$env/static/public";
  import { onMount, getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import AddPictureModal from "./AddPictureModal.svelte";
  import { apiFetch } from "$lib/utils/fetch";

  const {
    // provided by <Modals />
    isOpen,
    close,

    // your props
    initInstrument,
  } = $props();

  let instrument = $state(initInstrument);
  console.log(initInstrument);

  async function deltePicture(id, index) {
    try {
      const response = await apiFetch("/api/pictures/" + encodeURIComponent(id), {
        method: "DELETE",
      });
      if (!response.ok) {
        throw new Error("Échec de la suppression de l'image. Erreur : " + response.statusText);
      }
      // remove from array
      instrument.picturesId.splice(index, 1);
    } catch (error) {
      console.error("Erreur:", error);
    }
  }

  let deleteTimeouts = {};

  function handleDelete(id, index) {
    if (deleteTimeouts[id]) {
      // Second click: Delete the image
      clearTimeout(deleteTimeouts[id]);
      delete deleteTimeouts[id];
      deltePicture(id, index);
      toast.pop(0); // Remove the confirmation toast
      toast.push("Image supprimée avec succès.", {
        theme: {
          "--toastColor": "white",
          "--toastBackground": "#28a745",
          "--toastBarBackground": "#218838",
        },
      });
    } else {
      // First click: Ask for confirmation
      toast.push(
        "<strong>Êtes-vous sûr de vouloir supprimer cette image ?</strong><br>Cliquez à nouveau pour confirmer.",
        {
          theme: {
            "--toastColor": "black",
            "--toastBackground": "#e2854b",
            "--toastBarBackground": "#ca6221",
          },
        }
      );

      deleteTimeouts[id] = setTimeout(() => {
        delete deleteTimeouts[id];
      }, 5000); // 5 seconds timeout to cancel the confirmation
    }
  }
</script>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div
        class="flex flex-wrap flex-row min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0"
      >
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg lg:max-w-4xl"
        >
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start mb-5">
              <div
                class="mx-auto flex size-12 shrink-0 items-center justify-center rounded-full bg-blue-100 sm:mx-0 sm:size-10"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  fill="currentColor"
                  class="bi bi-camera"
                  viewBox="0 0 16 16"
                >
                  <path
                    d="M15 12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h1.172a3 3 0 0 0 2.12-.879l.83-.828A1 1 0 0 1 6.827 3h2.344a1 1 0 0 1 .707.293l.828.828A3 3 0 0 0 12.828 5H14a1 1 0 0 1 1 1zM2 4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-1.172a2 2 0 0 1-1.414-.586l-.828-.828A2 2 0 0 0 9.172 2H6.828a2 2 0 0 0-1.414.586l-.828.828A2 2 0 0 1 3.172 4z"
                  />
                  <path
                    d="M8 11a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5m0 1a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7M3 6.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0"
                  />
                </svg>
              </div>
              <div class="text-center sm:mt-0 sm:ml-4 sm:text-left place-self-center">
                <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                  Photos de l'instrument {instrument.reference}
                </h3>
              </div>
            </div>
            {#if instrument.picturesId.length == 0}
              <div class="text-center w-full m-5 my-12">
                <p>Pas de photos pour cet instrument</p>
              </div>
            {/if}
            <div class="mx-10 grid grid-cols-2 md:grid-cols-3 gap-4">
              {#each instrument.picturesId as id, index}
                <div class="relative">
                  <button
                    class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full shadow-md hover:bg-red-600"
                    onclick={() => handleDelete(id, index)}
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      fill="currentColor"
                      class="bi bi-trash"
                      viewBox="0 0 16 16"
                    >
                      <path
                        d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z"
                      />
                      <path
                        d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z"
                      />
                    </svg>
                  </button>
                  <img
                    class="h-auto max-w-full rounded-lg"
                    src={PUBLIC_API_URL + "/api/pictures/" + encodeURIComponent(id)}
                    alt="picture {index}"
                  />
                </div>
              {/each}
            </div>
            <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
              <button
                class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
                onclick={() => close()}>Fermer</button
              >
              <button
                class="inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold border shadow shadow-xs hover:bg-gray-100 sm:ml-3 sm:w-auto"
                onclick={() => modals.open(AddPictureModal, { instrument })}
                >Ajouter une image
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}
