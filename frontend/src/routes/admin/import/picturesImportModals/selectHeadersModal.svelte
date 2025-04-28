<script>
  import { getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import { selectedGroup } from "$lib/stores/searches";
  import { userId } from "$lib/stores/user_stores";
  import { apiFetch } from "$lib/utils/fetch";
  import { _ } from "svelte-i18n";
  import { createEventDispatcher } from "svelte";

  export let isOpen = false;
  export let close;

  export let headers = [];
  let referenceColumnName = "";
  let mappingColumnName = "";

  // If present, set default values
  if(headers.find((header) => header === "REFERENCE")) {
    referenceColumnName = "REFERENCE";
  }
  if(headers.find((header) => header === "MAPPING")) {
    mappingColumnName = "MAPPING";
  }

  const dispatch = createEventDispatcher();

  function erase() {
    referenceColumnName = "";
    mappingColumnName = "";
  }

  function cancel() {
    dispatch("cancel", { message: $_("modals.add_group.operation") });
    close();
  }

  let posX = 0,
    posY = 0,
    offsetX = 0,
    offsetY = 0,
    isDragging = false;

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

  async function submitForm(event) {
   close([referenceColumnName, mappingColumnName]);
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
          class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg select-none"
          style="cursor: move;"
          onmousedown={startDrag}
        >
          <h2 class="text-2xl font-bold text-teal-500 text-center">
            Select the columns for the importation of pictures
          </h2>
        </div>
        <form onsubmit={submitForm} class="bg-gray-100 p-6 rounded-lg">
          <div class="flex flex-col mb-4 gap-4">
            <label for="group_name" class="font-semibold text-lg">References column</label>
            <select
              id="referenceColumnName"
              bind:value={referenceColumnName}
              class="w-3/5 p-1 py-1.5 bg-transparent text-slate-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
            >
              <option value="none">---</option>
              {#each headers as header}
                <option value={header}>{header}</option>
              {/each}
            </select>

            <label for="group_name" class="font-semibold text-lg">Mapping column</label>
            <select
              id="mappingColumnName"
              bind:value={mappingColumnName}
              class="w-3/5 p-1 py-1.5 bg-transparent text-slate-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
            >
              <option value="none">---</option>
              {#each headers as header}
                <option value={header}>{header}</option>
              {/each}
            </select>
          </div>

          <div class="flex justify-end gap-4 mt-4">
            <button type="button" onclick={erase} class="px-4 py-2 bg-red-500 text-white rounded"
              >{$_("modals.add_group.erase")}</button
            >
            <button type="button" onclick={cancel} class="px-4 py-2 bg-gray-500 text-white rounded"
              >{$_("modals.add_group.cancel")}</button
            >
            <button type="submit" class="bg-teal-500 text-white px-4 py-2 rounded"
              >{$_("modals.add_group.save")}</button
            >
          </div>
        </form>
      </div>
    </div>
  </div>
{/if}
