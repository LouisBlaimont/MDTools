<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";

    let username = "";
    let email = "";
    let roles = ["ROLE_USER"];
    const dispatch = createEventDispatcher();

    async function submitForm() {
        const response = await apiFetch('/api/user', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, email, roles })
        });

        if (response.ok) {
            dispatch("Succès", { message: "Utilisateur ajouté!" });
            goto("/users");
        } else {
            dispatch("Erreur", { message: "Impossible d'ajouter un utilisateur." });
        }
    }

    function cancel() {
        dispatch("Annulé", { message: "Operation annulée." });
        goto("/users");
    }

    function erase() {
        username = "";
        email = "";
        roles = ["ROLE_USER"];
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <form on:submit|preventDefault={submitForm} class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">Ajouter un utilisateur</h2>
        
        <label for="username" class="font-semibold text-lg">Nom d'utilisateur:</label>
        <input type="text" bind:value={username} placeholder="Entrez le nom d'utilisateur"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="email" class="font-semibold text-lg">Email:</label>
        <input type="email" bind:value={email} placeholder="Entrez l'email"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <!-- <label for="roles" class="font-semibold text-lg">Rôles:</label>
        <input type="text" bind:value={roles} placeholder="Entrez les rôles séparés par des virgules"
            class="w-full p-2 mt-1 mb-3 border rounded"> -->

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">Ajouter</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>Éffacer</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>Annuler</button>
        </div>
    </form>
</main>
