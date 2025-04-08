<script>
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { isEditing, orderItems, reload, selectedCategoryIndex, 
        selectedSupplierIndex, quantity, selectedGroup, selectedSubGroup, 
        showChars, charValues, currentSuppliers, categories, characteristics, 
        showSubGroups, showCategories, subGroups, groups, errorMessage, 
    findSubGroupsStore, findCharacteristicsStore, alternatives, hoveredAlternativeIndex, autocompleteOptions} from "$lib/stores/searches";    
    import {startResize, resize, stopResize} from "$lib/resizableUtils.js";
    import { apiFetch } from "$lib/utils/fetch";
    import { toast } from "@zerodevx/svelte-toast";
    import AddCharacteristicModal from "$lib/modals/AddCharacteristicModal.svelte";    
  let showAddCharacteristicModal = false;

  // Initialize from URL params when component mounts
  onMount(() => {
    document.addEventListener('click', handleClickOutside);
    return() => {
      document.removeEventListener('click', handleClickOutside);
    };
  });
    

  /**
   * Gets characteristics and categories of subgroup with the name subGroup
   * @param subGroup
   */
  export async function findCharacteristics(subGroup) {
      if (subGroup == "none") {
          selectedSubGroup.set("");
          findSubGroups($selectedGroup);
          return;
      }
      selectedSubGroup.set(subGroup);
      showChars.set(true);

      charValues.set([]);
      selectedCategoryIndex.set("");
      currentSuppliers.set([]);
      selectedSupplierIndex.set("");
      alternatives.set([]);
      let subgroup = [];

      try {
      const response = await apiFetch(`/api/subgroups/${subGroup}`);
      const response_2 = await apiFetch(`/api/category/subgroup/${subGroup}`);

      if (!response.ok) {
          throw new Error(`Failed to fetch characteristics : ${response.statusText}`);
      }
      if (!response_2.ok) {
          throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subgroup = await response.json();
      categories.set(await response_2.json());
      } catch (error) {
      console.log(error);
      errorMessage.set(error.message);
      }

      characteristics.set(subgroup.subGroupCharacteristics.map(c => c.name));
      return;
  }

  /**
   * Filters the categories depending on the input of the user.
   */
  let minLength = null;
  let maxLength = null;
  function searchByCharacteristics() {
    let char_vals = [];
    for (let i = 0; i < $characteristics.length; i++) {
      if ($characteristics[i] === "Function" || $characteristics[i] === "Name" || $characteristics[i] === "Length") {
        continue;
      }
      if ($charValues[$characteristics[i]]) {
        let char = {
          name: $characteristics[i],
          value: $charValues[$characteristics[i]],
          abrev: "",
          };
          char_vals.push(char);
      } else {
          let char = {
          name: $characteristics[i],
          value: "",
          abrev: "",
          };
          char_vals.push(char);
      }
    }
    const data = {
      groupName: $selectedGroup,
      subGroupName: $selectedSubGroup,
      function: $charValues["Function"] || "",
      name: $charValues["Name"] || "",
      characteristics : char_vals,
      ...(minLength != null && {minLength : minLength}),
      ...(maxLength != null && {maxLength : maxLength}),
    };

      return apiFetch("/api/category/search/by-characteristics", {
      method: "POST",
      headers: { "Content-type": "application/json" },
      body: JSON.stringify(data),
      })
      .then((response) => {
          if (!response.ok) {
          categories.set([]);
          toast.push("Aucun résultat trouvé");
          throw new Error(`Failed to search by characteristics : ${response.status}`);
          }
          return response.json();
      })
      .then((result) => {
        categories.set(result);
        const activeElement = document.activeElement;
        if (activeElement && activeElement.tagName === 'INPUT') {
          activeElement.blur();
        }
      })
      .catch((error) => {
          console.log("Error :", error);
      });
  }

  /**
   * Delete the characteristic value given by id.
   * @param id
   */
  function deleteCharacteristic(id) {
    if(id == "Length"){
      const min = document.getElementById("min-length-input");
      const max = document.getElementById("max-length-input");
      if(min){
        min.value = "";
        minLength = null;
      }
      if(max){
        max.value = "";
        maxLength = null;
      }
      return;
    }
    const inputElement = document.getElementById(id);
    if (inputElement) {
      inputElement.value = "";
    }

      charValues.update(currentValues => {
          const updatedValues = { ...currentValues }; 
          updatedValues[id] = ""; 
          return updatedValues;
      });

      searchByCharacteristics();
  }
  function deleteAllCharacteristics() {
    for (let i = 0; i < $characteristics.length; i++) {
      const characteristic = $characteristics[i];

      if(characteristic == "Length"){
        const min = document.getElementById("min-length-input");
        const max = document.getElementById("max-length-input");
        if(min){
          min.value = "";
          minLength = null;
        }
        if(max){
          max.value = "";
          maxLength = null;
        }
        continue;
      }

          const inputElement = document.getElementById(characteristic);
          if (inputElement) {
              inputElement.value = "";
          }

          charValues.update(currentValues => {
              const updatedValues = { ...currentValues };
              updatedValues[characteristic] = ""; 
              return updatedValues;
          });
      }
      searchByCharacteristics();
  }

  /**
   * Gets subgroups of group and their categories
   * @param group
   */
  export async function findSubGroups(group) {
      if (group == "none") {
          selectedGroup.set("");
          selectedSubGroup.set("");
          selectedCategoryIndex.set(null);
          selectedSupplierIndex.set(null);
          showCategories.set(false);
          categories.set([]);
          showSubGroups.set(false);
          subGroups.set([]);
          showChars.set(false);
          charValues.set([]);
          characteristics.set([]);
          currentSuppliers.set([]);
          alternatives.set([]);
          return;
      }
      const previousGroup = $selectedGroup;
      selectedGroup.set(group);
      showSubGroups.set(true);
      showCategories.set(true);
      
      currentSuppliers.set([]);
      selectedSupplierIndex.set("");
      selectedCategoryIndex.set("");
      alternatives.set([]);

      // Only reset subgroup if the group has changed
      if (previousGroup !== group) {
      selectedSubGroup.set("");
      }
      showChars.set(false);
      characteristics.set([]);
      charValues.set([]);

      let subGroups_all_info = [];
      try {
      const response = await apiFetch(`/api/subgroups/group/${group}`);
      const response_2 = await apiFetch(`/api/category/group/${group}`);

      if (!response.ok) {
          throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
      }
      if (!response_2.ok) {
          throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subGroups_all_info = await response.json();
      categories.set(await response_2.json());
      } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
      }
      subGroups.set(subGroups_all_info.map((subgroup) => subgroup.name));
      return;
  }

  findSubGroupsStore.set(findSubGroups);
  findCharacteristicsStore.set(findCharacteristics);

  let autocompleteInput = '';
  let showAutocompleteDropDown = false;
  let filteredAutocompleteOptions = [];
  let currentAutocompleteField = null;

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
      const values = await response.json() ;
      autocompleteOptions.update(current => ({
        ...current,
        [charName]: values
      }));
    } catch(error){
      console.error('Failed to fetch options', error);
    }
  }

  function handleClickOutside(event){
    const inputField = document.querySelector(`#${currentAutocompleteField}`);
    const dropdown = inputField?.nextElementSibling;

    if (inputField && !inputField.contains(event.target) && !dropdown?.contains(event.target)) {
      showAutocompleteDropDown = false;
      currentAutocompleteField = null;
    }
  }

  function handleAutocompleteInput(event){
    const inputValue = event.target.value;
    autocompleteInput  = inputValue;
    showAutocompleteDropDown = true;
    filteredAutocompleteOptions = $autocompleteOptions[currentAutocompleteField].filter(option => option.toLowerCase().includes(inputValue.toLowerCase()));
  }

  function selectAutocompleteOption(option){
    charValues.update(currentValues => {
      const updatedValues = {...currentValues};
      updatedValues[currentAutocompleteField] = option;
      return updatedValues;
    });
    autocompleteInput = '';
    showAutocompleteDropDown = false;
    currentAutocompleteField = null;
    searchByCharacteristics();
  }

  function handleMinLengthInput(e){
    if(e.key === "Enter"){
      e.preventDefault();
      minLength = parseFloat(e.target.value);
      document.getElementById("max-length-input")?.focus();
    }
  }

  function handleMaxLengthInput(e){
    if(e.key === "Enter"){
      e.preventDefault();
      const input = e.target
      maxLength = parseFloat(input.value) ;
      if(!input.checkValidity()){
        input.reportValidity();
        return;
      }
      searchByCharacteristics();
    }
  }
