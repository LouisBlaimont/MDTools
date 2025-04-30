<script>
    import { onMount } from "svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { reload } from "$lib/stores/searches";
    import { goto } from "$app/navigation";
    import { _ } from "svelte-i18n";
  
    const {
      isOpen, // Indicates if the modal is open
      close,  // Function to close the modal
    } = $props();
  
    let name = $state("");
    let soldByMd = $state(true);
    let closed = $state(false);

    let inputSize;
  
    let posX = $state(0);
    let posY = $state(0);
    let offsetX = 0;
    let offsetY = 0;
    let isDragging = false;
  
    let autocompleteOptions = $state({});
    let autocompleteInput = $state("");
    let filteredAutocompleteOptions = $state([]);
    let showAutocompleteDropdown = $state(false);
    let currentAutocompleteField = $state(null);
  
    async function fetchAutocompleteOptions(characteristic) {
      try {
        const charateritsicEndpoints = {
          'name': {
              endpoint: `supplier`,
              extractValue: (item) => item.name,
          }
        }
        const config = charateritsicEndpoints[characteristic];
        if (!config) {
              console.warn(`No endpoint configured for ${characteristic}`);
              return;
        }
  
        const response = await apiFetch(`/api/${config.endpoint}/all`, {
          method: "GET",
          headers: { "Content-Type": "application/json" },
        });
        if (!response.ok) throw new Error(`Failed to fetch options}`);
  
        const options = await response.json();
  
        // Extract values using the provided function
        const extractedOptions = options.map(config.extractValue);
  
        autocompleteOptions[characteristic] = extractedOptions;
        return extractedOptions;
      } catch (error) {
        console.error(error);
      }
    }
  
    async function triggerAutocomplete(characteristicName) {
      if (currentAutocompleteField === characteristicName && showAutocompleteDropdown) {
        // If the same field is clicked again while dropdown is open, close it
        showAutocompleteDropdown = false;
        currentCategory = null;
        currentAutocompleteField = null;
        return;
      }
      // If a different field is clicked, fetch options for the new field
      currentAutocompleteField = characteristicName;
      
      // Fetch options if not already loaded
      if (!autocompleteOptions[characteristicName]) {
        await fetchAutocompleteOptions(characteristicName);
      }
  
      autocompleteInput = "";
      filteredAutocompleteOptions = (autocompleteOptions[characteristicName] || []);
  
      showAutocompleteDropdown = true;
  }
  
    function handleAutocompleteInput(event) {
      const inputValue = event.target.value;
      if (currentAutocompleteField !== "name") {
        console.warn(`No autocomplete field selected`);
        return;
      }
      name = inputValue;
      autocompleteInput = inputValue;
      showAutocompleteDropdown = true;

      const rawOptions = autocompleteOptions[currentAutocompleteField] || [];
      filteredAutocompleteOptions = rawOptions.filter(option =>
            String(option).toLowerCase().includes(inputValue.toLowerCase())
        );
    }
  
    function selectAutocompleteOption(option) {
      name = option;
      autocompleteInput = "";
      showAutocompleteDropdown = false;
      currentAutocompleteField = null;
    }
  
    function closeAutocomplete() {
      showAutocompleteDropdown = false;
      currentAutocompleteField = null;
    }
  
    async function handleSubmit(event) {
      event.preventDefault();
        try {
            if (!name) {
                throw new Error("Name is required");
            }
            if (soldByMd === null) {
                throw new Error("SoldByMd is required");
            }
            if (closed === null) {
                throw new Error("Closed is required");
            }
            
            const response = await apiFetch(`/api/supplier`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    name: name,
                    soldByMd,
                    closed
                }),
            });
            reload.set(true);
            close();
            setTimeout(() => {
              reload.set(true);
              goto("/admin/supplier", {
                replaceState: true,
              });
            }, 100);
        } catch (error) {
            console.error(error);
        }
    }
  
    async function erase() {
      name = "";
      soldByMd = null;
      closed = null;
    }
  
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
  
    let promise = fetchAutocompleteOptions("name");
  </script>
  
  {#if isOpen}
    <div
        class="relative z-10"
        aria-labelledby="modal-title"
        role="dialog"
        aria-modal="true"
    >
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore event_directive_deprecated -->
    <div
      class="fixed inset-0 z-10 flex items-center justify-center bg-gray-500 bg-opacity-50"
      on:mousemove={drag}
      on:mouseup={stopDrag}
    >
      <div
        class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
        style="transform: translate({posX}px, {posY}px);"
      >
        <div
          class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg"
          on:mousedown={startDrag}
        >
          <h2 class="text-2xl text-teal-500 font-bold select-none">{$_('modals.add_supplier.add_supp')}</h2>        
          <svg
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            class="w-6 h-6 text-teal-500"
          >
            <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
        </div> 
        <form on:submit|preventDefault={handleSubmit} class="bg-gray-100 p-6 rounded-b-lg">
            <label for="name" class="font-semibold text-lg">{$_('modals.add_supplier.name')}
            </label>
              <input
                type="text"
                bind:value={name}
                on:focus={() => {
                    currentAutocompleteField = "name";
                    triggerAutocomplete("name");
                }}
                on:input={handleAutocompleteInput}
                on:blur={closeAutocomplete}
                bind:this={inputSize}
                class="w-full p-2 mt-1 border rounded mb-4"
                placeholder={$_('modals.add_supplier.enter_supp')}
              />
              {#if showAutocompleteDropdown && currentAutocompleteField === "name"}
              <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
              <ul
                  class="absolute z-10 mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto"
                  style="width: {inputSize?.offsetWidth || 'auto'}px;"
                  on:mousedown={event => event.preventDefault()}
              >
                  {#each filteredAutocompleteOptions as option}
                  <!-- svelte-ignore a11y_click_events_have_key_events -->
                  <li
                      class="px-4 py-2 hover:bg-gray-200 cursor-pointer"
                      on:click={() => selectAutocompleteOption(option)}
                  >
                      {option}
                  </li>
                  {/each}
              </ul>
              {/if}

            <label for="soldByMd" class="font-semibold text-lg">{$_('modals.add_supplier.sold')}</label>
            <div class="flex gap-4 mb-4">
              <label>
                <input
                  type="radio"
                  bind:group={soldByMd}
                  value={true}
                />
                {$_('modals.add_supplier.yes')}

              </label>
              <label>
                <input
                  type="radio"
                  bind:group={soldByMd}
                  value={false}
                />
                {$_('modals.add_supplier.no')}
              </label>
            </div>

            <label for="closed" class="font-semibold text-lg">{$_('modals.add_supplier.status')}</label>
            <div class="flex gap-4 mb-4">
              <label>
                <input
                  type="radio"
                  bind:group={closed}
                  value={true}
                />
                {$_('modals.add_supplier.close')}
              </label>
              <label>
                <input
                  type="radio"
                  bind:group={closed}
                  value={false}
                />
                {$_('modals.add_supplier.open')}

              </label>
            </div>
          
          <div class="flex justify-end gap-4 mt-4">
            <button type="button" on:click={erase} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">{$_('modals.add_supplier.erase')}</button>
            <button type="button" on:click={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">{$_('modals.add_supplier.cancel')}
            </button>
            <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-teal-700">{$_('modals.add_supplier.save')}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
  {/if}
  