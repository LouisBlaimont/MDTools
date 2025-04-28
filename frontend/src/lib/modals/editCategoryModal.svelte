<script>
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";
    import { selectedSubGroup, characteristics, showChars, autocompleteOptions, categories, reload, selectedGroup } from "$lib/stores/searches";
    import { toast } from "@zerodevx/svelte-toast";

  const {
      isOpen,
      close,
      categoryToEdit,
  } = $props();
  
  // Only modified fields 
  let updatedCharVals = $state({})
  let updatedCharAbbrev = $state({})

  // All visible fields
  let newCharValues = $state({});
  let newCharAbbrev = $state({});

  // Pictures
  let files = $state([]);

  // Loading category characterstics when opening the modal
  $effect(() => {
    if (isOpen) {
      fetchInitialData();
    }
  });
  async function fetchInitialData() {
    try{
        const response = await apiFetch(`/api/category/${categoryToEdit.id}`);
        if (!response.ok) {
            const errorResponse = await response.json();
            throw new Error(errorResponse.error || "Failed to create fetch characteristics");
        }
        const characteristics = await response.json() ;
        for (const characteristic of characteristics){
            const name = characteristic.name;
            const value = characteristic.value;
            const abrev = characteristic.abrev;
            newCharValues[name] = value;
            newCharAbbrev[name] = abrev;
        }
    }catch(error){
        console.error(error);
    }
    
  }

  // Draggable modal functionality
  let posX = $state(0), posY = $state(0), offsetX = 0, offsetY = 0, isDragging = false;
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

  // Displaying possible values for the fields
  let autocompleteInput = '';
  let showAutocompleteDropDown = $state(false);
  let filteredAutocompleteOptions = $state([]);
  let currentAutocompleteField = $state(null);

  // Display values when clicking on fields
  async function triggerAutocomplete(charName){
      currentAutocompleteField = charName;
      if(!autocompleteOptions[currentAutocompleteField]){
          await fetchCharacteristicOptions(charName);
      }
      autocompleteInput = "";
      filteredAutocompleteOptions = $autocompleteOptions[charName] || [];
      showAutocompleteDropDown = true;
  }

  async function fetchCharacteristicOptions(charName){
      try{
      const response = await apiFetch(`/api/characteristics/${charName}/values-in/${$selectedSubGroup}`);
      if (!response.ok) {
          throw new Error(`Failed to fetch options for ${charName}`);
      }
      let values = await response.json() ;
      values = values.filter(value => value !== "");
      autocompleteOptions.update(current => ({
          ...current,
          [charName]: values
      }));
      } catch(error){
        console.error('Failed to fetch options', error);
      }
  }

  // Filter values when writing inside field
  function handleAutocompleteInput(event){
      const inputValue = event.target.value;
      autocompleteInput  = inputValue;
      showAutocompleteDropDown = true;
      filteredAutocompleteOptions = $autocompleteOptions[currentAutocompleteField].filter(option => option.toLowerCase().includes(inputValue.toLowerCase()));
      updatedCharVals[currentAutocompleteField] = newCharValues[currentAutocompleteField];
  }

  // Select value when clicking on an option, adding this value to the fields to update
  function selectAutocompleteOption(option){
      newCharValues[currentAutocompleteField] = option;
      newCharValues = {...newCharValues};
      updatedCharVals[currentAutocompleteField] = option;
      updatedCharVals = {...updatedCharVals};
      autocompleteInput = '';
      showAutocompleteDropDown = false;
      currentAutocompleteField = null;
  }

  // For fields without options, still need to add value to fields to update when writing
  function handleInputWithoutAutoComplete(char){
    if(char === "Length"){
        updatedCharVals[char] = String(newCharValues[char]);
    }
    else{
        updatedCharVals[char] = newCharValues[char];
        updatedCharAbbrev[`${char}abrev`] = newCharAbbrev[char];
    }
  }

  // For fields with abreviations, when an exisiting option is clicked on, the corresponding alternative is displayed
  async function selectAutocompleteOptionWithAbrev(option){
      newCharValues[currentAutocompleteField] = option;
      newCharValues = {...newCharValues};
      updatedCharVals[currentAutocompleteField] = option;
      updatedCharVals = {...updatedCharVals};
      try{
          const response = await apiFetch(`/api/category/abbreviation/of/${currentAutocompleteField}/${option}`);
          if (!response.ok) {
              throw new Error(`Failed to fetch abbreviation for ${option}`);
          }
          const characteristic = await response.json() ;
          const abbrev = characteristic.abrev;
          newCharAbbrev[currentAutocompleteField] = abbrev;
          newCharAbbrev = {...newCharAbbrev};
      } catch(error){
          console.error('Failed to fetch options', error);
      }
      autocompleteInput = '';
      showAutocompleteDropDown = false;
      currentAutocompleteField = null;    
  }

  // Edit the category using the modified input fields
  async function editCategory() {
    if(Object.keys(updatedCharVals).length === 0 && Object.keys(updatedCharAbbrev).length === 0 && files.length === 0){
        const errorNoModif = document.getElementById("error-no-modif");
        errorNoModif.classList.remove("hidden");
        return;
    }
    let data = {
        ...updatedCharVals, 
        ...updatedCharAbbrev
    };
    try {
        const response = await apiFetch(`/api/category/${categoryToEdit.id}/subgroup/${$selectedSubGroup}`, {
        method: "PATCH", 
        headers : { "Content-type" : "application/json"},
        body : JSON.stringify(data),
        });
        if(!response.ok){
            if(response.status === 400){
                const errorSameCat = document.getElementById("error-same-category");
                errorSameCat.classList.remove("hidden");
            }
            const errorResponse = await response.json();
            throw new Error(errorResponse.error || "failed to update cat");
        }

        const result = await response.json();
        categories.update(currentCat => {
            return [...currentCat, result];
        });

        if(files.length > 0){
            const formData = new FormData();
            files.forEach((file) => {formData.append("files", file); });
            formData.append("referenceId", result.id);
            formData.append("type", "category");
            const response = await apiFetch(`/api/pictures/multiple`, {
                method: "POST",
                body: formData,
            });
            if(!response.ok){
                const pbPicture= document.getElementById("error-picture");
                pbPicture.classList.remove("hidden");
                const errorResponse = await response.json();
                throw new Error(errorResponse.error);
            }
            const pbPicture= document.getElementById("error-picture");
            pbPicture.classList.add("hidden");
            const responseData = await response.json();
            responseData.forEach((picture) => {
                categories.update((cats) => {
                cats.forEach((cat) => {
                    if (cat.id === result.id) {
                    cat.picturesId.push(picture.id);
                    }
                });
                return cats;
            }); });   
        }
      close();
      goto(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=${encodeURIComponent(result.id)}&instrument=`);
      reload.set(true);

      }catch(error){
          console.log("Error:", error);
      }
  }

  // Deleting the category 
  async function handleDelete() {
    if(confirm($_('modals.edit_cat.delete'))){
        try {
            const response = await apiFetch(`/api/category/delete/${categoryToEdit.id}`, {
            method: "DELETE",
            });
            if (!response.ok) {
                if(response.status === 400){
                const errorCatNotEmpty = document.getElementById("error-category-not-empty");
                errorCatNotEmpty.classList.remove("hidden");
                }
                return;
            }
            close(); // Close the modal
            goto(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=&instrument=`);
            reload.set(true); // Trigger a reload
        } catch (error) {
            console.error("Error:", error);
        }
    }
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
                onmousedown={startDrag}
            >
            <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.edit_cat.edit')}</h2>
                  <h4 class="text-xl font-bold text-teal-500 text-center">{categoryToEdit.groupName}, {categoryToEdit.subGroupName}, {categoryToEdit.name}, {categoryToEdit.function}, {categoryToEdit.shape}</h4>
                  <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="teal-500"
                  version="1.1"
                  id="Capa_1"
                  viewBox="0 0 494.936 494.936"
                  class="w-6 h-6"
                >
                <g>
                    <g>
                      <path
                        d="M389.844,182.85c-6.743,0-12.21,5.467-12.21,12.21v222.968c0,23.562-19.174,42.735-42.736,42.735H67.157 c-23.562,0-42.736-19.174-42.736-42.735V150.285c0-23.562,19.174-42.735,42.736-42.735h267.741c6.743,0,12.21-5.467,12.21-12.21 s-5.467-12.21-12.21-12.21H67.157C30.126,83.13,0,113.255,0,150.285v267.743c0,37.029,30.126,67.155,67.157,67.155h267.741 c37.03,0,67.156-30.126,67.156-67.155V195.061C402.054,188.318,396.587,182.85,389.844,182.85z"
                      />
                      <path
                        d="M483.876,20.791c-14.72-14.72-38.669-14.714-53.377,0L221.352,229.944c-0.28,0.28-3.434,3.559-4.251,5.396l-28.963,65.069 c-2.057,4.619-1.056,10.027,2.521,13.6c2.337,2.336,5.461,3.576,8.639,3.576c1.675,0,3.362-0.346,4.96-1.057l65.07-28.963 c1.83-0.815,5.114-3.97,5.396-4.25L483.876,74.169c7.131-7.131,11.06-16.61,11.06-26.692 C494.936,37.396,491.007,27.915,483.876,20.791z M466.61,56.897L257.457,266.05c-0.035,0.036-0.055,0.078-0.089,0.107 l-33.989,15.131L238.51,247.3c0.03-0.036,0.071-0.055,0.107-0.09L447.765,38.058c5.038-5.039,13.819-5.033,18.846,0.005 c2.518,2.51,3.905,5.855,3.905,9.414C470.516,51.036,469.127,54.38,466.61,56.897z"
                      />
                    </g>
                  </g>
                </svg>
              </div>

              <div class="bg-gray-100 p-6 rounded-b-lg">
              {#if $showChars}
              {#each $characteristics as char}
                  {#if char==="Length"}
                      <label class="font-semibold text-lg" for="input-{char}">{char}:</label>
                      <div class="relative mb-2">
                          <input 
                          type="number" 
                          min="0"
                          step="0.01"
                          id="input-{char}"
                          bind:value={newCharValues[char]}
                          class="w-full p-2 mt-1 mb-3 border rounded"
                          oninput={()=>handleInputWithoutAutoComplete(char)} 
                          >
                      </div>
                  {:else if char === "Function" || char === "Name"}
                      <label class="font-semibold text-lg" for="input-{char}">{char}:</label>
                      <div class="relative mb-2">
                          <input 
                          type="text" 
                          id="input-{char}"
                          bind:value={newCharValues[char]}
                          onfocus={()=> triggerAutocomplete(char)}
                          oninput={handleAutocompleteInput}
                          onblur={()=>showAutocompleteDropDown = false}
                          class="w-full p-2 mt-1 mb-3 border rounded" 
                          >
                          {#if showAutocompleteDropDown && currentAutocompleteField === char}
                              <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                              <ul 
                              class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
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
                  {:else}
                      <div class="grid grid-cols-2 gap-4 items-center mb-2 ">
                          <div class="relative">
                              <label class="font-semibold text-lg" for="input-{char}">{char}:</label>
                              <input
                                  type = "text"
                                  id="input-{char}"
                                  bind:value={newCharValues[char]}
                                  onfocus={()=> triggerAutocomplete(char)}
                                  oninput={handleAutocompleteInput}
                                  onblur={()=>showAutocompleteDropDown = false}
                                  class="w-full p-2 mt-1 mb-3 border rounded"
                              >
                              {#if showAutocompleteDropDown && currentAutocompleteField === char}
                                  <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                  <ul 
                                  class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
                                  onmousedown={event => event.preventDefault()}
                                  >
                                  {#each filteredAutocompleteOptions as option}
                                  <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                  <button
                                      type="button"
                                      class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                      role="option"
                                      onclick={() => selectAutocompleteOptionWithAbrev(option)}
                                  >
                                      {option}
                                  </button>
                                  {/each}
                                  </ul>
                              {/if}
                          </div>
                          <div>
                              <label class="font-semibold text-lg" for="input-{char}-abbrev">{$_('modals.edit_cat.abb')}</label>
                              <input
                                  type = "text"
                                  id="input-{char}-abbrev"
                                  bind:value={newCharAbbrev[char]}
                                  class="w-full p-2 mt-1 mb-3 border rounded"
                                  oninput={()=>handleInputWithoutAutoComplete(char)}
                              >
                          </div>
                      </div>
                  {/if}
              {/each}
              {/if}
                <label class="block font-semibold text-lg" for="category-img">{$_('modals.edit_cat.picture')}</label>
                <input
                    class="block w-1/2 text-sm text-gray-900 border border-gray-300 rounded cursor-pointer bg-gray-50 focus:outline-none p-2.5 mb-4"
                    type="file"
                    accept="image/*"
                    multiple
                    id = "category-img"
                    onchange={(e) => (files = Array.from(e.target.files))}
                />
                <span id="error-picture" class="mb-5 text-red-600 hidden">{$_('modals.edit_cat.pb')}</span> 
                <span id="error-same-category" class="ml-5 mb-5 text-red-600 hidden">{$_('modals.edit_cat.exists')}</span>
                <span id="error-no-modif" class="ml-5 mb-5 text-red-600 hidden">A{$_('modals.edit_cat.modif')}.</span>
                <span id="error-category-not-empty" class="ml-5 mb-5 text-red-600 hidden">{$_('modals.edit_cat.not_deleted')}</span>
              <div class="flex justify-end gap-4 mb-4 mr-4">
                <button type="button" onclick={handleDelete} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">{$_('modals.edit_cat.supp')}</button>
                <button type="button" onclick={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">{$_('modals.edit_cat.cancel')}</button>
                <button type="button" onclick={()=>editCategory()} class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-blue-700">{$_('modals.edit_cat.save')}</button>
              </div>
              </div>
          </div>
      </div>
  </div>
{/if}