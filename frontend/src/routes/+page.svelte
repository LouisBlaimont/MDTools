<script>
  import { goto } from "$app/navigation";
  import { onMount } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import AddGroupModal from "$lib/modals/AddGroupModal.svelte";
  import AddSubGroupModal from "$lib/modals/AddSubGroupModal.svelte";
  import editGroupModal from "$lib/modals/editGroupModal.svelte";
  import editSubgroupModal from "$lib/modals/editSubgroupModal.svelte";
  import { ROLES } from "../constants";
  import { ordersNames, selectedOrderId } from "$lib/stores/searches";
  import { user, isAdmin, isWebmaster, isLoggedIn, userId } from "$lib/stores/user_stores";
  import { errorMessage, keywords, keywordsResult, hoveredInstrumentIndex, selectedInstrumentIndex, selectedCategoryIndex, currentSuppliers, reload} from "$lib/stores/searches";
  import { apiFetch } from "$lib/utils/fetch";
  import { findOrderItems, findOrdersNames } from "$lib/components/order_component.js";
  import Loading from "$lib/Loading.svelte";
  import editInstrumentModal from "$lib/modals/editInstrumentModal.svelte";
  import { PUBLIC_API_URL } from "$env/static/public";
  import Icon from "@iconify/svelte";
  import { _ } from "svelte-i18n";

  let groups_summary = $state([]);

  // deal with the "add group" and "add subgroup" buttons
  let selected = $state(true);
  $effect(() => {
    if (isAdmin && selectedGroup) {
      selected = false;
    }
    if (isAdmin && !selectedGroup) {
      selected = true;
    }
  });

  /**
   * fetching needed data while mounting
   */
  async function fetchData() {
      if ($userId != null) {
        await findOrdersNames();
      }

      const response = await apiFetch("/api/groups/summary");
      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }
      groups_summary = await response.json();
      groups_summary.sort((a, b) => a.name.localeCompare(b.name));
  }

  // Run this effect whenever isLoggedIn changes
  $effect(() => {
    if (isLoggedIn) {
      fetchData();
    }
  });

  /**
   * moving to the search page with selected group (and subgroup)
   * @param group selected group
   * @param subgroup selected subgroup
   */
  function moveToSearches(group, subgroup) {
    clearTimeout(clickTimeout);
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup ? subgroup : "")}&category=${encodeURIComponent("")}&instrument=${encodeURIComponent("")}`
    );
  }

  // Deal with group/subgroup navigation
  let selectedGroup = $state(null);
  let selectedSubgroups = $state([]);
  let clickTimeout;
  let isEditing = false;
  let showModal = $state(false);
  let showKeywordsResult = $state(false);

  // Deal with the editing of the groups and the subgroups
  function startEditing() {
    if (isEditing) {
      isEditing = false;
      toast.push($_('homepage.toast1'));
      document.getElementById("editGroupsButton").classList.remove("bg-yellow-500");
      return;
    } else {
      isEditing = true;
      toast.push($_('homepage.toast2'));
      document.getElementById("editGroupsButton").classList.add("bg-yellow-500");
    }
  }

  // handle when a group is selected and gets all the subgroups
  async function handleGroupClick(group) {
    if (isEditing) {
      await modals.open(editGroupModal, { group });
      fetchData();
      return;
    }
    clickTimeout = setTimeout(() => {
      selectedGroup = group;
      getSubgroups(group);
    }, 500);
  }

  // getting all the subgroups of a given group
  async function getSubgroups(group) {
    if (!group || !group.name) {
      console.error("Group is null or undefined:", group);
      return;
    }

    try {
      const response = await apiFetch(`/api/groups/${group.name}`);

      if (!response.ok) {
        throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
      }

      let data = await response.json();

      if (!data || !data.subGroups) {
        console.error("Invalid response format:", data);
        return;
      }

      selectedSubgroups = data.subGroups;
      selectedSubgroups.sort((a, b) => a.name.localeCompare(b.name));
    } catch (error) {
      console.error(error);
    }
  }

  // going to searches when a subgroup is selected
  async function handleSubGroupClick(group, subgroup) {
    if (isEditing) {
      await modals.open(editSubgroupModal, { subgroup });
      getSubgroups(selectedGroup);
      return;
    }
    moveToSearches(group.name, subgroup.name);
  }

  /* Dealing with the search by keywords */
  /**
   * goto searches with the selected instrument found by keywords
   * @param instrument
   * @param group
   * @param subgroup
   * @param catId
   * @param instrumentId
   */
  async function moveToSearchesBis(instrument, group, subgroup, catId, instrumentId) {
    clearTimeout(clickTimeout);
    
    // handle when the instrument has no category
    if (catId == null) {
      await modals.open(editInstrumentModal, { 
        instrument,
        message: $_('search_page.assign') 
      });
    }
    else {
      keywords.set(null);
      goto(
        `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup)}&category=${encodeURIComponent(catId)}&instrument=${encodeURIComponent(instrumentId)}`
      );
      reload.set(true);
    }
  }

  /**
   * function to handle the keyword inputs and calling endpoint
   */
  let searchByKeywords = throttle(async () => {
    try {
      showKeywordsResult = false;
      let data = null;
      let params = new URLSearchParams();
      $keywords.split(",").forEach((element) => {
        const keyword = element.trim();
        if (keyword.length > 0) {
          params.append("keywords", keyword);
        }
      });
      if ($keywords == null) {
        data = null;
      }
      else {
        let response = await apiFetch(`/api/instrument/search?${params}`);
        data = await response.json();
        keywordsResult.set(Array.isArray(data) ? data.slice(0, 5) : []);
        if (Object.keys(data).length > 0) {
          showKeywordsResult = true;
        }
      }
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }, 300);

  // handles the delay between to search by keywords
  function throttle(fn, delay) {
    let lastCall = 0;
    return (...args) => {
      const now = Date.now();
      if (now - lastCall >= delay) {
        lastCall = now;
        fn(...args);
      }
    };
  }

  /**
   * get category, group and subgroup from selected instrument, then call moveToSearchesBis
   * @param row
   */
  async function selectedInstrumentHome(row) {
    try {
      let response = await apiFetch(`/api/instrument/getCategory/${row.categoryId}`);
      let cat = await response.json();
      showKeywordsResult = false;
      moveToSearchesBis(row, cat.groupName, cat.subGroupName, row.categoryId, row.id);
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }


  /* Functions for handling the orders */
  function seePreviousOrders() {
    goto("/previous_orders");
  }

  async function singleOrderView() {
    if(!$selectedOrderId){
      const errorNoOrder = document.getElementById("error-no-order");
      errorNoOrder.classList.remove('hidden');
      return;
    }
    findOrderItems($selectedOrderId);
    goto(`/single_order_view?id=${encodeURIComponent($selectedOrderId)}`);
  }

  </script>


<svelte:head>
  <title>{$_('header.home')}</title>
</svelte:head>

<div
class="flex flex-row justify-center items-start space-x-8 px-5 py-6 max-w-screen-xl mx-auto text-[14px]"
>
<div class="flex flex-col space-y-6 w-1/2 lg:min-w-[90px] xl:min-w-[250px]">
  <div class="w-full bg-gray-100 rounded-lg p-8 shadow-md">
    <form class="space-y-5">
      <div class="relative w-full">
        <label for="id_search_keyword" class="font-semibold text-lg block mb-2">
            {$_('homepage.keyword_search.label')}:
          </label>
          <input
            type="text"
            name="search_keyword"
            id="id_search_keyword"
            autocomplete="off"
            placeholder={$_('homepage.keyword_search.placeholder')}
            class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 w-full"
            bind:value={$keywords}
            oninput={searchByKeywords}
          />
        
          <!-- Search results dropdown -->
          {#if showKeywordsResult}
            {#if $keywords}
              <ul
                class="absolute left-0 w-full bg-white border border-gray-300 rounded-lg shadow-lg z-10 max-h-60 overflow-y-auto text-gray-800"
                style="width: calc(100% + 80px);"
              >
                {#each $keywordsResult as row, index}
                  <li class="p-0 text-sm border-b last:border-none">
                  <button
                    type="button"
                    class="w-full text-left p-2 hover:bg-gray-100 cursor-pointer flex items-center space-x-4"
                    onclick={() => selectedInstrumentHome(row)}
                  >
                    <span class="text-black">{row.reference}</span>
                    <span class="text-black">{row.supplier}</span>
                    <span
                    class="text-black line-clamp-2 w-full tooltip-container"
                    data-tooltip={row.supplierDescription}
                    title={row.supplierDescription} 
                    >
                      {row.supplierDescription}
                    </span>  
                  </button>              
                  </li>
                {/each}
              </ul>
            {/if}
          {/if}
        </div>
      </form>
    </div>


    <div class="w-full bg-gray-100 rounded-lg p-8 shadow-md mt-6">
      <div class="mb-6">
        <label for="search-order" class="font-semibold text-lg"> {$_('homepage.search_order.label')}: </label>
        <select
          class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 mb-2"
          bind:value={$selectedOrderId}
        >
          {#each $ordersNames as order}
            {#if !order.isExported}
            <option value={order.id}>{order.name} </option>
            {/if}
          {/each}
        </select>
        <span id="error-no-order" class="text-red-600 text-sm hidden"> {$_('homepage.search_order.error')}</span>
        <button
          class="w-full p-3 bg-teal-500 text-white rounded-lg hover:bg-teal-600"
          onclick={() => singleOrderView()}>{$_('homepage.button.search')}</button
        >
      </div>

      <div class="mb-6">
        <label for="previous-orders" class="font-semibold text-lg">
          {$_('homepage.previous_orders.label')}:
        </label>
        <button
          class="w-full p-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600 flex items-center justify-center"
          onclick={() => seePreviousOrders()}
        >
          {$_('homepage.previous_orders.button')}
        </button>
      </div>
    </div>
  </div>

  <div
    class="w-full bg-white md:w-3/4 lg:min-w-[900px] xl:min-w-[1200px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[500px] overflow-y-auto"
  >
      <div class="flex gap-2 h-14">
        <!-- Buttons div -->
        {#if selectedGroup}
          <button
            class="px-4 py-2 bg-gray-100 hover:bg-gray-300 rounded-lg mb-2 "
            aria-label="back to groups"
            onclick={() => (
              (selectedGroup = null), (selectedSubgroups = []), isEditing ? startEditing() : null
            )}
          >
            <Icon icon="material-symbols:arrow-back-ios-new-rounded" width="24" height="24" />
          </button>
        {/if}

        {#if $isAdmin}
          <button
            class="px-4 py-2 bg-gray-100 hover:bg-yellow-500 rounded-lg mb-2"
            aria-label="edit groups"
            id="editGroupsButton"
            onclick={() => startEditing()}
          >
            <Icon icon="material-symbols:edit" width="24" height="24" />
          </button>
        {/if}
        {#if $isAdmin}
          {#if selected}
              <button
              class="px-4 py-2 bg-yellow-300 rounded-lg hover:bg-yellow-500 mb-2 text-lg"
              onclick={()=> modals.open(AddGroupModal)}
              >{$_('homepage.admin.button.add_group')}
            </button>
          {/if}
          {#if selectedGroup}
            <button
              class="px-4 py-2 bg-yellow-300 rounded-lg hover:bg-yellow-500 mb-2 text-lg"
              onclick={()=> modals.open(AddSubGroupModal)}
              >{$_('homepage.admin.button.add_subgroup')}
            </button>
          {/if}
        {/if}
      </div>

      <div class="grid grid-cols-2 sm:grid-cols-3 sm:min-w-[600px] lg:grid-cols-4">
        {#if !selectedGroup}
          {#each groups_summary as group}
            <div class="relative group w-64 h-40">
              <button
                class="cursor-pointer w-full h-full object-cover rounded-lg"
                style="background-image: url({group.pictureId
                  ? PUBLIC_API_URL + `/api/pictures/${group.pictureId}`
                  : '/default/group_picture_default.png'}); background-size: cover;"
                aria-label="group image"
                onclick={() => handleGroupClick(group)}
                ondblclick={() => moveToSearches(group.name)}
                onkeydown={(e) => {
                  if (e.key === "Enter") handleGroupClick(group);
                }}
              ></button>
              <div
                class="absolute bottom-0 left-0 w-full bg-black bg-opacity-50 p-2 text-white text-lg rounded-b-lg"
              >
                {group.name} ({group.instrCount})
              </div>
            </div>
          {/each}
        {/if}

        {#if selectedGroup}
          {#each selectedSubgroups as subgroup}
            <div class="relative group w-64 h-40">
              <button
                class="cursor-pointer w-full h-full object-cover rounded-lg"
                style="background-image: url({subgroup.pictureId
                  ? PUBLIC_API_URL + `/api/pictures/${subgroup.pictureId}`
                  : '/default/group_picture_default.png'}); background-size: cover;"
                aria-label="subgroup image"
                onclick={() => handleSubGroupClick(selectedGroup, subgroup)}
                onkeydown={(e) => {
                  if (e.key === "Enter") handleSubGroupClick(selectedGroup, subgroup);
                }}
              ></button>
              <div
                class="absolute bottom-0 left-0 w-full bg-black bg-opacity-50 p-2 text-white text-lg rounded-b-lg"
              >
                {subgroup.name} ({subgroup.instrCount})
              </div>
            </div>
          {/each}
        {/if}
      </div>
  </div>
</div>
