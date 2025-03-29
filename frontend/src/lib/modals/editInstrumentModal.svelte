<script>
    import { PUBLIC_API_URL } from "$env/static/public";
    import { onMount, getContext } from "svelte";
    import { reload } from "$lib/stores/searches";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
  
    // Destructure the props provided by <Modals />
    const {
      isOpen, // Indicates if the modal is open
      close,  // Function to close the modal
      instrument, // The instrument data passed as a prop
    } = $props();
  
    let file; // Variable to store the selected file


    let reference = $state(instrument.reference); // State for the instrument reference
    let characteristics = $state([]); // State for the instrument characteristics
  
    // Function to handle form submission
    async function handleSubmit(event) {
      event.preventDefault(); // Prevent the default form submission behavior
  
      // If a file is selected, upload it
      if (file) {
        try {
          const fileData = new FormData();
          fileData.append("file", file);
          const response = await apiFetch("/api/instrument/" + encodeURIComponent(instrument.id) + "/picture",
            {
              method: "POST",
              body: fileData,
            }
          );
          if (!response.ok) {
            throw new Error("Failed to update the image");
          }
        } catch (error) {
          console.error("Error:", error);
        }
      }
  
      // If characteristics are edited, update them
      if (characteristicsEdited) {
        try {
          const filteredCharacteristics = characteristics.filter(c => c.name !== 'id');
          const response = await apiFetch("/api/instrument/" + encodeURIComponent(instrument.id),
            {
              method: "PATCH",
              headers: {
                "Content-Type": "application/json",
              },
              body: JSON.stringify(
            Object.fromEntries(characteristics.map(c => [c.name, c.value])) // Convert array back to object
          ),
            }
          );
          console.log(response);
          if (!response.ok) {
            throw new Error("Failed to update the instrument characteristics");
          }
        } catch (error) {
          console.error("Error:", error);
        }
      }
      reload.set(true); // Trigger a reload
      close(); // Close the modal
      reload.set(true);
      goto("../../searches"); // Navigate to the searches page
    }
  
    // Function to fetch the characteristics of the instrument
    async function fetchCharacteristics() {
      try {
        const response = await apiFetch("/api/instrument/" + encodeURIComponent(instrument.id)
        );
        if (!response.ok) {
          throw new Error(`Failed to fetch characteristics: ${response.statusText}`);
        }

        const data = await response.json();

        // Convert object to an array of { name, value }
        characteristics = Object.entries(data).map(([key, value]) => ({
          name: key,
          value: value
        }));

        console.log(characteristics);
      } catch (error) {
        console.error(error);
      }
    }

    let characteristicsEdited = false;
    let promise = fetchCharacteristics();

    // Function to handle instrument deletion
    async function handleDelete() {
        if (confirm("Êtes-vous sûr de vouloir supprimer cet instrument ?")) {
            try {
                const response = await apiFetch("/api/instrument/" + encodeURIComponent(instrument.id), {
                    method: "DELETE",
                });
                if (!response.ok) {
                    throw new Error("Failed to delete the instrument");
                }
                reload.set(true); // Trigger a reload
                close(); // Close the modal
                goto("../../searches"); // Navigate to the searches page
            } catch (error) {
                console.error("Error:", error);
            }
        }
    }

    let modalX = $state(200);
    let modalY = $state(100);

    let dragging = false;
    let offsetX, offsetY;

    function startDrag(event) {
        dragging = true;
        offsetX = event.clientX - modalX;
        offsetY = event.clientY - modalY;

        window.addEventListener('mousemove', drag);
        window.addEventListener('mouseup', stopDrag);
    }

    function drag(event) {
        if (dragging) {
            modalX = event.clientX - offsetX;
            modalY = event.clientY - offsetY;
        }
    }

    function stopDrag() {
        dragging = false;
        window.removeEventListener('mousemove', drag);
        window.removeEventListener('mouseup', stopDrag);
    }

    // New state for autocomplete
    let autocompleteOptions = $state({});
    let currentAutocompleteField = $state(null);
    let autocompleteInput = $state("");
    let filteredAutocompleteOptions = $state([]);
    let showAutocompleteDropdown = $state(false);

    async function fetchCharacteristicOptions(characteristicName) {
      try {
        // Map of characteristics to their respective API endpoints
        const characteristicEndpoints = {
          'supplier': {
            endpoint: 'supplier',
            extractValue: (item) => item.name,
          },
          'reference': {
            endpoint: 'instrument',
            extractValue: (item) => item.reference,
          },
          'supplierDescription': {
            endpoint: 'instrument',
            extractValue: (item) => item.supplierDescription,
          },
          // Add more mappings as needed
        };

        const config = characteristicEndpoints[characteristicName];
        console.log(`Config for ${characteristicName}:`, config);

        console.log(`Fetching options for ${characteristicName} from endpoint: ${config?.endpoint}`);
        
        if (!config) {
          console.warn(`No options endpoint found for ${characteristicName}`);
          return [];
        }

        const response = await apiFetch(`/api/${config.endpoint}/all`, {
          method: "GET",
          headers: {
            "Content-Type": "application/json",
          },
        });

        if (!response.ok) {
          throw new Error(`Failed to fetch options for ${characteristicName}`);
        }

        const options = await response.json();

        // Extract values using the provided function
        const extractedOptions = options.map(config.extractValue);
        console.log(`Fetched options for ${characteristicName}:`, extractedOptions);
        
        // Store options for this characteristic
        autocompleteOptions[characteristicName] = extractedOptions;
        
        return extractedOptions;
      } catch (error) {
        console.error(`Error fetching options for ${characteristicName}:`, error);
        return [];
      }
    }

    // Handle input for autocomplete
    function handleAutocompleteInput(event) {
      const inputValue = event.target.value;
      autocompleteInput = inputValue;
      showAutocompleteDropdown = true;

      // Ensure we're working with plain array, not a proxy
      const rawOptions = JSON.parse(JSON.stringify(
            autocompleteOptions[currentAutocompleteField] || []
        ));

      console.log(`Raw options for ${currentAutocompleteField}:`, rawOptions);
      console.log(`Input value:`, inputValue);

      // Filter options based on input
      filteredAutocompleteOptions = rawOptions.filter(option =>
            option.toLowerCase().includes(inputValue.toLowerCase())
        );

      console.log(`Filtered options in handleAutocompleteInput:`, filteredAutocompleteOptions);

      // if (currentAutocompleteField && autocompleteOptions[currentAutocompleteField]) {
      //   filteredAutocompleteOptions = autocompleteOptions[currentAutocompleteField].filter(option =>
      //     option.toLowerCase().includes(inputValue.toLowerCase())
      //   );
      // }
    }

    // Select an option from autocomplete
    function selectAutocompleteOption(option) {
      // Find the characteristic and update its value
      const characteristicIndex = characteristics.findIndex(
        c => c.name.toLowerCase() === currentAutocompleteField.toLowerCase()
      );

      if (characteristicIndex !== -1) {
        characteristics[characteristicIndex].value = option;
        characteristicsEdited = true;
      }

      // Reset autocomplete states
      autocompleteInput = option;
      showAutocompleteDropdown = false;
      currentAutocompleteField = null;
    }

    function closeAutocomplete() {
      console.log("Closing autocomplete dropdown");
      showAutocompleteDropdown = false;
      currentAutocompleteField = null;
    }


    // Trigger autocomplete for a specific field
    async function triggerAutocomplete(characteristicName) {
      if (currentAutocompleteField === characteristicName) {
        // If the same field is clicked again, toggle the dropdown
        showAutocompleteDropdown = !showAutocompleteDropdown;
        return;
      }
      
      // If a different field is clicked, fetch options for the new field
      currentAutocompleteField = characteristicName;
      
      // Fetch options if not already loaded
      if (!autocompleteOptions[characteristicName]) {
        await fetchCharacteristicOptions(characteristicName);
        console.log(`Fetched options in triggerAutocomplete ${characteristicName}:`, autocompleteOptions[characteristicName]);
      }

      // Reset and show dropdown
      autocompleteInput = "";
      filteredAutocompleteOptions = autocompleteOptions[characteristicName] || [];

      showAutocompleteDropdown = true;
    }
