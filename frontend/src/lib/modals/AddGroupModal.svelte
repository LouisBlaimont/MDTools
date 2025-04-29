<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { selectedGroup, reload, autocompleteOptions} from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { get } from "svelte/store";

    export let isOpen = false;
    export let close;
    
    let groupName = "";
    let subGroupName = "";
    let file;
    let characteristics = [""];
    const dispatch = createEventDispatcher();

    let autocompleteInput = '';
    let showAutocompleteDropDown = false;
    let filteredAutocompleteOptions = [];
    let currentAutocompleteField = null;
    let currentAutocompleteIndex = null;

    async function triggerAutocomplete(charName, index) {
        currentAutocompleteField = charName;
        currentAutocompleteIndex = index;

        if (!autocompleteOptions[charName]) {
            try {
            const response = await apiFetch(`/api/characteristics/all`);
            if (!response.ok) throw new Error();
            const blacklist = ["Name", "Function", "Length"];
            const values = await response.json();
            const filtered = values.filter(v => !blacklist.includes(v));
            autocompleteOptions.update((current) => ({
                ...current,
                [charName]: filtered
            }));
            } catch (e) {
            console.error("Failed to load options");
            }
        }
        const allOptions = get(autocompleteOptions)[charName] || [];
        const selected = characteristics.filter((val, i) => i !== index);
        filteredAutocompleteOptions = allOptions.filter(opt => !selected.includes(opt));

        showAutocompleteDropDown = true;
    }

    function handleAutocompleteInput(event, index) {
        const inputValue = event.target.value;
        autocompleteInput = inputValue;
        updateCharacteristic(index, inputValue);

        const allOptions = get(autocompleteOptions)[currentAutocompleteField] || [];
        const selected = characteristics.filter((val, i) => i !== index);
        filteredAutocompleteOptions = allOptions
            .filter(opt => !selected.includes(opt))
            .filter(opt => opt.toLowerCase().includes(inputValue.toLowerCase()));

        showAutocompleteDropDown = true;
    }

    function selectAutocompleteOption(option, index) {
        updateCharacteristic(index, option);
        autocompleteInput = '';
        showAutocompleteDropDown = false;
        currentAutocompleteField = null;
    }

    function addCharacteristic() {
        characteristics = [...characteristics, ""];
    }

    function updateCharacteristic(index, value) {
        characteristics[index] = value;
    }

    function erase() {
        groupName = "";
        subGroupName = "";
        file = null;
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
                characteristics 
            })
        });

        if (response.ok) {
            dispatch("success", { message: $_('modals.add_group.success') });
            goto(`/searches?group=${encodeURIComponent(groupName)}&subgroup=${encodeURIComponent(subGroupName)}&category=&instrument=`);
            reload.set(true);
        } else {
            dispatch("error", { message: $_('modals.add_group.fail') });
        }
        // Update group picture if a new file is provided
        if (file) {
        try {
            const fileData = new FormData();
            fileData.append("file", file);
            const response = await apiFetch("/api/groups/" + encodeURIComponent(groupName) + "/picture",
            {
                method: "POST",
                body: fileData,
            }
            );
            if (!response.ok) {
            throw new Error("Échec de la mise à jour de l'image");
            }
        } catch (error) {
            console.error("Erreur:", error);
        }
        }
        close();
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
        <h2 class="text-2xl font-bold text-teal-500 text-center select-none">{$_('modals.add_group.add_group')}</h2>
    </div>
        <form on:submit|preventDefault={submitForm} class="bg-gray-100 p-6 rounded-lg">
            <label for="group_name" class="font-semibold text-lg">{$_('modals.add_group.name')}</label>
            <input type="text" bind:value={groupName} placeholder="Entrez le nom du groupe"
                class="w-full p-2 mt-1 mb-3 border rounded">

            <label for="subgroup_name" class="font-semibold text-lg">{$_('modals.add_group.subname')}</label>
            <input type="text" bind:value={subGroupName} placeholder={$_('modals.add_group.enter_subname')}
                class="w-full p-2 mt-1 mb-3 border rounded">
            
            <label for="characateristics" class="font-semibold text-lg">{$_('modals.add_group.char')}</label>   
            {#each characteristics as char, index}
                <div class="relative">
                    <input
                    type="text"
                    class="flex-1 p-2 border rounded w-full"
                    bind:value={characteristics[index]}
                    on:focus={() => triggerAutocomplete(characteristics[index], index)}
                    on:input={(e) => handleAutocompleteInput(e, index)}
                    autocomplete="off"
                    />
                    {#if showAutocompleteDropDown && currentAutocompleteIndex === index}
                    <ul class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-1">
                        {#each filteredAutocompleteOptions as option}
                        <!-- svelte-ignore a11y_click_events_have_key_events -->
                        <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                        <li
                            class="px-4 py-2 text-left hover:bg-gray-200 cursor-pointer"
                            on:click={() => selectAutocompleteOption(option, index)}
                        >
                            {option}
                        </li>
                        {/each}
                    </ul>
                    {/if}
                </div>
                {/each}

                            
            <button type="button" on:click={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">{$_('modals.add_group.add_char')}</button>
            
            <div class="mt-3">
                <label for="picture" class="font-semibold text-lg">{$_('modals.add_group.add_pictures')}</label>
                <input
                    class="w-full p-2 mt-1 mb-3 border rounded"
                    type="file"
                    on:change={(e) => (file = e.target.files[0])}
                />
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