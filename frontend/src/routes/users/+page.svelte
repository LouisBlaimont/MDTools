<script>
    import { goto } from "$app/navigation";
    import { user } from "$lib/stores/user_stores";
    import { modals } from "svelte-modals";
    import EditUserProfileModal from "$lib/modals/editUserProfileModal.svelte";
    import { _ } from "svelte-i18n";

    let email = $user?.email;
    let name = $user?.name;

    function goBack() {
        goto("../");
    }

    function editProfile() {
        modals.open(EditUserProfileModal, { user: $user });
    }
</script>

<main class="flex flex-col items-center w-full p-6 mt-3">
    <div class="w-1/2 bg-gray-100 p-6 rounded-lg shadow-lg">
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-4">{$_('profile_page.title')}</h2>
        
        <p class="text-lg"><strong>{$_('profile_page.name')}:</strong> {name}</p>
        <p class="text-lg"><strong>{$_('profile_page.email')}:</strong> {email}</p>

        <div class="flex gap-4 mt-6">
            <button class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={goBack}>{$_('profile_page.button.back')}</button>
            <button class="bg-yellow-500 text-white p-2 rounded hover:bg-yellow-700" on:click={editProfile}>{$_('profile_page.button.edit_profile')}</button>
        </div>
    </div>
</main>
