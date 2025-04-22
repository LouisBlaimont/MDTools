<script>
  import { PUBLIC_API_URL } from "$env/static/public";
  import { getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { apiFetch } from "$lib/utils/fetch";
  import { currentSuppliers, categories } from "$lib/stores/searches";
  import Icon from "@iconify/svelte";
  import { _ } from "svelte-i18n";
  import { modals } from "svelte-modals";
  import BigPicturesModal from "./BigPicturesModal.svelte";

  const { isOpen, close, instrument, index , isInstrument } = $props();

  let files = $state([]);

  /**
   * Sending the pictures to the backend
   */
  async function submitForm() {
    if (files.length === 0) {
      toast.push($_('modals.add_picture.need_select'));
      return;
    }

    const formData = new FormData();
    files.forEach((file) => {
      formData.append("files", file);
    });
    formData.append("referenceId", instrument.id);
    if (isInstrument) {
      formData.append("type", "instrument");
    }
    else {
      formData.append("type", "category");
    }
  
    try {
      const response = await apiFetch(`/api/pictures/multiple`, {
        method: "POST",
        body: formData,
      });

      if (response.ok) {
        toast.push($_('modals.add_picture.ok'));
        if (isInstrument) {
          const responseData = await response.json();
          responseData.forEach((picture) => {
            currentSuppliers.update((suppliers) => {
              suppliers.forEach((supplier) => {
                if (supplier.id === instrument.id) {
                  supplier.picturesId.push(picture.id);
                }
              });
              return suppliers;
            });
          close();
          modals.open(BigPicturesModal, { instrument, index, isInstrument });
          });
        }
        else {
          const responseData = await response.json();
          responseData.forEach((picture) => {
            categories.update((cats) => {
              cats.forEach((cat) => {
                if (cat.id === instrument.id) {
                  cat.picturesId.push(picture.id);
                }
              });
              return cats;
            });
          close();
          modals.open(BigPicturesModal, { instrument, index, isInstrument });
          });
        }
      }
    
      else {
        toast.push($_('modals.add_picture.fail') + response.statusText);
      }
    } catch (error) {
      toast.push($_('modals.add_picture.fail2'));
      console.error($_('modals.add_picture.error'), error);
    }
  }

  /**
   * binding the files to the real input 
   */
  function inputFiles() {
    document.getElementById('fileInput').click();
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
                <Icon icon="material-symbols:photo-rounded" width="24" height="24" />
              </div>
              <div class="text-center sm:mt-0 sm:ml-4 sm:text-left place-self-center">
                <h3 class="text-base font-semibold text-gray-900" id="modal-title">
                  {#if isInstrument}
                    {$_('modals.add_picture.titleInstr')} {instrument.reference}
                  {:else} 
                    {$_('modals.add_picture.titleCat')}
                  {/if}
                </h3>
              </div>
            </div>

            <form onsubmit={submitForm} class="max-w-4xl mx-10">
              <div class="mb-4 flex">
                <!-- svelte-ignore a11y_label_has_associated_control -->
                <div>
                  <!-- Second input to cover the one by default (is hidden) in order to change the language -->
                  <input 
                  type="button"
                  id="loadFile" 
                  for="fileInput"
                  class="border rounded bg-gray-200 border-black p-1"
                  value={$_('modals.add_picture.file')}
                  onclick={() => inputFiles()}
                  />
                  <input
                    type="file"
                    accept="image/*"
                    class="mt-1 block w-full"
                    style="display:none;"
                    id="fileInput"
                    multiple
                    onchange={(e) => (files = Array.from(e.target.files))}
                  />
                </div>
                <div class="ml-2">
                  {#each files as file}
                    {file.name}
                    <br>
                  {/each}
                </div>
              </div>
              <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                <button
                  type="submit"
                  class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
                >{$_('modals.add_picture.add')}</button>
                <button
                  type="button"
                  class="inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold border shadow shadow-xs hover:bg-gray-100 sm:ml-3 sm:w-auto"
                  onclick={() => ( modals.open(BigPicturesModal, { instrument, index, isInstrument }),
                close())}
                >
                  {$_('modals.add_picture.button.cancel')}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}
