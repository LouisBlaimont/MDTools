<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";

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
            dispatch("Succès", { message: $_('add_user.toast1') });
            goto("/users");
        } else {
            dispatch("Erreur", { message: $_('add_user.toast2') });
        }
    }

    function cancel() {
        dispatch("Annulé", { message: $_('add_user.toast3') });
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
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">{$_('add_user.add')}</h2>
        
        <label for="username" class="font-semibold text-lg">{$_('add_user.name')}</label>
        <input type="text" bind:value={username} placeholder="Entrez le nom d'utilisateur"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="email" class="font-semibold text-lg">{$_('add_user.email')}</label>
        <input type="email" bind:value={email} placeholder="Entrez l'email"
            class="w-full p-2 mt-1 mb-3 border rounded">

        <!-- <label for="roles" class="font-semibold text-lg">Rôles:</label>
        <input type="text" bind:value={roles} placeholder="Entrez les rôles séparés par des virgules"
            class="w-full p-2 mt-1 mb-3 border rounded"> -->

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">{$_('add_user.button_add')}</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>{$_('add_user.erase')}</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>{$_('add_user.cancel')}</button>
        </div>
    </form>
</main>
