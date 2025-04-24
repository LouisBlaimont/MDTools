<script>
  import { onMount } from "svelte";
  import { addCharacteristicToSubGroup, fetchCharacteristics, fetchAllCharacteristics, updateCharacteristicOrder , removeCharacteristicFromSubGroup} from "../../api.js";
  import { _ } from "svelte-i18n";
  import { dndzone } from "svelte-dnd-action";

  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();

  const { isOpen, selectedSubGroup, onClose } = $props();

  // Dragging functionality
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

  let newCharacteristicName = $state("");
  let allCharacteristics = [];
  let requiredColumns = [];
  let filteredSuggestions = $state([]);

  let characteristicsInShape = $state([]);
  let characteristicsOutOfShape = $state([]);

  $effect(() => {
    if (newCharacteristicName.trim() !== "") {
      const input = newCharacteristicName.trim().toLowerCase();
      filteredSuggestions = allCharacteristics
        .filter(c => c.toLowerCase().includes(input))
        .filter(c => !requiredColumns.includes(c))
        .filter(c => !["name", "function", "length"].includes(c.toLowerCase()));
    }
  });

  /**
   * Create and add a new characteristic to the selected subgroup.
   * Dispatches an `added` event on success.
   * Clears the input field after creation.
   * Does nothing if the input is empty.
   * @returns {Promise<void>}
   */
  async function createNewCharacteristic() {
    const trimmedName = newCharacteristicName.trim();
    if (!trimmedName) return;

    try {
      await addCharacteristicToSubGroup(selectedSubGroup, trimmedName);
      dispatch('added', { name: trimmedName });
      newCharacteristicName = "";
      await loadCharacteristics(selectedSubGroup);
    } catch (error) {
      console.error("Error adding characteristic:", error);
    }
  }

  /**
   * Loads all characteristics for the given subgroup and separates them
   * into those with and without defined order positions.
   * Also updates the list of all known characteristic names and required columns.
   * @param {string} subGroup - The name of the selected subgroup.
   * @returns {Promise<void>}
   */
  async function loadCharacteristics(subGroup) {
    const characteristics = await fetchCharacteristics(subGroup);
    allCharacteristics = await fetchAllCharacteristics();

    requiredColumns = characteristics.map(c => c.name);

    characteristicsInShape = characteristics
      .filter(c => c.orderPosition !== null)
      .sort((a, b) => a.orderPosition - b.orderPosition)
      .map((c, i) => ({ id: `${c.name}-${i}`, name: c.name, orderPosition: c.orderPosition }));

    characteristicsOutOfShape = characteristics
      .filter(c => c.orderPosition === null)
      .filter(c => !["name", "function", "length"].includes(c.name.toLowerCase()))
      .map((c, i) => ({ id: `${c.name}-out-${i}`, name: c.name, orderPosition: null }));
  }

  $effect(() => {
    if (isOpen && selectedSubGroup) {
      loadCharacteristics(selectedSubGroup).catch(console.error);
    }
  });

  /**
   * Deletes a characteristic from the selected subgroup, regardless of whether it's in the form.
   * @param {string} name - The name of the characteristic to delete.
   * @returns {Promise<void>}
   */
    async function deleteCharacteristic(name) {
    try {
      await removeCharacteristicFromSubGroup(selectedSubGroup, name);
      dispatch('deleted', { name });
      await loadCharacteristics(selectedSubGroup);
    } catch (err) {
      console.error('Error deleting characteristic:', err);
    }
  }

  /**
   * Removes a characteristic from the shape (form) and updates the DB.
   * @param {{ name: string, id: string }} char
   */
  async function removeFromShape(char) {
    // Remove visually from the list
    characteristicsInShape = characteristicsInShape.filter(c => c.id !== char.id);
    characteristicsOutOfShape = [...characteristicsOutOfShape, {
      name: char.name,
      id: `${char.name}-out-${Math.random().toString(36).substring(2, 6)}`,
      orderPosition: null
    }];

    // Prepare new order payload (excluding the removed one)
    const payload = characteristicsInShape.map((item, idx) => ({
      name: item.name,
      order_position: idx + 1
    }));

    // Add the removed one with null
    payload.push({
      name: char.name,
      order_position: null
    });

    try {
      await updateCharacteristicOrder(selectedSubGroup, payload);
      dispatch('added');
    } catch (err) {
      console.error("Failed to update characteristic order:", err);
    }
  }

</script>
  
