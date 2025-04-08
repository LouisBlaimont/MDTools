<script>
  import { goto } from "$app/navigation";
  import { page } from "$app/stores";
  import { onMount } from "svelte";
  import { preventDefault } from "svelte/legacy";
  import { get } from "svelte/store";
  import { isEditing, reload, groups_summary, groups, 
    errorMessage, findSubGroupsStore, findCharacteristicsStore, findOrdersNamesStore, categories, 
    selectedCategoryIndex, currentSuppliers, alternatives,
    selectedSupplierIndex, charValues} from "$lib/stores/searches";
  import { user, isAdmin } from "$lib/stores/user_stores";
  import EditButton from "./EditButton.svelte";
  import EditInstrumentButton from "./EditInstrumentButton.svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { checkRole } from "$lib/rbacUtils";
  import { ROLES } from "../../constants";
  import CategoryComponent from "$lib/components/category_component.svelte";
  import InstrumentComponent from "$lib/components/instrument_component.svelte";
  import OrderComponent from "$lib/components/order_component.svelte";
  import SearchComponent from "$lib/components/search_component.svelte";
  import {startResizeBoth, startResizeVertical, startResizeLeftToRight, startResizeRightToLeft, startResizeMiddleLeft, startResizeMiddleRight} from "$lib/resizableUtils.js";
  import { apiFetch } from "$lib/utils/fetch.js";


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
  let findSubGroups = null;
  let findCharacteristics = null;
  let initialized = false;

  async function findInstrumentsOfCategory(categoryId){
    const index = $categories.findIndex(category => category.id === Number(categoryId));
    selectedCategoryIndex.set(index);

    currentSuppliers.set([]);
    alternatives.set([]);  
    try{
    const response = await apiFetch(`/api/category/instruments/${categoryId}`);
    let response2;
    if( $isAdmin){
        response2 = await apiFetch(`/api/alternatives/admin/category/${categoryId}`);
    }
    else{
        response2 = await apiFetch(`/api/alternatives/user/category/${categoryId}`);
    }
    const response3 = await apiFetch(`/api/category/${categoryId}`);
    if (!response.ok){
        throw new Error("Failed to fetch instruments of category");
    }
    const answer = await response.json();
    currentSuppliers.set(Array.isArray(answer) ? answer : [answer]);

    const categoryChars = await response3.json();

    charValues.update(currentValues => {
        let updatedValues = { ...currentValues }; // Clone current object

        for (let i = 0; i < categoryChars.length; i++) {
            let key = categoryChars[i].name;
            let value = categoryChars[i].value;

            if (key === "Length") {
                value = value.replace(/[^\d.]/g, "");
            }

            const element = document.getElementById(key);
            if (element) {
                element.value = value;
            }

            updatedValues[key] = value;
        }
        return updatedValues;
    });

    if (!response2.ok){
        return;
    }
    const answer2 = await response2.json();
    alternatives.set(Array.isArray(answer2)? answer2 : [answer2]);
    }catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
    return;
  }

  function selectInstrument(instrumentId){
    const index = $currentSuppliers.findIndex(instr => instr.id === Number(instrumentId));
    selectedSupplierIndex.set(index);
  }

  /**
   * Fetch when page is rendered.
   */
  async function fetchData() {
    await getGroupsSummary();

    const group = $page.url.searchParams.get("group");
    const subgroup = $page.url.searchParams.get("subgroup");
    const categoryId = $page.url.searchParams.get("category");
    const instrumentId = $page.url.searchParams.get("instrument");

    if (group) {
      await findSubGroups(group);
      // If a subgroup exists in the URL, load its characteristics
      if (subgroup) {
        await findCharacteristics(subgroup);
      }
      if (categoryId) {
          await findInstrumentsOfCategory(categoryId);
          if (instrumentId){
            selectInstrument(instrumentId);
          }
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

  let div1, div2, div3, div4, div5;
</script>

<svelte:head>
  <title>Recherches</title>
</svelte:head>

<div id="container" class="text-[14px]">
  <div class="flex flex-col gap-[5px] box-border w-full">
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div class="flex w-full h-[70vh] relative overflow-auto border border-gray-300" bind:this={div1}>
    <!-- TOP PART -->
        <!-- SEARCHES -->
        <div
          class="relative overflow-auto border border-gray-300 flex-none"
          bind:this={div2}
          style="min-width: 20px;" 
        >
          <SearchComponent />
          <!-- svelte-ignore a11y_no_static_element_interactions -->
          <div
            class="absolute w-0.5 h-full bg-gray-300 bottom-0 right-0 cursor-ew-resize"
            on:mousedown={(e) => startResizeLeftToRight(e, div2)}            
          ></div>
        </div>
      
        <!-- CATEGORIES -->
        <div
          class="relative overflow-auto border border-gray-300 flex-grow"
          style="min-width: 20px;" >
          <CategoryComponent />
        </div>
      
        <!-- INSTRUMENTS -->
        <div
          class="relative overflow-auto border border-gray-300 flex-none"
          bind:this={div4}
          style="min-width: 20px;" 
        >
          <InstrumentComponent />
          <!-- svelte-ignore a11y_no_static_element_interactions -->
          <div
            class="absolute w-0.5 h-full bg-gray-300 bottom-0 left-0 cursor-ew-resize"
            on:mousedown={(e) => startResizeRightToLeft(e, div4)}
          ></div>
        </div>
      <div class="absolute w-full h-0.5 bg-gray-300 bottom-0 left-0 cursor-ns-resize" on:mousedown={(e) => startResizeVertical(e, div1)}></div>
    </div>    

    <!-- BOTTOM PART -->
    <!-- ORDERS  -->
    <OrderComponent />

  </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>
