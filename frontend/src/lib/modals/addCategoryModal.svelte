<script>
    import { goto } from "$app/navigation";
    import { apiFetch } from "$lib/utils/fetch";
    import { selectedSubGroup, characteristics, showChars, autocompleteOptions, categories, reload, selectedGroup } from "$lib/stores/searches";

    const {
        isOpen,
        close,
    } = $props();


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
</script>

{#if isOpen}
    <div class="relative z-10" aria-labelledby="modal-title" role="dialog" aria-modal="true">
        <div class="fixed inset-0 bg-gray-500 bg-opacity-10 transition-opacity" aria-hidden="true"></div>
        <!-- svelte-ignore a11y_no_static_element_interactions -->
        <div 
            class="fixed inset-0 z-10 flex items-center justify-center bg-gray-500 bg-opacity-50"
            onmousemove={drag}
            onmouseup={stopDrag}
        >
            <div 
                class="bg-white rounded-lg shadow-lg w-1/2 max-h-[80vh] overflow-y-auto absolute"
                style="transform: translate({posX}px, {posY}px);"
            >
                <div 
                    class="p-4 border-b cursor-move bg-black text-white flex items-center justify-between"
                    onmousedown={startDrag}
                >
                    <h2 class="text-xl font-bold">Ajouter une catégorie au sous groupe {$selectedSubGroup}</h2>
                </div>

                <div class="p-4">
                {#if $showChars}
                {#each $characteristics as char}
                    {#if char==="Length"}
                        <label class="block mb-2" for="input-{char}">{char}:</label>
                        <div class="relative mb-4">
                            <input 
                            type="number" 
                            min="0"
                            step="0.01"
                            id="input-{char}"
                            bind:value={newCharValues[char]}
                            class="w-full p-2 border rounded" 
                            >
                        </div>
                    {:else if char === "Function" || char === "Name"}
                        <label class="block mb-2" for="input-{char}">{char}:</label>
                        <div class="relative mb-4">
                            <input 
                            type="text" 
                            id="input-{char}"
                            bind:value={newCharValues[char]}
                            onfocus={()=> triggerAutocomplete(char)}
                            oninput={handleAutocompleteInput}
                            class="w-full p-2 border rounded" 
                            >
                            {#if showAutocompleteDropDown && currentAutocompleteField === char}
                                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                <ul 
                                class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
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
                        <div class="grid grid-cols-2 gap-4 items-center mb-4 ">
                            <div class="relative">
                                <label class="block mb-2" for="input-{char}">{char}:</label>
                                <input
                                    type = "text"
                                    id="input-{char}"
                                    bind:value={newCharValues[char]}
                                    onfocus={()=> triggerAutocomplete(char)}
                                    oninput={handleAutocompleteInput}
                                    class="w-full p-2 border rounded"
                                >
                                {#if showAutocompleteDropDown && currentAutocompleteField === char}
                                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                                    <ul 
                                    class="absolute z-10 w-full bg-white border border-gray-300 rounded-lg shadow-lg max-h-40 overflow-y-auto mt-0"
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
                                <label class="block mb-2" for="input-{char}-abbrev">Abbréviation:</label>
                                <input
                                    type = "text"
                                    id="input-{char}-abbrev"
                                    bind:value={newCharAbbrev[char]}
                                    class="w-full p-2 border rounded"
                                >
                            </div>
                        </div>
                    {/if}
                {/each}
                {/if}
                </div>
                <span id="error-same-category" class="ml-5 mb-5 text-red-600 hidden">Cette catégorie existe déjà.</span>

                <div class="flex justify-end gap-4 mb-4">
                    <button type="button" onclick={()=>eraseInputs()} class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-700">Effacer</button>
                    <button type="button" onclick={close} class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700">Annuler</button>
                    <button type="button" onclick={()=>addCategory()} class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-700">Ajouter</button>
                </div>
            </div>
        </div>
    </div>
{/if}