<script>
  import { goto } from "$app/navigation";
  import { onMount } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import editGroupModal from "$lib/modals/editGroupModal.svelte";
  import editSubgroupModal from "$lib/modals/editSubgroupModal.svelte";
  import { modals } from "svelte-modals";
  import { ROLES } from "../constants";
  import { ordersNames, userId, selectedOrderId } from "$lib/stores/searches";
  import { user, isAdmin, isWebmaster, isLoggedIn } from "$lib/stores/user_stores";
  import { errorMessage, keywords, keywordsResult, hoveredInstrumentIndex, selectedInstrumentIndex, selectedCategoryIndex, currentSuppliers} from "$lib/stores/searches";
  import { apiFetch } from "$lib/utils/fetch";
  import { findOrderItems } from "$lib/components/order_component.js";
  import Loading from "$lib/Loading.svelte";
  import editInstrumentModal from "$lib/modals/editInstrumentModal.svelte";
  import { PUBLIC_API_URL } from "$env/static/public";
  import Icon from "@iconify/svelte";

  let groups_summary = $state([]);

  async function fetchData() {
    try {
      const response = await apiFetch("/api/groups/summary");
      showKeywordsResult = false;
      keywords.set(null);

      const response2 = await apiFetch(`/api/orders/user/${$userId}`);
      if (!response2.ok) {
        throw new Error(`Failed to fetch orders: ${response2.statusText}`);
      }
      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }

      ordersNames.set(await response2.json());
      groups_summary = await response.json();
      groups_summary.sort((a, b) => a.name.localeCompare(b.name));
    } catch (error) {
      console.error(error);
    }
  }

  // Run this effect whenever isLoggedIn changes
  $effect(() => {
    if (isLoggedIn) {
      fetchData();
    }
  });

  function moveToSearches(group, subgroup) {
    clearTimeout(clickTimeout);
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup ? subgroup : "")}&category=${encodeURIComponent("")}&instrument=${encodeURIComponent("")}`
    );
  }

  async function moveToSearchesBis(instrument, group, subgroup, catId, instrumentId) {
    clearTimeout(clickTimeout);
    if (catId == 1) {
      await modals.open(editInstrumentModal, { instrument });
    }
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup ? subgroup : "")}&category=${encodeURIComponent(catId)}&instrument=${encodeURIComponent(instrumentId)}`
    );
  }

  // Deal with group/subgroup navigation
  let selectedGroup = $state(null);
  let selectedSubgroups = $state([]);
  let clickTimeout;
  let isEditing = false;
  let showModal = $state(false);
  let showKeywordsResult = $state(false);

  function startEditing() {
    if (isEditing) {
      isEditing = false;
      toast.push("Fin de l'édition des groupes.");
      document.getElementById("editGroupsButton").classList.remove("bg-orange-600");
      return;
    } else {
      isEditing = true;
      toast.push("Choisissez un groupe pour le modifier.");
      document.getElementById("editGroupsButton").classList.add("bg-orange-600");
    }
  }

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

  async function handleSubGroupClick(group, subgroup) {
    if (isEditing) {
      await modals.open(editSubgroupModal, { subgroup });
      getSubgroups(selectedGroup);
      return;
    }
    moveToSearches(group.name, subgroup.name);
  }

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


  async function selectedInstrumentHome(row) {
    try {
      let response = await apiFetch(`/api/instrument/getCategory/${row.categoryId}`);
      let cat = await response.json();
      showKeywordsResult = false;
      console.log("id insturment", row.id);
      moveToSearchesBis(row, cat.groupName, cat.subGroupName, row.categoryId, row.id);
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
    }
  }

  function seePreviousOrders() {
    goto("/previous_orders");
  }

  function getSelectedOrderName(orderId) {
    const selectedOrder = $ordersNames.find((order) => order.id === orderId);
    return selectedOrder ? selectedOrder.name : null;
  }

  async function singleOrderView() {
    const name = getSelectedOrderName($selectedOrderId);
    findOrderItems($selectedOrderId);
    goto(`/single_order_view?name=${name}`);
  }

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

