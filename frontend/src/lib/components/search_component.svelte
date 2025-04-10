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
    findSubGroupsStore, findCharacteristicsStore, alternatives, hoveredAlternativeIndex, categories_pageable, keywords2, keywordsResult2}
    from "$lib/stores/searches";    
    import {startResize, resize, stopResize} from "$lib/resizableUtils.js";
    import { apiFetch } from "$lib/utils/fetch";
    import { browser } from "$app/environment";
    import { derived } from 'svelte/store';

  let page_size = 2;

  export const updateURLParams = derived(
    [selectedGroup, selectedSubGroup],  // Dependencies
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
    const url = new URL(window.location);
    const groupParam = url.searchParams.get("group");
    const subgroupParam = url.searchParams.get("subgroup");

    if (groupParam && groupParam !== "none") {
      // We'll use a setTimeout to ensure stores are initialized
      setTimeout(() => {
        findSubGroups(groupParam);
        if (subgroupParam && subgroupParam !== "none") {
          setTimeout(() => {
            findCharacteristics(subgroupParam);
          }, 100);
        }
      }, 100);
    }
  });

  /**
   * Gets characteristics and categories of subgroup with the name subGroup
   * @param subGroup
   */
  export async function findCharacteristics(subGroup, page = 0) {
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
      const response_2 = await apiFetch(
        `/api/category/subgroup/${subGroup}?size=${page_size}&page=${page}`
      );

      if (!response.ok) {
        throw new Error(`Failed to fetch characteristics : ${response.statusText}`);
      }
      if (!response_2.ok) {
        throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subgroup = await response.json();
      let cat_page = await response_2.json();
      if ($categories && $categories.length > 0 && page > 0) {
        categories.set([...$categories, ...cat_page.content]);
      } else {
        categories.set(cat_page.content);
      }
      delete cat_page.content;
      categories_pageable.set(cat_page);
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }

    characteristics.set(subgroup.subGroupCharacteristics.map(c => c.name));
    return;
  }

  /**
   * Filters the categories depending on the input of the user.
   */
  function searchByCharacteristics() {
    let char_vals = [];
    for (let i = 0; i < $characteristics.length; i++) {
      if ($characteristics[i] === "Function" || $characteristics[i] === "Name") {
        continue;
      }
      if ($characteristics[i] === "Length" && $charValues[$characteristics[i]]) {
        let char = {
          name: $characteristics[i],
          value: $charValues[$characteristics[i]] + "cm",
          abrev: "",
        };
        char_vals.push(char);
      } else if ($charValues[$characteristics[i]]) {
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
      })
      .catch((error) => {
        console.error("Error :", error);
      });
  }

  /**
   * Delete the characteristic value given by id.
   * @param id
   */
  function deleteCharacteristic(id) {
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
  function deleteAllCharacteristics() {
    for (let i = 0; i < $characteristics.length; i++) {
      const characteristic = $characteristics[i];

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

  /**
   * Gets subgroups of group and their categories
   * @param group
   */
  export async function findSubGroups(group, page = 0) {
    if (group == "none") {
      selectedGroup.set("");
      selectedSubGroup.set("");
      selectedCategoryIndex.set(null);
      selectedSupplierIndex.set(null);
      showCategories.set(false);
      categories.set([]);
      categories_pageable.set(null);
      showSubGroups.set(false);
      subGroups.set([]);
      showChars.set(false);
      charValues.set([]);
      characteristics.set([]);
      currentSuppliers.set([]);
      alternatives.set([]);
      return;
    }
    selectedGroup.set(group);
    showSubGroups.set(true);
    showCategories.set(true);

    currentSuppliers.set([]);
    selectedSupplierIndex.set("");
    selectedCategoryIndex.set("");
    alternatives.set([]);

    showChars.set(false);
    characteristics.set([]);
    charValues.set([]);

    let subGroups_all_info = [];
    try {
      const response = await apiFetch(`/api/subgroups/group/${group}`);
      const response_2 = await apiFetch(
        `/api/category/group/${group}?size=${page_size}&page=${page}`
      );

      if (!response.ok) {
        throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
      }
      if (!response_2.ok) {
        throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subGroups_all_info = await response.json();
      let cat_page = await response_2.json();
      if ($categories && $categories.length > 0 && page > 0) {
        categories.set([...$categories, ...cat_page.content]);
      } else {
        categories.set(cat_page.content);
      }
      delete cat_page.content;
      categories_pageable.set(cat_page);
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
    subGroups.set(subGroups_all_info.map((subgroup) => subgroup.name));
    return;
  }

  findSubGroupsStore.set(findSubGroups);
  findCharacteristicsStore.set(findCharacteristics);


  /* Dealing with the search by keywords */
  let showKeywordsResult = $state(false);
  let clickTimeout = 500;

  // goto searches with the selected instrument found by keywords
  async function moveToSearchesBis(group, subgroup, catId, instrumentId) {
    clearTimeout(clickTimeout);
    // handle when the instrument has no category
    if (catId == null) {
      await modals.open(editInstrumentModal, { 
        instrument,
        message: "You need to assign a category to this instrument!" 
      });
    }
    keywords2.set(null);
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup)}&category=${encodeURIComponent(catId)}&instrument=${encodeURIComponent(instrumentId)}`
    );
    reload.set(true);
  }

  // function to handle the keyword inputs and calling endpoint
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
      }
      else {
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

  // handles the delay between to search by keywords
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

  // get category, group and subgroup from selected instrument, then call moveToSearchesBis
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
</script>

<div class="flex-[1.3] h-full ml-3 p-2 bg-gray-100 rounded-lg shadow-md">
  <form class="space-y-5">
    <div class="relative w-full">
      <label for="id_search_keyword" class="font-semibold">
        Recherche par mot(s) clé(s):
      </label>
      
      <input
        type="text"
        name="search_keyword"
        id="id_search_keyword"
        autocomplete="off"
        placeholder="Entrez un mot clé"
        class="p-2 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 w-3/5 mb-1 "
        bind:value={$keywords2}
        oninput={searchByKeywords}
      />
    
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

  <label class="font-semibold">Recherche par caractéristiques:</label>
  <div class="flex items-center">
    <label class="w-2/5 mt-2 mb-2" for="groupOptions">Groupe:</label>
    <select
      id="groupOptions"
      bind:value={$selectedGroup}
      onchange={(e) => {
        selectedSubGroup.set("");
        findSubGroups(e.target.value);
      }}
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
        onchange={(e) => findCharacteristics(e.target.value)}
      >
        <option value="none">---</option>
        {#each $subGroups as subGroup}
          <option value={subGroup}>{subGroup}</option>
        {/each}
      </select>
    </div>
  {/if}

  {#if $showChars}
    <form class="flex flex-col w-full gap-2.5" onsubmit={searchByCharacteristics} preventdefault>
      <div class="flex gap-2 mb-2 mt-4">
        <button
          type="submit"
          class="w-[90px] border border-gray-400 rounded bg-gray-400 border-solid border-[black] rounded-sm"
          >Chercher</button
        >
        <button
          type="button"
          class="w-[90px] border border-red-700 rounded bg-red-700 border-solid border-[black] rounded-sm"
          onclick={deleteAllCharacteristics}>Tout effacer</button
        >
      </div>
      {#each $characteristics as char}
        <div class="flex items-center">
          <label for={char} class="w-2/5">{char}:</label>
          <input
            type={char === "Length" ? "number" : "text"}
            class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black] mb-2"
            id={char}
            name={char}
            data-testid={char}
            bind:value={$charValues[char]}
          />
          <button
            class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer"
            onclick={() => deleteCharacteristic(char)}>&times;</button
          >
        </div>
      {/each}
    </form>
  {/if}
</div>
