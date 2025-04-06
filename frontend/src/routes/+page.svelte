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
  import { apiFetch } from "$lib/utils/fetch";
  import { findOrderItems } from "$lib/components/order_component.js";
  import Loading from "$lib/Loading.svelte";
  import { PUBLIC_API_URL } from "$env/static/public";
  import Icon from "@iconify/svelte";

  let groups_summary = $state([]);

  async function fecthData() {
    try {
      const response = await apiFetch("/api/groups/summary");

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
      fecthData();
    }
  });

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
      fecthData();
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
</script>

<svelte:head>
  <title>Accueil</title>
</svelte:head>

<div
  class="flex flex-row justify-center items-start space-x-8 px-8 py-16 max-w-screen-xl mx-auto text-[14px]"
>
  <div class="flex flex-col space-y-8 w-1/3">
    <div class="w-full bg-gray-100 rounded-lg p-8 shadow-md">
      <form class="space-y-6">
        <div class="flex flex-col">
          <label for="id_search_keyword" class="font-semibold text-lg"
            >Recherche par mot(s) clé(s):</label
          >
          <input
            type="text"
            name="search_keyword"
            id="id_search_keyword"
            placeholder="Entrez un mot clé"
            class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500"
          />
        </div>

        <div class="flex flex-col">
          <label for="id_search_by_groups" class="font-semibold text-lg"
            >Recherche par groupe ou sous-groupe:</label
          >
          <input
            type="text"
            name="search_by_groups"
            id="id_search_by_groups"
            placeholder="Entrez un groupe"
            class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500"
          />
        </div>

        <div class="flex flex-col">
          <label for="id_search_set" class="font-semibold text-lg">Recherche par référence:</label>
          <input
            list="ref"
            name="ref"
            placeholder="Entrez une référence"
            class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500"
          />
          <datalist id="ref">
            <option value="ref1"> </option><option value="ref2"> </option><option value="ref3">
            </option><option value="ref4"> </option><option value="ref5"> </option></datalist
          >
        </div>

        <a href="/searches"><button
          type="submit"
          class="w-full bg-teal-500 text-white py-3 rounded-lg hover:bg-teal-600 text-lg mt-2"
          >Rechercher</button
        ></a>
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
      <div class="mb-6">
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

      <div class="mb-6">
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

  <div
    class="w-full bg-white md:w-3/4 lg:min-w-[900px] xl:min-w-[1200px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[500px] overflow-y-auto"
  >
    {#if $isLoggedIn}
      <div class="flex gap-2">
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
    {:else}
      <div class="flex flex-col items-center justify-center h-full max-height-[200px]">
        <Loading />
      </div>
    {/if}
  </div>
</div>
