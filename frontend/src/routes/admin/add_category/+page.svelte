<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { groups, subGroups, selectedGroup, selectedSubGroup, selectedGroupIndex, selectedSubGroupIndex, reload } from "$lib/stores/searches";
    import { _ } from "svelte-i18n";

    let id = "";
    let groupName = "";
    let subGroupName = "";
    let name = "";
    let fct = "";
    let shape = "";
    let lenAbrv = "";
    let pictureId = "";
    $selectedGroupIndex = $groups.indexOf($selectedGroup);
    $selectedSubGroupIndex = $subGroups.indexOf($selectedSubGroup);
    let groupId = $selectedGroupIndex + 1;
    let subGroupId = $selectedSubGroupIndex + 1;

    const dispatch = createEventDispatcher();

    async function submitForm() {
        if(groupId == 0 || subGroupId == 0) {
            dispatch("Erreur", { message: $_('admin.add_cat.dispatch') });
            goto("../../searches");
        }
        const response = await apiFetch('/api/category/group/' + groupId + '/subgroup/' + subGroupId + '/add', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                id,
                groupName,
                subGroupName,
                name,
                function: fct,
                shape,
                lenAbrv,
                pictureId
            })
        });
        
        if (response.ok) {
            dispatch("Succès", { message: $_('admin.add_cat.added_cat') });
            reload.set(true);
            goto("../../searches");
        } else {
            dispatch("Erreur", { message: $_('admin.add_cat.not_poss') });
        }
    }

    function cancel() {
        dispatch("Annulé", { message: $_('admin.add_cat.op_cancel') });
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
        <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">{$_('admin.add_cat.add_typ')}</h2>
        
        <label for="group" class="font-semibold text-lg">{$_('admin.add_cat.group')}</label>
        <input type="text" bind:value={groupName} placeholder={$_('admin.add_cat.enter_group')}
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="subgroup" class="font-semibold text-lg">{$_('admin.add_cat.subgroup')}</label>
        <input type="text" bind:value={subGroupName} placeholder={$_('admin.add_cat.enter_subgroup')}
            class="w-full p-2 mt-1 mb-3 border rounded">
        
        <label for="fct" class="font-semibold text-lg">{$_('admin.add_cat.fct')}</label>
        <input type="text" bind:value={fct} placeholder={$_('admin;add_cat.enter_fct')}
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="name" class="font-semibold text-lg">{$_('admin.add_cat.type_name')}</label>
        <input type="text" bind:value={name} placeholder={$_('admin.add_cat.enter_type')}
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="shape" class="font-semibold text-lg">{$_('admin.add_cat.shape')}</label>
        <input type="text" bind:value={shape} placeholder={$_('admin.add_cat.enter_shape')}
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="lenAbrv" class="font-semibold text-lg">{$_('admin.add_cat.abbreviation')}</label>
        <input type="text" bind:value={lenAbrv} placeholder={$_('admin.add_cat.enter_abb')}
            class="w-full p-2 mt-1 mb-3 border rounded">

        <label for="id" class="font-semibold text-lg">{$_('admin.add_cat.source')}</label>
        <input type="text" bind:value={pictureId} placeholder={$_('admin.add_cat.enter_id')}
            class="w-full p-2 mt-1 mb-3 border rounded">

        <div class="flex gap-4 mt-4">
            <button type="submit" class="bg-green-500 text-white p-2 rounded hover:bg-green-700">{$_('admin.add_cat.add')}</button>
            <button type="button" class="bg-red-500 text-white p-2 rounded hover:bg-red-700" on:click={erase}>{$_('admin.add_cat.erase')}</button>
            <button type="button" class="bg-blue-500 text-white p-2 rounded hover:bg-blue-700" on:click={cancel}>{$_('admin.add_cat.cancel')}</button>
        </div>
    </form>
</main>
