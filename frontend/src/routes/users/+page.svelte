<script>
    import { goto } from "$app/navigation";
    import { user } from "$lib/stores/user_stores";
    import { _ } from "svelte-i18n";
    import { apiFetch } from "$lib/utils/fetch";

    let isEditing = false;
    let updatedName = $user.name;
    let updatedEmail = $user.email;
    let updatedJobPosition = $user.jobPosition;
    let updatedWorkplace = $user.workplace;
    let updatedRoleName = $user.roleName;

    function goBack() {
        goto("../");
    }

    async function saveChanges() {
        try {
            const updateDate = new Date().toISOString(); // Add updateDate
            const response = await apiFetch(`/api/user/username/${$user.name}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    name: updatedName, 
                    email: updatedEmail, 
                    jobPosition: updatedJobPosition, 
                    workplace: updatedWorkplace, 
                    roleName: updatedRoleName, 
                    updateDate 
                }),
            });
            if (!response.ok) throw new Error("Failed to update user");
            else {
                $user.name = updatedName;
                $user.email = updatedEmail;
                $user.jobPosition = updatedJobPosition;
                $user.workplace = updatedWorkplace;
                $user.roleName = updatedRoleName;
                $user.updateDate = updateDate; 
            }
            isEditing = false;
        } catch (error) {
            console.error("Error:", error);
        }
    }

    function cancelEdit() {
        updatedName = $user.name;
        updatedEmail = $user.email;
        updatedJobPosition = $user.jobPosition;
        updatedWorkplace = $user.workplace;
        updatedRoleName = $user.roleName;
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
                <div class="grid grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.name')}</label>
                    <input type="text" bind:value={updatedName} class="w-full p-2 border rounded" />
                </div>
                <div class="grid grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.email')}</label>
                    <input type="email" bind:value={updatedEmail} class="w-full p-2 border rounded" />
                </div>
                <div class="grid grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.job_position')}</label>
                    <input type="text" bind:value={updatedJobPosition} class="w-full p-2 border rounded" />
                </div>
                <div class="grid grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.workplace')}</label>
                    <input type="text" bind:value={updatedWorkplace} class="w-full p-2 border rounded" />
                </div>
                <div class="grid grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.role')}</label>
                    <input type="text" bind:value={updatedRoleName} class="w-full p-2 border rounded" />
                </div>
                <div class="flex gap-4 justify-center mt-4">
                    <button class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={saveChanges}>{$_('profile_page.button.save')}</button>
                    <button class="bg-gray-500 text-white p-2 rounded hover:bg-gray-700" on:click={cancelEdit}>{$_('profile_page.button.cancel')}</button>
                </div>
            </div>
        {:else}
            <div class="grid grid-cols-2 gap-4 text-left">
                <p class="text-lg font-medium">{$_('profile_page.name')}</p>
                <p class="text-lg">{$user.name}</p>
                <p class="text-lg font-medium">{$_('profile_page.email')}</p>
                <p class="text-lg">{$user.email}</p>
                <p class="text-lg font-medium">{$_('profile_page.job_position')}</p>
                <p class="text-lg">{$user.jobPosition}</p>
                <p class="text-lg font-medium">{$_('profile_page.workplace')}</p>
                <p class="text-lg">{$user.workplace}</p>
                <p class="text-lg font-medium">{$_('profile_page.role')}</p>
                <p class="text-lg">{$user.roleName}</p>
            </div>
            <div class="flex gap-4 mt-6 justify-center">
                <button class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={goBack}>{$_('profile_page.button.back')}</button>
                <button class="bg-yellow-500 text-white p-2 rounded hover:bg-yellow-700" on:click={() => (isEditing = true)}>{$_('profile_page.button.edit_profile')}</button>
            </div>
        {/if}
    </div>
</main>
