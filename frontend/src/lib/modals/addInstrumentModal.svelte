<script>
    import { createEventDispatcher } from "svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { reload } from "$lib/stores/searches";

    export let isOpen = false;
    export let close;

    let reference = "";
    let supplier = "";
    let supplierDescription = "";
    let price = "";
    let alt = "";
    let obsolete = false;
    let id = "";
    let categoryId = null;

    const dispatch = createEventDispatcher();

    async function submitForm() {
        const response = await apiFetch('/api/instrument', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                reference, 
                supplier, 
                categoryId,
                supplierDescription, 
                price, 
                alt, 
                obsolete, 
                id 
            })
        });

        if (response.ok) {
            dispatch("success", { message: "Instrument ajouté!" });
            reload.set(true);
            close();
        } else {
            dispatch("error", { message: "Impossible d'ajouter un instrument." });
        }
    }

    function erase() {
        reference = "";
        supplier = "";
        supplierDescription = "";
        price = "";
        alt = "";
        obsolete = false;
        id = "";
    }
</script>

{#if isOpen}
<div class="fixed inset-0 z-10 flex items-center justify-center bg-gray-500 bg-opacity-50">
    <div class="bg-white rounded-lg shadow-lg w-1/2">
        <div class="p-4 border-b">
            <h2 class="text-xl font-bold">Ajouter un instrument</h2>
        </div>
        <form on:submit|preventDefault={submitForm} class="p-4">
            <label class="block mb-2">Référence:</label>
            <input type="text" bind:value={reference} class="w-full p-2 border rounded mb-4" />

            <label class="block mb-2">Fournisseur:</label>
            <input type="text" bind:value={supplier} class="w-full p-2 border rounded mb-4" />

            <label class="block mb-2">Description du fournisseur:</label>
            <input type="text" bind:value={supplierDescription} class="w-full p-2 border rounded mb-4" />

            <label class="block mb-2">Prix:</label>
            <input type="text" bind:value={price} class="w-full p-2 border rounded mb-4" />

            <label class="block mb-2">Alternatives:</label>
            <input type="text" bind:value={alt} class="w-full p-2 border rounded mb-4" />

            <label class="block mb-2">Obsolescence:</label>
            <div class="flex gap-4 mb-4">
                <label><input type="radio" bind:group={obsolete} value={true} /> Oui</label>
                <label><input type="radio" bind:group={obsolete} value={false} /> Non</label>
            </div>

            <label class="block mb-2">Source de l'image:</label>
            <input type="text" bind:value={id} class="w-full p-2 border rounded mb-4" />

            <div class="flex justify-end gap-4">
                <button type="button" on:click={erase} class="bg-red-500 text-white px-4 py-2 rounded">Effacer</button>
                <button type="button" on:click={close} class="bg-gray-500 text-white px-4 py-2 rounded">Annuler</button>
                <button type="submit" class="bg-green-500 text-white px-4 py-2 rounded">Ajouter</button>
            </div>
        </form>
    </div>
</div>
{/if}
