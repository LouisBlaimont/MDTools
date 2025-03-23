<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { category_to_addInstrument, reload } from "$lib/stores/searches";

    let reference = "";
    let supplier = "";
    let supplierDescription = "";
    let price = "";
    let alt = "";
    let obsolete = "";
    let id = "";
    let categoryId = $category_to_addInstrument; // can be a number or null
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
        
        <label for="reference" class="font-semibold text-lg">Référence:</label>
        <input type="text" bind:value={reference} placeholder="Entrez la référence"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="brand" class="font-semibold text-lg">Fournisseur:</label>
        <input type="text" bind:value={supplier} placeholder="Entrez le fournisseur"
            class="w-full p-2 mt-1 mb-3 border rounded">
        
        <label for="supplierDescription" class="font-semibold text-lg">Description du fournisseur:</label>
        <input type="text" bind:value={supplierDescription} placeholder="Entrez la description du fournisseur"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="price" class="font-semibold text-lg">Prix:</label>
        <input type="text" bind:value={price} placeholder="Entrez le prix"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="alt" class="font-semibold text-lg">Alternatives:</label>
        <input type="text" bind:value={alt} placeholder="Entrez les alternatives"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="obsolete" class="font-semibold text-lg">Obsolescence:</label>
        <div class="flex items-center gap-4 mt-1 mb-3">
            <label class="flex items-center">
                <input
                    type="radio"
                    bind:group={obsolete}
                    value={true}
                    class="mr-2"
                />
                Oui
            </label>
            <label class="flex items-center">
                <input
                    type="radio"
                    bind:group={obsolete}
                    value={false}
                    class="mr-2"
                />
                Non
            </label>
        </div>
        <!-- <input type="text" bind:value={obsolete} placeholder="Enter if obsolete"
            class="w-full p-2 mt-1 mb-3 border rounded"> -->

        <label for="id" class="font-semibold text-lg">Source de l'image:</label>
        <input type="text" bind:value={id} placeholder="Entrez l'ID de l'image"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">Ajouter</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>Éffacer</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>Annuler</button>
        </div>
    </form>
</main>