</script>

<svelte:head>
  <title>Accueil</title>
</svelte:head>

<div
  class="flex flex-row justify-center items-start space-x-8 px-5 py-6 max-w-screen-xl mx-auto text-[14px]"
>
  <div class="flex flex-col space-y-6 w-1/2 lg:min-w-[90px] xl:min-w-[250px]">
    <div class="w-full bg-gray-100 rounded-lg p-8 shadow-md">
      <form class="space-y-5">
        <div class="relative w-full">
          <label for="id_search_keyword" class="font-semibold text-lg block mb-2">
            Recherche par mot(s) clé(s):
          </label>
          
          <input
            type="text"
            name="search_keyword"
            id="id_search_keyword"
            autocomplete="off"
            placeholder="Entrez un mot clé"
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
                  <li
                    class="p-2 hover:bg-gray-100 cursor-pointer flex items-center space-x-4 text-sm border-b last:border-none"
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
                  </span>                </li>
                {/each}
              </ul>
            {/if}
          {/if}
        </div>
             

        {#if $isAdmin}
          <div class="flex flex-col">
            <a href="/admin/add_group"><button
              class="w-full bg-yellow-400 text-white py-3 rounded-lg hover:bg-yellow-500 text-lg"
              >Ajouter un groupe</button
            ></a>
          </div>
          {#if selectedGroup}
            <div class="flex flex-col">
              <a href="/admin/add_subgroup"><button
                class="w-full bg-yellow-400 text-white py-3 rounded-lg hover:bg-yellow-500 text-lg"
                >Ajouter un sous-groupe</button
              ></a>
            </div>
          {/if}
        {/if}
      </form>
    </div>

    <div class="w-full bg-gray-100 rounded-lg p-8 shadow-md mt-6">
      <div class="mb-4">
        <label for="search-order" class="font-semibold text-lg"> Rechercher une commande : </label>
        <select
          class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 mb-2"
          bind:value={$selectedOrderId}
        >
          {#each $ordersNames as order}
            <option value={order.id}>{order.name} </option>
          {/each}
        </select>
        <button
          class="w-full p-3 bg-teal-500 text-white rounded-lg hover:bg-teal-600"
          onclick={() => singleOrderView()}>Rechercher</button
        >
      </div>

      <div class="mb-4">
        <label for="previous-orders" class="font-semibold text-lg">
          Voir les commandes précédentes:
        </label>
        <button
          class="w-full p-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600 flex items-center justify-center"
          onclick={() => seePreviousOrders()}
        >
          Voir
        </button>
      </div>
    </div>
  </div>

  <div class="bg-white md:w-2/4 lg:min-w-[800px] xl:min-w-[1100px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[500px] overflow-y-auto"
  >
  {#if $isLoggedIn}  
      <div class="flex gap-2">
        <!-- Buttons div -->
        {#if selectedGroup}
          <button
            class="px-4 py-2 bg-gray-100 hover:bg-gray-300 rounded-lg mb-2 "
            aria-label="back to groups"
            onclick={() => ((selectedGroup = null), (selectedSubgroups = []), isEditing ? startEditing() : null)}
          >
            <Icon icon="material-symbols:arrow-back-ios-new-rounded" width="24" height="24" />
          </button>
        {/if}

        {#if $isAdmin}
          <button
            class="px-4 py-2 bg-gray-100 hover:bg-orange-300 rounded-lg mb-2"
            aria-label="edit groups"
            id="editGroupsButton"
            onclick={() => startEditing()}
          >
            <Icon icon="material-symbols:edit" width="24" height="24" />
          </button>
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
  {/if}
  {#if !$isLoggedIn}
    <div class="flex flex-col items-center justify-center h-full max-height-[200px]">
      <Loading />
    </div>
  {/if}
  </div>
</div>
