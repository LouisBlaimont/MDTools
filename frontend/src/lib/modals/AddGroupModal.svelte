<script>
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    import { createEventDispatcher } from "svelte";
    import { apiFetch } from "$lib/utils/fetch";
    
    const {
        isOpen, // Indicates if the modal is open
        close,  // Function to close the modal
    } = $props();

    let groupName = "";
    let subGroupName = "";
    let picture = "";
    let characteristics = [""];
    const dispatch = createEventDispatcher();

    // Draggable modal functionality
    let posX = 0, posY = 0, offsetX = 0, offsetY = 0, isDragging = false;

    function startDrag(event) {
        isDragging = true;
        offsetX = event.clientX - posX;
        offsetY = event.clientY - posY;
    }

    function drag(event) {
        if (isDragging) {
            posX = event.clientX - offsetX;
            posY = event.clientY - offsetY;
        }
    }

    function stopDrag() {
        isDragging = false;
    }

    function addCharacteristic() {
        characteristics = [...characteristics, ""];
    }

    function updateCharacteristic(index, value) {
        characteristics[index] = value;
    }

    function erase() {
        groupName = "";
        subGroupName = "";
        picture = "";
        characteristics = [""];
    }

    function cancel() {
        dispatch("cancel", { message: "Operation cancelled." });
        goto("../../..");
    }

    async function submitForm() {
        const response = await apiFetch('/api/groups', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                groupName, 
                subGroupName, 
                characteristics: characteristics.join(", "),
                picture 
            })
        });
        
        if (response.ok) {
            dispatch("success", { message: "Group added successfully!" });
        } else {
            dispatch("error", { message: "Failed to add group." });
        }
    }
</script>

{#if isOpen}
    <div class="flex flex-col items-center w-full p-6 mt-3">
        <form on:submit|preventDefault={submitForm} class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
            <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter un groupe d'instruments</h2>
            
            <label for="group_name" class="font-semibold text-lg">Nom du groupe:</label>
            <input type="text" bind:value={groupName} placeholder="Entrez le nom du groupe"
                class="w-full p-2 mt-1 mb-3 border rounded">

            <label for="subgroup_name" class="font-semibold text-lg">Nom du sous-groupe:</label>
            <input type="text" bind:value={subGroupName} placeholder="Entrez le nom du sous-groupe"
                class="w-full p-2 mt-1 mb-3 border rounded">
            
            <label for="characateristics" class="font-semibold text-lg">Caractéristiques:</label>   
            {#each characteristics as char, index}
                <div class="flex items-center mt-2">
                    <input type="text" bind:value={characteristics[index]} placeholder="Entrez une caractéristique"
                        class="flex-1 p-2 border rounded" on:input={(e) => updateCharacteristic(index, e.target.value)}>
                </div>
            {/each}
            
            <button type="button" on:click={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">Ajouter une caractéristique</button>
            
            <div class="mt-3">
            <label for="picture" class="font-semibold text-lg">Ajouter une image du groupe:</label>
            <input type="file" bind:value={picture} class="w-full p-2 mt-1 border rounded">
            </div>

            <button type="submit" class="mt-4 px-4 py-2 bg-teal-500 text-white rounded ">Enregistrer</button>
            <button type="button" on:click={erase} class="mt-4 px-4 py-2 bg-red-500 text-white rounded">Effacer</button>
            <button type="button" on:click={cancel} class="mt-4 px-4 py-2 bg-gray-500 text-white rounded">Annuler</button>
        </form>
    </div>
{/if}