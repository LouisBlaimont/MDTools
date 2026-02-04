<script>
  import { get } from "svelte/store";
  import { PUBLIC_API_URL } from "$env/static/public";
  import { modals } from "svelte-modals";
  import { _ } from "svelte-i18n";
  import { apiFetch } from "$lib/utils/fetch";
  import { isAdmin } from "$lib/stores/user_stores";

  import {
    isEditing,
    selectedGroup,
    selectedSubGroup,
    selectedCategoryIndex,
    hoveredCategoryIndex,
    hoveredCategoryImageIndex,
    charValues,
    categories,
    currentSuppliers,
    showCategories,
    errorMessage,
    alternatives,
    selectedSupplierIndex,

    // pagination/sort state
    categoriesIsPaged,
    categoriesSort,
    categoriesPage,
    categoriesSize,
    categories_pageable
  } from "$lib/stores/searches";

  import EditCategoryButton from "../../routes/searches/EditCategoryButton.svelte";
  import addCategoryModal from "$lib/modals/addCategoryModal.svelte";
  import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";

  import { loadCategoriesPage } from "./search.js";

  let imageContainerRef;
  let imageRefs = [];

  function imageRef(node, index) {
    imageRefs[index] = node;
    return {
      destroy() {
        imageRefs[index] = null;
      }
    };
  }

  // --- SERVER SORT (paged mode) ---
  // Note: only used for server sort (because pagination needs global sort)
  let sortKey = "name";     // default
  let sortDir = "asc";      // "asc" | "desc"

  function setSort(key) {
    // toggle or set
    if (sortKey === key) {
      sortDir = (sortDir === "asc") ? "desc" : "asc";
    } else {
      sortKey = key;
      sortDir = "asc";
    }

    // Paged mode -> reload from backend with new global sort
    if ($categoriesIsPaged) {
      categoriesSort.set(`${key},${sortDir}`);
      categoriesPage.set(0);
      loadCategoriesPage().catch((e) => {
        console.error(e);
        errorMessage.set(e.message);
      });
      return;
    }

    // Filtered mode -> local sort is handled by displayedCategories reactive block.
    // We still "touch" categories to force an update if Svelte doesn't rerender.
    categories.update((arr) => Array.isArray(arr) ? [...arr] : []);
  }

  /**
   * Extracts the first numeric value from a string.
   * Supports formats like: "100", "100 mm", "12.5", "12,5", "Ø 10", "10x20".
   *
   * @param {unknown} v
   * @returns {number|null} parsed number or null if none
   */
  function extractNumber(v) {
    if (v == null) return null;

    // already a number
    if (typeof v === "number" && Number.isFinite(v)) return v;

    const s = v.toString().trim();

    // normalize comma decimals: "12,5" -> "12.5"
    const normalized = s.replace(",", ".");

    // capture first number
    const m = normalized.match(/-?\d+(\.\d+)?/);
    if (!m) return null;

    const n = Number(m[0]);
    return Number.isFinite(n) ? n : null;
  }

  /**
   * String compare (case-insensitive, null-safe).
   */
  function compareNullableString(a, b, dir) {
    const A = (a ?? "").toString().toLowerCase();
    const B = (b ?? "").toString().toLowerCase();

    if (A < B) return dir === "asc" ? -1 : 1;
    if (A > B) return dir === "asc" ? 1 : -1;
    return 0;
  }

  /**
   * Numeric compare based on extracted numbers (null-safe).
   * - null/invalid numbers go to the end
   * - ties fall back to string compare
   */
  function compareNullableNumber(a, b, dir) {
    const na = extractNumber(a);
    const nb = extractNumber(b);

    const aMissing = na == null;
    const bMissing = nb == null;

    if (aMissing && bMissing) return compareNullableString(a, b, dir);
    if (aMissing) return 1;   // push missing to end
    if (bMissing) return -1;

    if (na < nb) return dir === "asc" ? -1 : 1;
    if (na > nb) return dir === "asc" ? 1 : -1;

    // equal numbers -> stable-ish fallback
    return compareNullableString(a, b, dir);
  }


  /**
   * Displayed list:
   * - paged mode => backend already returns a globally sorted page
   * - filtered mode => we sort locally (in-memory) because list is smaller
   */
  $: displayedCategories = (() => {
    const list = Array.isArray($categories) ? [...$categories] : [];

    // paged mode => already sorted by server
    if ($categoriesIsPaged) return list;

    // filtered mode => local sort
    const isNumericKey = (k) => k === "lenAbrv" || k === "dimOrig";

    return list.sort((r1, r2) => {
      const a = r1?.[sortKey];
      const b = r2?.[sortKey];

      if (isNumericKey(sortKey)) {
        return compareNullableNumber(a, b, sortDir);
      }
      return compareNullableString(a, b, sortDir);
    });
  })();



  // keep selection stable (if you ever reload/sort pages)
  let selectedCategoryId = null;
  $: if (selectedCategoryId !== null) {
    const i = displayedCategories.findIndex((r) => r.id === selectedCategoryId);
    if (i !== -1 && $selectedCategoryIndex !== i) {
      selectedCategoryIndex.set(i);
    }
  }

  async function selectCategoryWithChar(index) {
    await selectCategory(index);
    selectedCategoryIndex.set(index);

    const cat = displayedCategories[index];
    const catId = cat.id;

    // keep only selected category in store (your original behavior)
    categories.set([cat]);

    try {
      const response = await apiFetch(`/api/category/${catId}`);
      if (!response.ok) throw new Error("Failed to fetch characteristics of category");

      const categoryChars = await response.json();

      charValues.update((currentValues) => {
        let updatedValues = { ...currentValues };

        for (let i = 0; i < categoryChars.length; i++) {
          let key = categoryChars[i].name;
          let value = categoryChars[i].value;

          if (key === "Length" && typeof value === "string") {
            value = value.replace(/[^\d.]/g, "");
          }

          const element = document.getElementById(key);
          if (element) element.value = value;

          updatedValues[key] = value;
        }
        return updatedValues;
      });
    } catch (error) {
      console.log(error);
      errorMessage.set(error.message);
    }
  }

  async function selectCategory(index) {
    currentSuppliers.set([]);
    alternatives.set([]);
    selectedCategoryIndex.set(index);
    selectedSupplierIndex.set("");

    if (imageRefs[index] instanceof HTMLElement) {
      imageRefs[index].scrollIntoView({ behavior: "smooth", block: "start" });
    }

    const cat = displayedCategories[index];
    const categoryId = cat.id;
    selectedCategoryId = categoryId;

    try {
      const response = await apiFetch(`/api/category/instruments/${categoryId}`);
      let response2;

      if ($isAdmin) response2 = await apiFetch(`/api/alternatives/admin/category/${categoryId}`);
      else response2 = await apiFetch(`/api/alternatives/user/category/${categoryId}`);

      if (!response.ok) throw new Error("Failed to fetch instruments of category");

      const answer = await response.json();
      let supplierArray = Array.isArray(answer) ? answer : [answer];

      // NOTE: you used isWebmaster but it's not defined in your snippet.
      // I'm keeping your logic but removing isWebmaster check to avoid crash.
      // If you have isWebmaster somewhere else, re-add it.
      if (!$isAdmin) {
        for (let i = 0; i < supplierArray.length; i++) {
          let supp = supplierArray[i].supplier;
          const getSupplier = await apiFetch(`/api/supplier/name/${supp}`);
          const gotSupplier = await getSupplier.json();

          if (gotSupplier.soldByMd != true) {
            for (let j = i; j < supplierArray.length - 1; j++) {
              supplierArray[j] = supplierArray[j + 1];
            }
            i--;
            supplierArray.splice(supplierArray.length - 1, supplierArray.length);
          }
        }
      }

      currentSuppliers.set(supplierArray);

      if (response2.ok) {
        const answer2 = await response2.json();
        alternatives.set(Array.isArray(answer2) ? answer2 : [answer2]);
      }
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }

  function prevPage() {
    const current = $categories_pageable?.number ?? 0;
    if (current <= 0) return;
    categoriesPage.set(current - 1);
    loadCategoriesPage().catch((e) => {
      console.error(e);
      errorMessage.set(e.message);
    });
  }

  function nextPage() {
    const current = $categories_pageable?.number ?? 0;
    const totalPages = $categories_pageable?.totalPages ?? 0;
    if (current >= totalPages - 1) return;
    categoriesPage.set(current + 1);
    loadCategoriesPage().catch((e) => {
      console.error(e);
      errorMessage.set(e.message);
    });
  }

  function changeSize(e) {
    const newSize = parseInt(e.target.value, 10);
    categoriesSize.set(newSize);
    categoriesPage.set(0);
    loadCategoriesPage().catch((e2) => {
      console.error(e2);
      errorMessage.set(e2.message);
    });
  }
</script>

<div class="flex">
  <div class="flex-[3] max-h-[70vh] box-border ml-3 overflow-y-auto">

    {#if $showCategories}

      <!-- PAGINATION BAR (only when paged mode) -->
      {#if $categoriesIsPaged}
        <div class="flex items-center justify-between mb-2 gap-2 bg-white rounded-md p-2 shadow-sm">
          <div class="text-sm text-gray-700">
            {$categories_pageable.totalElements} results
          </div>

          <div class="flex items-center gap-2">
            <!-- svelte-ignore a11y_label_has_associated_control -->
            <label class="text-sm">Rows</label>
            <select class="p-1 border rounded" on:change={changeSize} bind:value={$categoriesSize}>
              <option value="50">50</option>
              <option value="100">100</option>
              <option value="250">250</option>
              <option value="1000">1000</option>
            </select>

            <button
              class="px-2 py-1 border rounded disabled:opacity-50"
              disabled={($categories_pageable.number ?? 0) <= 0}
              on:click={prevPage}
            >
              Prev
            </button>

            <span class="text-sm">
              Page {($categories_pageable.number ?? 0) + 1} / {$categories_pageable.totalPages ?? 0}
            </span>

            <button
              class="px-2 py-1 border rounded disabled:opacity-50"
              disabled={($categories_pageable.number ?? 0) >= (($categories_pageable.totalPages ?? 0) - 1)}
              on:click={nextPage}
            >
              Next
            </button>
          </div>
        </div>
      {/if}

      <table id="tools-table" data-testid="categories-table" class="w-full border-collapse">
        <thead class="bg-teal-400">
          {#if $selectedSubGroup}
            <tr class="bg-white text-teal-400">
              {#if $isEditing}
                <th colspan="5" class="text-center py-2">
                  <button
                    class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
                    on:click={() => modals.open(addCategoryModal, { fromSearches: true })}
                  >
                    {$_('category_component.admin.button.add_category')}
                  </button>
                </th>
              {/if}
            </tr>
            <tr class="bg-white text-teal-400">
              <th colspan="2" class="text-center pb-1">{$selectedGroup}</th>
              <th colspan="2" class="text-center pb-1">{$selectedSubGroup}</th>
            </tr>
          {:else}
            <tr class="bg-white text-teal-400">
              <th colspan="5" class="text-center pb-1">{$selectedGroup}</th>
            </tr>
          {/if}

          <tr>
            {#if $isEditing && $selectedSubGroup}
              <th class="text-center border border-solid border-[black]"></th>
            {/if}

            {#if !$selectedSubGroup}
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                on:click={() => setSort("subGroupName")}
              >
                {$_('category_component.title.table.subgroup')}
              </th>
            {/if}

            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("externalCode")}>
              {$_('category_component.title.table.external_code')}
            </th>
            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("function")}>
              {$_('category_component.title.table.function')}
            </th>
            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("author")}>
              {$_('category_component.title.table.author')}
            </th>
            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("name")}>
              {$_('category_component.title.table.name')}
            </th>
            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("design")}>
              {$_('category_component.title.table.design')}
            </th>
            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("dimOrig")}>
              {$_('category_component.title.table.dim_orig')}
            </th>
            <th class="text-center border border-solid border-[black] cursor-pointer select-none" on:click={() => setSort("lenAbrv")}>
              {$_('category_component.title.table.dimension')}
            </th>
          </tr>
        </thead>

        <tbody>
          {#each displayedCategories as row, index}
            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
            <tr
              class:bg-[cornflowerblue]={$selectedCategoryIndex === index}
              class:bg-[lightgray]={$hoveredCategoryIndex === index && $selectedCategoryIndex !== index}
              on:click={() => selectCategory(index)}
              on:dblclick={() => selectCategoryWithChar(index)}
              on:mouseover={() => hoveredCategoryIndex.set(index)}
              on:mouseout={() => hoveredCategoryIndex.set(null)}
            >
              {#if $isEditing && $selectedSubGroup}
                <EditCategoryButton category={row} />
              {/if}

              {#if !$selectedSubGroup}
                <td class="text-center border border-solid border-[black]">{row.subGroupName}</td>
              {/if}

              <td class="text-center border border-solid border-[black]">{row.externalCode}</td>
              <td class="text-center border border-solid border-[black]">{row.function}</td>
              <td class="text-center border border-solid border-[black]">{row.author}</td>
              <td class="text-center border border-solid border-[black]">{row.name}</td>
              <td class="text-center border border-solid border-[black]">{row.design}</td>
              <td class="text-center border border-solid border-[black]">{row.dimOrig}</td>
              <td class="text-center border border-solid border-[black]">{row.lenAbrv}</td>
            </tr>
          {/each}
        </tbody>
      </table>
    {/if}
  </div>

  <!-- PICTURES -->
  <div class="flex-[1] max-h-[70vh] overflow-y-auto ml-3 max-w-[150px]" bind:this={imageContainerRef}>
    <div class="border bg-teal-400 mb-[5px] font-sans text-base py-0.5 px-2">
      <span class="p-1">{$_('category_component.pictures.title')}</span>
    </div>

    {#each displayedCategories as row, index}
      <div class="flex justify-center items-center border-[1px] border-solid border-[lightgray] mb-0.5">
        <!-- svelte-ignore a11y_click_events_have_key_events -->
        <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
        <!-- svelte-ignore a11y_mouse_events_have_key_events -->
        <img
          use:imageRef={index}
          alt={"tool" + row.id}
          src={row.picturesId && row.picturesId[0]
            ? PUBLIC_API_URL + `/api/pictures/${row.picturesId[0]}`
            : "/default/no_picture.png"}
          on:click={() => modals.open(BigPicturesModal, { instrument: row, index, isInstrument: false, isAlternative: false })}
          on:mouseover={() => hoveredCategoryImageIndex.set(index)}
          on:mouseout={() => hoveredCategoryImageIndex.set(null)}
          class="{$selectedCategoryIndex === index
            ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
            : ''} {$hoveredCategoryImageIndex === index && $selectedCategoryIndex !== index
            ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image'
            : ''}"
        />
      </div>
    {/each}
  </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>
