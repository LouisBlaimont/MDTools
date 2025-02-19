<script>
  import { goto } from "$app/navigation";
  import { onMount } from "svelte";
  // const images: Record<string, {default: string}> = import.meta.glob('/groups/*.png', {eager: true});
  import { PUBLIC_API_URL } from '$env/static/public'

  let groups_summary = [];
  onMount(async () => {
    try {
      const response = await fetch(PUBLIC_API_URL + "/api/groups/summary");

      if (!response.ok) {
        throw new Error(`Failed to fetch groups: ${response.statusText}`);
      }

      groups_summary = await response.json();
    } catch (error) {
      console.error(error);
      errorMessage = error.message;
    }
  });

  function moveToSearches(group, subgroup) {
    clearTimeout(clickTimeout);
    goto(
      `/searches?group=${encodeURIComponent(group)}&subgroup=${encodeURIComponent(subgroup ? subgroup : "")}`
    );
  }

  // Deal with group/subgroup navigation
  let selectedGroup = null;
  let selectedSubgroups = [];
  let clickTimeout;

  function handleGroupClick(group) {
    clickTimeout = setTimeout(() => {
      selectedGroup = group;
      // Fetch subgroups of group
      fetch(PUBLIC_API_URL + `/api/groups/${group.name}`)
        .then((response) => response.json())
        .then((data) => {
          selectedSubgroups = data.subGroups;
        })
        .catch((error) => console.error(error));
    }, 500);
  }
</script>

<head><title>Accueil</title></head>

<main
  class="flex flex-col md:flex-row justify-center items-start space-y-8 md:space-y-0 md:space-x-10 px-8 py-16 max-w-screen-xl mx-auto text-[14px"
>
  <aside class="w-full md:w-1/4 bg-gray-100 rounded-lg p-8 shadow-md">
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

      <button
        type="submit"
        class="w-full bg-teal-500 text-white py-3 rounded-lg hover:bg-teal-600 text-lg"
        ><a href="/searches">Rechercher</a></button
      >
    </form>
  </aside>

  <section
    class="w-full md:w-3/4 lg:min-w-[900px] xl:min-w-[1200px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[500px] overflow-y-auto"
  >
    {#if selectedGroup}
      <button
        class="flex items-center gap-2 px-4 py-2 bg-gray-100 hover:bg-gray-300 rounded-lg mb-2"
        aria-label="back to groups"
        on:click={() => (selectedGroup = null)}
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
              on:click={() => handleGroupClick(group)}
              on:dblclick={() => moveToSearches(group.name)}
              on:keydown={(e) => {
                if (e.key === "Enter") handleGroupClick(group);
              }}
              on:keydown={(e) => {
                if (e.key === "Enter") moveToSearches(group.name);
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
              on:click={() => moveToSearches(selectedGroup.name, subgroup.name)}
              on:keydown={(e) => {
                if (e.key === "Enter") moveToSearches(selectedGroup.name, subgroup.name);
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
