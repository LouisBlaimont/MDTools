<script>
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { preventDefault } from "svelte/legacy";
  import { get } from "svelte/store";
  import { modals } from "svelte-modals";
  import {
    isEditing,
    orderItems,
    reload,
    selectedCategoryIndex,
    selectedSupplierIndex,
    quantity,
    selectedGroup,
    selectedSubGroup,
    showChars,
    charValues,
    currentSuppliers,
    categories,
    characteristics,
    showSubGroups,
    showCategories,
    subGroups,
    groups,
    errorMessage,
        findSubGroupsStore,
    findCharacteristicsStore,
    alternatives,
    hoveredAlternativeIndex, 
     
    categories_pageable,
    keywords2,
    keywordsResult2,
    autocompleteOptions,
  } from "$lib/stores/searches";
  import { apiFetch } from "$lib/utils/fetch";
  import { browser } from "$app/environment";
  import { derived } from "svelte/store";
  import { toast } from "@zerodevx/svelte-toast";
  import AddCharacteristicModal from "$lib/modals/AddCharacteristicModal.svelte";
  let showAddCharacteristicModal = $state(false);
  import { _ } from "svelte-i18n";
  import AddGroupModal from "$lib/modals/AddGroupModal.svelte";
  import AddSubGroupModal from "$lib/modals/AddSubGroupModal.svelte";
  import { findCharacteristics, findSubGroups } from "./search.js";

  let page_size = 2;

  export const updateURLParams = derived(
    [selectedGroup, selectedSubGroup], // Dependencies
    ([$selectedGroup, $selectedSubGroup]) => {
      if (!browser) return; // Only run in browser environment

      // Get the current URL from the page store
      const currentUrl = new URL($page.url);

      // Update the URL with group and subgroup parameters
      if ($selectedGroup) {
        currentUrl.searchParams.set("group", $selectedGroup);
      } else {
        currentUrl.searchParams.delete("group");
      }

      if ($selectedSubGroup) {
        currentUrl.searchParams.set("subgroup", $selectedSubGroup);
      } else {
        currentUrl.searchParams.delete("subgroup");
      }

      // Use Svelte's goto function to update the URL (replaceState prevents page reload)
      goto(currentUrl.toString(), { replaceState: true, noScroll: true });
    }
  );

  // Initialize from URL params when component mounts
  onMount(() => {
    document.addEventListener("click", handleClickOutside);
    return () => {
      document.removeEventListener("click", handleClickOutside);
    };
  });

  /**
   * Filters the categories depending on the input of the user.
   */
  let minLength = $state(null);
  let maxLength = $state(null);
  function searchByCharacteristics() {
    let char_vals = [];
    for (let i = 0; i < $characteristics.length; i++) {
      if (
        $characteristics[i] === "Function" ||
        $characteristics[i] === "Name" ||
        $characteristics[i] === "Length"
      ) {
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
      characteristics: char_vals,
      ...(minLength != null && { minLength: minLength }),
      ...(maxLength != null && { maxLength: maxLength }),
    };

    return apiFetch("/api/category/search/by-characteristics", {
      method: "POST",
      headers: { "Content-type": "application/json" },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (!response.ok) {
          categories.set([]);
          toast.push($_('search_page.no_result'));
          throw new Error(`Failed to search by characteristics : ${response.status}`);
        }
        return response.json();
      })
      .then((result) => {
        categories.set(result);
        const activeElement = document.activeElement;
        if (activeElement && activeElement.tagName === "INPUT") {
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
    if (id == "Length") {
      const min = document.getElementById("min-length-input");
      const max = document.getElementById("max-length-input");
      if (min) {
        min.value = "";
        minLength = null;
      }
      if (max) {
        max.value = "";
        maxLength = null;
      }
      return;
    }
    const inputElement = document.getElementById(id);
    if (inputElement) {
      inputElement.value = "";
    }

    charValues.update((currentValues) => {
      const updatedValues = { ...currentValues };
      updatedValues[id] = "";
      return updatedValues;
    });

    searchByCharacteristics();
  }

  /**
   * Deleting all the filled characteristics
   */
  function deleteAllCharacteristics() {
    for (let i = 0; i < $characteristics.length; i++) {
      const characteristic = $characteristics[i];

      if (characteristic == "Length") {
        const min = document.getElementById("min-length-input");
        const max = document.getElementById("max-length-input");
        if (min) {
          min.value = "";
          minLength = null;
        }
        if (max) {
          max.value = "";
          maxLength = null;
        }
        continue;
      }

      const inputElement = document.getElementById(characteristic);
      if (inputElement) {
        inputElement.value = "";
      }

      charValues.update((currentValues) => {
        const updatedValues = { ...currentValues };
        updatedValues[characteristic] = "";
        return updatedValues;
      });
    }
    searchByCharacteristics();
  }

  findSubGroupsStore.set(findSubGroups);
  findCharacteristicsStore.set(findCharacteristics);

  /* Dealing with the search by keywords */
  let showKeywordsResult = $state(false);
  let clickTimeout = 500;

  /**
   * goto searches with the selected instrument found by keywords
   * @param group selected group
   * @param subgroup selected subgroup
   * @param catId selected catId
   * @param instrumentId selected instrumentId
   */
  async function moveToSearchesBis(group, subgroup, catId, instrumentId) {
    clearTimeout(clickTimeout);
    // handle when the instrument has no category
    if (catId == null) {
      await modals.open(editInstrumentModal, {
        instrument,
        message: $_('search_page.assign')
      });
    }
    keywords2.set(null);
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup)}&category=${encodeURIComponent(catId)}&instrument=${encodeURIComponent(instrumentId)}`
    );
    reload.set(true);
  }

  /**
   * function to handle the keyword inputs and calling endpoint
   */
  let searchByKeywords = throttle(async () => {
    try {
      showKeywordsResult = false;
      let data = null;
      let params = new URLSearchParams();
      $keywords2.split(",").forEach((element) => {
        const keyword = element.trim();
        if (keyword.length > 0) {
          params.append("keywords", keyword);
        }
      });
      if ($keywords2 == null) {
        data = null;
      } else {
        let response = await apiFetch(`/api/instrument/search?${params}`);
        data = await response.json();
        keywordsResult2.set(Array.isArray(data) ? data.slice(0, 5) : []);
        if (Object.keys(data).length > 0) {
          showKeywordsResult = true;
        }
      }
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }, 300);

  /**
   * handles the delay between to search by keywords
   * @param fn
   * @param delay
   */
  function throttle(fn, delay) {
    let lastCall = 0;
    return (...args) => {
      const now = Date.now();
      if (now - lastCall >= delay) {
        lastCall = now;
        fn(...args);
      }
    };
  }

  /**
   *  get category, group and subgroup from selected instrument, then call moveToSearchesBis
   * @param row selected instrument
   */
  async function selectedInstrument(row) {
    try {
      let response = await apiFetch(`/api/instrument/getCategory/${row.categoryId}`);
      let cat = await response.json();
      showKeywordsResult = false;
      moveToSearchesBis(cat.groupName, cat.subGroupName, row.categoryId, row.id);
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }

  /**
   * Options for different fields
   */
  let autocompleteInput = "";
  let showAutocompleteDropDown = $state(false);
  let filteredAutocompleteOptions = $state([]);
  let currentAutocompleteField = $state(null);

  /**
   * triggering the autocomplete option
   * @param charName
   */
  async function triggerAutocomplete(charName) {
    currentAutocompleteField = charName;
    if (!autocompleteOptions[currentAutocompleteField]) {
      await fetchCharacteristicOptions(charName);
    }
    autocompleteInput = "";
    filteredAutocompleteOptions = $autocompleteOptions[charName] || [];
    showAutocompleteDropDown = true;
  }

  /**
   * fetching all the characteristics
   * @param charName
   */
  async function fetchCharacteristicOptions(charName) {
    try {
      const response = await apiFetch(
        `/api/characteristics/${charName}/values-in/${$selectedSubGroup}`
      );
      if (!response.ok) {
        throw new Error(`Failed to fetch options for ${charName}`);
      }
      const values = await response.json();
      autocompleteOptions.update((current) => ({
        ...current,
        [charName]: values,
      }));
    } catch (error) {
      console.error("Failed to fetch options", error);
    }
  }

  function handleClickOutside(event) {
    const inputField = document.querySelector(`#${currentAutocompleteField}`);
    const dropdown = inputField?.nextElementSibling;

    if (inputField && !inputField.contains(event.target) && !dropdown?.contains(event.target)) {
      showAutocompleteDropDown = false;
      currentAutocompleteField = null;
    }
  }

  function handleAutocompleteInput(event) {
    const inputValue = event.target.value;
    autocompleteInput = inputValue;
    showAutocompleteDropDown = true;
    filteredAutocompleteOptions = $autocompleteOptions[currentAutocompleteField].filter((option) =>
      option.toLowerCase().includes(inputValue.toLowerCase())
    );
  }

  function selectAutocompleteOption(option) {
    charValues.update((currentValues) => {
      const updatedValues = { ...currentValues };
      updatedValues[currentAutocompleteField] = option;
      return updatedValues;
    });
    autocompleteInput = "";
    showAutocompleteDropDown = false;
    currentAutocompleteField = null;
    searchByCharacteristics();
  }

  function handleMinLengthInput(e) {
    if (e.key === "Enter") {
      e.preventDefault();
      minLength = parseFloat(e.target.value);
      document.getElementById("max-length-input")?.focus();
    }
  }

  function handleMaxLengthInput(e) {
    if (e.key === "Enter") {
      e.preventDefault();
      const input = e.target;
      maxLength = parseFloat(input.value);
      if (!input.checkValidity()) {
        input.reportValidity();
        return;
      }
      searchByCharacteristics();
    }
  }
</script>

<div class="flex-[1.3] h-full ml-3 p-2 bg-gray-100 rounded-lg shadow-md">
  <div class="bg-white rounded-xl shadow-md py-2 px-2">
    <form class="space-y-5">
      <div class="relative w-full">
        <label for="google-search" class="font-semibold mt-1"
          >{$_("search_page.label.keyword")}:</label
        >
        <input
          type="text"
          name="search_keyword"
          id="id_search_keyword"
          autocomplete="off"
          placeholder={$_("search_page.placeholder.keyword")}
          class="p-2 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 w-3/5 mb-1"
          bind:value={$keywords2}
          oninput={searchByKeywords}
        />

        <!-- Search results dropdown -->
        <!-- Search results dropdown -->
        {#if showKeywordsResult}
          {#if $keywords2}
            <ul
              class="absolute left-0 w-full bg-white border border-gray-300 rounded-lg shadow-lg z-10 max-h-60 overflow-y-auto text-gray-800"
              style="width: calc(100% + 80px);"
            >
              {#each $keywordsResult2 as row, index}
                <!-- svelte-ignore a11y_click_events_have_key_events -->
                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                <li
                  class="p-2 hover:bg-gray-100 cursor-pointer flex items-center space-x-4 text-sm border-b last:border-none"
                  onclick={() => selectedInstrument(row)}
                >
                  <span class="text-black">{row.reference}</span>
                  <span class="text-black">{row.supplier}</span>
                  <span
                    class="text-black line-clamp-2 w-full tooltip-container"
                    data-tooltip={row.supplierDescription}
                    title={row.supplierDescription}
                  >
                    {row.supplierDescription}
                  </span>
                </li>
              {/each}
            </ul>
          {/if}
        {/if}
      </div>
    </form>
  </div>
  <div class="bg-white rounded-xl shadow-md py-2 px-2 my-2">
    <label class="font-semibold" for="char">{$_("search_page.label.characteristics")}</label>
    <div class="flex items-center min-w-[200px]">
      <label class="w-2/5" for="groupOptions">{$_("search_page.label.group")}</label>
      <select
        id="groupOptions"
        bind:value={$selectedGroup}
        onchange={(e) => {
          selectedSubGroup.set("");
          findSubGroups(e.target.value);
        }}
        class="w-3/5 p-1 py-1.5 bg-transparent text-slate-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
      >
        <option value="none">---</option>
        {#each $groups as group}
          <option value={group}>{group}</option>
        {/each}
      </select>
      {#if $isEditing}
        <!-- svelte-ignore node_invalid_placement_ssr -->
        <button
          type="button"
          class="ml-2 px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
          onclick={() => modals.open(AddGroupModal)}
        >
        {$_('search_page.button.add')}
        </button>
      {/if}
    </div>

    {#if $showSubGroups}
      <div class="flex items-center mt-2">
        <label class="w-2/5" for="subGroupOptions">{$_("search_page.label.subgroup")}</label>
        <select
          id="subGroupOptions"
          bind:value={$selectedSubGroup}
          onchange={(e) => findCharacteristics(e.target.value)}
          class="w-3/5 p-1 py-1.5 bg-transparent text-slate-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
        >
          <option value="none">---</option>
          {#each $subGroups as subGroup}
            <option value={subGroup}>{subGroup}</option>
          {/each}
      </select>
      {#if $isEditing && $selectedGroup}
        <!-- svelte-ignore node_invalid_placement_ssr -->
        <button
          type="button"
          class="ml-2 px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
          onclick={()=> modals.open(AddSubGroupModal)}
        >
        {$_('search_page.button.add')}
        </button>
      {/if}
      </div>
    {/if}
  
  {#if $showChars}
    <form class="flex flex-col w-full gap-2.5" onsubmit={searchByCharacteristics} preventdefault>
      <div class="flex gap-2 mb-2 mt-4">
        <button
          type="submit"
          class="bg-gray-300 hover:bg-gray-500 focus:outline-none focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-5 py-2"
          >{$_("search_page.button.search")}</button
        >
        <button
          type="button"
          class="bg-red-600 hover:bg-red-800 focus:outline-none focus:ring-4 focus:ring-gray-300 font-medium rounded-lg text-sm px-5 py-2"
          onclick={deleteAllCharacteristics}>{$_("search_page.button.clear")}</button
        >
      </div>

      {#each $characteristics as char}
        {#if char === "Length"}
          <div class="flex items-center justify-center relative">
            <label for={char} class="w-2/5">{char}:</label>
            <div class="flex w-3/5 gap-2 items-center">
              <input
                id="min-length-input"
                type="number"
                min="0"
                step="0.01"
                class="border border-gray-300 rounded-lg w-1/2 px-1 py-0.5"
                placeholder={$_("search_page.placeholder.min")}
                bind:value={minLength}
                onkeydown={handleMinLengthInput}
                autocomplete="off"
              />
              <input
                id="max-length-input"
                type="number"
                min={minLength || 0}
                step="0.01"
                class="border border-gray-300 rounded-lg w-1/2 px-1 py-0.5"
                placeholder={$_("search_page.placeholder.max")}
                bind:value={maxLength}
                onkeydown={handleMaxLengthInput}
                autocomplete="off"
              />
            </div>
            <button
              class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer"
              onclick={() => deleteCharacteristic(char)}>&times;</button
            >
          </div>
        {:else}
          <div class="flex items-center justify-center relative">
            <label for={char} class="w-2/5">{char}:</label>
            <div class="relative w-3/5">
              <input
                type="text"
                class="border border-gray-300 rounded-lg w-full px-1 py-0.5"
                id={char}
                name={char}
                data-testid={char}
                bind:value={$charValues[char]}
                onfocus={() => triggerAutocomplete(char)}
                oninput={handleAutocompleteInput}
                autocomplete="off"
              />
              {#if showAutocompleteDropDown && currentAutocompleteField === char}
                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                <ul
                  class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
                  onmousedown={(event) => event.preventDefault()}
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
            <button
              class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer"
              onclick={() => deleteCharacteristic(char)}>&times;</button
            >
          </div>
        {/if}
      {/each}

      {#if $isEditing}
        <button
          class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none self-end mt-2"
          onclick={() => (showAddCharacteristicModal = true)}
        >
          {$_("search_page.button.add")}
        </button>
      {/if}
    </form>
  {/if}
</div>
<AddCharacteristicModal
  isOpen={showAddCharacteristicModal}
  onClose={() => (showAddCharacteristicModal = false)}
  selectedSubGroup={$selectedSubGroup}
  on:added={async (e) => {
    const sub = $selectedSubGroup;
    await findCharacteristics(sub);
  }}
/>

</div>
