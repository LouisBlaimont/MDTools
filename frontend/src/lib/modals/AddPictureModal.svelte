<script>
  import { PUBLIC_API_URL } from "$env/static/public";
  import { getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { apiFetch } from "$lib/utils/fetch";

  const { isOpen, close, instrument = $bindable() } = $props();

  let files = [];

  async function submitForm() {
    if (files.length === 0) {
      toast.push("Veuillez sélectionner des images.");
      return;
    }

    const formData = new FormData();
    files.forEach((file) => {
      formData.append("files", file);
    });
    formData.append("referenceId", instrument.id);
    formData.append("type", "instrument");

    try {
      const response = await apiFetch(`/api/pictures/multiple`, {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        toast.push("Images ajoutées avec succès !");
        const responseData = await response.json();
        responseData.forEach((picture) => {
          instrument.picturesId.push(picture.id);
        });
        close();
      } else {
        toast.push("Échec de l'envoi des images. <br> Erreur : " + response.statusText);
      }
    } catch (error) {
      toast.push("Erreur lors de l'envoi des images.");
      console.error("Erreur:", error);
    }
  }
</script>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg lg:max-w-4xl"
        >
          <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="sm:flex sm:items-start mb-5">
              <div
                class="mx-auto flex size-12 shrink-0 items-center justify-center rounded-full bg-blue-100 sm:mx-0 sm:size-10"
              >
                <svg
                  width="16"
                  height="16"
                  fill="currentColor"
                  class="bi bi-camera"
                  viewBox="0 0 16 16"
                >
                  <path
                    d="M15 12a1 1 0 0 1-1 1H2a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h1.172a3 3 0 0 0 2.12-.879l.83-.828A1 1 0 0 1 6.827 3h2.344a1 1 0 0 1 .707.293l.828.828A3 3 0 0 0 12.828 5H14a1 1 0 0 1 1 1z"
                  />
                  <path
                    d="M8 11a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5m0 1a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7M3 6.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0"
                  />
                </svg>
              </div>
              <div class="text-center sm:mt-0 sm:ml-4 sm:text-left place-self-center">
                <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                  Ajouter des images pour {instrument.reference}
                </h3>
              </div>
            </div>

            <form onsubmit={submitForm} class="max-w-4xl mx-10">
              <div class="mb-4">
                <label class="block text-sm font-medium text-gray-700"
                  >Sélectionner des images</label
                >
                <input
                  type="file"
                  accept="image/*"
                  class="mt-1 block w-full"
                  multiple
                  onchange={(e) => (files = Array.from(e.target.files))}
                />
              </div>
              <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                <button
                  type="submit"
                  class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
                >
                  Ajouter les images
                </button>
                <button
                  type="button"
                  class="inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold border shadow shadow-xs hover:bg-gray-100 sm:ml-3 sm:w-auto"
                  onclick={close}
                >
                  Annuler
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}
