<script>
  import { goto } from "$app/navigation";
  import { onMount } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import editGroupModal from "$lib/modals/editGroupModal.svelte";
  import editSubgroupModal from "$lib/modals/editSubgroupModal.svelte";
  import { modals } from "svelte-modals";
  import { checkRole } from "$lib/rbacUtils";
	import { ROLES } from "../constants";
	import { user } from "$lib/stores/user_stores"; 
  import { errorMessage, keywords, keywordsResult } from "$lib/stores/searches";
  import { apiFetch } from "$lib/utils/fetch";

  // RBAC 
  let userValue;
  user.subscribe(value => {
    userValue = value;
  });
  // returns true if user is admin
  let isAdmin = checkRole(userValue, ROLES.ADMIN);

  let groups_summary = $state([]);

  async function fecthData() {
    try {
      const response = await apiFetch("/api/groups/summary");

      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }

      groups_summary = await response.json();
      groups_summary.sort((a, b) => a.name.localeCompare(b.name));
    } catch (error) {
      console.error(error);
    }
  }
  onMount(fecthData);

  function moveToSearches(group, subgroup) {
    clearTimeout(clickTimeout);
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup ? subgroup : "")}`
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

  $effect(() => {
    if ($keywords().trim()) {
      debouncedSearch();
    }
  }); 

  const debouncedSearch = debounce(searchByKeywords, 300);
  function debounce(fn, delay = 300) {
    let timeout;
    return (...args) => {
      clearTimeout(timeout);
      timeout = setTimeout(() => fn(...args), delay);
    };
  }

  async function searchByKeywords() {
    if (!$keywords.trim()) return;

    try {
      const foundElements = await apiFetch(`/api/instruments/search?keywords=${encodeURIComponent($keywords)}`);
      keywordsResult.set(await response.json());
      console.log($keywords);
      console.log($keywordsResult);
    } catch (error) {
      console.error(error);
      errorMessage.set(error.message);
      }
  }
  
</script>

<svelte:head>
  <title>Accueil</title>
</svelte:head>

<main
  class="flex flex-col md:flex-row justify-center items-start space-y-8 md:space-y-0 md:space-x-10 px-8 py-10 max-w-screen-xl mx-auto text-[14px"
>
  <aside class="w-full md:w-1/3 bg-gray-100 rounded-lg p-8 shadow-md">
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
          bind:value={$keywords}
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

      <button
        type="submit"
        class="w-full bg-teal-500 text-white py-3 rounded-lg hover:bg-teal-600 text-lg" onclick={searchByKeywords}
        >Rechercher</button
      >
      {#if isAdmin}
        <div class="flex flex-col">
          <button class="w-full bg-yellow-400 text-white py-3 rounded-lg hover:bg-yellow-500 text-lg"><a href="/admin/add_group">Ajouter un groupe</a></button>
        </div>
      {/if}
    </form>
  </aside>

  <section
    class="w-full bg-white md:w-1/3 lg:min-w-[800px] xl:min-w-[1100px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[400px] overflow-y-auto"
  >
    <div class="flex gap-2">
      <!-- Buttons div -->
      {#if selectedGroup}
        <button
          class="px-4 py-2 bg-gray-100 hover:bg-gray-300 rounded-lg mb-2"
          aria-label="back to groups"
          onclick={() => ((selectedGroup = null), (selectedSubgroups = []), isEditing ? startEditing() : null)}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M15 18l-6-6 6-6" />
          </svg>
        </button>
      {/if}

      {#if isAdmin}
        <button
          class="px-4 py-2 bg-gray-100 hover:bg-orange-300 rounded-lg mb-2"
          aria-label="edit groups"
          id="editGroupsButton"
          onclick={() => startEditing()}
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.121 2.121 0 1 1 3 3L6 20l-4 1 1-4L16.5 3.5z" />
          </svg>
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
  </section>
</main>

<div
  class="container mx-auto bg-gray-50 p-6 shadow-lg flex justify-center flex items-center space-x-6"
>
  <div>
    <span class="text-teal-600 font-semibold text-2xl">Set/commande</span>
  </div>
  <div>
    <label for="id_ref" class="font-semibold text-lg">Rechercher une commande:</label>
    <div class="flex flex-row">
      <div>
        <input
          list="commandes"
          name="commandes"
          placeholder="Entrez un numéro de commande"
          class="w-[350px] p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg"
        />
        <datalist id="commandes">
          <option value="#123456"> </option><option value="#123457"> </option><option
            value="#123458"
          >
          </option><option value="#123459"> </option></datalist
        >
      </div>
      <div>
        <button class="bg-teal-500 text-white py-3 px-6 rounded-lg hover:bg-teal-600 text-lg"
          >Rechercher</button
        >
      </div>
    </div>
  </div>
</div>
