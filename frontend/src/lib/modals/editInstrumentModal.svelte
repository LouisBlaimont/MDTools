<script>
  import { alternatives, reload, selectedGroup, selectedSubGroup} from "$lib/stores/searches";
  import { goto } from "$app/navigation";
  import { apiFetch } from "$lib/utils/fetch";
  import { addingAlt, altToAdd, removingAlt, altToRemove } from "$lib/stores/searches";
  import Icon from "@iconify/svelte";
  import Loading from "$lib/Loading.svelte";

  // Destructure the props provided by <Modals />
  const {
    isOpen, // Indicates if the modal is open
    close,  // Function to close the modal
    instrument, // The instrument data passed as a prop
  } = $props();

  let hoveredAlternativeIndex = $state(null);

  //get alternative when displaying modal
  let alternativesOfInstr = $state([]);
  $effect(() => {
    async function getAlternativesOfInstr(){
      if(isOpen && instrument?.id){
        try{
          const response = await apiFetch(`/api/alternatives/admin/instrument/${instrument.id}`);
          if(!response.ok){
            throw new Error(`Failed get alternatives of ${instrument.id}`);
          }
          const data = await response.json();
          alternativesOfInstr = data;
        }catch(error){
          console.log(error);
        }
      }
    }
    getAlternativesOfInstr();
  });

  let file; // Variable to store the selected file

  let reference = $state(instrument.reference); // State for the instrument reference
  let characteristics = $state([]); // State for the instrument characteristics

  // Function to handle form submission
  async function handleSubmit(event) {

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
        if (!response.ok) {
          throw new Error("Failed to update the instrument characteristics");
        }
      } catch (error) {
        console.error("Error:", error);
      }
    }
    
    if($addingAlt){
      addingAlt.set(false);
      for (const instr2 of $altToAdd){
        try {
          const response = await apiFetch(`/api/alternatives?instr1=${instrument.id}&instr2=${instr2.id}`, {
            method : "POST",
            credentials: "include"
          });
          if (!response.ok){
            throw new Error("Impossible to add alternative");
          }
          const data = await response.json();
          alternatives.set(data);
        }catch(error){
          console.log(error);
        }
      }
      altToAdd.set([]);
    }

    if($removingAlt){
      removingAlt.set(false);
      for (const instr2 of $altToRemove){
        try { 
          const response = await apiFetch(`/api/alternatives/${instr2}/instrument/${instrument.id}`,{
          method : "DELETE",
          });
          if(!response.ok){
              throw new Error(`Failed to remove alternative`);
          }
          const data = await response.json();
          alternatives.set(result);
        } catch(error) {
            console.log("Error :", error);
        }
      }
      altToRemove.set([]);
    }
    close(); // Close the modal
    goto(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=${encodeURIComponent(instrument.categoryId)}&instrument=${encodeURIComponent(instrument.id)}`);
    reload.set(true);
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

    } catch (error) {
      console.error(error);
    }
  }

  let characteristicsEdited = false;

  // Computed property for binding
  let characteristicBoundValue = $state(null);
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

  // Dragging functionality to match addModal
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

  // Autocomplete functionality
  let autocompleteOptions = $state({});
  let categorizedOptions = $state({});
  let currentAutocompleteField = $state(null);
  let currentCategory = $state(null);
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
        'categoryId': {
          endpoint: 'category',
          extractValue: (item) => item
        },
      };

      const config = characteristicEndpoints[characteristicName];
      
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
      
      // For categoryId, organize data into categorized options
      if (characteristicName === 'categoryId') {
        // Process the data to create categorized options
        categorizedOptions = {}; // Reset categorized options
        
        extractedOptions.forEach(item => {
          // Create categories based on groupName + subGroupName
          const categoryKey = `${item.groupName} - ${item.subGroupName}`;
        
          
          if (!categorizedOptions[categoryKey]) {
            categorizedOptions[categoryKey] = [];
          }
            
          // Add characteristics with their IDs
          let display = [];
          if (item.name) display.push(item.name);
          if (item.shape) display.push(item.shape);
          if (item.lenAbrv) display.push(item.lenAbrv);
          
          const displayText = display.join(' - ');
          
          categorizedOptions[categoryKey].push({
            id: item.id,
            display: displayText
          });
        });
        
        // Set the initial options to be the category names
        autocompleteOptions[characteristicName] = Object.keys(categorizedOptions);
        return Object.keys(categorizedOptions);
      } else {
        // For other fields, just store the options directly
        autocompleteOptions[characteristicName] = extractedOptions;
        return extractedOptions;
      }
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

    if (currentAutocompleteField === 'categoryId') {
      if (currentCategory === null) {
        // Filter categories
        filteredAutocompleteOptions = Object.keys(categorizedOptions)
          .filter(category => category.toLowerCase().includes(inputValue.toLowerCase()));
      } else {
        // Filter characteristics within the selected category
        filteredAutocompleteOptions = categorizedOptions[currentCategory]
          .filter(item => item.display.toLowerCase().includes(inputValue.toLowerCase()))
          .map(item => item.display);
      }
    } else {
      // Standard filtering for other fields
      const rawOptions = JSON.parse(JSON.stringify(
          autocompleteOptions[currentAutocompleteField] || []
      ));
      filteredAutocompleteOptions = rawOptions.filter(option =>
          String(option).toLowerCase().includes(inputValue.toLowerCase())
      );
    }
  }

  // Select an option from autocomplete
  function selectAutocompleteOption(option) {
    if (currentAutocompleteField === 'categoryId') {
      if (currentCategory === null) {
        // User selected a category, now show characteristics for that category
        currentCategory = option;
        filteredAutocompleteOptions = categorizedOptions[option].map(item => item.display);
        return; // Don't close dropdown yet
      } else {
        // User selected a characteristic
        const selectedOption = categorizedOptions[currentCategory].find(item => item.display === option);
        
        if (selectedOption) {
          // Find the characteristic and update its value
          const characteristicIndex = characteristics.findIndex(
            c => c.name === 'categoryId'
          );
          
          if (characteristicIndex !== -1) {
            // Update display value (for showing in the input)
            characteristics[characteristicIndex].displayValue = option;
            // Update actual value (ID for sending to API)
            characteristics[characteristicIndex].value = selectedOption.id;
            characteristicsEdited = true;
          }
        }
        
        // Reset category state
        currentCategory = null;
      }
    } else {
      // Standard selection for other fields
      const characteristicIndex = characteristics.findIndex(
        c => c.name === currentAutocompleteField
      );

      if (characteristicIndex !== -1) {
        characteristics[characteristicIndex].value = option;
        characteristicsEdited = true;
      }
    }

    // Reset autocomplete states
    autocompleteInput = '';
    showAutocompleteDropdown = false;
    currentAutocompleteField = null;
  }

  function closeAutocomplete() {
    showAutocompleteDropdown = false;
    currentAutocompleteField = null;
  }

  // Trigger autocomplete for a specific field
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
      await fetchCharacteristicOptions(characteristicName);
    }

    // Reset and show dropdown
    autocompleteInput = "";
    if (characteristicName === 'categoryId') {
      // For categoryId, show categories first
      filteredAutocompleteOptions = Object.keys(categorizedOptions);
    } else {
      // For other fields, show all options
      filteredAutocompleteOptions = autocompleteOptions[characteristicName] || [];
    }

    showAutocompleteDropdown = true;
  }

  //change with addAlternative(row) after adding search by keyword
  function addAlternative(){
    const errorSameSupp = document.getElementById("error-same-supplier");
    const errorAlreadyAlt = document.getElementById("error-already-alt");
    const errorDifferentGroup = document.getElementById("error-different-group");
    const instrumenttoAdd = {supplier: "Maganovum",
    groupId : 1,
    subGroupId : 1,
    categoryId: 3,
    reference: "PLS-3003",
    supplierDescription: "Plastic Scalpel Type G",
    price: 10.5,
    obsolete: false,
    picturesId: null,
    id: 7};
    if(instrument.supplier === instrumenttoAdd.supplier){
      errorSameSupp.classList.remove('hidden');
      return;
    }
    if(alternativesOfInstr.some(instr => instr.id === instrumenttoAdd.id) ||
    $altToAdd.some(instr => instr.id === instrumenttoAdd.id)){
      errorAlreadyAlt.classList.remove('hidden');
      return;
    }
    if(instrument.groupId !== instrumenttoAdd.groupId){
      errorDifferentGroup.classList.remove('hidden');
      return;
    }
    errorSameSupp.classList.add('hidden');
    errorAlreadyAlt.classList.add('hidden');
    errorDifferentGroup.classList.add('hidden');
    addingAlt.set(true);
    altToAdd.update(alt => [...alt, instrumenttoAdd]);
  }

  async function removeAlt(instrId){
    removingAlt.set(true);
    altToRemove.update(alt => [...alt, instrId]);
    alternativesOfInstr = alternativesOfInstr.filter(instr => instr.id !== instrId);
  }

  function canceling(){
    addingAlt.set(false);
    altToAdd.set([]);
    removingAlt.set(false);
    altToRemove.set([]);
    close();
  }

</script>

{#if isOpen}
<div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500 bg-opacity-10 transition-opacity" aria-hidden="true"></div>
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div 
        class="fixed inset-0 z-10 flex items-center justify-center bg-gray-500 bg-opacity-50"
        onmousemove={drag}
        onmouseup={stopDrag}
    >
        <div 
            class="bg-white rounded-lg shadow-lg w-1/2 absolute"
            style="transform: translate({posX}px, {posY}px);"
        >
            <div 
                class="p-4 border-b cursor-move bg-black text-white flex items-center justify-between"
                onmousedown={startDrag}
            >
                <h2 class="text-xl font-bold">Modifier l'instrument {reference}</h2>
                <!-- Edit Icon -->
                <Icon icon="material-symbols:edit" width="24" height="24" />
            </div>
            
            {#await promise}
                <div role="status" class="my-6 flex items-center justify-center p-4">
                    <Loading />
                    <span class="sr-only">Chargement...</span>
                </div>
            {:then}
              <div class="p-4">
                  <div class="grid grid-cols-2 gap-4">
                      {#each characteristics as characteristic}
                          {#if characteristic.name !== 'id' && characteristic.name !== 'picturesId' && characteristic.name !== 'alt'} 
                              <div>
                                  <label class="block mb-2">
                                      {#if characteristic.name === 'supplier'}
                                          Fournisseur:
                                      {:else if characteristic.name === 'categoryId'}
                                          Catégorie:
                                      {:else if characteristic.name === 'reference'}
                                          Référence:
                                      {:else if characteristic.name === 'supplierDescription'}
                                          Description du fournisseur:
                                      {:else if characteristic.name === 'price'}
                                          Prix:
                                      {:else if characteristic.name === 'obsolete'}
                                          Obsolescence:
                                      {:else}
                                          {characteristic.name}:
                                      {/if}
                                  </label>

                                  <!-- Check if the characteristic is 'obsolete' -->
                                  {#if characteristic.name === 'obsolete'}
                                      <div class="flex gap-4 mb-4">
                                          <label><input type="radio" bind:group={characteristic.value} value={true} /> Oui</label>
                                          <label><input type="radio" bind:group={characteristic.value} value={false} /> Non</label>
                                      </div>
                                  {:else if characteristic.name === 'price'}
                                      <input
                                          type="number"
                                          bind:value={characteristic.value}
                                          min="0"
                                          step="0.01"
                                          onchange={() => {
                                              characteristicsEdited = true;
                                              if(characteristic.value < 0) {
                                                  characteristic.value = 0;
                                              }
                                          }}
                                          oninput={() => {
                                              characteristicsEdited = true;
                                              if(characteristic.value < 0) {
                                                  characteristic.value = 0;
                                              }
                                          }}
                                          class="w-full p-2 border rounded mb-4"
                                      />
                                  {:else}
                                      <input
                                          type="text"
                                          bind:value={characteristic.value}
                                          onchange={() => (characteristicsEdited = true)}
                                          onfocus={() => triggerAutocomplete(characteristic.name)}
                                          oninput={handleAutocompleteInput}
                                          onblur={() => closeAutocomplete()}
                                          class="w-full p-2 border rounded mb-4"
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
                                                  class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full {currentAutocompleteField === 'categoryId' && currentCategory === option ? 'bg-blue-100' : ''}"
                                                  role="option"
                                                  onclick={() => selectAutocompleteOption(option)}
                                              >
                                                  {option}
                                                  {#if currentAutocompleteField === 'categoryId' && currentCategory === null && categorizedOptions[option]}
                                                      <span class="text-xs text-gray-500 ml-2">
                                                          ({categorizedOptions[option].length} items)
                                                      </span>
                                                  {/if}
                                              </button>
                                              {/each}
                                          </ul>
                                      {/if}
                                  {/if}
                              </div>
                          {/if}
                      {/each}
                  </div>
                  
                  <label class="block mb-2">Image:</label>
                  <input
                      class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5 mb-4"
                      type="file"
                      onchange={(e) => (file = e.target.files[0])}
                  />
                  {#if instrument.pictureId}
                      <div class="mt-1 text-sm text-red-500 mb-4">
                          Une image existe déjà pour cet instrument, en indiquant une image ci-dessus, l'image actuelle sera supprimée.
                      </div>
                  {/if}

                  <label class="block mb-2 flex items-center">
                    Alternatives:
                    <input type="text" class="ml-2 p-1 border rounded" />
                    <button class="ml-2 px-4 py-1 bg-yellow-100 text-black hover:bg-gray-500 transition rounded" onclick={() => addAlternative()}>
                      Ajouter
                    </button>
                  </label>
                  <span id="error-same-supplier" class="mb-5 text-red-600 text-sm hidden">Les alternatives doivent avoir des fournisseurs differents.</span>
                  <span id="error-different-group" class="mb-5 text-red-600 text-sm hidden">Les alternatives doivent faire partie du même groupe.</span>
                  <span id="error-already-alt" class="mb-5 text-red-600 text-sm hidden">Cette alternative existe déjà.</span>
                  
                  <table class="w-full border border-gray-200 text-sm">
                    <thead>
                      <tr class="bg-gray-200">
                        <th class="p-2 text-center">Référence</th>
                        <th class="p-2 text-center">Marque</th>
                        <th class="p-2 text-center">Description</th>
                        <th class="p-2 text-center">Prix</th>
                        <th class="p-2 text-center">Supprimer</th>
                      </tr>
                    </thead>
                    <tbody>
                      {#each alternativesOfInstr as row, index}
                        <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                        <tr class="border-t cursor-pointer {row.obsolete ? 'bg-red-500' : ''}"
                        class:bg-[lightgray]={hoveredAlternativeIndex === index}
                        ondblclick={() => selectAlternative(row, index)}
                        onmouseover={() => (hoveredAlternativeIndex = index)}
                        onmouseout={() => (hoveredAlternativeIndex = null)}
                        >
                          <td class="text-center p-2">{row.reference}</td>
                          <td class="text-center p-2">{row.supplier}</td>
                          <td class="text-center p-2">{row.supplierDescription}</td>
                          <td class="text-center p-2">{row.price}</td>
                          <td 
                            class="text-center"
                            onclick={() => removeAlt(row.id)}
                          >
                            <span class="{row.obsolete ? 'text-white' : 'text-red-500'}">&times;</span>
                          </td>
                        </tr>
                      {/each}
                        {#if $addingAlt}
                          {#each $altToAdd as row, index}
                            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                            <tr class="border-t cursor-pointer {row.obsolete ? 'bg-red-500' : 'bg-green-500'}">
                              <td class="text-center p-2">{row.reference}</td>
                              <td class="text-center p-2">{row.supplier}</td>
                              <td class="text-center p-2">{row.supplierDescription}</td>
                              <td class="text-center p-2">{row.price}</td>
                              <td 
                                class="text-center"
                                onclick={() => removeAlt(row.id)}
                              >
                                <span class="{row.obsolete ? 'text-white' : 'text-red-500'}">&times;</span>
                              </td>
                            </tr>
                          {/each}
                        {/if}
                    </tbody>
                  </table>
                  
                  <div class="flex justify-end gap-4 mt-4">
                      <button type="button" onclick={handleDelete} class="bg-red-500 text-white px-4 py-2 rounded">Supprimer</button>
                      <button type="button" onclick={canceling} class="bg-gray-500 text-white px-4 py-2 rounded">Annuler</button>
                      <button type="button" onclick={handleSubmit} class="bg-blue-500 text-white px-4 py-2 rounded">Enregistrer</button>
                  </div>
                </div>
              {/await}
        </div>
    </div>
</div>
{/if}
