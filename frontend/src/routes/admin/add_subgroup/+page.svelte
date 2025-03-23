<script>
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    import { createEventDispatcher } from "svelte";
    import { selectedGroup } from "$lib/stores/searches";
    import { apiFetch } from "$lib/utils/fetch";
    
    let groupName = $selectedGroup;
    console.log("Group : " + groupName);
    let name = "";
    let picture = "";
    let characteristics = [""];
    const dispatch = createEventDispatcher();

    function addCharacteristic() {
        characteristics = [...characteristics, ""];
    }

    function updateCharacteristic(index, value) {
        characteristics[index] = value;
    }

    function erase() {
        groupName = "";
        name = "";
        picture = "";
        characteristics = [""];
    }

    function cancel() {
        dispatch("cancel", { message: "Operation cancelled." });
        goto("../../..");
    }

    async function submitForm() {
        if (characteristics === null) {
            characteristics = [""];
        }
        else {
            characteristics.join(", ");
        }
        const response = await apiFetch('/api/subgroups/group/' + groupName, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                name, 
                groupName,
                characteristics,
                picture 
            })
        });
        
        if (response.ok) {
            dispatch("success", { message: "Ajout sous-groupe réussi!" });
        } else {
            dispatch("error", { message: "Impossible d'ajouter un sous-groupe." });
        }
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <form on:submit|preventDefault={submitForm} class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter un sous-groupe d'instruments</h2>

        <label for="subgroup_name" class="font-semibold text-lg">Nom du sous-groupe:</label>
        <input type="text" bind:value={name} placeholder="Entrez le nom du sous-groupe"
            class="w-full p-2 mt-1 mb-3 border rounded">
        
        <label for="characateristics" class="font-semibold text-lg">Caractéristiques:</label>   
        {#if characteristics !== null}
            {#each characteristics as char, index}
                <div class="flex items-center mt-2">
                    <input type="text" bind:value={characteristics[index]} placeholder="Entrez une caractéristique"
                        class="flex-1 p-2 border rounded" on:input={(e) => updateCharacteristic(index, e.target.value)}>
                </div>
            {/each}
        {/if}
        
        <button type="button" on:click={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">Ajouter une caractéristique</button>
        
        <div class="mt-3">
        <label for="picture" class="font-semibold text-lg">Ajouter une image du sous-groupe:</label>
        <input type="file" bind:value={picture} class="w-full p-2 mt-1 border rounded">
        </div>

        <button type="submit" class="mt-4 px-4 py-2 bg-teal-500 text-white rounded ">Enregistrer</button>
        <button type="button" on:click={erase} class="mt-4 px-4 py-2 bg-red-500 text-white rounded">Effacer</button>
        <button type="button" on:click={cancel} class="mt-4 px-4 py-2 bg-gray-500 text-white rounded">Annuler</button>
    </form>
</main>
