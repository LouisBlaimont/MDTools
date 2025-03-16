<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { PUBLIC_API_URL } from "$env/static/public";
    import { groups, subGroups, selectedGroup, selectedSubGroup, selectedGroupIndex, selectedSubGroupIndex, reload } from "$lib/stores/searches";

    let id = "";
    let groupName = "";
    let subGroupName = "";
    let name = "";
    let fct = "";
    let shape = "";
    let lenAbrv = "";
    let pictureId = "";
    $selectedGroupIndex = $groups.indexOf($selectedGroup);
    $selectedSubGroupIndex = $subGroups.indexOf($selectedSubGroup);
    let groupId = $selectedGroupIndex + 1;
    let subGroupId = $selectedSubGroupIndex + 1;

    const dispatch = createEventDispatcher();

    async function submitForm() {
        if(groupId == 0 || subGroupId == 0) {
            dispatch("Erreur", { message: "Veuillez sélectionner un groupe et un sous-groupe." });
            goto("../../searches");
        }
        const response = await fetch('http://localhost:8080/api/category/group/' + groupId + '/subgroup/' + subGroupId + '/add', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                id,
                groupName,
                subGroupName,
                name,
                function: fct,
                shape,
                lenAbrv,
                pictureId
            })
        });
        
        if (response.ok) {
            dispatch("Succès", { message: "Catégorie ajouté!" });
            reload.set(true);
            goto("../../searches");
        } else {
            dispatch("Erreur", { message: "Impossible d'ajouter une catégorie." });
        }
    }

    function cancel() {
        dispatch("Annulé", { message: "Operation annulée." });
        goto("../../searches");
    }

    function erase() {
        reference = "";
        supplier = "";
        supplierDescription = "";
        price = "";
        alt = "";
        obsolete = "";
        id = "";
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <form on:submit|preventDefault={submitForm} class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter une catégorie</h2>
        
        <label for="group" class="font-semibold text-lg">Groupe:</label>
        <input type="text" bind:value={groupName} placeholder="Entrez un groupe"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="subgroup" class="font-semibold text-lg">Nom du sous-groupe:</label>
        <input type="text" bind:value={subGroupName} placeholder="Entrez un sous-groupe"
            class="w-full p-2 mt-1 mb-3 border rounded">
        
        <label for="fct" class="font-semibold text-lg">Fonction:</label>
        <input type="text" bind:value={fct} placeholder="Entrez une fonction"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="name" class="font-semibold text-lg">Nom de la catégorie:</label>
        <input type="text" bind:value={name} placeholder="Entrez un nom de catégorie"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="shape" class="font-semibold text-lg">Forme:</label>
        <input type="text" bind:value={shape} placeholder="Entrez une forme"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="lenAbrv" class="font-semibold text-lg">Abréviation:</label>
        <input type="text" bind:value={lenAbrv} placeholder="Entrez une abréviation"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="id" class="font-semibold text-lg">Source de l'image:</label>
        <input type="text" bind:value={pictureId} placeholder="Entre l'ID de l'image"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">Ajouter</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>Éffacer</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>Annuler</button>
        </div>
    </form>
</main>
