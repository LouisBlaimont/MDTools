<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { selectedGroup } from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";
    import { createEventDispatcher } from "svelte";

    const {
        isOpen,
        close, 
    } = $props();
    
    let groupName = $state("");
    let subGroupName = $state("");
    let picture = $state("");
    let characteristics = $state([""]);
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
    class="fixed inset-0 z-10 box-border bg-[rgba(0,0,0,0.8)] flex items-center justify-center p-[50px] rounded-[30px] flex-col"
    aria-labelledby="modal-title"
    role="dialog"
    aria-modal="true"
    >
    <div class="absolute inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>
        <div
        class="relative transform overflow-auto rounded-lg text-left transition-all sm:w-full sm:max-w-lg lg:max-w-4xl"
        style="max-height: 80vh;">
            <form onsubmit={submitForm} preventDefault class="bg-gray-100 p-6 rounded-lg">
                <h2 class="text-2xl font-bold text-teal-500 text-center mb-2">{$_('modals.add_group.add_group')}</h2>
                
                <label for="group_name" class="font-semibold text-lg">{$_('modals.add_group.name')}</label>
                <input type="text" bind:value={groupName} placeholder={$_('modals.add_group.enter_name')}
                    class="w-full p-2 mt-1 mb-3 border rounded">

                <label for="subgroup_name" class="font-semibold text-lg">{$_('modals.add_group.subname')}</label>
                <input type="text" bind:value={subGroupName} placeholder={$_('modals.add_group.enter_subname')}
                    class="w-full p-2 mt-1 mb-3 border rounded">
                
                <label for="characateristics" class="font-semibold text-lg">{$_('modals.add_group.char')}</label>   
                {#each characteristics as char, index}
                    <div class="flex items-center mt-2">
                        <input type="text" bind:value={characteristics[index]} placeholder={$_('modals.add_group.enter_char')}
                            class="flex-1 p-2 border rounded" oninput={(e) => updateCharacteristic(index, e.target.value)}>
                    </div>
                {/each}
                
                <button type="button" onclick={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">{$_('modals.add_group.add_char')}</button>
                
                <div class="mt-3">
                <label for="picture" class="font-semibold text-lg">{$_('modals.add_group.add_pictures')}</label>
                <input type="file" bind:value={picture} class="w-full p-2 mt-1 border rounded">
                </div>

                <button type="submit" class="mt-4 px-4 py-2 bg-teal-500 text-white rounded ">{$_('modals.add_group.save')}</button>
                <button type="button" onclick={erase} class="mt-4 px-4 py-2 bg-red-500 text-white rounded">{$_('modals.add_group.erase')}</button>
                <button type="button" onclick={cancel} class="mt-4 px-4 py-2 bg-gray-500 text-white rounded">{$_('modals.add_group.cancel')}</button>
            </form>
        </div>
    </div>
{/if}