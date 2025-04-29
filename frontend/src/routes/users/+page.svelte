<script>
    import { goto } from "$app/navigation";
    import { user } from "$lib/stores/user_stores";
    import { _ } from "svelte-i18n";
    import { apiFetch } from "$lib/utils/fetch";
    import { toast } from "@zerodevx/svelte-toast";
    import Icon from "@iconify/svelte";
    import { PUBLIC_API_URL } from "$env/static/public";

    let isEditing = false;
    let updatedJobPosition = $user.jobPosition;
    let updatedWorkplace = $user.workplace;
    let updatedRoleName = $user.roleName;

    function goBack() {
        goto("../");
    }

    async function saveChanges() {
        try {
            const updateDate = new Date().toISOString();
            const response = await apiFetch(`/api/user/username/${$user.name}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ 
                    jobPosition: updatedJobPosition, 
                    workplace: updatedWorkplace, 
                    roleName: updatedRoleName, 
                    updateDate 
                }),
            });
            if (!response.ok) throw new Error("Failed to update user");
            else {
                $user.jobPosition = updatedJobPosition;
                $user.workplace = updatedWorkplace;
                $user.roleName = updatedRoleName;
                $user.updateDate = updateDate; 
                toast.push($_('profile_page.success'));
            }
            isEditing = false;
        } catch (error) {
            console.error("Error:", error);
            toast.push($_('profile_page.error'));
        }
    }

    function cancelEdit() {
        updatedJobPosition = $user.jobPosition;
        updatedWorkplace = $user.workplace;
        updatedRoleName = $user.roleName;
        isEditing = false;
    }

    function startEditing() {
        isEditing = true;
    }
</script>

<svelte:head>
  <title>{$_('profile_page.title')}</title>
</svelte:head>

<div class="flex flex-row justify-center items-start space-x-8 px-5 py-6 max-w-screen-xl mx-auto">
    <div class="w-full md:w-2/3 lg:w-1/2 bg-white border border-gray-300 rounded-lg p-8 shadow-md">
        <div class="flex items-center mb-6">
            <div class="w-20 h-20 rounded-full border-4 border-teal-500 overflow-hidden mr-4">
                <img
                    alt="Profile logo"
                    src="logo-profile.png"
                    class="w-full h-full object-cover"
                />
            </div>
            <h2 class="text-2xl font-bold text-teal-500">{$_('profile_page.title')}</h2>
            
            {#if !isEditing}
                <button 
                    class="ml-auto flex items-center px-4 py-2 bg-gray-100 hover:bg-yellow-500 rounded-lg text-lg"
                    aria-label="edit profile"
                    onclick={() => startEditing()}
                >
                    <Icon icon="material-symbols:edit" width="24" height="24" />
                </button>
            {/if}
        </div>

        {#if isEditing}
            <div class="space-y-4">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.name')}</label>
                    <input type="text" value={$user.name} class="w-full p-3 border border-gray-300 rounded-lg bg-gray-100" readonly />
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.email')}</label>
                    <input type="text" value={$user.email} class="w-full p-3 border border-gray-300 rounded-lg bg-gray-100" readonly />
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.job_position')}</label>
                    <input type="text" bind:value={updatedJobPosition} class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500" />
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.workplace')}</label>
                    <input type="text" bind:value={updatedWorkplace} class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500" />
                </div>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-left items-center">
                    <label class="text-lg font-medium">{$_('profile_page.role')}</label>
                    <input type="text" bind:value={updatedRoleName} class="w-full p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500" />
                </div>
                <div class="flex gap-4 justify-center mt-6">
                    <button class="flex items-center px-4 py-3 bg-teal-500 text-white rounded-lg hover:bg-teal-600" onclick={saveChanges}>
                        <Icon icon="material-symbols:save" width="20" height="20" class="mr-2" />
                        {$_('profile_page.button.save')}
                    </button>
                    <button class="flex items-center px-4 py-3 bg-gray-500 text-white rounded-lg hover:bg-gray-600" onclick={cancelEdit}>
                        <Icon icon="material-symbols:cancel" width="20" height="20" class="mr-2" />
                        {$_('profile_page.button.cancel')}
                    </button>
                </div>
            </div>
        {:else}
            <div class="bg-gray-100 p-6 rounded-lg">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 text-left">
                    <div>
                        <p class="text-lg font-medium text-teal-600">{$_('profile_page.name')}</p>
                        <p class="text-lg">{$user.name}</p>
                    </div>
                    <div>
                        <p class="text-lg font-medium text-teal-600">{$_('profile_page.email')}</p>
                        <p class="text-lg">{$user.email}</p>
                    </div>
                    <div>
                        <p class="text-lg font-medium text-teal-600">{$_('profile_page.job_position')}</p>
                        <p class="text-lg">{$user.jobPosition || "-"}</p>
                    </div>
                    <div>
                        <p class="text-lg font-medium text-teal-600">{$_('profile_page.workplace')}</p>
                        <p class="text-lg">{$user.workplace || "-"}</p>
                    </div>
                    <div>
                        <p class="text-lg font-medium text-teal-600">{$_('profile_page.role')}</p>
                        <p class="text-lg">{$user.roleName || "-"}</p>
                    </div>
                </div>
            </div>
            <div class="flex gap-4 mt-6 justify-center">
                <button class="flex items-center px-4 py-3 bg-teal-500 text-white rounded-lg hover:bg-teal-600" onclick={goBack}>
                    <Icon icon="material-symbols:arrow-back-ios-new-rounded" width="20" height="20" class="mr-2" />
                    {$_('profile_page.button.back')}
                </button>
            </div>
        {/if}
    </div>
</div>