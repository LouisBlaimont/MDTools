import { get } from "svelte/store";
import { selectedGroup, showCategories, showSubGroups, alternatives, 
    subGroups, selectedSubGroup, showChars, charValues, selectedCategoryIndex, currentSuppliers, selectedSupplierIndex, categories,
    characteristics, categories_pageable, categoriesSort, categoriesPage, categoriesSize, categoriesIsPaged} from "$lib/stores/searches";
import { apiFetch } from "$lib/utils/fetch";

    /**
     * Gets subgroups of group and their categories
     * @param group
     */
    /**
     * Loads subgroups for the selected group **without loading categories**.
     *
     * Why:
     * - Loading categories at the group level can return a huge list and recreate the memory/perf issue.
     * - Categories are now loaded **only when a subgroup is selected**, using the paged endpoint.
     *
     * Behavior:
     * - If group === "none": reset all search state (same as before).
     * - Else: fetch subgroups, reset category-related UI/state, and keep categories empty.
     *
     * @param {string} group - Group name (or "none")
     * @throws {Error} When the subgroups request fails.
     */
    export async function findSubGroups(group) {
    if (group === "none") {
        selectedGroup.set("");
        selectedSubGroup.set("");
        selectedCategoryIndex.set(null);
        selectedSupplierIndex.set(null);

        showCategories.set(false);
        categories.set([]);
        categories_pageable.set({
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: get(categoriesSize) || 50
        });
        categoriesIsPaged.set(true);
        categoriesPage.set(0);

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

    // Show only subgroups at group level (categories will be shown only after subgroup selection)
    showSubGroups.set(true);
    showCategories.set(false);

    // Reset right-side panels and selections
    currentSuppliers.set([]);
    selectedSupplierIndex.set("");
    selectedCategoryIndex.set("");
    alternatives.set([]);

    // Reset subgroup + characteristics view
    selectedSubGroup.set("");
    showChars.set(false);
    characteristics.set([]);
    charValues.set([]);

    // Reset categories state (important: do NOT fetch group categories)
    categories.set([]);
    categories_pageable.set({
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: get(categoriesSize) || 50
    });
    categoriesIsPaged.set(true);
    categoriesPage.set(0);

    try {
        const response = await apiFetch(`/api/subgroups/group/${group}`);
        if (!response.ok) {
        subGroups.set([]);
        throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
        }

        const subGroups_all_info = await response.json();
        subGroups.set(subGroups_all_info.map((subgroup) => subgroup.name));
    } catch (error) {
        console.error(error);
        errorMessage.set(error.message);
    }
    }

/**
 * Loads characteristics for a subgroup and loads categories ONLY at subgroup level (paged).
 *
 * Why:
 * - We do NOT load categories at group level (too heavy).
 * - When a subgroup is selected, we enable categories UI and load the first page.
 *
 * Behavior:
 * - If subGroup === "none": reset subgroup selection and reload subgroups list.
 * - Else: fetch subgroup details, set characteristics, enable paged mode, load page 0.
 *
 * @param {string} subGroup - Subgroup name (or "none")
 * @throws {Error} When subgroup fetch fails or categories page fetch fails.
 */
export async function findCharacteristics(subGroup) {
  if (subGroup === "none") {
    selectedSubGroup.set("");
    showChars.set(false);

    // Important: categories should be hidden again because we're back to group level
    showCategories.set(false);
    categories.set([]);
    categories_pageable.set({
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: get(categoriesSize) || 50
    });

    await findSubGroups(get(selectedGroup));
    return;
  }

  selectedSubGroup.set(subGroup);

  // We are now at subgroup level => categories must be shown
  showCategories.set(true);
  showChars.set(true);

  // reset selection + right panels
  charValues.set([]);
  selectedCategoryIndex.set("");
  currentSuppliers.set([]);
  selectedSupplierIndex.set("");
  alternatives.set([]);

  try {
    const response = await apiFetch(`/api/subgroups/${encodeURIComponent(subGroup)}`);
    if (!response.ok) {
      throw new Error(`Failed to fetch characteristics: ${response.statusText}`);
    }

    const subgroup = await response.json();
    characteristics.set(subgroup.subGroupCharacteristics.map(c => c.name));

    // default paged mode when entering subgroup
    categoriesIsPaged.set(true);
    categoriesPage.set(0);

    // load first page
    await loadCategoriesPage();
  } catch (error) {
    console.error(error);
    errorMessage.set(error.message);
  }
}

/**
 * Loads one page of categories for the currently selected subgroup.
 *
 * Uses:
 * - categoriesPage, categoriesSize, categoriesSort
 * Updates:
 * - categories_pageable + categories
 */
export async function loadCategoriesPage() {
  const sub = get(selectedSubGroup);
  if (!sub) return;

  const page = get(categoriesPage);
  const size = get(categoriesSize);
  const sort = get(categoriesSort);

  const response = await apiFetch(
    `/api/category/subgroup/${encodeURIComponent(sub)}?page=${page}&size=${size}&sort=${encodeURIComponent(sort)}`
  );

  if (!response.ok) {
    categories.set([]);
    categories_pageable.set({
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: 0,
      size: get(categoriesSize) || 50
    });
    throw new Error(`Failed to fetch paged categories: ${response.statusText}`);
  }

  const result = await response.json();
  categories_pageable.set(result);
  categories.set(result.content || []);
}
