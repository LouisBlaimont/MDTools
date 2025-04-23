<script>
    import { createEventDispatcher } from "svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { instrumentCharacteristics, reload } from "$lib/stores/searches";
    import { _ } from "svelte-i18n";
    import { modals } from "svelte-modals";

    export let isOpen = false;
    export let close;
    export let initInstrument = null;
    export let initCategory = null;

    let file = null;
    let reference = "";
    let supplier = "";
    let supplierDescription = "";
    let price = "";
    let alt = "";
    let obsolete = false;
    let id = "";
    let categoryId = initCategory ? initCategory.id : ""; // Set default category ID

    const dispatch = createEventDispatcher();

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
        if (file)
        {
            try {
                const response = await apiFetch('/api/instrument', {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ 
                        reference, 
                        supplier, 
                        categoryId,
                        supplierDescription, 
                        price, 
                        alt, 
                        obsolete, 
                        id 
                    })
                });
                if (!response.ok) {
                    throw new Error("Failed to add instrument");
                }
                const formData = new FormData();
                formData.append("file", file);
                const img = await apiFetch("/api/instrument/" + encodeURIComponent(response.getbody(id)) + "/picture", {
                    method: "POST",
                    body: formData,
                });
                if (img.ok) {
                    const data = await response.json();
                    id = data.filePath; // Assuming the API returns the file path
                } else if (!img.ok) {
                    dispatch("error", { message: $_('modals.add_instrument.error_loading') });
                    return;
                }
            } catch (error) {
                dispatch("error", { message: $_('modals.add_instrument.error_add') });
                return;
            }
        }

        if (response.ok) {
            dispatch("success", { message: $_('modals.add_instrument.added') });
            close();
            reload.set(true);
        } else {
            dispatch("error", { message: $_('modals.add_instrument.not_possible')});
        }
    }

    function erase() {
        reference = "";
        supplier = "";
        supplierDescription = "";
        price = "";
        alt = "";
        obsolete = false;
        id = "";
        categoryId = null;
    }

    // Autocomplete functionality
    let autocompleteOptions = {};
    let categorizedOptions = {};
    let currentAutocompleteField = null;
    let currentCategory = null;
    let autocompleteInput = "";
    let filteredAutocompleteOptions = [];
    let showAutocompleteDropdown = false;

    async function fetchCharacteristicOptions(characteristicName) {
        try {
            // Map of characteristics to their respective API endpoints
            const characteristicEndpoints = {
                'supplier': {
                    endpoint: 'supplier',
                    extractValue: (item) => item.name,
                },
                'reference': {
                    endpoint: 'instrument',
                    extractValue: (item) => item.reference,
                },
                'supplierDescription': {
                    endpoint: 'instrument',
                    extractValue: (item) => item.supplierDescription,
                },
                'categoryId': {
                    endpoint: 'category',
                    extractValue: (item) => item
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
            const extractedOptions = options.map(config.extractValue);
            
            // For categoryId, organize data into categorized options
            if (characteristicName === 'categoryId') {
                // Process the data to create categorized options
                categorizedOptions = {}; // Reset categorized options
                
                extractedOptions.forEach(item => {
                    // Create categories based on groupName + subGroupName
                    const categoryKey = `${item.groupName} - ${item.subGroupName}`;
                
                    if (!categorizedOptions[categoryKey]) {
                        categorizedOptions[categoryKey] = [];
                    }
                        
                    // Add characteristics with their IDs
                    let display = [];
                    if (item.name) display.push(item.name);
                    if (item.shape) display.push(item.shape);
                    if (item.lenAbrv) display.push(item.lenAbrv);
                    
                    const displayText = display.join(' - ');
                    
                    categorizedOptions[categoryKey].push({
                        id: item.id,
                        display: displayText
                    });
                });
                
                // Set the initial options to be the category names
                autocompleteOptions[characteristicName] = Object.keys(categorizedOptions);
                return Object.keys(categorizedOptions);
            } else {
                // For other fields, just store the options directly
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
        
        if (currentAutocompleteField === 'categoryId') {
            // For category, just update the display value but keep trackable
        } else if (currentAutocompleteField) {
            // For other fields, update the corresponding variable
            switch (currentAutocompleteField) {
                case 'reference':
                    reference = inputValue;
                    break;
                case 'supplier':
                    supplier = inputValue;
                    break;
                case 'supplierDescription':
                    supplierDescription = inputValue;
                    break;
            }
        }
        
        autocompleteInput = inputValue;
        showAutocompleteDropdown = true;

        if (currentAutocompleteField === 'categoryId') {
            if (currentCategory === null) {
                // Filter categories
                filteredAutocompleteOptions = Object.keys(categorizedOptions)
                    .filter(category => category.toLowerCase().includes(inputValue.toLowerCase()));
            } else {
                // Filter characteristics within the selected category
                filteredAutocompleteOptions = categorizedOptions[currentCategory]
                    .filter(item => item.display.toLowerCase().includes(inputValue.toLowerCase()))
                    .map(item => item.display);
            }
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
        if (currentAutocompleteField === 'categoryId') {
            if (currentCategory === null) {
                // User selected a category, now show characteristics for that category
                currentCategory = option;
                filteredAutocompleteOptions = categorizedOptions[option].map(item => item.display);
                return; // Don't close dropdown yet
            } else {
                // User selected a characteristic
                const selectedOption = categorizedOptions[currentCategory].find(item => item.display === option);
                
                if (selectedOption) {
                    // Update with category ID
                    categoryId = selectedOption.id;
                    // Update display value for input field
                    document.querySelector(`input[data-field="${currentAutocompleteField}"]`).value = option;
                }
                
                // Reset category state
                currentCategory = null;
            }
        } else {
            // Standard selection for other fields
            switch(currentAutocompleteField) {
                case 'reference':
                    reference = option;
                    break;
                case 'supplier':
                    supplier = option;
                    break;
                case 'supplierDescription':
                    supplierDescription = option;
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
            currentCategory = null;
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
            case 'reference':
                autocompleteInput = reference;
                break;
            case 'supplier':
                autocompleteInput = supplier;
                break;
            case 'supplierDescription':
                autocompleteInput = supplierDescription;
                break;
            default:
                autocompleteInput = "";
        }

        // Reset and show dropdown
        if (characteristicName === 'categoryId') {
            // For categoryId, show categories first
            filteredAutocompleteOptions = Object.keys(categorizedOptions);
        } else {
            // For other fields, show all options
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
                class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
                style="transform: translate({posX}px, {posY}px);"
            >
                <div 
                    class="p-4 border-b cursor-move bg-black text-white flex items-center justify-between"
                    on:mousedown={startDrag}
                >
                    <h2 class="text-xl font-bold">{$_('modals.add_instrument.add_instr')}</h2>
                </div>
                <form on:submit|preventDefault={submitForm} class="p-4">
                    <div class="grid grid-cols-2 gap-4">
                        <div>
                            <!-- svelte-ignore a11y_label_has_associated_control -->
                            <label class="block mb-2">{$_('modals.add_instrument.ref')}</label>
                            <div class="relative">
                                <input 
                                    type="text" 
                                    bind:value={reference} 
                                    data-field="reference"
                                    on:focus={() => triggerAutocomplete("reference")}
                                    on:input={handleAutocompleteInput}
                                    on:blur={closeAutocomplete}
                                    class="w-full p-2 border rounded" 
                                    placeholder={$_('modals.add_instrument.enter_ref')}
                                />
                                {#if showAutocompleteDropdown && currentAutocompleteField === "reference"}
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
                        </div>
                        <div>
                            <label class="block mb-2">{$_('modals.add_instrument.supplier')}</label>
                            <div class="relative">
                                <input 
                                    type="text" 
                                    bind:value={supplier} 
                                    data-field="supplier"
                                    on:focus={() => triggerAutocomplete("supplier")}
                                    on:input={handleAutocompleteInput}
                                    on:blur={closeAutocomplete}
                                    class="w-full p-2 border rounded" 
                                    placeholder={$_('modals.add_instrument.enter_supplier')}
                                />
                                {#if showAutocompleteDropdown && currentAutocompleteField === "supplier"}
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
                        </div>
                        <div>
                            <label class="block mb-2">{$_('modals.add_instrument.description')}</label>
                            <div class="relative">
                                <input 
                                    type="text" 
                                    bind:value={supplierDescription} 
                                    data-field="supplierDescription"
                                    on:focus={() => triggerAutocomplete("supplierDescription")}
                                    on:input={handleAutocompleteInput}
                                    on:blur={closeAutocomplete}
                                    class="w-full p-2 border rounded"
                                    placeholder={$_('modals.add_instrument.enter_description')} 
                                />
                                {#if showAutocompleteDropdown && currentAutocompleteField === "supplierDescription"}
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
                        </div>
                        <div>
                            <label class="block mb-2">{$_('modals.add_instrument.price')}</label>
                            <input 
                                type="number" 
                                bind:value={price} 
                                min="0" 
                                step="0.01" 
                                class="w-full p-2 border rounded" 
                                placeholder={$_('modals.add_instrument.enter_price')}
                            />
                        </div>
                    </div>
                    <label class="block mb-2 mt-4">{$_('modals.add_instrument.cat')}</label>
                    <div class="relative mb-4">
                        <input 
                            type="text" 
                            data-field="categoryId"
                            bind:value={categoryId}
                            on:focus={() => triggerAutocomplete("categoryId")}
                            on:input={handleAutocompleteInput}
                            on:blur={closeAutocomplete}
                            class="w-full p-2 border rounded" 
                            placeholder={$_('modals.add_instrument.enter_cat')}
                        />
                        {#if showAutocompleteDropdown && currentAutocompleteField === "categoryId"}
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <ul 
                                class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto"
                                on:mousedown={event => event.preventDefault()}
                            >
                                {#each filteredAutocompleteOptions as option}
                                    <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                    <button
                                        type="button"
                                        class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full {currentAutocompleteField === 'categoryId' && currentCategory === option ? 'bg-blue-100' : ''}"
                                        role="option"
                                        on:click={() => selectAutocompleteOption(option)}
                                    >
                                        {option}
                                        {#if currentAutocompleteField === 'categoryId' && currentCategory === null && categorizedOptions[option]}
                                            <span class="text-xs text-gray-500 ml-2">
                                                ({categorizedOptions[option].length} items)
                                            </span>
                                        {/if}
                                    </button>
                                {/each}
                            </ul>
                        {/if}
                    </div>

                    <label class="block mb-2">{$_('modals.add_instrument.alt')}</label>
                    <input type="text" bind:value={alt} class="w-full p-2 border rounded mb-4" placeholder={$_('modals.add_instrument.enter_alt')}/>

                    <label class="block mb-2">{$_('modals.add_instrument.obs')}</label>
                    <div class="flex gap-4 mb-4">
                        <label><input type="radio" bind:group={obsolete} value={true} /> {$_('modals.add_instrument.yes')}</label>
                        <label><input type="radio" bind:group={obsolete} value={false} /> {$_('modals.add_instrument.no')}</label>
                    </div>

                    <label class="block mb-2">{$_('modals.add_instrument.picture')}</label>
                    <input
                        class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5 mb-4"
                        type="file"
                        on:change={(e) => (file = e.target.files[0])}
                    />
                    <div class="flex justify-end gap-4">
                        <button type="button" on:click={erase} class="bg-red-500 text-white px-4 py-2 rounded">{$_('modals.add_instrument.erase')}</button>
                        <button type="button" on:click={close} class="bg-gray-500 text-white px-4 py-2 rounded">{$_('modals.add_instrument.cancel')}</button>
                        <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded">{$_('modals.add_instrument.add')}</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
{/if}