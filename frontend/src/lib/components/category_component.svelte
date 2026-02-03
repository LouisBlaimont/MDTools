<script>
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { isAdmin } from "$lib/stores/user_stores";
    import { PUBLIC_API_URL } from "$env/static/public";
    import { isEditing, reload, selectedGroup, selectedSubGroup, selectedCategoryIndex,
        hoveredCategoryIndex, charValues, categories, currentSuppliers,showCategories,
        errorMessage, hoveredCategoryImageIndex, alternatives,selectedSupplierIndex,
        findSubGroupsStore,findCharacteristicsStore} from "$lib/stores/searches";
    import EditButton from "../../routes/searches/EditButton.svelte";
    import EditCategoryButton from "../../routes/searches/EditCategoryButton.svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { modals } from "svelte-modals";
    import addCategoryModal from "$lib/modals/addCategoryModal.svelte";
    import { _ } from "svelte-i18n";
    import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";

    
    let imageContainerRef;
    let imageRefs = [];
    let findSubGroups = $findSubGroupsStore;
    let findCharacteristics = $findCharacteristicsStore;

    function imageRef(node, index) {
      imageRefs[index] = node;
      return {
          destroy() {
              imageRefs[index] = null;
          }
      };
    }

  // Sorting state
  let sortKey = null; // ex: "function", "name", "externalCode", ...
  let sortDir = 1;    // 1 = asc, -1 = desc
  let selectedCategoryId = null;

  // Columns that should be treated as numeric (or numeric-like)
  const numericKeys = new Set(["dimOrig", "lenAbrv", "externalCode"]);

  function extractNumber(value) {
    if (value === null || value === undefined) return NaN;
    const s = String(value).replace(",", "."); // safety
    const match = s.match(/-?\d+(\.\d+)?/);
    return match ? parseFloat(match[0]) : NaN;
  }

  function getSortableValue(row, key) {
    const v = row?.[key];

    // If you know some columns contain units like "120 mm", handle them here
    if (key === "dimOrig" || key === "lenAbrv") {
      return extractNumber(v);
    }

    // externalCode can be numeric or string, choose numeric if possible
    if (key === "externalCode") {
      const n = extractNumber(v);
      return Number.isNaN(n) ? (v ?? "") : n;
    }

    return v ?? "";
  }

  function compareValues(a, b, key) {
    const va = getSortableValue(a, key);
    const vb = getSortableValue(b, key);

    // Numeric compare when both are numbers (or key is numeric-like)
    if (numericKeys.has(key)) {
      const na = typeof va === "number" ? va : extractNumber(va);
      const nb = typeof vb === "number" ? vb : extractNumber(vb);

      // Put NaN at the end in ascending
      const aNan = Number.isNaN(na);
      const bNan = Number.isNaN(nb);
      if (aNan && bNan) return 0;
      if (aNan) return 1;
      if (bNan) return -1;

      return na - nb;
    }

    // String compare (case-insensitive, natural order)
    const sa = String(va);
    const sb = String(vb);
    return sa.localeCompare(sb, undefined, { sensitivity: "base", numeric: true });
  }

  function setSort(key) {
    if (sortKey === key) {
      sortDir = -sortDir; // toggle asc/desc
    } else {
      sortKey = key;
      sortDir = 1; // default asc
    }
  }

  // This is the list you render in the table + images (sorted copy)
  $: displayedCategories = (() => {
    const list = Array.isArray($categories) ? $categories.slice() : [];
    if (!sortKey) return list;
    return list.sort((a, b) => compareValues(a, b, sortKey) * sortDir);
  })();

  // Keep selection highlight stable even after sorting
  $: if (selectedCategoryId !== null) {
    const i = displayedCategories.findIndex((r) => r.id === selectedCategoryId);
    if (i !== -1 && $selectedCategoryIndex !== i) {
      selectedCategoryIndex.set(i);
    }
  }

  // --- Update your selection functions to use displayedCategories ---
  async function selectCategoryWithChar(index) {
    await selectCategory(index);
    selectedCategoryIndex.set(index);

    const cat = displayedCategories[index];
    const catId = cat.id;

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
    } else {
      console.warn(`Element at index ${index} is not available or not a valid HTMLElement.`);
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

      if (!$isAdmin && !isWebmaster) {
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
</script>


<div class="flex">
  <div class="flex-[3] max-h-[70vh] box-border ml-3 overflow-y-auto">
    <!-- TABLE OF CATEGORIES CORRESPONDING TO RESEARCH  -->
    {#if $showCategories}
      <table id="tools-table" data-testid="categories-table" class="w-full border-collapse">
        <thead class="bg-teal-400">
            {#if $selectedSubGroup}
              <tr class="bg-white text-teal-400">
                {#if $isEditing}
                  <th colspan="5" class="text-center py-2">
                    <button
                      class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
                      onclick={()=>modals.open(addCategoryModal, {fromSearches : true})}
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
                onclick={() => setSort("subGroupName")}
              >
                {$_('category_component.title.table.subgroup')}
              </th>

              {/if}
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("externalCode")}
              >
                {$_('category_component.title.table.external_code')}
              </th>
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("function")}
              >
                {$_('category_component.title.table.function')}
              </th>
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("author")}
              >
                {$_('category_component.title.table.author')}
              </th>
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("name")}
              >
                {$_('category_component.title.table.name')}
              </th>
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("design")}
              >
                {$_('category_component.title.table.design')}
              </th>
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("dimOrig")}
              >
                {$_('category_component.title.table.dim_orig')}
              </th>
              <th
                class="text-center border border-solid border-[black] cursor-pointer select-none"
                onclick={() => setSort("lenAbrv")}
              >
                {$_('category_component.title.table.dimension')}
              </th>
            </tr>
        </thead>
        <tbody>
          {#each displayedCategories as row, index}
            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
            <tr
              class:bg-[cornflowerblue]={$selectedCategoryIndex === index}
              class:bg-[lightgray]={$hoveredCategoryIndex === index &&
                $selectedCategoryIndex !== index}
              onclick={() => selectCategory(index)}
              ondblclick={() => selectCategoryWithChar(index)}
              onmouseover={() => hoveredCategoryIndex.set(index)}
              onmouseout={() => hoveredCategoryIndex.set(null)}
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

  <!-- PICTURES OF THE CATEGORIES -->
  <div class="flex-[1] max-h-[70vh] overflow-y-auto ml-3 max-w-[150px]" bind:this={imageContainerRef}>
      <div class="border bg-teal-400 mb-[5px] font-sans text-base py-0.5 px-2">
          <span class="p-1">{$_('category_component.pictures.title')}</span>
      </div>
      {#each displayedCategories as row, index}
          <!-- svelte-ignore a11yå_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
          <!-- svelte-ignore a11y_mouse_events_have_key_events -->
          <!-- svelte-ignore a11y_click_events_have_key_events -->
           <div class="flex justify-center items-center border-[1px] border-solid border-[lightgray] mb-0.5">
          <img
              use:imageRef={index}
              alt="tool{row.id}"
              src={row.picturesId && row.picturesId[0]
                    ? PUBLIC_API_URL + `/api/pictures/${row.picturesId[0]}`: "/default/no_picture.png"}
              onclick= {() => modals.open(BigPicturesModal, { instrument: row, index: index , isInstrument: false, isAlternative : false })}
              onmouseover={() => (hoveredCategoryImageIndex.set(index))}
              onmouseout={() => (hoveredCategoryImageIndex.set(null))}
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
