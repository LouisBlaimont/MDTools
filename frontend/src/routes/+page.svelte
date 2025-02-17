<script >
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';
  // const images: Record<string, {default: string}> = import.meta.glob('/groups/*.png', {eager: true});
  const imageUrls = [
    '/Groups_img/1.png','/Groups_img/2.png','/Groups_img/3.png','/Groups_img/4.png','/Groups_img/5.png','/Groups_img/6.png','/Groups_img/7.png',
    '/Groups_img/8.png','/Groups_img/9.png','/Groups_img/10.png','/Groups_img/11.png','/Groups_img/12.png','/Groups_img/13.png','/Groups_img/14.png',
  ];

  let groups_summary = [];
  let groups = [];
  let number_of_tool = [];
  onMount(async () => {
            try {
                const response = await fetch('http://localhost:8080/api/groups/summary');

                if (!response.ok) {
                    throw new Error(`Failed to fetch groups: ${response.statusText}`);
                }

                groups_summary = await response.json();
            } catch (error) {
                console.error(error);
                errorMessage = error.message;
            }
        });
    $: groups = groups_summary.map(group => group.name);
    $: number_of_tool = groups_summary.map(group => group.instrCount);

    function moveToSearches(group){
      console.log(group.group);
      goto(`/searches?group=${encodeURIComponent(group.group)}`);
    }
</script>

<head><title>Accueil</title></head>

<main class="flex flex-col md:flex-row justify-center items-start space-y-8 md:space-y-0 md:space-x-10 px-8 py-16 max-w-screen-xl mx-auto text-[14px">
  <aside class="w-full md:w-1/4 bg-gray-100 rounded-lg p-8 shadow-md">
    <form class="space-y-6">
      <div class="flex flex-col">
        <label for="id_search_keyword" class="font-semibold text-lg">Recherche par mot(s) clé(s):</label>
        <input type="text" name="search_keyword" id="id_search_keyword" placeholder="Entrez un mot clé"
               class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">
      </div>
      
      <div class="flex flex-col">
        <label for="id_search_by_groups" class="font-semibold text-lg">Recherche par groupe ou sous-groupe:</label>
        <input type="text" name="search_by_groups" id="id_search_by_groups" placeholder="Entrez un groupe"
               class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">
      </div>
      
      <div class="flex flex-col">
        <label for="id_search_set" class="font-semibold text-lg">Recherche par référence:</label>
        <input list="ref" name="ref" placeholder="Entrez une référence"
               class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500">
        <datalist id="ref">
          <option value="ref1">
          <option value="ref2">
          <option value="ref3">
          <option value="ref4">
          <option value="ref5">
        </datalist>
      </div>

      <button type="submit" class="w-full bg-teal-500 text-white py-3 rounded-lg hover:bg-teal-600 text-lg"><a href="/searches">Rechercher</a></button>
    </form>
  </aside>

  <section class="w-full md:w-3/4 grid grid-cols-2 sm:grid-cols-3 sm:min-w-[600px] lg:grid-cols-4 lg:min-w-[900px] xl:min-w-[1200px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[500px] overflow-y-auto">
    {#each groups as group, index}
    <div class="relative group">
      <img class="cursor-pointer w-full object-cover rounded-lg" src='/Groups_img/1.png' alt="group_img" on:dblclick={()=>moveToSearches({group})}/>
      <div class="absolute bottom-0 left-0 p-4 text-white text-lg">
        {group} ({number_of_tool[index]})
      </div>
    </div>
    {/each}
  </section>
</main>

<div class="container mx-auto bg-gray-50 p-6 shadow-lg flex justify-center flex items-center space-x-6">
  <div>
    <span class="text-teal-600 font-semibold text-2xl">Set/commande</span>
  </div>
  <div> 
    <label for="id_ref" class="font-semibold text-lg">Rechercher une commande:</label> 
    <div class= "flex flex-row"> 
      <div>   
        <input list="commandes" name="commandes" placeholder="Entrez un numéro de commande"
              class="w-[350px] p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg">
        <datalist id="commandes">
          <option value="#123456">
          <option value="#123457">
          <option value="#123458">
          <option value="#123459">
        </datalist>
      </div>
      <div>
        <button class="bg-teal-500 text-white py-3 px-6 rounded-lg hover:bg-teal-600 text-lg">Rechercher</button>
      </div>
    </div>
  </div>  
</div>
