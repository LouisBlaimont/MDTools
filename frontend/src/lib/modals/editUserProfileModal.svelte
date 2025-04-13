<script>
    import { apiFetch } from "$lib/utils/fetch";
    import { reload } from "$lib/stores/searches";
    import { goto } from "$app/navigation";
    import { _ } from "svelte-i18n";

    export let isOpen;
    export let close;
    export let user;

    let previousName = user.name;
    let username = user.name;
    let email = user.email;
    let active = user.expiresAt > Date.now();

    async function handleSave() {
        try {
            const response = await apiFetch(`/api/user/username/` + previousName, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, email, active }),
            });
            if (!response.ok) throw new Error("Failed to update user");
            reload.set(true);
            close();
        } catch (error) {
            console.error("Error:", error);
        }
        goto("/users");
    }

    async function handleDelete() {
        if (confirm("Êtes-vous sûr de vouloir supprimer ce compte ?")) {
            try {
                const response = await apiFetch(`/api/user/${user.userId}`, {
                    method: "DELETE",
                });
                if (!response.ok) throw new Error("Failed to delete user");
                reload.set(true);
                close();
                goto("/users");
            } catch (error) {
                console.error("Error:", error);
            }
        }
    }
</script>

{#if isOpen}
<div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
    <div class="fixed inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>
    <div class="fixed inset-0 z-10 w-screen overflow-y-auto">
        <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
            <div class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
                <form class="max-w-4xl mx-auto" on:submit|preventDefault={handleSave}>
                    <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                        <h3 class="text-base font-semibold text-gray-900" id="modal-title">{$_('modals.edit_user.title')}</h3>
                        <div class="mt-2">
                            <!-- svelte-ignore a11y_label_has_associated_control -->
                            <label class="block my-2 text-sm font-medium text-gray-900">{$_('modals.edit_user.name')}</label>
                            <input type="text" bind:value={username} class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5" />

                            <!-- svelte-ignore a11y_label_has_associated_control -->
                            <label class="block my-2 text-sm font-medium text-gray-900">{$_('modals.edit_user.email')}</label>
                            <input type="email" bind:value={email} class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5" />

                            <!-- svelte-ignore a11y_label_has_associated_control -->
                            {#if user.roles.includes("ROLE_ADMIN")}
                                <label class="block my-2 text-sm font-medium text-gray-900">{$_('modals.edit_user.active')}</label>
                                <select bind:value={active} class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5">
                                    <option value={true}>{$_('modals.edit_user.yes')}</option>
                                    <option value={false}>{$_('modals.edit_user.no')}</option>
                                </select>
                            {/if}
                        </div>
                    </div>
                    <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:gap-4 sm:px-6">
                        <button type="submit" class="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500 sm:ml-3 sm:w-auto">{$_('modals.edit_user.button.save')}</button>
                        <button type="button" class="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50 sm:mt-0 sm:w-auto" on:click={close}>{$_('modals.edit_user.button.cancel')}</button>
                        <button type="button" class="mt-3 inline-flex w-full justify-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-red-500 sm:mt-0 sm:w-auto" on:click={handleDelete}>{$_('modals.edit_user.button.delete')}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
{/if}
