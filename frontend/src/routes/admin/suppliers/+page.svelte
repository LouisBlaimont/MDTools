<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";

    let supplierName = "";
    let soldByMd = false;
    let closed = false;
    const dispatch = createEventDispatcher();

    async function submitForm() {
        const response = await apiFetch('/api/supplier', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name: supplierName, soldByMD: soldByMd, closed })
        });

        if (response.ok) {
            dispatch("Succès", { message: "Fournisseur ajouté!" });
            goto("/admin/suppliers");
        } else {
            dispatch("Erreur", { message: "Impossible d'ajouter un fournisseur." });
        }
    }

    function cancel() {
        dispatch("Annulé", { message: "Operation annulée." });
        goto("/admin/suppliers");
    }

    function erase() {
        supplierName = "";
        soldByMd = false;
        closed = false;
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <form on:submit|preventDefault={submitForm} class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter un fournisseur</h2>
        
        <label for="supplierName" class="font-semibold text-lg">Nom du fournisseur:</label>
        <input type="text" bind:value={supplierName} placeholder="Entrez le nom du fournisseur"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="soldByMd" class="font-semibold text-lg">Vendu par MD:</label>
        <div class="flex items-center gap-4 mt-1 mb-3">
            <label class="flex items-center">
                <input
                    type="radio"
                    bind:group={soldByMd}
                    value={true}
                    class="mr-2"
                />
                Oui
            </label>
            <label class="flex items-center">
                <input
                    type="radio"
                    bind:group={soldByMd}
                    value={false}
                    class="mr-2"
                />
                Non
            </label>
        </div>

        <label for="closed" class="font-semibold text-lg">Fermé:</label>
        <div class="flex items-center gap-4 mt-1 mb-3">
            <label class="flex items-center">
                <input
                    type="radio"
                    bind:group={closed}
                    value={true}
                    class="mr-2"
                />
                Oui
            </label>
            <label class="flex items-center">
                <input
                    type="radio"
                    bind:group={closed}
                    value={false}
                    class="mr-2"
                />
                Non
            </label>
        </div>

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">Ajouter</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>Éffacer</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>Annuler</button>
        </div>
    </form>
</main>
