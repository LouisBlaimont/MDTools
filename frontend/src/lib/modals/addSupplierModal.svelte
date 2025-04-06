<script>
    import { onMount } from "svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { reload } from "$lib/stores/searches";
    import { goto } from "$app/navigation";
  
    const {
      isOpen, // Indicates if the modal is open
      close,  // Function to close the modal
    } = $props();
  
    let file;
    let name = $state("");
    let soldByMd = $state(null);
    let closed = $state(null);
  
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
            if (file) {
                const fileData = new FormData();
                fileData.append("file", file);
                const response = await apiFetch(`/api/supplier/${encodeURIComponent(supplier.id)}/picture`, {
                    method: "POST",
                    body: fileData,
                });
                if (!response.ok) throw new Error("Failed to upload the image");    
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
        } catch (error) {
            console.error(error);
        }

      close();
      reload.set(true);
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
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500 bg-opacity-10 transition-opacity" aria-hidden="true"></div>
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <!-- svelte-ignore event_directive_deprecated -->
    <div
      class="fixed inset-0 z-10 flex items-center justify-center bg-gray-500 bg-opacity-50"
      on:mousemove={drag}
      on:mouseup={stopDrag}
    >
      <div
        class="bg-white rounded-lg shadow-lg w-1/2 absolute"
        style="transform: translate({posX}px, {posY}px);"
      >
        <div
          class="p-4 border-b cursor-move bg-black text-white flex items-center justify-between"
          on:mousedown={startDrag}
        >
          <h2 class="text-xl font-bold">Ajouter un fournisseur</h2>        
        </div> 
        <form on:submit|preventDefault={handleSubmit} class="p-4">
            <label class="block mb-2">Nom:</label>
            <div class="relative mb-4">
              <input
                type="text"
                bind:value={name}
                on:focus={() => {
                    currentAutocompleteField = "name";
                    triggerAutocomplete("name");
                }}
                on:input={handleAutocompleteInput}
                on:blur={closeAutocomplete}
                class="w-full p-2 border rounded mb-4"
              />
              {#if showAutocompleteDropdown && currentAutocompleteField === "name"}
              <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
              <ul
                  class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                  on:mousedown={event => event.preventDefault()}
              >
                  {#each filteredAutocompleteOptions as option}
                  <li
                      class="px-4 py-2 hover:bg-gray-200 cursor-pointer"
                      on:click={() => selectAutocompleteOption(option)}
                  >
                      {option}
                  </li>
                  {/each}
              </ul>
              {/if}
            </div>

            <label class="block mb-2">En vente:</label>
            <div class="flex gap-4 mb-4">
              <label>
                <input
                  type="radio"
                  bind:group={soldByMd}
                  value={true}
                />
                Oui
              </label>
              <label>
                <input
                  type="radio"
                  bind:group={soldByMd}
                  value={false}
                />
                Non
              </label>
            </div>

            <label class="block mb-2">Statut:</label>
            <div class="flex gap-4 mb-4">
              <label>
                <input
                  type="radio"
                  bind:group={closed}
                  value={true}
                />
                Fermé
              </label>
              <label>
                <input
                  type="radio"
                  bind:group={closed}
                  value={false}
                />
                Ouvert
              </label>
            </div>
          
          <label class="block mb-2">Image:</label>
          <input
            type="file"
            class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5 mb-4"
            on:change={(e) => (file = e.target.files[0])}
          />
          <div class="flex justify-end gap-4 mt-4">
            <button type="button" on:click={erase} class="bg-red-500 text-white px-4 py-2 rounded">Effacer</button>
            <button type="button" on:click={close} class="bg-gray-500 text-white px-4 py-2 rounded">Annuler</button>
            <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded">Enregistrer</button>
          </div>
        </form>
      </div>
    </div>
  </div>
  {/if}
  