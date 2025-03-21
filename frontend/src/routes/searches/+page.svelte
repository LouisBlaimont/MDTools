<script>
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { preventDefault } from "svelte/legacy";
  import { get } from "svelte/store";
  import { isEditing, reload, groups_summary, groups, 
    errorMessage, findSubGroupsStore, findCharacteristicsStore, findOrdersNamesStore } from "$lib/stores/searches";
  import { user, isAdmin } from "$lib/stores/user_stores";
  import EditButton from "./EditButton.svelte";
  import EditCategoryButton from "./EditCategoryButton.svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { checkRole } from "$lib/rbacUtils";
  import { ROLES } from "../../constants";
  import CategoryComponent from "$lib/components/category_component.svelte";
  import InstrumentComponent from "$lib/components/instrument_component.svelte";
  import OrderComponent from "$lib/components/order_component.svelte";
  import SearchComponent from "$lib/components/search_component.svelte";

  import { modals } from "svelte-modals";
  import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";
  import { apiFetch } from "$lib/utils/fetch.js";

  /**
   * Display the characteristic values of the category at line index in the table.
   * Update categories to have only the selected one.
   * @param index
   */
  async function selectCategoryWithChar(index) {
    selectCategory(index);
    selectedCategoryIndex = index;
    let cat = categories[selectedCategoryIndex];
    let catId = cat.id;
    categories = [cat];

    try{
      const response = await apiFetch(`/api/category/${catId}`);
      if(!response.ok){
        throw new Error("Failed to fetch characteristics of category");
      }
      const categoryChars = await response.json();
      for (let i = 0; i < categoryChars.length; i++) {
        if (categoryChars[i].name === "Length") {
          const len_val = categoryChars[i].value.replace(/[^\d.]/g, "");
          document.getElementById(categoryChars[i].name).value = len_val;
          charValues[categoryChars[i].name] = len_val;
        } else {
          document.getElementById(categoryChars[i].name).value = categoryChars[i].value;
          charValues[categoryChars[i].name] = categoryChars[i].value;
        }
      }
    }catch(error){
      console.error(error)
      errorMessage.set(error.message);
    }
    return;
  }

  /**
   * Gets the suppliers of the category given by the line index in the table
   * @param index
   */
  async function selectCategory(index) {
    selectedCategoryIndex = index;

    // selecting the categoryId
    const cat = categories[selectedCategoryIndex];
    const categoryId = cat.id;

    try{
      const response = await apiFetch(`/api/category/instruments/${categoryId}`);
      if (!response.ok){
        throw new Error("Failed to fetch instruments of category");
      }
      const answer = await response.json();
      currentSuppliers = Array.isArray(answer) ? answer : [answer];
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
    return;
  }

  let hoveredSupplierIndex = null;
  let hoveredSupplierImageIndex = null;
  let selectedSupplierIndex = null;

  function selectSupplier(index) {
    selectedSupplierIndex = index;
  }

  function showBigPicture(img) {
    const pannel = document.getElementById("big-category-pannel");
    const overlay = document.getElementById("overlay");
    const picture = document.getElementById("big-category");
    pannel.style.display = "flex";
    overlay.style.display = "block";
    console.log("a " + img);
    picture.src = img;
  }
  function closeBigPicture() {
    const pannel = document.getElementById("big-category-pannel");
    const overlay = document.getElementById("overlay");
    pannel.style.display = "none";
    overlay.style.display = "none";
  }

  /**
   * Delete the characteristic value given by id.
   * @param id
   */
  function deleteCharacteristic(id) {
    const texte = document.getElementById(id);
    texte.value = "";
    charValues[id] = "";
    searchByCharacteristics();
  }
  function deleteAllCharacteristics() {
    for (let i = 0; i < characteristics.length; i++) {
      let texte = document.getElementById(characteristics[i]);
      texte.value = "";
      if (charValues[characteristics[i]]) {
        charValues[characteristics[i]] = "";
      }
    }
    searchByCharacteristics();
  }

  // let groups_summary = [];
  // let groups = [];
  export let subGroups = [];
  export let characteristics = [];
  export let categories = [];
  let selectedGroup = "";
  let selectedSubGroup = "";
  export let showSubGroups = false;
  export let showCategories = false;
  export let showChars = false;
  // let errorMessage = "";

  /**
   * Fetches the group summary.
   */
  async function getGroupsSummary() {
    try {
      const response = await apiFetch("/api/groups/summary");

      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }

      groups_summary.set(await response.json());
      groups.set($groups_summary.map(($group) => $group.name));
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }

  let findOrdersNames = null;
  // let findSubGroups = null;
  let findCharacteristics = null;
  let initialized = false;
  /**
   * Gets subgroups of group and their categories
   * @param group
   */
  async function findSubGroups(group) {
    if (group == "none") {
      selectedGroup = "";
      selectedSubGroup = "";
      selectedCategoryIndex = null;
      selectedSupplierIndex = null;
      showCategories = false;
      categories = [];
      showSubGroups = false;
      subGroups = [];
      showChars = false;
      charValues = [];
      characteristics = [];
      currentSuppliers = [];
      return;
    }
    const previousGroup = selectedGroup;
    selectedGroup = group;
    showSubGroups = true;
    showCategories = true;

    currentSuppliers = [];
    selectedSupplierIndex = "";
    selectedCategoryIndex = "";
    // Only reset subgroup if the group has changed
    if (previousGroup !== group) {
      selectedSubGroup = "";
    }
    showChars = false;
    characteristics = [];
    charValues = [];

    let subGroups_all_info = [];
    try {
      const response = await apiFetch(`/api/subgroups/group/${group}`);
      const response_2 = await apiFetch(`/api/category/group/${group}`);

      if (!response.ok) {
        throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
      }
      if (!response_2.ok) {
        throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
      }

      subGroups_all_info = await response.json();
      categories = await response_2.json();
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
    subGroups = subGroups_all_info.map((subgroup) => subgroup.name);
    return;
  }

  /**
   * Fetch when page is rendered.
   */
  async function fetchData() {
    await getGroupsSummary();

    const group = $page.url.searchParams.get("group");
    const subgroup = $page.url.searchParams.get("subgroup");

    if (group) {
      await findSubGroups(group);
      // If a subgroup exists in the URL, load its characteristics
      if (subgroup) {
        await findCharacteristics(subgroup);
      }
    }
    await findOrdersNames();
  }

  function tryFetchData() {
    if (findSubGroups && findCharacteristics && findOrdersNames && !initialized) {
      initialized = true; 
      fetchData();
    }
  }

  onMount(() => {
    findSubGroupsStore.subscribe(value => {
      if (value) {
        findSubGroups = value;
        tryFetchData(); 
      }
    });
    findCharacteristicsStore.subscribe(value => {
      if (value) {
        findCharacteristics = value;
        tryFetchData();
      }
    });
    findOrdersNamesStore.subscribe(value => {
      if(value){
        findOrdersNames = value;
        tryFetchData();
      }
    })
  });

  reload.subscribe((v) => {
    if (v) {
      fetchData();
      reload.set(false);
    }
  });

  function openEditPage(toolId) {
    goto(`/admin/instrument_edit/${toolId}`);
  }

</script>

<svelte:head>
  <title>Recherches</title>
</svelte:head>

<div id="container" class="text-[14px]">
  <div class="flex flex-col gap-[5px] box-border w-full">
    <!-- TOP PART -->
    <div class="flex-[5] flex flex-row mt-3 h-max-[50vh]">
      <!-- FORM OF SEARCHES -->
      <SearchComponent />

      <!-- TABLE AND PICTURES OF CATEGORIES CORRESPONDING TO RESEARCH  -->
      <CategoryComponent />
      
      <!-- TABLE AND PICTURES OF THE SUPPLIERS -->
      <InstrumentComponent />

    </div>

    <!-- BOTTOM PART -->
    <!-- ORDERS  -->
    <OrderComponent />

  </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>
