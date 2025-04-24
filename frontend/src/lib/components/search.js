import { get } from "svelte/store";
import { selectedGroup, showCategories, showSubGroups, alternatives, 
    subGroups, selectedSubGroup, showChars, charValues, selectedCategoryIndex, currentSuppliers, selectedSupplierIndex, categories, characteristics} from "$lib/stores/searches";
import { apiFetch } from "$lib/utils/fetch";

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
    const previousGroup = get(selectedGroup);
    selectedGroup.set(group);
    showSubGroups.set(true);
    showCategories.set(true);
    
    currentSuppliers.set([]);
    selectedSupplierIndex.set("");
    selectedCategoryIndex.set("");
    alternatives.set([]);

    selectedSubGroup.set("");

    showChars.set(false);
    characteristics.set([]);
    charValues.set([]);

    let subGroups_all_info = [];
    try {
    const response = await apiFetch(`/api/subgroups/group/${group}`);
    const response_2 = await apiFetch(`/api/category/group/${group}`);

    if (!response.ok) {
        subGroups.set([]);
        throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
    }
    if (!response_2.ok) {
        subGroups.set([]);
        categories.set([]);
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

/**
 * Gets characteristics and categories of subgroup with the name subGroup
 * @param subGroup
 */
export async function findCharacteristics(subGroup) {
    if (subGroup == "none") {
        selectedSubGroup.set("");
        findSubGroups(get(selectedGroup));
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
    }

    characteristics.set(subgroup.subGroupCharacteristics.map(c => c.name));
    return;
}