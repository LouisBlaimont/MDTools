<script>
    import { goto } from "$app/navigation";
    import { user } from "$lib/stores/user_stores";
    import { _ } from "svelte-i18n";
    import { apiFetch } from "$lib/utils/fetch";

    let email = $user?.email;
    let name = $user?.name;
    let isEditing = false;
    let updatedName = name;
    let updatedEmail = email;

    function goBack() {
        goto("../");
    }

    async function saveChanges() {
        try {
            const response = await apiFetch(`/api/user/${$user.userId}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name: updatedName, email: updatedEmail }),
            });
            if (!response.ok) throw new Error("Failed to update user");
            name = updatedName;
            email = updatedEmail;
            isEditing = false;
        } catch (error) {
            console.error("Error:", error);
        }
    }

    function cancelEdit() {
        updatedName = name;
        updatedEmail = email;
        isEditing = false;
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <div class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg text-center">
        <div class="w-24 h-24 rounded-full border-4 border-black mx-auto mb-4 overflow-hidden">
            <img
                alt="Profile logo"
                src="logo-profile.png"
                class="w-full h-full object-cover"
            />
        </div>
        <h2 class="text-2xl font-bold text-teal-500 mb-4">{$_('profile_page.title')}</h2>

        {#if isEditing}
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-gray-700">{$_('profile_page.name')}</label>
                    <input type="text" bind:value={updatedName} class="w-full p-2 border rounded" />
                </div>
                <div>
                    <label class="block text-sm font-medium text-gray-700">{$_('profile_page.email')}</label>
                    <input type="email" bind:value={updatedEmail} class="w-full p-2 border rounded" />
                </div>
                <div class="flex gap-4 justify-center mt-4">
                    <button class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={saveChanges}>{$_('profile_page.button.save')}</button>
                    <button class="bg-gray-500 text-white p-2 rounded hover:bg-gray-700" on:click={cancelEdit}>{$_('profile_page.button.cancel')}</button>
                </div>
            </div>
        {:else}
            <p class="text-lg"><strong>{$_('profile_page.name')}:</strong> {name}</p>
            <p class="text-lg"><strong>{$_('profile_page.email')}:</strong> {email}</p>
            <div class="flex gap-4 mt-6 justify-center">
                <button class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={goBack}>{$_('profile_page.button.back')}</button>
                <button class="bg-yellow-500 text-white p-2 rounded hover:bg-yellow-700" on:click={() => (isEditing = true)}>{$_('profile_page.button.edit_profile')}</button>
            </div>
        {/if}
    </div>
</main>
