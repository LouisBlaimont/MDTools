import { selectedGroup, selectedSubGroup, selectedCategoryIndex, categories, alternatives } from "$lib/stores/searches";
import { apiFetch } from "$lib/utils/fetch"; 
import { get } from "svelte/store";

export async function selectAlternative(row, index){
    const categoryId = row.categoryId;
    const instrumentId = row.id;
    const group = get(selectedGroup);
    const subgroup = get(selectedSubGroup)
    window.open(`/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup)}&category=${encodeURIComponent(categoryId)}&instrument=${encodeURIComponent(instrumentId)}`, '_blank');
    return;
}

export async function removeAlternative(instrId){
    const currentCategories = get(categories);
    const currentSelectedCat = get(selectedCategoryIndex);
    const categoryId = currentCategories[currentSelectedCat].id;
    return apiFetch(`/api/alternatives/${instrId}/category/${categoryId}`,{
        method : "DELETE",
    })
    .then((response) =>{
        if(!response.ok){
            throw new Error(`Failed to remove alternative ${instrId} within category ${categoryId}`);
        }
        return response.json();
    })
    .then((result) => {
        alternatives.update(currentAlt => currentAlt.filter(alt => alt.id !== instrId));
    })
    .catch((error) => {
        console.log("Error :", error);
    });
}