</script>


<div class="flex-[1.3] h-full ml-3 p-2 bg-gray-100 rounded-lg shadow-md">
  <form class="flex flex-col w-[90%] mb-2.5">
      <label for="google-search" class="font-semibold mt-1">Recherche par mot(s) clé(s):</label>
      <input
      type="text"
      class="border border-gray-400 rounded p-0.5 border-solid border-[black]"
      id="google-search"
      name="google-search"
      placeholder="Entrez un mot clé"
      />
  </form>

  <label class="font-semibold">Recherche par caractéristiques:</label>
  <div class="flex items-center">
      <label class="w-2/5 mt-2 mb-2" for="groupOptions">Groupe:</label>
      <select
      id="groupOptions"
      bind:value={$selectedGroup}
      on:change={(e) => findSubGroups(e.target.value)}
      >
      <option value="none">---</option>
      {#each $groups as group}
          <option value={group}>{group}</option>
      {/each}
      </select>
  </div>

  {#if $showSubGroups}
      <div class="flex items-center">
      <label class="w-2/5 mb-2" for="subGroupOptions">Sous gp:</label>
      <select
          id="subGroupOptions"
          bind:value={$selectedSubGroup}
          on:change={(e) => findCharacteristics(e.target.value)}
      >
          <option value="none">---</option>
          {#each $subGroups as subGroup}
          <option value={subGroup}>{subGroup}</option>
          {/each}
      </select>
      </div>
  {/if}

  {#if $showChars}
    <form class="flex flex-col w-full gap-2.5" on:submit|preventDefault={searchByCharacteristics}>
        <div class="flex gap-2 mb-2 mt-4">
        <button
            type="submit"
            class="w-[90px] border border-gray-400 rounded bg-gray-400 border-solid border-[black] rounded-sm"
        >Chercher</button>
        <button
            type="button"
            class="w-[90px] border border-red-700 rounded bg-red-700 border-solid border-[black] rounded-sm"
            on:click={deleteAllCharacteristics}
        >Tout effacer</button>
        </div>

        {#each $characteristics as char}
        {#if char === "Length"}
            <div class="flex items-center">
            <label for={char} class="w-2/5">{char}:</label>
            <div class="flex w-3/5 gap-2">
                <input
                id="min-length-input"
                type="number"
                min="0"
                step="0.01"
                class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]"
                placeholder="min"
                bind:value={minLength}
                on:keydown={handleMinLengthInput}
                />
                <input
                id="max-length-input"
                type="number"
                min={minLength || 0}
                step="0.01"
                class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]"
                placeholder="max"
                bind:value={maxLength}
                on:keydown={handleMaxLengthInput}
                />
            </div>
            <button
                class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer"
                on:click={() => deleteCharacteristic(char)}
            >&times;</button>
            </div>
        {:else}
            <div class="flex items-center relative">
            <label for={char} class="w-2/5">{char}:</label>
            <div class="relative w-3/5">
                <input
                type="text"
                class="w-full border border-gray-400 rounded p-0.5 border-solid border-[black] mb-2"
                id={char}
                name={char}
                data-testid={char}
                bind:value={$charValues[char]}
                on:focus={() => triggerAutocomplete(char)}
                on:input={handleAutocompleteInput}
                />
                {#if showAutocompleteDropDown && currentAutocompleteField === char}
                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                <ul 
                    class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
                    on:mousedown={event => event.preventDefault()}
                >
                    {#each filteredAutocompleteOptions as option}
                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                    <button
                        type="button"
                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                        role="option"
                        on:click={() => selectAutocompleteOption(option)}
                    >
                        {option}
                    </button>
                    {/each}
                </ul>
                {/if}
            </div>
            <button
                class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer"
                on:click={() => deleteCharacteristic(char)}
            >&times;</button>
            </div>
        {/if}
        {/each}

        {#if $isEditing}
            <button
                class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none self-end mt-2"
                on:click={() => showAddCharacteristicModal = true}
            >
                Ajouter
            </button>
        {/if}
    </form>
    {/if}
</div>
<AddCharacteristicModal
  isOpen={showAddCharacteristicModal}
  onClose={() => showAddCharacteristicModal = false}
  selectedSubGroup={$selectedSubGroup}
  on:added={async () => {
    await findCharacteristics($selectedSubGroup); 
  }}
/>

