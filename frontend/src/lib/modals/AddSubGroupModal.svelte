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

    
    export let group = null;
    let groupName;
    $: groupName = group?.name ?? $selectedGroup;
    let name = "";
    let file;
    let characteristics = [""];
    const dispatch = createEventDispatcher();

    let posX = 0, posY = 0, offsetX = 0, offsetY = 0, isDragging = false;
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

    function addCharacteristic() {
        characteristics = [...characteristics, ""];
    }

    function updateCharacteristic(index, value) {
        characteristics[index] = value;
    }

    function erase() {
        groupName = "";
        name = "";
        picture = "";
        characteristics = [""];
    }

    function cancel() {
        dispatch("cancel", { message: $_('modals.add_subgroup.cancel_op') });
        close();
    }

    async function submitForm() {
        if (characteristics === null) {
            characteristics = [""];
        }
        else {
            characteristics.join(", ");
        }
        const response = await apiFetch('/api/subgroups/group/' + groupName, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                name, 
                groupName,
                characteristics
            })
        });
        if (response.ok) {
            dispatch("success", { message: $_('modals.add_subgroup.success') });
            close();
            goto(`/searches?group=${encodeURIComponent(groupName)}&subgroup=${encodeURIComponent(name)}&category=&instrument=`);
            reload.set(true);
        } else {
            dispatch("error", { message: $_('modals.add_subgroup.fail') });
        }
        // Update group picture if a new file is provided
        if (file) {
        try {
            const fileData = new FormData();
            fileData.append("file", file);
            const response = await apiFetch("/api/subgroups/" + encodeURIComponent(name) + "/picture",
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
    <!-- svelte-ignore event_directive_deprecated -->
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
        <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.add_subgroup.add_subgroup')}</h2>
    </div>
        <form on:submit|preventDefault={submitForm}  class="bg-gray-100 p-6 rounded-lg shadow-lg">        
            <label for="subgroup_name" class="font-semibold text-lg">{$_('modals.add_subgroup.name')}</label>
            <input type="text" bind:value={name} placeholder={$_('modals.add_subgroup.enter_name')}
                class="w-full p-2 mt-1 mb-3 border rounded">
            
            <label for="characateristics" class="font-semibold text-lg">{$_('modals.add_subgroup.char')}</label>   
            {#if characteristics !== null}
                {#each characteristics as char, index}
                    <div class="flex items-center mt-2">
                        <div class="relative w-full">
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
                                <!-- svelte-ignore a11y_click_events_have_key_events -->
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
                    </div>
                {/each}
            {/if}
            
            <button type="button" on:click={addCharacteristic} class="mt-4 px-4 py-2 bg-teal-500 text-white rounded">{$_('modals.add_subgroup.add_char')}</button>
            
            <div class="mt-3">
            <label for="picture" class="font-semibold text-lg">{$_('modals.add_subgroup.add_picture')}</label>
            <input
                class="w-full p-2 mt-1 mb-3 border rounded"
                type="file"
                on:change={(e) => (file = e.target.files[0])}
            />
            </div>
            
            <div class="flex justify-end gap-4 mt-4">
                <button type="button" on:click={erase} class="mt-4 px-4 py-2 bg-red-500 text-white rounded">{$_('modals.add_subgroup.erase')}</button>
                <button type="button" on:click={cancel} class="mt-4 px-4 py-2 bg-gray-500 text-white rounded">{$_('modals.add_subgroup.cancel')}</button>
                <button type="submit" class="mt-4 px-4 py-2 bg-teal-500 text-white rounded ">{$_('modals.add_subgroup.save')}</button>
            </div>
        </form>
    </div>
</div>
    </div>
{/if}