</script>

<style>
    .modal-header {
        background-color: #1f2937; /* Dark gray */
        color: white;
        padding: 1rem;
        font-size: 1.25rem;
        font-weight: bold;
        border-top-left-radius: 0.5rem;
        border-top-right-radius: 0.5rem;
    }

    .modal-body {
        padding: 1.5rem;
        background-color: #f9fafb; /* Light gray */
    }

    .modal-footer {
        display: flex;
        justify-content: flex-end;
        gap: 0.5rem;
        padding: 1rem;
        background-color: #f3f4f6; /* Slightly darker gray */
        border-bottom-left-radius: 0.5rem;
        border-bottom-right-radius: 0.5rem;
    }

    .btn-primary {
        background-color: #2563eb; /* Blue */
        color: white;
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        font-weight: bold;
        border: none;
        cursor: pointer;
    }

    .btn-primary:hover {
        background-color: #1d4ed8; /* Darker blue */
    }

    .btn-secondary {
        background-color: white;
        color: #374151; /* Gray */
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        font-weight: bold;
        border: 1px solid #d1d5db; /* Light gray border */
        cursor: pointer;
    }

    .btn-secondary:hover {
        background-color: #f3f4f6; /* Slightly darker gray */
    }

    .btn-danger {
        background-color: #dc2626; /* Red */
        color: white;
        padding: 0.5rem 1rem;
        border-radius: 0.375rem;
        font-weight: bold;
        border: none;
        cursor: pointer;
    }

    .btn-danger:hover {
        background-color: #b91c1c; /* Darker red */
    }
</style>

