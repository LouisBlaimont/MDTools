import { writable } from 'svelte/store';

export const isEditing = writable(false);

export const reload = writable(false);

export const sharedData = writable({ name: 'Alice' });

export let selectedCategoryIndex = writable(null);
export let currentSuppliers = writable([]);
export let charValues = writable({});
export let groups = writable([]);
export let subGroups = writable([]);
export let characteristics = writable([]);
export let categories = writable([]);
export let hoveredSupplierIndex = writable(null);
export let hoveredSupplierImageIndex = writable(null);
export let selectedSupplierIndex = writable(null);
export let groups_summary = writable([]);  
export let hoveredCategoryIndex = writable(null);
export let hoveredCategoryImageIndex = writable(null);  
export let selectedGroup = writable("");
export let selectedSubGroup = writable("");
export let showSubGroups = writable(false);
export let showCategories = writable(false);
export let showChars = writable(false);
export let errorMessage = writable("");
export let toolToAddRef = writable("");
export let quantity = writable("");
// getorder()
export let order = writable(null);