<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { selectedGroup } from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";
    import { createEventDispatcher } from "svelte";

    export let isOpen = false;
    export let close;
    
    let groupName = "";
    let subGroupName = "";
    let picture = "";
    let characteristics = [""];
    const dispatch = createEventDispatcher();

    function addCharacteristic() {
        characteristics = [...characteristics, ""];
    }

    function updateCharacteristic(index, value) {
        characteristics[index] = value;
    }

    function erase() {
        groupName = "";
        subGroupName = "";
        picture = "";
        characteristics = [""];
    }

    function cancel() {
        dispatch("cancel", { message: $_('modals.add_group.operation') });
        close();
    }

    let posX = 0, posY = 0, offsetX = 0, offsetY = 0, isDragging = false;

    function startDrag(event) {
        isDragging = true;
        offsetX = event.clientX - posX;
        offsetY = event.clientY - posY;
    }

    function drag(event) {
        if (isDragging) {
            posX = event.clientX - offsetX;
            posY = event.clientY - offsetY;
        }
    }

    function stopDrag() {
        isDragging = false;
    }

    async function submitForm() {
        const response = await apiFetch('/api/groups', {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                groupName, 
                subGroupName, 
                characteristics: characteristics.join(", "),
                picture 
            })
        });
        close();
        if (response.ok) {
            dispatch("success", { message: $_('modals.add_group.success') });
        } else {
            dispatch("error", { message: $_('modals.add_group.fail') });
        }
    }

</script>

{#if isOpen}
    <div
        class="relative z-10"
        aria-labelledby="modal-title"
        role="dialog"
        aria-modal="true"
    >
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <div
        class="fixed inset-0 z-10 flex items-center justify-center"
        on:mousemove={drag}
        on:mouseup={stopDrag}
    >
    <div 
        class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
        style="transform: translate({posX}px, {posY}px);"
    >
    <div
        class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg"
        style="cursor: move;"
        on:mousedown={startDrag}
    >
        <h2 class="text-2xl font-bold text-teal-500 text-center">Ajouter un group d'instruments</h2>
    </div>
        <form on:submit|preventDefault={submitForm} class="bg-gray-100 p-6 rounded-lg">
            <label for="group_name" class="font-semibold text-lg">Nom du groupe:</label>
            <input type="text" bind:value={groupName} placeholder="Entrez le nom du groupe"
                class="w-full p-2 mt-1 mb-3 border rounded">

            <label for="subgroup_name" class="font-semibold text-lg">{$_('modals.add_group.subname')}</label>
            <input type="text" bind:value={subGroupName} placeholder={$_('modals.add_group.enter_subname')}
                class="w-full p-2 mt-1 mb-3 border rounded">
            
            <label for="characateristics" class="font-semibold text-lg">{$_('modals.add_group.char')}</label>   
            {#each characteristics as char, index}
                <div class="flex items-center mt-2">
                    <input type="text" bind:value={characteristics[index]} placeholder={$_('modals.add_group.enter_char')}
                        class="flex-1 p-2 border rounded" on:input={(e) => updateCharacteristic(index, e.target.value)}>
                </div>
            {/each}
            
            <button type="button" on:click={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">{$_('modals.add_group.add_char')}</button>
            
            <div class="mt-3">
                <label for="picture" class="font-semibold text-lg">{$_('modals.add_group.add_pictures')}</label>
                <input type="file" bind:value={picture} class="w-full p-2 mt-1 border rounded">
            </div>

            <div class="flex justify-end gap-4 mt-4">
                <button type="button" on:click={erase} class="px-4 py-2 bg-red-500 text-white rounded">{$_('modals.add_group.erase')}</button>
                <button type="button" on:click={cancel} class="px-4 py-2 bg-gray-500 text-white rounded">{$_('modals.add_group.cancel')}</button>
                <button type="submit" class="bg-teal-500 text-white px-4 py-2 rounded">{$_('modals.add_group.save')}</button>
            </div>
        </form>
    </div>
    </div>
    </div>
{/if}