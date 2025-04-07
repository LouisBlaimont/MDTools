<script>
    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { groups, subGroups, selectedGroup, selectedSubGroup, selectedGroupIndex, selectedSubGroupIndex, reload } from "$lib/stores/searches";

    export let isOpen = false;
    export let close;
    export let initCategory = null;

    let id = "";
    let groupName = $selectedGroup ? $selectedGroup : "";
    let subGroupName = selectedSubGroup ? $selectedSubGroup : "";
    let name = "";
    let fct = "";
    let shape = "";
    let lenAbrv = "";
    let pictureId = initCategory ? initCategory.pictureId : "";
    $selectedGroupIndex = $groups.indexOf($selectedGroup);
    $selectedSubGroupIndex = $subGroups.indexOf($selectedSubGroup);

    // For existing category, use stored indexes
    let groupId = $selectedGroupIndex + 1;
    let subGroupId = $selectedSubGroupIndex + 1;

    const dispatch = createEventDispatcher();

    // Draggable modal functionality
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
        if(groupId == 0 || subGroupId == 0) {
            dispatch("error", { message: "Veuillez sélectionner un groupe et un sous-groupe." });
            return;
        }
        
        const response = await apiFetch('/api/category/group/' + groupId + '/subgroup/' + subGroupId, {
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
            dispatch("success", { message: "Catégorie ajoutée!" });
            reload.set(true);
            close();
        } else {
            dispatch("error", { message: "Impossible d'ajouter une catégorie." });
        }
    }

    function erase() {
        name = "";
        fct = "";
        shape = "";
        lenAbrv = "";
        pictureId = "";
        id = "";
    }

    // Autocomplete functionality
    let autocompleteOptions = {};
    let groupToSubgroups = {}; // Mapping of groups to their subgroups
    let currentAutocompleteField = null;
    let currentGroup = null; // Track selected group for subgroup filtering
    let autocompleteInput = "";
    let filteredAutocompleteOptions = [];
    let showAutocompleteDropdown = false;

    async function fetchCharacteristicOptions(characteristicName) {
        try {
            // Map of characteristics to their respective API endpoints
            const characteristicEndpoints = {
                'groupName': {
                    endpoint: 'groups',
                    extractValue: (item) => item.name,
                },
                'subGroupName': {
                    endpoint: 'subgroups',
                    extractValue: (item) => item,
                },
                'shape': {
                    endpoint: 'category',
                    extractValue: (item) => item.shape,
                },
                'lenAbrv': {
                    endpoint: 'category',
                    extractValue: (item) => item.lenAbrv,
                },
                'name': {
                    endpoint: 'category',
                    extractValue: (item) => item.name,
                },
                'fct': {
                    endpoint: 'category',
                    extractValue: (item) => item.function,
                },
            };

            const config = characteristicEndpoints[characteristicName];
            
            if (!config) {
                console.warn(`No options endpoint found for ${characteristicName}`);
                return [];
            }

            const response = await apiFetch(`/api/${config.endpoint}/all`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (!response.ok) {
                throw new Error(`Failed to fetch options for ${characteristicName}`);
            }

            const options = await response.json();

            // Extract values using the provided function
            if (characteristicName === 'subGroupName') {
                // For subgroups, organize them by their parent groups
                groupToSubgroups = {}; // Reset the mapping
                
                options.forEach(item => {
                    if (!groupToSubgroups[item.groupName]) {
                        groupToSubgroups[item.groupName] = [];
                    }
                    groupToSubgroups[item.groupName].push(item.name);
                });
                
                // Store all subgroups for when no group is selected
                autocompleteOptions[characteristicName] = options.map(config.extractValue).map(item => item.name);
                return autocompleteOptions[characteristicName];
            } else {
                // For other fields, just store the options directly
                const extractedOptions = options.map(config.extractValue);
                autocompleteOptions[characteristicName] = extractedOptions;
                return extractedOptions;
            }
        } catch (error) {
            console.error(`Error fetching options for ${characteristicName}:`, error);
            return [];
        }
    }

    // Handle input for autocomplete
    function handleAutocompleteInput(event) {
        const inputValue = event.target.value;
        
        // Update the corresponding variable
        switch (currentAutocompleteField) {
            case 'groupName':
                groupName = inputValue;
                break;
            case 'subGroupName':
                subGroupName = inputValue;
                break;
            case 'name':
                name = inputValue;
                break;
            case 'fct':
                fct = inputValue;
                break;
            case 'shape':
                shape = inputValue;
                break;
            case 'lenAbrv':
                lenAbrv = inputValue;
                break;
        }
        
        autocompleteInput = inputValue;
        showAutocompleteDropdown = true;

        // Handle specific filtering for subgroups based on selected group
        if (currentAutocompleteField === 'subGroupName' && currentGroup) {
            // Filter subgroups that belong to the selected group
            const subgroupsForGroup = groupToSubgroups[currentGroup] || [];
            filteredAutocompleteOptions = subgroupsForGroup.filter(option =>
                option.toLowerCase().includes(inputValue.toLowerCase())
            );
        } else {
            // Standard filtering for other fields
            const rawOptions = autocompleteOptions[currentAutocompleteField] || [];
            filteredAutocompleteOptions = rawOptions.filter(option =>
                String(option).toLowerCase().includes(inputValue.toLowerCase())
            );
        }
    }

    // Select an option from autocomplete
    function selectAutocompleteOption(option) {
        // Handle group selection specifically
        if (currentAutocompleteField === 'groupName') {
            groupName = option;
            // When a group is selected, save it for filtering subgroups
            currentGroup = option;
            
            // Find the group ID based on the selected group name
            const groupIndex = $groups.indexOf(option);
            if (groupIndex !== -1) {
                groupId = groupIndex + 1;
            }
            
            // Clear subgroup when group changes
            subGroupName = "";
            subGroupId = 0;
        } 
        // Handle subgroup selection
        else if (currentAutocompleteField === 'subGroupName') {
            subGroupName = option;
            
            // Find the subgroup ID based on the selected subgroup name
            if (currentGroup) {
                // Get all subgroups for the current group
                const availableSubgroups = groupToSubgroups[currentGroup] || [];
                const subgroupIndex = availableSubgroups.indexOf(option);
                
                // We need to map this to the global subgroup ID
                // This assumes subgroups are 1-indexed in the API
                if (subgroupIndex !== -1) {
                    // Find the global index in the $subGroups store
                    const globalSubgroupIndex = $subGroups.indexOf(option);
                    if (globalSubgroupIndex !== -1) {
                        subGroupId = globalSubgroupIndex + 1;
                    }
                }
            }
        }
        // Standard selection for other fields
        else {
            switch(currentAutocompleteField) {
                case 'name':
                    name = option;
                    break;
                case 'fct':
                    fct = option;
                    break;
                case 'shape':
                    shape = option;
                    break;
                case 'lenAbrv':
                    lenAbrv = option;
                    break;
            }
        }

        // Reset autocomplete states
        autocompleteInput = '';
        showAutocompleteDropdown = false;
        currentAutocompleteField = null;
    }

    function closeAutocomplete() {
        showAutocompleteDropdown = false;
        currentAutocompleteField = null;
    }

    // Trigger autocomplete for a specific field
    async function triggerAutocomplete(characteristicName) {
        if (currentAutocompleteField === characteristicName && showAutocompleteDropdown) {
            // If the same field is clicked again while dropdown is open, close it
            showAutocompleteDropdown = false;
            currentAutocompleteField = null;
            return;
        }
        
        // If a different field is clicked, fetch options for the new field
        currentAutocompleteField = characteristicName;
        
        // Fetch options if not already loaded
        if (!autocompleteOptions[characteristicName]) {
            await fetchCharacteristicOptions(characteristicName);
        }

        // Get current value from the field
        switch(characteristicName) {
            case 'groupName':
                autocompleteInput = groupName;
                break;
            case 'subGroupName':
                autocompleteInput = subGroupName;
                break;
            case 'name':
                autocompleteInput = name;
                break;
            case 'fct':
                autocompleteInput = fct;
                break;
            case 'shape':
                autocompleteInput = shape;
                break;
            case 'lenAbrv':
                autocompleteInput = lenAbrv;
                break;
            default:
                autocompleteInput = "";
        }

        // Special handling for subgroups
        if (characteristicName === 'subGroupName' && currentGroup) {
            // If a group is selected, only show subgroups for that group
            filteredAutocompleteOptions = groupToSubgroups[currentGroup] || [];
        } else {
            // Show all options for other fields
            filteredAutocompleteOptions = autocompleteOptions[characteristicName] || [];
        }
        
        showAutocompleteDropdown = true;
    }
</script>

{#if isOpen}
    <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <div class="fixed inset-0 bg-gray-500 bg-opacity-10 transition-opacity" aria-hidden="true"></div>
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <div 
            class="fixed inset-0 z-10 flex items-center justify-center bg-gray-500 bg-opacity-50"
            on:mousemove={drag}
            on:mouseup={stopDrag}
        >
            <div 
                class="bg-white rounded-lg shadow-lg w-1/2 absolute"
                style="transform: translate({posX}px, {posY}px);"
            >
                <div 
                    class="p-4 border-b cursor-move bg-black text-white flex items-center justify-between"
                    on:mousedown={startDrag}
                >
                    <h2 class="text-xl font-bold">Ajouter une catégorie</h2>
                </div>
                <form on:submit|preventDefault={submitForm} class="p-4">
                    <label class="block mb-2">Groupe:</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            bind:value={groupName} 
                            data-field="groupName"
                            on:focus={() => triggerAutocomplete("groupName")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder="Entrez un groupe"
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "groupName"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">Nom du sous-groupe:</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            bind:value={subGroupName} 
                            data-field="subGroupName"
                            on:focus={() => triggerAutocomplete("subGroupName")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder="Entrez un sous-groupe"
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "subGroupName"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">Fonction:</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            bind:value={fct} 
                            data-field="fct"
                            on:focus={() => triggerAutocomplete("fct")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder="Entrez une fonction"
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "fct"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">Nom de la catégorie:</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            bind:value={name} 
                            data-field="name"
                            on:focus={() => triggerAutocomplete("name")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder="Entrez un nom de catégorie"
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "name"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">Forme:</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            bind:value={shape} 
                            data-field="shape"
                            on:focus={() => triggerAutocomplete("shape")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder="Entrez une forme"
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "shape"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">Abréviation:</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            bind:value={lenAbrv} 
                            data-field="lenAbrv"
                            on:focus={() => triggerAutocomplete("lenAbrv")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder="Entrez une abréviation"
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "lenAbrv"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">Source de l'image:</label>
                    <input 
                        type="text" 
                        bind:value={pictureId} 
                        class="w-full p-2 border rounded mb-4" 
                        placeholder="Entrez l'ID de l'image"
                    />

                    <div class="flex justify-end gap-4">
                        <button type="button" on:click={erase} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">Effacer</button>
                        <button type="button" on:click={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">Annuler</button>
                        <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700">Ajouter</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
{/if}