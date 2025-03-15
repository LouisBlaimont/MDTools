<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { PUBLIC_API_URL } from "$env/static/public";
    import { selectedGroup, selectedSubGroup, reload, subGroups } from "$lib/stores/searches";

    let id = "";
    let groupName = "";
    let subGroupName = "";
    let name = "";
    let fct = "";
    let shape = "";
    let lenAbrv = "";
    let pictureId = "";
    const dispatch = createEventDispatcher();

    async function submitForm() {
        const response = await fetch('http://localhost:8080/api/category/group/' + groupId + 'subgroup/' + subGroupId + 'add', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                id,
                groupName,
                subGroupName,
                name,
                fct,
                shape,
                lenAbrv,
                pictureId
            })
        });
        
        if (response.ok) {
            dispatch("Succès", { message: "Instrument ajouté!" });
            reload.set(true);
            goto("../../searches");
        } else {
            dispatch("Erreur", { message: "Impossible d'ajouter un instrument." });
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
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter un instrument</h2>
        
        <label for="reference" class="font-semibold text-lg">Groupe:</label>
        <input type="text" bind:value={groupName} placeholder="Enter the reference"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="brand" class="font-semibold text-lg">Nom du sous-groupe:</label>
        <input type="text" bind:value={subGroupName} placeholder="Enter the brand"
            class="w-full p-2 mt-1 mb-3 border rounded">
        
        <label for="supplierDescription" class="font-semibold text-lg">Fonction:</label>
        <input type="text" bind:value={fct} placeholder="Enter the description"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="price" class="font-semibold text-lg">Nom de la catégorie:</label>
        <input type="text" bind:value={name} placeholder="Enter the price"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="alt" class="font-semibold text-lg">Forme:</label>
        <input type="text" bind:value={shape} placeholder="Enter the alt"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="obsolete" class="font-semibold text-lg">Abréviation:</label>
        <input type="text" bind:value={lenAbrv} placeholder="Enter if obsolete"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="id" class="font-semibold text-lg">Source de l'image:</label>
        <input type="text" bind:value={pictureId} placeholder="Enter the image source"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">Ajouter</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>Éffacer</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>Annuler</button>
        </div>
    </form>
</main>
