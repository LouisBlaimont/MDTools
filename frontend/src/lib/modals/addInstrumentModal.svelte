<script>
    import { apiFetch } from "$lib/utils/fetch";
    import { instrumentCharacteristics, reload, selectedGroup, selectedSubGroup, currentSuppliers } from "$lib/stores/searches";
    import { _ } from "svelte-i18n";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";

    const {
        isOpen,
        close,
        initCategory, 
    } = $props();

    let files = $state([]);
    let reference = $state("");
    let supplier = $state("");
    let supplierDescription = $state("");
    let price = $state("");
    let obsolete = $state(false);
    let id = $state("");
    let categoryId = $state(initCategory ? initCategory.id : ""); // Set default category ID

    let inputSize = $state();

    let posX =$state(0), posY = $state(0), offsetX = 0, offsetY = 0, isDragging = false;

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
        if(supplier===""){
            const errorNoSupp = document.getElementById("error-no-supplier");
            errorNoSupp.classList.remove("hidden");
            return
        }
        const errorNoSupp = document.getElementById("error-no-supplier");
        errorNoSupp.classList.add("hidden");
        const errorSameRef = document.getElementById("error-same-ref");
        errorSameRef.classList.add("hidden");
        const pbPicture= document.getElementById("error-picture");
        pbPicture.classList.add("hidden");
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
                obsolete, 
                id 
                })
            });
            if (!response.ok) {
                if(response.status === 400){
                    const errorSameRef = document.getElementById("error-same-ref");
                    errorSameRef.classList.remove("hidden");
                    return;
                }
                throw new Error("Failed to create instrument");
            }
            const instr = await response.json();

            if(files.length>0){
                const formData = new FormData();
                files.forEach((file)=> {
                    formData.append("files", file);
                });
                formData.append("referenceId", instr.id);
                formData.append("type", "instrument");
                try {
                    const response = await apiFetch("/api/pictures/multiple", {
                        method: "POST",
                        body: formData,
                    });
                    if (!response.ok) {
                        const response = await apiFetch("/api/instrument/" + encodeURIComponent(instrument.id), {
                            method: "DELETE",
                            });
                        if (!response.ok) {
                            throw new Error("Failed to delete the instrument");
                        }
                        const pbPicture= document.getElementById("error-picture");
                        pbPicture.classList.remove("hidden");
                        throw new Error("Failed to update the image");
                    }
                    const pbPicture= document.getElementById("error-picture");
                    pbPicture.classList.add("hidden");
                    const responseData = await response.json();
                    responseData.forEach((picture) => {
                    currentSuppliers.update((suppliers) => {
                        suppliers.forEach((supplier) => {
                        if (supplier.id === instr.id) {
                            supplier.picturesId.push(picture.id);
                        }
                        });
                        return suppliers;
                    }); });
                } catch (error) {
                    console.error("Error:", error);
                }

            }
            close();
            goto(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=${encodeURIComponent(instr.categoryId)}&instrument=${encodeURIComponent(instr.id)}`);
            reload.set(true);
        }catch(error){
            console.error("Error", error);
            return;
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
    let categorizedOptions = $state({});
    let currentAutocompleteField = $state(null);
    let currentCategory = $state(null);
    let autocompleteInput = "";
    let filteredAutocompleteOptions = $state([]);
    let showAutocompleteDropdown = $state(false);

    async function fetchCharacteristicOptions(characteristicName) {
        try {
            // Map of characteristics to their respective API endpoints
            const characteristicEndpoints = {
                'supplier': {
                    endpoint: 'supplier',
                    extractValue: (item) => item.name,
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
    <div
        class="relative z-10"
        aria-labelledby="modal-title"
        role="dialog"
        aria-modal="true"
    >
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
                <h2 class="text-2xl font-bold text-teal-500 text-center">{$_('modals.add_instrument.add_instr')}</h2>
            </div>
            <form onsubmit={submitForm} preventDefault class="bg-gray-100 p-6 rounded-b-lg">
                <label for="ref" class="font-semibold text-lg">{$_('modals.add_instrument.ref')}</label>
                <input 
                    id="ref"
                    type="text" 
                    bind:value={reference} 
                    data-field="reference"
                    class="w-full p-2 mt-1 mb-3 border rounded" 
                    placeholder={$_('modals.add_instrument.enter_ref')}
                />
                <span id="error-same-ref" class="block w-full mb-2 text-red-600 hidden">{$_('modals.add_instrument.exists')}</span>

                <label for="supplier" class="font-semibold text-lg">{$_('modals.add_instrument.supplier')}</label>
                <input 
                    id="supplier"
                    type="text" 
                    bind:value={supplier} 
                    data-field="supplier"
                    bind:this={inputSize}
                    onfocus={() => triggerAutocomplete("supplier")}
                    oninput={handleAutocompleteInput}
                    onblur={closeAutocomplete}
                    class="w-full p-2 mt-1 mb-3 border rounded" 
                    placeholder="Entrer un fournisseur"
                />
                {#if showAutocompleteDropdown && currentAutocompleteField === "supplier"}
                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                    <ul 
                        class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto max-w-[80vw]"
                        style="width: {inputSize?.offsetWidth || 'auto'}px;"
                        onmousedown={event => event.preventDefault()}
                    >
                        {#each filteredAutocompleteOptions as option}
                            <!-- svelte-ignore a11y_role_has_required_aria_props -->
                            <button
                                type="button"
                                class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full max-w-[80vw]"
                                role="option"
                                onclick={() => selectAutocompleteOption(option)}
                            >
                                {option}
                            </button>
                        {/each}
                    </ul>
                {/if}
                <span id="error-no-supplier" class="block w-full mb-2 text-red-600 hidden">{$_('modals.add_instrument.enter_supplier')} </span>

                <label for="supplierDescription" class="font-semibold text-lg">{$_('modals.add_instrument.description')} </label>
                <input 
                    id="supplierDescription"
                    type="text" 
                    bind:value={supplierDescription} 
                    data-field="supplierDescription"
                    bind:this={inputSize}
                    onfocus={() => triggerAutocomplete("supplierDescription")}
                    oninput={handleAutocompleteInput}
                    onblur={closeAutocomplete}
                    class="w-full p-2 mt-1 mb-3 border rounded"
                    placeholder={$_('modals.add_instrument.enter_description')} 
                />
                {#if showAutocompleteDropdown && currentAutocompleteField === "supplierDescription"}
                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                    <ul 
                        class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto max-w-[80vw]"
                        style="width: {inputSize?.offsetWidth || 'auto'}px;"
                        onmousedown={event => event.preventDefault()}
                    >
                        {#each filteredAutocompleteOptions as option}
                            <!-- svelte-ignore a11y_role_has_required_aria_props -->
                            <button
                                type="button"
                                class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full max-w-[80vw]"
                                role="option"
                                onclick={() => selectAutocompleteOption(option)}
                            >
                                {option}
                            </button>
                        {/each}
                    </ul>
                {/if}

                <label for="price" class="font-semibold text-lg">{$_('modals.add_instrument.price')} </label>
                <input 
                    id="price"
                    type="number" 
                    bind:value={price} 
                    min="0" 
                    step="0.01" 
                    class="w-full p-2 mt-1 mb-3 border rounded" 
                    placeholder={$_('modals.add_instrument.enter_price')} 
                />
                
                {#if !initCategory}
                    <label for="categoryId" class="font-semibold text-lg">{$_('modals.add_instrument.cat')} </label>
                    <input 
                        id="categoryId"
                        type="text" 
                        data-field="categoryId"
                        bind:value={categoryId}
                        bind:this={inputSize}
                        onfocus={() => triggerAutocomplete("categoryId")}
                        oninput={handleAutocompleteInput}
                        onblur={closeAutocomplete}
                        class="w-full p-2 mt-1 mb-3 border rounded" 
                        placeholder={$_('modals.add_instrument.enter_cat')} 
                    />
                    {#if showAutocompleteDropdown && currentAutocompleteField === "categoryId"}
                        <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                        <ul 
                            class="absolute z-10 mt-1 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto max-w-[80vw]"
                            style="width: {inputSize?.offsetWidth || 'auto'}px;"
                            onmousedown={event => event.preventDefault()}
                        >
                            {#each filteredAutocompleteOptions as option}
                                <!-- svelte-ignore a11y_role_has_required_aria_props -->
                                <button
                                    type="button"
                                    class="dropdown-option px-4 py-2 text-left hover:bg-gray-200 cursor-pointer w-full max-w-[80vw] {currentAutocompleteField === 'categoryId' && currentCategory === option ? 'bg-blue-100' : ''}"
                                    role="option"
                                    onclick={() => selectAutocompleteOption(option)}
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
                {/if}

                <label for="obsolete" class="font-semibold text-lg">{$_('modals.add_instrument.obs')} </label>
                <div class="flex gap-4 mt-1 mb-3">
                    <label><input type="radio" bind:group={obsolete} value={true} /> {$_('modals.add_instrument.yes')} </label>
                    <label><input type="radio" bind:group={obsolete} value={false} /> {$_('modals.add_instrument.no')}</label>
                </div>

                <label for="img" class="font-semibold text-lg">{$_('modals.add_instrument.picture')}</label>
                <input
                    id="img"
                    class="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none p-2.5 mt-1 mb-3"
                    type="file"
                    accept="image/*"
                    multiple
                    onchange={(e) => (files = Array.from(e.target.files))}
                />
                <span id="error-picture" class="block w-full mb-2 text-red-600 hidden">{$_('modals.add_instrument.pb')}</span>

                <div class="flex justify-end gap-4">
                    <button type="button" onclick={erase} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">{$_('modals.add_instrument.erase')}</button>
                    <button type="button" onclick={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">{$_('modals.add_instrument.cancel')}</button>
                    <button type="submit" class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-700">{$_('modals.add_instrument.add')}</button>
                </div>
            </form>
        </div>
    </div>
    </div>
{/if}