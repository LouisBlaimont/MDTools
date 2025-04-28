<script>
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { _ } from "svelte-i18n";
    import { selectedSubGroup, characteristics, showChars, autocompleteOptions, categories, reload, selectedGroup, groups, showSubGroups, subGroups } from "$lib/stores/searches";
    import { preventDefault } from "svelte/legacy";
    import { findSubGroups, findCharacteristics } from "$lib/components/search";
    import { fetchGroups } from "../../api";
    import { onMount } from "svelte";

    const {
        isOpen,
        close,
        fromSearches,
    } = $props();

    onMount(async () => {
        if(!fromSearches){
            selectedGroup.set("");
            selectedSubGroup.set("");
            showChars.set(false);
            const data = await fetchGroups();
            groups.set(data.map(group => group.name));
            subGroups.set(Object.fromEntries(
            data.map(group => [group.name, group.subGroups.map(sub => sub.name)])
            ));
        }
    });


    // Draggable modal functionality
    let posX = $state(0), posY = $state(0), offsetX = 0, offsetY = 0, isDragging = false;
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

    //Pictures
    let files = $state([]);

    // New values
    let newCharValues = $state({});
    let newCharAbbrev = $state({});


    // Displaying possible values for the fields
    let autocompleteInput = '';
    let showAutocompleteDropDown = $state(false);
    let filteredAutocompleteOptions = $state([]);
    let currentAutocompleteField = $state(null);

    // Display values when clicking on fields
    async function triggerAutocomplete(charName){
        currentAutocompleteField = charName;
        if(!autocompleteOptions[currentAutocompleteField]){
            await fetchCharacteristicOptions(charName);
        }
        autocompleteInput = "";
        filteredAutocompleteOptions = $autocompleteOptions[charName] || [];
        showAutocompleteDropDown = true;
    }
    async function fetchCharacteristicOptions(charName){
        try{
        const response = await apiFetch(`/api/characteristics/${charName}/values-in/${$selectedSubGroup}`);
        if (!response.ok) {
            throw new Error(`Failed to fetch options for ${charName}`);
        }
        let values = await response.json() ;
        values = values.filter(value => value !== "");
        autocompleteOptions.update(current => ({
            ...current,
            [charName]: values
        }));
        } catch(error){
        console.error('Failed to fetch options', error);
        }
    }

    // Filter values when writing inside field
    function handleAutocompleteInput(event){
        const inputValue = event.target.value;
        autocompleteInput  = inputValue;
        showAutocompleteDropDown = true;
        filteredAutocompleteOptions = $autocompleteOptions[currentAutocompleteField].filter(option => option.toLowerCase().includes(inputValue.toLowerCase()));
    }

    // Select value when clicking on an option, adding this value to the new fields
    function selectAutocompleteOption(option){
        newCharValues[currentAutocompleteField] = option;
        newCharValues = {...newCharValues};
        autocompleteInput = '';
        showAutocompleteDropDown = false;
        currentAutocompleteField = null;
    }

    // For fields with abreviations, when an exisiting option is clicked on, the corresponding alternative is displayed
    async function selectAutocompleteOptionWithAbrev(option){
        newCharValues[currentAutocompleteField] = option;
        newCharValues = {...newCharValues};
        try{
            const response = await apiFetch(`/api/category/abbreviation/of/${currentAutocompleteField}/${option}`);
            if (!response.ok) {
                throw new Error(`Failed to fetch abbreviation for ${option}`);
            }
            const characteristic = await response.json() ;
            const abbrev = characteristic.abrev;
            newCharAbbrev[currentAutocompleteField] = abbrev;
            newCharAbbrev = {...newCharAbbrev};
        } catch(error){
            console.error('Failed to fetch options', error);
        }
        autocompleteInput = '';
        showAutocompleteDropDown = false;
        currentAutocompleteField = null;    
    }

    // Adding the category using the new input fields
    async function addCategory() {
        let data = {};

        $characteristics.forEach(char => {
            if(char === "Length"){
                data[char] = String(newCharValues[char]||"");
            }
            else if (char === "Function" || char === "Name"){
                data[char] = newCharValues[char] ||"";
            }
            else{
                data[char] = newCharValues[char] || "";
                const abrevIndex = `${char}abrev`;
                data[abrevIndex] = newCharAbbrev[char] || "";
            }

        })
        try {
            const response = await apiFetch(`/api/category/subgroup/${$selectedSubGroup}`, {
            method: "POST", 
            headers : { "Content-type" : "application/json"},
            body : JSON.stringify(data),
        });
        if(!response.ok){
            if(response.status === 400){
                const errorSameCat = document.getElementById("error-same-category");
                errorSameCat.classList.remove("hidden");
            }
            const errorResponse = await response.json();
            throw new Error(errorResponse.error || "failed to create new cat");
        }
        const result = await response.json();
        categories.update(currentCat => {
            return [...currentCat, result];
        });

        if(files.length>0){
            const formData = new FormData();
            files.forEach((file) => {formData.append("files", file); });
            formData.append("referenceId", result.id);
            formData.append("type", "category");
            const response = await apiFetch(`/api/pictures/multiple`, {
                method: "POST",
                body: formData,
            });
            if(!response.ok){
                const response = await apiFetch(`/api/category/delete/${result.id}`, {
                    method: "DELETE",
                });
                const pbPicture= document.getElementById("error-picture");
                pbPicture.classList.remove("hidden");
                const errorResponse = await response.json();
                throw new Error(errorResponse.error);    
            }
            const pbPicture= document.getElementById("error-picture");
            pbPicture.classList.add("hidden");
            const responseData = await response.json();
            responseData.forEach((picture) => {
                categories.update((cats) => {
                cats.forEach((cat) => {
                    if (cat.id === result.id) {
                    cat.picturesId.push(picture.id);
                    }
                });
                return cats;
            }); }); 
        }
        close();
        goto(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=${encodeURIComponent(result.id)}&instrument=`);
        reload.set(true);

        }catch(error){
            console.log("Error:", error);
        }
    }

    //Erasing the inputs
    function eraseInputs(){
        newCharValues = {};
        newCharAbbrev = {};
    }

    // Close dropdown when clicking outside
    function handleClickOutside(event) {
        const dropdown = document.querySelector('.autocomplete-dropdown');
        const input = document.querySelector(`#input-${currentAutocompleteField}`);
        if (dropdown && !dropdown.contains(event.target) && input && !input.contains(event.target)) {
            showAutocompleteDropDown = false;
            currentAutocompleteField = null;
        }
    }

    // Add and remove event listeners for outside clicks
    $effect(() => {
        if (showAutocompleteDropDown) {
            document.addEventListener('mousedown', handleClickOutside);
            return () => document.removeEventListener('mousedown', handleClickOutside);
        }
    });
</script>

{#if isOpen}
    <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <div
            class="fixed inset-0 z-10 flex items-center justify-center"
            onmousemove={drag}
            onmouseup={stopDrag}
        >
            <div 
                class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
                style="transform: translate({posX}px, {posY}px);"
            >
                <div 
                    class="p-4 border-b cursor-move bg-gray-200 text-white flex items-center justify-between rounded-t-lg"
                    onmousedown={startDrag}
                >
                    {#if fromSearches}
                    <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.add_category.add_cat')} {$selectedSubGroup}</h2>
                    {:else}
                    <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.add_category.add_cat2')} </h2>  
                    {/if}
                </div>

                <div class="bg-gray-100 p-6 rounded-b-lg">
                {#if !fromSearches}
                    <label class="w-2/5 mt-2 mb-5 font-semibold text-lg" for="groupOptions">{$_('search_page.label.group')} : </label>
                    <select
                    id="groupOptions"
                    bind:value={$selectedGroup}
                    onchange={(e) => {
                    selectedSubGroup.set("");
                    findSubGroups(e.target.value);
                    }}
                    >
                    <option value="none">---</option>
                    {#each $groups as group}
                        <option value={group}>{group}</option>
                    {/each}
                    </select>

                    {#if $showSubGroups}
                    <label class="w-2/5 mt-2 mb-5 ml-7 font-semibold text-lg" for="subGroupOptions">{$_('search_page.label.subgroup')} : </label>
                    <select
                        id="subGroupOptions"
                        bind:value={$selectedSubGroup}
                        onchange={(e) => findCharacteristics(e.target.value)}
                    >
                        <option value="none">---</option>
                        {#each $subGroups as subGroup}
                        <option value={subGroup}>{subGroup}</option>
                        {/each}
                    </select>
                    {/if}
                {/if}
                {#if $showChars}
                {#each $characteristics as char}
                    {#if char==="Length"}
                        <label class="block font-semibold text-lg" for="input-{char}">{char}:</label>
                        <div class="relative mb-2">
                            <input 
                            type="number" 
                            min="0"
                            step="0.01"
                            id="input-{char}"
                            bind:value={newCharValues[char]}
                            class="w-full p-2 mt-1 mb-3 border rounded" 
                            >
                        </div>
                    {:else if char === "Function" || char === "Name"}
                        <label class="font-semibold text-lg" for="input-{char}">{char}:</label>
                        <div class="relative mb-2">
                            <input 
                            type="text" 
                            id="input-{char}"
                            bind:value={newCharValues[char]}
                            onfocus={()=> triggerAutocomplete(char)}
                            oninput={handleAutocompleteInput}
                            class="w-full p-2 mt-1 mb-3 border rounded" 
                        >
                            {#if showAutocompleteDropDown && currentAutocompleteField === char}
                                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                <ul 
                                class="autocomplete-dropdown absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
                                onmousedown={event => event.preventDefault()}
                                >
                                {#each filteredAutocompleteOptions as option}
                                <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                <button
                                    type="button"
                                    class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                    role="option"
                                    onclick={() => selectAutocompleteOption(option)}
                                >
                                    {option}
                                </button>
                                {/each}
                                </ul>
                            {/if}
                        </div>
                    {:else}
                        <div class="grid grid-cols-2 gap-4 items-center mb-2 ">
                            <div class="relative">
                                <label class="font-semibold text-lg" for="input-{char}">{char}:</label>
                                <input
                                    type = "text"
                                    id="input-{char}"
                                    bind:value={newCharValues[char]}
                                    onfocus={()=> triggerAutocomplete(char)}
                                    oninput={handleAutocompleteInput}
                                    class="w-full p-2 mt-1 mb-3 border rounded"
                                >
                                {#if showAutocompleteDropDown && currentAutocompleteField === char}
                                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                    <ul 
                                    class="autocomplete-dropdown absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
                                    onmousedown={event => event.preventDefault()}
                                    >
                                    {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        onclick={() => selectAutocompleteOptionWithAbrev(option)}
                                    >
                                        {option}
                                    </button>
                                    {/each}
                                    </ul>
                                {/if}
                            </div>
                            <div>
                                <label class="block mb-2" for="input-{char}-abbrev">{$_('modals.add_category.abbreviations')}</label>
                                <input
                                    type = "text"
                                    id="input-{char}-abbrev"
                                    bind:value={newCharAbbrev[char]}
                                    class="w-full p-2 mt-1 mb-3 border rounded"
                                >
                            </div>
                        </div>
                    {/if}
                {/each}
                <label class="block font-semibold text-lg" for="category-img">{$_('modals.add_category.picture')}</label>
                <input
                    class="block w-1/2 text-sm text-gray-900 border border-gray-300 rounded cursor-pointer bg-gray-50 focus:outline-none p-2.5 mb-4"
                    type="file"
                    accept="image/*"
                    multiple
                    id = "category-img"
                    onchange={(e) => (files = Array.from(e.target.files))}
                />
                <span id="error-picture" class="block w-full mb-2 text-red-600 hidden">{$_('modals.add_category.pb')}</span> 
                {/if}
                <span id="error-same-category" class="block w-full ml-5 mb-2 text-red-600 hidden">{$_('modals.add_category.exists')}</span>

                <div class="flex justify-end gap-4 mb-4">
                    <button type="button" onclick={()=>eraseInputs()} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">{$_('modals.add_category.erase')}</button>
                    <button type="button" onclick={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">{$_('modals.add_category.cancel')}</button>
                    <button type="button" onclick={()=>addCategory()} class="mr-2 bg-teal-500 text-white px-4 py-2 mr-4 rounded hover:bg-teal-700">{$_('modals.add_category.add')}</button>
                </div>
            </div>
        </div>
        </div>
    </div>
{/if}