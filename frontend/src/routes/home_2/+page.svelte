<script lang="ts">
  const images: Record<string, {default: string}> = import.meta.glob('$lib/assets/groups/*.png', {eager: true});
</script>

<head><title>Accueil</title></head>
<header class="bg-teal-500 h-20 flex items-center px-6">
  <img alt="Logo MD" src="src/lib/assets/logo-blanc.png" class="h-full p-4" />
</header>

<main class="flex flex-col md:flex-row justify-center items-start space-y-8 md:space-y-0 md:space-x-10 px-8 py-16 max-w-screen-xl mx-auto">
  <aside class="w-full md:w-1/4 bg-gray-100 rounded-lg p-8 shadow-md">
    <form class="space-y-6">
      <div class="flex flex-col">
        <label for="id_search_keyword" class="font-semibold text-lg">Recherche par mot(s) clé(s):</label>
        <input type="text" name="search_keyword" id="id_search_keyword" placeholder="Entrez un mot clé"
               class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg">
      </div>
      
      <div class="flex flex-col">
        <label for="id_search_by_groups" class="font-semibold text-lg">Recherche par groupe ou sous-groupe:</label>
        <input type="text" name="search_by_groups" id="id_search_by_groups" placeholder="Entrez un groupe"
               class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg">
      </div>
      
      <div class="flex flex-col">
        <label for="id_search_set" class="font-semibold text-lg">Recherche par référence:</label>
        <input list="ref" name="ref" placeholder="Entrez une référence"
               class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg">
        <datalist id="ref">
          <option value="ref1">
          <option value="ref2">
          <option value="ref3">
          <option value="ref4">
          <option value="ref5">
        </datalist>
      </div>
      
      <button type="submit" class="w-full bg-teal-500 text-white py-3 rounded-lg hover:bg-teal-600 text-lg">Rechercher</button>
    </form>
  </aside>

  <section class="w-full md:w-3/4 grid grid-cols-2 sm:grid-cols-3 sm:min-w-[600px] lg:grid-cols-4 lg:min-w-[900px] xl:min-w-[1200px] gap-6 p-4 border border-gray-300 rounded-lg shadow-md max-h-[500px] overflow-y-auto">
    {#each Object.values(images) as { default: imageUrl }}
    <div class="relative group">
      <img class="w-full object-cover rounded-lg" src={imageUrl} alt="group_img"/>
      <div class="absolute bottom-0 left-0 p-2 text-white text-xs">
        Your overlay text here
      </div>
    </div>
    {/each}
  </section>
</main>

<div class="container mx-auto w-full bg-gray-50 p-6 shadow-lg flex justify-center items-center space-x-6">
  <div class="flex flex-col items-center">
    <span class="text-teal-600 font-semibold text-2xl">Set/commande</span>
    <label for="id_ref" class="text-gray-700 text-lg">Rechercher une commande:</label>
  </div>
  
  <input list="commandes" name="commandes" placeholder="Entrez un numéro de commande"
         class="p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg">
  <datalist id="commandes">
    <option value="#123456">
    <option value="#123457">
    <option value="#123458">
    <option value="#123459">
  </datalist>
  
  <button class="bg-teal-500 text-white py-3 px-6 rounded-lg hover:bg-teal-600 text-lg">Rechercher</button>
</div>
