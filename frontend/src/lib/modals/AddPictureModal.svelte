<script>
  import { PUBLIC_API_URL } from "$env/static/public";
  import { getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { apiFetch } from "$lib/utils/fetch";
  import { currentSuppliers, categories, alternatives } from "$lib/stores/searches";
  import Icon from "@iconify/svelte";
  import { _ } from "svelte-i18n";
  import { modals } from "svelte-modals";
  import BigPicturesModal from "./BigPicturesModal.svelte";

  const { isOpen, close, instrument, index, isInstrument, isAlternative } = $props();

  let files = $state([]);

  let posX = $state(0);
  let posY = $state(0); 
  let offsetX = 0;
  let offsetY = 0;
  let isDragging = false;

  function startDrag(event) {
      isDragging = true;
      offsetX = event.clientX - posX;
      offsetY = event.clientY - posY;
  }

  function drag(event) {
      if (isDragging) {
          posX = event.clientX - offsetX;
          posY = event.clientY - offsetY;
      }
  }

  function stopDrag() {
      isDragging = false;
  }

  /**
   * Sending the pictures to the backend
   */
  async function submitForm(event) {
    event.preventDefault();
    
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
          const newPictureIds = responseData.map((picture)=> picture.id);
          currentSuppliers.update((suppliers) => {
            suppliers.forEach((supplier) => {
              if(supplier.id === instrument.id){
                supplier.picturesId.push(...newPictureIds);
              }
            });
            return suppliers;
          });
          if(isAlternative){
            alternatives.update((alts) => {
              alts.forEach((alt)=>{
                if(alt.id === instrument.id){
                  alt.picturesId.push(...newPictureIds);
                }
              });
              return alts;
            });
          }
          close();
          modals.open(BigPicturesModal, { instrument, index, isInstrument, isAlternative });
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
          modals.open(BigPicturesModal, { instrument, index, isInstrument, isAlternative });
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
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div 
      class="fixed inset-0 z-10 flex items-center justify-center"
      onmousemove={drag}
      onmouseup={stopDrag}
  >
      <div 
          class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
          style="transform: translate({posX}px, {posY}px);"
      >
          <div 
              class="p-4 border-b cursor-move bg-gray-200 flex items-center justify-between rounded-t-lg select-none"
              onmousedown={startDrag}
          >
              <h2 class="text-2xl font-bold text-teal-500 text-center" id="modal-title">
                {#if isInstrument}
                  {$_('modals.add_picture.titleInstr')} {instrument.reference}
                {:else} 
                  {$_('modals.add_picture.titleCat')}
                {/if}
              </h2>
              <Icon icon="material-symbols:photo-rounded" width="24" height="24" class="text-teal-500"/>
          </div>
          
          <div class="p-6 bg-gray-100">
            <form onsubmit={submitForm} class="w-full">
              <div class="mb-4 flex">
                <div>
                  <!-- Second input to cover the one by default (is hidden) in order to change the language -->
                  <input 
                    type="button"
                    id="loadFile" 
                    for="fileInput"
                    class="border rounded bg-gray-200 border-gray-400 p-2 hover:bg-gray-300 transition"
                    value={$_('modals.add_picture.file')}
                    onclick={() => inputFiles()}
                  />
                  <input
                    type="file"
                    accept="image/*"
                    class="hidden"
                    id="fileInput"
                    multiple
                    onchange={(e) => (files = Array.from(e.target.files))}
                  />
                </div>
                <div class="ml-4 flex-1">
                  {#if files.length > 0}
                    <div class="p-2 bg-white rounded border border-gray-300">
                      {#each files as file}
                        <div class="text-sm text-gray-600">{file.name}</div>
                      {/each}
                    </div>
                  {:else}
                    <div class="p-2 text-gray-500 italic">{$_('modals.add_picture.no_files_selected')}</div>
                  {/if}
                </div>
              </div>
              
              <div class="mt-6 sm:flex sm:flex-row-reverse">
                <button
                  type="submit"
                  class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
                >{$_('modals.add_picture.add')}</button>
                <button
                  type="button"
                  class="mt-3 sm:mt-0 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold border shadow-xs hover:bg-gray-100 sm:ml-3 sm:w-auto"
                  onclick={() => (modals.open(BigPicturesModal, { instrument, index, isInstrument, isAlternative }), close())}
                >
                  {$_('modals.add_picture.button.cancel')}
                </button>
              </div>
            </form>
          </div>
      </div>
  </div>
</div>
{/if}