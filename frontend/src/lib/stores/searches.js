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
export let autocompleteOptions = writable({});
export let instrumentCharacteristics = writable([]);
export let category_to_addInstrument = writable(null);
export let categories = writable([]);
export let hoveredSupplierIndex = writable(null);
export let hoveredAlternativeIndex = writable(null);
export let hoveredInstrumentIndex = writable(null);
export let selectedInstrumentIndex = writable(null);
export let selectedInstrumentHome = writable(null);
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
export let orderItems = writable(null);
export let ordersNames = writable(null);
export let findSubGroupsStore = writable(null);
export let findCharacteristicsStore = writable(null);
export let keywords = writable(null);
export let keywords2 = writable(null);
export let keywords3 = writable(null);
export let keywordsResult = writable(null);
export let keywordsResult2 = writable(null);
export let keywordsResult3 = writable(null);
export let findOrdersNamesStore = writable(null);
export let selectedOrderId = writable(null);
export let orders = writable(null);
export let alternatives = writable([]);
export let categories_pageable = writable({
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 50
});
export let categoriesIsPaged = writable(true);
export let categoriesPage = writable(0);
export let categoriesSize = writable(50);
export let categoriesSort = writable("name,asc");

export let addingAlt = writable(false);
export let altToAdd = writable([]);
export let removingAlt = writable(false);
export let altToRemove = writable([]);