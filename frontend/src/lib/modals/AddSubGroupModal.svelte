<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { selectedGroup } from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { apiFetch } from "$lib/utils/fetch";
    import { createEventDispatcher } from "svelte";

    const {
        isOpen,
        close, 
    } = $props();

    
    let groupName = $selectedGroup;
    console.log("Group : " + groupName);
    let name = $state("");
    let picture = $state("");
    let characteristics = $state([""]);
    const dispatch = createEventDispatcher();

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
        name = "";
        picture = "";
        characteristics = [""];
    }

    function cancel() {
        dispatch("cancel", { message: "Operation cancelled." });
        close();
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
        close();
        if (response.ok) {
            dispatch("success", { message: "Ajout sous-groupe réussi!" });
        } else {
            dispatch("error", { message: "Impossible d'ajouter un sous-groupe." });
        }
    }

</script>

{#if isOpen}
    <div
    class="fixed inset-0 z-10 box-border bg-[rgba(0,0,0,0.8)] flex items-center justify-center p-[50px] rounded-[30px] flex-col"
    aria-labelledby="modal-title"
    role="dialog"
    aria-modal="true"
    >
        <div class="absolute inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>
        <div
        class="relative transform overflow-auto rounded-lg text-left transition-all sm:w-full sm:max-w-lg lg:max-w-4xl"
        style="max-height: 80vh;">
            <form onsubmit={submitForm} preventDefault class="bg-gray-100 p-6 rounded-lg shadow-lg">
                <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter un sous-groupe d'instruments</h2>
        
                <label for="subgroup_name" class="font-semibold text-lg">Nom du sous-groupe:</label>
                <input type="text" bind:value={name} placeholder="Entrez le nom du sous-groupe"
                    class="w-full p-2 mt-1 mb-3 border rounded">
                
                <label for="characateristics" class="font-semibold text-lg">Caractéristiques:</label>   
                {#if characteristics !== null}
                    {#each characteristics as char, index}
                        <div class="flex items-center mt-2">
                            <input type="text" bind:value={characteristics[index]} 
                            placeholder="Entrez une caractéristique"
                                class="flex-1 p-2 border rounded" oninput={(e) => updateCharacteristic(index, e.target.value)}>
                        </div>
                    {/each}
                {/if}
                
                <button type="button" onclick={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">Ajouter une caractéristique</button>
                
                <div class="mt-3">
                <label for="picture" class="font-semibold text-lg">Ajouter une image du sous-groupe:</label>
                <input type="file" bind:value={picture} class="w-full p-2 mt-1 border rounded">
                </div>
        
                <button type="submit" class="mt-4 px-4 py-2 bg-teal-500 text-white rounded ">Enregistrer</button>
                <button type="button" onclick={erase} class="mt-4 px-4 py-2 bg-red-500 text-white rounded">Effacer</button>
                <button type="button" onclick={cancel} class="mt-4 px-4 py-2 bg-gray-500 text-white rounded">Annuler</button>
            </form>
        </div>
</div>
{/if}