{#if isOpen}
  <div
        class="relative z-10"
        aria-labelledby="modal-title"
        role="dialog"
        aria-modal="true"
    >
    <div class="fixed inset-0 bg-black bg-opacity-30"></div>
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore event_directive_deprecated -->
    <div
            class="fixed inset-0 z-10 flex items-center justify-center"
            on:mousemove={drag}
            on:mouseup={stopDrag}
        >
    
  <div class="bg-white p-6 rounded-lg shadow-xl w-full max-w-lg absolute max-h-[80vh] overflow-y-auto"
       style="transform: translate({posX}px, {posY}px);"
  >
  <div class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg" 
       on:mousedown={startDrag}
  >
    <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.add_char.add_char')}</h2>
  </div>
  <div class="p-4">
    <input type="text" placeholder={$_('modals.add_char.name')} class="w-full p-2 border rounded mb-4" bind:value={newCharacteristicName} />

    {#if filteredSuggestions.length > 0}
      <ul class="mt-2 border rounded bg-white max-h-40 overflow-auto text-sm z-50">
        {#each filteredSuggestions as suggestion}
          <!-- svelte-ignore a11y_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
          <li class="p-2 hover:bg-gray-200 cursor-pointer" on:click={() => newCharacteristicName = suggestion}>{suggestion}</li>
        {/each}
      </ul>
    {:else if newCharacteristicName.trim() !== ""}
      <p class="text-xs text-gray-400 mt-2 italic">{$_('modals.add_char.no_suggestion')}</p>
    {/if}

    <button on:click={createNewCharacteristic} class="bg-blue-600 text-white px-4 py-2 rounded mt-4">{$_('modals.add_char.add_button')}</button>

    <hr class="my-4" />

    <h3 class="text-lg font-semibold mb-2">{$_('modals.add_char.form')}</h3>
    <div
      use:dndzone={{
        items: characteristicsInShape,
        flipDurationMs: 300,
        dropTargetStyle: { class: 'dnd-shadow' }
      }}
      on:consider={({ detail }) => characteristicsInShape = detail.items}
      on:finalize={async ({ detail }) => {
        characteristicsInShape = detail.items;
        dispatch('orderUpdated', characteristicsInShape);
        await updateCharacteristicOrder(
          selectedSubGroup,
          characteristicsInShape.map((item, idx) => ({
            name: item.name,
            order_position: idx + 1
          }))
        );
        dispatch('added');
      }}
      class="border rounded p-2 bg-gray-50"
    >
      {#each characteristicsInShape as char, index (char.id)}
        <div class="p-2 bg-white border mb-1 rounded cursor-move flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="text-gray-500 w-6 text-right">{index + 1}.</span>
            <span>{char.name}</span>
          </div>
          <button
            class="text-xs text-red-500 hover:text-red-700 ml-2"
            title="Remove from form"
            on:click={() => removeFromShape(char)}
          >
            ✖
          </button>
        </div>
      {/each}
    </div>

    <h4 class="mt-4 font-medium">{$_('modals.add_char.not_included')}</h4>
    <ul class="mt-2">
      {#each characteristicsOutOfShape as char (char.name)}
        <li class="flex justify-between items-center bg-gray-100 p-2 rounded mb-1">
          <span>{char.name}</span>
          <div class="flex gap-2">
            <button
              class="text-sm bg-blue-500 text-white px-2 py-1 rounded"
              on:click={async () => {
                const newList = [...characteristicsInShape, {
                  name: char.name,
                  id: `${char.name}-${Math.random().toString(36).substring(2, 6)}`
                }];

                characteristicsInShape = newList;
                characteristicsOutOfShape = characteristicsOutOfShape.filter(c => c.name !== char.name);

                // Appelle l'API pour mettre à jour l'ordre
                await updateCharacteristicOrder(
                  selectedSubGroup,
                  newList.map((item, idx) => ({
                    name: item.name,
                    order_position: idx + 1
                  }))
                );
                dispatch('added');
              }}
            >
              {$_('modals.add_char.add_to_form')}
            </button>
            <button
              class="w-8 h-8 rounded-full bg-red-500 text-white flex items-center justify-center text-xs hover:bg-red-600"
              on:click={() => deleteCharacteristic(char.name)}
              title="Supprimer la caractéristique"
            >
              ✖
            </button>
          </div>
        </li>
      {/each}
    </ul>  

    <div class="flex justify-end gap-4 mt-6">
      <button 
        class="bg-gray-500 text-white px-4 py-2 rounded"
        on:click={onClose}
      >
        Annuler
      </button>
    </div>
  </div>
</div>
  </div>
  </div>
{/if}
