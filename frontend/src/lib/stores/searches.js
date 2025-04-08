import { writable } from 'svelte/store';

export const isEditing = writable(false);

export const reload = writable(false);

export const sharedData = writable({ name: 'Alice' });

export let selectedCategoryIndex = writable(null);
export let selectedGroupIndex = writable(null);
export let selectedSubGroupIndex = writable(null);
export let currentSuppliers = writable([]);
export let charValues = writable({});
export let groups = writable([]);
export let subGroups = writable([]);
export let characteristics = writable([]);
export let instrumentCharacteristics = writable([]);
export let category_to_addInstrument = writable(null);
export let categories = writable([]);
export let hoveredSupplierIndex = writable(null);
export let hoveredAlternativeIndex = writable(null);
export let hoveredSupplierImageIndex = writable(null);
export let selectedSupplierIndex = writable(null);
export let groups_summary = writable([]);  
export let hoveredCategoryIndex = writable("");
export let hoveredCategoryImageIndex = writable(null);  
export let selectedGroup = writable(null);
export let selectedSubGroup = writable("");
export let showSubGroups = writable(false);
export let showCategories = writable(false);
export let showChars = writable(false);
export let errorMessage = writable(null);
export let toolToAddRef = writable(null);
export let quantity = writable(1);
export let orderItems = writable([]);
export let ordersNames = writable([]);
export let findSubGroupsStore = writable(null);
export let findCharacteristicsStore = writable(null);
export let findOrdersNamesStore = writable(null);
export let selectedOrderId = writable(null);
export let userId = writable(1); //default value because for now no user but change!!
export let orders = writable(null);
export let alternatives = writable([]);

export let addingAlt = writable(false);
export let altToAdd = writable([]);
export let removingAlt = writable(false);
export let altToRemove = writable([]);