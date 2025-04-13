<script>
  import { PUBLIC_API_URL } from "$env/static/public";
  import { onMount, getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import AddPictureModal from "./AddPictureModal.svelte";
  import { apiFetch } from "$lib/utils/fetch";
  import { currentSuppliers } from "$lib/stores/searches";
  import Icon from "@iconify/svelte";
  import { isAdmin } from "$lib/stores/user_stores";
  import { _ } from "svelte-i18n";

  const {
    // provided by <Modals />
    isOpen,
    close,

    // your props
    instrument,
    index, // index of the instrument in the list
  } = $props();

  let instrument_reactive = $state(instrument);

  async function deltePicture(id, index) {
    try {
      const response = await apiFetch("/api/pictures/" + encodeURIComponent(id), {
        method: "DELETE",
      });
      if (!response.ok) {
        throw new Error($_('modals.big_picture.deletion_error') + response.statusText);
      }
      // remove from array
      instrument_reactive.picturesId.splice(index, 1);
      // remove from the list of instruments
      currentSuppliers.update((suppliers) => {
        suppliers.forEach((supplier) => {
          if (supplier.id === instrument.id) {
            supplier.picturesId.splice(index, 1);
          }
        });
        return suppliers;
      });
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
      toast.push($_('modals.big_picture.deletion_success'), {
        theme: {
          "--toastColor": "white",
          "--toastBackground": "#28a745",
          "--toastBarBackground": "#218838",
        },
      });
    } else {
      // First click: Ask for confirmation
      toast.push(
        $_('modals.big_picture.deletion_confirmation'),
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
                <Icon icon="material-symbols:photo-rounded" width="24" height="24" />
              </div>
              <div class="text-center sm:mt-0 sm:ml-4 sm:text-left place-self-center">
                <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                  {$_('modals.big_picture.title')} {instrument.reference}
                </h3>
              </div>
            </div>
            {#if instrument_reactive.picturesId.length == 0}
              <div class="text-center w-full m-5 my-12">
                <p>{$_('modals.big_picture.no_pictures')}</p>
              </div>
            {/if}
            <div class="mx-10 grid grid-cols-2 md:grid-cols-3 gap-4">
              {#each instrument_reactive.picturesId as id, index}
                <div class="relative">
                  <!-- svelte-ignore a11y_consider_explicit_label -->
                  {#if $isAdmin}
                    <button
                      class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full shadow-md hover:bg-red-600"
                      onclick={() => handleDelete(id, index)}
                    >
                      <Icon icon="material-symbols:delete-forever" width="16" height="16" />
                    </button>
                  {/if}
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
                onclick={() => close()}>{$_('modals.big_picture.close')}</button
              >
              <button
                class="inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold border shadow shadow-xs hover:bg-gray-100 sm:ml-3 sm:w-auto"
                onclick={() => modals.open(AddPictureModal, { instrument, index })}
                >
                {$_('modals.big_picture.add_picture')}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}