{#if isOpen}
  <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <!-- Transparent Background -->
    <div class="fixed inset-0 bg-gray-500/50 transition-opacity" aria-hidden="true"></div>

    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
      <div class="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
        <div
          class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:w-full sm:max-w-lg lg:max-w-4xl"
          style="top: {modalY}px; left: {modalX}px; position: absolute;"
        >
          <!-- Modal Header -->
          <div 
            class="modal-header cursor-move" 
            onmousedown={startDrag} 
            role="button" 
            tabindex="0"
          >
            Modifier l'instrument {reference}
          </div>

          <!-- Modal Body -->
          <div class="modal-body">
            <form class="max-w-4xl mx-auto" onsubmit={handleSubmit}>
              <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                <div class="sm:flex sm:items-start">
                  <div
                    class="mx-auto flex size-12 shrink-0 items-center justify-center rounded-full bg-orange-100 sm:mx-0 sm:size-10"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      class="w-5 h-5"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path d="M12 20h9" />
                      <path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L6 20l-4 1 1-4L16.5 3.5z" />
                    </svg>
                  </div>
                  <div class="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                    {#await promise}
                      <div role="status" class="my-6 flex items-center justify-center">
                        <svg
                          aria-hidden="true"
                          class="w-16 h-16 text-gray-200 animate-spin dark:text-gray-600 fill-blue-600"
                          viewBox="0 0 100 101"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
                            fill="currentColor"
                          />
                          <path
                            d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
                            fill="currentFill"
                          />
                        </svg>
                        <span class="sr-only">Chargement...</span>
                      </div>
                    {:then}
                        <div class="grid grid-cols-2 gap-4">
                          {#each characteristics as characteristic}
                            {#if characteristic.name !== 'id' && characteristic.name !== 'picturesId' && characteristic.name !== 'categoryId' && characteristic.name !== 'alt'} 
                              <div>
                                <label class="text-sm font-medium text-gray-900" for="user_avatar"
                                  >{characteristic.name}</label
                                >

                                <!-- Check if the characteristic is 'obsolete' -->
                                {#if characteristic.name === 'obsolete'}
                                  <div class="flex items-center gap-4 mt-1">
                                    <label class="flex items-center">
                                      <input
                                        type="radio"
                                        bind:group={characteristic.value}
                                        value={true}
                                        class="mr-2"
                                      />
                                      Oui
                                    </label>
                                    <label class="flex items-center">
                                      <input
                                        type="radio"
                                        bind:group={characteristic.value}
                                        value={false}
                                        class="mr-2"
                                      />
                                      Non
                                    </label>
                                  </div>
                                {:else if characteristic.name === 'price'}
                                  <input
                                    type="number"
                                    bind:value={characteristic.value}
                                    min="0"
                                    step="0.01"
                                    onchange={() => {
                                      (characteristicsEdited = true)
                                      if(characteristic.value < 0) {
                                        characteristic.value = 0;
                                      }
                                    }}
                                    oninput={() => {
                                      (characteristicsEdited = true)
                                      if(characteristic.value < 0) {
                                        characteristic.value = 0;
                                      }
                                    }}
                                    class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
                                  />
                                {:else}
                                  <div class="relative">
                                    <input
                                      type="text"
                                      bind:value={characteristic.value}
                                      onchange={() => (characteristicsEdited = true)}
                                      onfocus={() => triggerAutocomplete(characteristic.name)}
                                      oninput={handleAutocompleteInput}
                                      onblur={() => closeAutocomplete()}
                                      class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
                                    />
                                    {#if showAutocompleteDropdown && currentAutocompleteField === characteristic.name}
                                      <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                      <ul 
                                        class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                        onmousedown={event => event.preventDefault()}
                                      >
                                        {#each filteredAutocompleteOptions as option}
                                        <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                        <button
                                          type="button"
                                          class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                          role="option"
                                          onclick={() => selectAutocompleteOption(option)}
                                        >
                                          {option}
                                        </button>
                                        {/each}
                                        </ul>
                                    {/if}
                                  </div>
                                {/if}
                              </div>
                            {/if}
                          {/each}
                        </div>
                      <div class="mt-2">
                        <label class="block my-2 text-sm font-medium text-gray-900" for="user_avatar"
                          >Image</label
                        >
                        <input
                          class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5"
                          type="file"
                          onchange={(e) => (file = e.target.files[0])}
                        />
                        {#if !instrument.pictureId}
                          <div class="mt-1 text-sm text-red-500">
                            Une image existe déjà pour cet instrument, en indiquant une image ci-dessus, l'image actuelle sera supprimée.
                          </div>
                        {/if}
                      </div>
                    {/await}
                  </div>
                </div>
              </div>
            </form>
          </div>

          <!-- Modal Footer -->
          <div class="modal-footer">
            <button
              type="submit"
              class="btn-primary"
            >
              Enregistrer
            </button>
            <button
              type="button"
              class="btn-secondary"
              onclick={() => close()}
            >
              Annuler
            </button>
            <button
              type="button"
              class="btn-danger"
              onclick={handleDelete}
            >
              Supprimer
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
{/if}