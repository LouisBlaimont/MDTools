<script>
  import { PUBLIC_API_URL } from "$env/static/public";
  import { onMount, getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import AddPictureModal from "./AddPictureModal.svelte";
  import { apiFetch } from "$lib/utils/fetch";
  import { currentSuppliers, categories } from "$lib/stores/searches";
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
    isInstrument, // to use the modal with instruments and categories
    isAlternative, 
  } = $props();

  let instrument_reactive = $state(instrument);

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
   * Handle the deleting of a picture
   * @param id id of the picture
   * @param index position of the picture in the array of pictures
   */
  async function deletePicture(id, index) {
    try {
      const response = await apiFetch("/api/pictures/" + encodeURIComponent(id), {
        method: "DELETE",
      });
      if (!response.ok) {
        throw new Error($_('modals.big_picture.deletion_error') + response.statusText);
      }
      // remove from array
      if (isInstrument) {
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
      }
      else {
        instrument_reactive.picturesId.splice(index, 1);
        // remove from the list of categories
        categories.update((cats) => {
          cats.forEach((cat) => {
            if (cat.id === instrument.id) {
              cat.picturesId.splice(index, 1);
            }
          });
          return cats;
        });
      }

    } catch (error) {
      console.error("Erreur:", error);
    }
  }

  let deleteTimeouts = {};

  /**
   * Handling the toasts for deleting a picture
   * @param id id of the picture to delete
   * @param index position of the picture in the array of pictures
   */
  function handleDelete(id, index) {
    if (deleteTimeouts[id]) {
      // Second click: Delete the image
      clearTimeout(deleteTimeouts[id]);
      delete deleteTimeouts[id];
      deletePicture(id, index);
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

  function reloadPage() {
    console.log("finished adding pictures");
  }

    // ---- FULLSCREEN IMAGE VIEWER ----
  let viewerOpen = $state(false);
  let viewerSrc = $state("");
  let viewerAlt = $state("");

  let zoom = $state(1); // 1 = fit
  let vPosX = $state(0);
  let vPosY = $state(0);
  let vDragging = false;
  let vOffsetX = 0;
  let vOffsetY = 0;

  function openViewer(id, index) {
    viewerSrc = PUBLIC_API_URL + "/api/pictures/" + encodeURIComponent(id);
    viewerAlt = `picture ${index}`;
    viewerOpen = true;

    // reset transform
    zoom = 1;
    vPosX = 0;
    vPosY = 0;
  }

  function closeViewer() {
    viewerOpen = false;
  }

  function zoomIn() {
    zoom = Math.min(zoom + 0.25, 5);
  }

  function zoomOut() {
    zoom = Math.max(zoom - 0.25, 1);
    if (zoom === 1) {
      vPosX = 0;
      vPosY = 0;
    }
  }

  function onWheelZoom(e) {
    e.preventDefault();
    if (e.deltaY < 0) zoomIn();
    else zoomOut();
  }

  function startViewerDrag(e) {
    // only drag when zoomed
    if (zoom <= 1) return;
    vDragging = true;
    vOffsetX = e.clientX - vPosX;
    vOffsetY = e.clientY - vPosY;
  }

  function viewerDrag(e) {
    if (!vDragging) return;
    vPosX = e.clientX - vOffsetX;
    vPosY = e.clientY - vOffsetY;
  }

  function stopViewerDrag() {
    vDragging = false;
  }

  function onKeyDown(e) {
    if (!viewerOpen) return;
    if (e.key === "Escape") closeViewer();
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
      <!-- DRAGGABLE MODAL (transformed) -->
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
              {$_('modals.big_picture.titleInstrument')} {instrument.reference}
            {:else}
              {$_('modals.big_picture.titleCategory')}
            {/if}
          </h2>
          <Icon icon="material-symbols:photo-rounded" width="24" height="24" class="text-teal-500" />
        </div>

        <div class="p-6 bg-gray-100">
          {#if isInstrument}
            {#if instrument_reactive.picturesId.length == 0}
              <div class="text-center w-full my-8">
                <p>{$_('modals.big_picture.no_picturesInstrument')}</p>
              </div>
            {/if}
          {:else}
            {#if instrument_reactive.picturesId.length == 0}
              <div class="text-center w-full my-8">
                <p>{$_('modals.big_picture.no_picturesCategory')}</p>
              </div>
            {/if}
          {/if}

          <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
            {#each instrument_reactive.picturesId as id, picIndex}
              <div class="relative">
                <!-- svelte-ignore a11y_consider_explicit_label -->
                {#if $isAdmin}
                  <button
                    class="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full shadow-md hover:bg-red-600"
                    onclick={() => handleDelete(id, picIndex)}
                  >
                    <Icon icon="material-symbols:delete-forever" width="16" height="16" />
                  </button>
                {/if}

                <!-- svelte-ignore a11y_click_events_have_key_events -->
                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                <img
                  class="h-auto max-w-full rounded-lg cursor-zoom-in"
                  src={PUBLIC_API_URL + "/api/pictures/" + encodeURIComponent(id)}
                  alt={"picture " + picIndex}
                  onclick={() => openViewer(id, picIndex)}
                />
              </div>
            {/each}
          </div>

          <div class="mt-6 sm:flex sm:flex-row-reverse">
            <button
              class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto"
              onclick={() => close()}
            >
              {$_('modals.big_picture.close')}
            </button>

            <button
              class="mt-3 sm:mt-0 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold border shadow-xs hover:bg-gray-100 sm:ml-3 sm:w-auto"
              onclick={() => (modals.open(AddPictureModal, { instrument, index, isInstrument, isAlternative }), close())}
            >
              {$_('modals.big_picture.add_picture')}
            </button>
          </div>
        </div>
      </div>

      <!-- FULLSCREEN VIEWER (SIBLING of transformed modal => truly fullscreen) -->
      {#if viewerOpen}
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <!-- svelte-ignore a11y_no_noninteractive_tabindex -->
        <div
          class="fixed inset-0 z-[9999] bg-black/80"
          tabindex="0"
          onkeydown={onKeyDown}
          onclick={() => closeViewer()}
          onmouseup={stopViewerDrag}
        >
          <!-- Viewer stage -->
          <!-- svelte-ignore a11y_no_static_element_interactions -->
          <!-- svelte-ignore a11y_click_events_have_key_events -->
          <div
            class="absolute inset-0 flex items-center justify-center overflow-hidden"
            onclick={(e) => e.stopPropagation()}
            onmousemove={viewerDrag}
            onmouseup={stopViewerDrag}
            onmouseleave={stopViewerDrag}
            onwheel={onWheelZoom}
          >
            <!-- Everything inside this wrapper follows the same transform (image + buttons) -->
            <div
              class="relative"
              style="transform: translate({vPosX}px, {vPosY}px) scale({zoom}); transform-origin: center;"
            >
              <div class="absolute top-2 right-2 flex gap-2 z-10">
                <button
                  class="bg-white/90 px-3 py-1 rounded shadow hover:bg-white"
                  type="button"
                  onclick={(e) => { e.stopPropagation(); zoomOut(); }}
                >
                  -
                </button>

                <button
                  class="bg-white/90 px-3 py-1 rounded shadow hover:bg-white"
                  type="button"
                  onclick={(e) => { e.stopPropagation(); zoomIn(); }}
                >
                  +
                </button>

                <button
                  class="bg-white/90 px-3 py-1 rounded shadow hover:bg-white"
                  type="button"
                  onclick={(e) => { e.stopPropagation(); closeViewer(); }}
                >
                  ✕
                </button>
              </div>

              <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
              <img
                src={viewerSrc}
                alt={viewerAlt}
                class="block select-none cursor-grab active:cursor-grabbing max-w-[95vw] max-h-[95vh]"
                draggable="false"
                onmousedown={(e) => { e.stopPropagation(); startViewerDrag(e); }}
              />
            </div>
          </div>
        </div>
      {/if}
    </div>
  </div>
{/if}