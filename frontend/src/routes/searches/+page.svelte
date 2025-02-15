<script>
    import { tools } from '../../tools.js';
    import { suppliers } from '../../suppliers.js';
    import { getOrder, addTool } from '../../order.js';
    import { goto } from '$app/navigation';
    import { onMount } from 'svelte';
    import { preventDefault } from 'svelte/legacy';

    let hoveredCategoryIndex = null;
    let hoveredCategoryImageIndex = null;
    let selectedCategoryIndex = null;
    let currentSuppliers = [];

    function selectCategory(index) {
        selectedCategoryIndex = index;
        currentSuppliers = suppliers[index] || [];
    }

    let hoveredSupplierIndex = null;
    let hoveredSupplierImageIndex = null;
    let selectedSupplierIndex = null;
    function selectSupplier(index) {
        selectedSupplierIndex = index;
    }

    function showBigPicture(img){
        const pannel = document.getElementById("big-category-pannel");
        const overlay = document.getElementById("overlay");
        const picture = document.getElementById("big-category")
        pannel.style.display = "flex";
        overlay.style.display = "block";
        picture.src = img
    }
    function closeBigPicture(){
        const pannel = document.getElementById("big-category-pannel");
        const overlay = document.getElementById("overlay");
        pannel.style.display = "none";
        overlay.style.display = "none";
    }

    let toolToAddRef = ''
    let quantity = ''
    let order = getOrder()
    function addToOrderPannel(ref){
        const pannel = document.getElementById("add-order-pannel");
        const overlay = document.getElementById("overlay");
        toolToAddRef = ref;
        pannel.style.display = "flex";
        overlay.style.display = "block";
    }
    function closeAddToOrder(){
        const pannel = document.getElementById("add-order-pannel");
        const overlay = document.getElementById("overlay");
        pannel.style.display = "none";
        overlay.style.display = "none";
    }
    function addToOrder(){
        const tool_ref = suppliers[selectedCategoryIndex][selectedSupplierIndex].ref;
        const tool_brand = suppliers[selectedCategoryIndex][selectedSupplierIndex].brand;
        const tool_group = tools[selectedCategoryIndex].group;
        const tool_fct = tools[selectedCategoryIndex].fct;
        const tool_name = tools[selectedCategoryIndex].name;
        const tool_form = tools[selectedCategoryIndex].form;
        const tool_dim = tools[selectedCategoryIndex].dim;
        const tool_qte = quantity;
        const tool_pu_htva = suppliers[selectedCategoryIndex][selectedSupplierIndex].price;
        order = addTool(tool_ref, tool_brand, tool_group, tool_fct, tool_name, tool_form, tool_dim, tool_qte, tool_pu_htva);
        closeAddToOrder();
    }
    function exportOrderToExcel(){
        //smth to do with the database I think
    }
    function deleteCharacteristic(id){
        console.log("delete one")
        const texte = document.getElementById(id);
        texte.value = "";
        charValues[id] = "";
        searchByCharacteristics();
    }
    function deleteAllCharacteristics(){
        //for (let i = 0; i<characteristics.length; i++){
            //let texte = document.getElementById(characteristics[i]);
            //texte.value = "";
            //if (charValues[characteristics[i]]){
                //charValues[characteristics[i]]= "";
            //}
        //}
        //searchByCharacteristics();
    }

    let groups_summary = [];
    let groups = [];
    let subGroups = []
    let characteristics =[];
    let charValues = {};
    let categories = [];
    let selectedGroup ="";
    let selectedSubGroup = "";
    let showSubGroups = false;
    let showCategories = false;
    let showChars = false;
    let errorMessage = '';

    onMount(async () => {
            try {
                const response = await fetch('http://localhost:8080/api/groups/summary');

                if (!response.ok) {
                    throw new Error(`Failed to fetch groups: ${response.statusText}`);
                }

                groups_summary = await response.json();
            } catch (error) {
                console.error(error);
                errorMessage = error.message;
            }
        });
    $: groups = groups_summary.map(group => group.name);

    
    async function findSubGroups(group){
        selectedGroup = group;
        showSubGroups = true;
        showChars = false;
        showCategories = true;
        selectedSubGroup ="";
        characteristics =[];
        
        let subGroups_all_info = [];

        try {
            const response = await fetch(`http://localhost:8080/api/subgroups/group/${group}`);
            const response_2 = await fetch(`http://localhost:8080/api/category/group/${group}`);

            if (!response.ok) {
                throw new Error(`Failed to fetch subgroups: ${response.statusText}`);
            }
            if (!response_2.ok) {
                throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
            }

            subGroups_all_info = await response.json();
            categories = await response_2.json();
        } catch (error) {
            console.error(error);
            errorMessage = error.message;
        }

        subGroups = subGroups_all_info.map(subgroup => subgroup.name);
        return;
    }

    async function findCharacteristics(subGroup){
        selectedSubGroup = subGroup;
        showChars = true;
        let subgroup = [];

        try {
            const response = await fetch (`http://localhost:8080/api/subgroups/${subGroup}`);
            const response_2 = await fetch(`http://localhost:8080/api/category/subgroup/${subGroup}`);

            if (!response.ok){
                throw new Error(`Failed to fetch characteristics : ${response.statusText}`);
            }
            if (!response_2.ok) {
                throw new Error(`Failed to fetch categories: ${response_2.statusText}`);
            }

            subgroup = await response.json();
            categories = await response_2.json();
        }catch (error){
            console.log(error);
            errorMessage = error.message;
        }

        characteristics = subgroup.characteristics;
        return;
    }

    function searchByCharacteristics(){
        let char_vals = [];
        for (let i=0; i<characteristics.length; i++){
            if (characteristics[i] === "Function" || characteristics[i] === "Name"){ continue; }
            if (characteristics[i] === "Length" && charValues[characteristics[i]]){
                let char = {
                    name : characteristics[i],
                    value : charValues[characteristics[i]]+"cm",
                    abrev : ""
                }
                char_vals.push(char);
            }
            else if (charValues[characteristics[i]]){
                let char = {
                    name : characteristics[i], 
                    value : charValues[characteristics[i]], 
                    abrev : ""
                };
                char_vals.push(char);
            }
            else{
                let char = {
                    name : characteristics[i],
                    value : "",
                    abrev : ""
                };
                char_vals.push(char);
            }
        }
        const data = { 
            groupName: selectedGroup,
            subGroupName : selectedSubGroup,
            function : charValues["Function"] || "",
            name : charValues["Name"] || "",
            characteristics : char_vals
        }

        return fetch("http://localhost:8080/api/category/search/by-characteristics", {
            method: "POST",
            headers:{"Content-type":"application/json"},
            body : JSON.stringify(data)
        })
        .then(response => {
            if(!response.ok){
                categories = [];
                throw new Error(`Failed to search by characteristics : ${response.status}`);
            }
            return response.json();
        })
        .then(result => {
            categories = result;
        })
        .catch(error => {
            console.log("Error :", error);
        });

    }

    function openModifPage(){
        goto('/searches_admin');
    }

</script>
<head><title>Recherches</title></head>

<div class="text-[14px]">
    <div class = "flex flex-col gap-[5px] box-border w-full">

        <!-- PARTIE DU DESSUS -->
        <div class="flex-[5] flex flex-row mt-3 h-max-[50vh]">

            <!-- FORM DES RECHERCHES -->
            <div class ="flex-[1.3] h-full ml-[6px] m-0 bg-gray-100 rounded-lg shadow-md">
                <form class="flex flex-col w-[90%] mb-2.5">
                    <label for="google-search" class="font-semibold mt-1">Recherche par mot(s) clé(s):</label>
                    <input type="text" class="border border-gray-400 rounded p-0.5 border-solid border-[black]" id="google-search" name="google-search" placeholder="Entrez un mot clé">
                </form>
                
                <label class="font-semibold">Recherche par caractéristiques:</label>
                <div class = "flex items-center">
                    <label class="w-2/5 mt-2 mb-2" for="group">Groupe:</label>
                    <select id="groupOptions" on:change="{(e) => findSubGroups(e.target.value)}">
                        <option>---</option>
                        {#each groups as group}
                            <option value="{group}">{group}</option>
                        {/each}
                    </select>
                </div>

                {#if showSubGroups}
                    <div class = "flex items-center">
                        <label class="w-2/5 mb-2"for="name">Sous gp:</label>
                        <select id="subGroupOptions" on:change="{(e) => findCharacteristics(e.target.value)}">
                            <option>---</option>
                            {#each subGroups as subGroup}
                                <option value="{subGroup}">{subGroup}</option>
                            {/each}
                        </select>
                    </div>
                {/if}
                
                {#if showChars}
                    <form class="flex flex-col w-full gap-2.5" on:submit|preventDefault={searchByCharacteristics}>
                        <button id="clear-all" class="w-[90px] border border-gray-400 rounded bg-gray-400 border-solid border-[black] rounded-sm mb-2" on:click={()=>deleteAllCharacteristics()}>Tout effacer</button>
                        {#each characteristics as char}
                            <div class = "flex items-center">
                                <label for="{char}" class="w-2/5">{char}:</label>
                                <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black] mb-2" id="{char}" name="{char}" bind:value={charValues[char]}>
                                <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic(char)}>&times;</button>
                            </div>
                        {/each}
                    </form>  
                {/if}  
            </div>

            <!-- TABLEAU CORRESPONDANT AUX RECHERCHES -->
            <div class="flex-[3] h-full overflow-y-auto box-border ml-3">
                <table id="tools-table" class="w-full border-collapse">
                    <thead class="bg-teal-400">
                        <tr>
                            <th class="text-center border border-solid border-[black]">GROUPE</th>
                            <th class="text-center border border-solid border-[black]">SOUS GP</th>
                            <th class="text-center border border-solid border-[black]">FCT</th>
                            <th class="text-center border border-solid border-[black]">NOM</th>
                            <th class="text-center border border-solid border-[black]">FORME</th>
                            <th class="text-center border border-solid border-[black]">DIM</th>
                        </tr>
                    </thead>
                    {#if showCategories}
                        <tbody>
                            {#each categories as row, index}

                                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                                <tr
                                    class="cursor-pointer"
                                    class:bg-[cornflowerblue]={selectedCategoryIndex === index}
                                    class:bg-[lightgray]={hoveredCategoryIndex === index && selectedCategoryIndex !== index}
                                    on:click={()=>selectCategory(index)}
                                    on:mouseover={()=> hoveredCategoryIndex = index}
                                    on:mouseout={() => hoveredCategoryIndex = null}
                                >
                                    <td class="text-center border border-solid border-[black]">{row.groupName}</td>
                                    <td class="text-center border border-solid border-[black]">{row.subGroupName}</td>
                                    <td class="text-center border border-solid border-[black]">{row.function}</td>
                                    <td class="text-center border border-solid border-[black]">{row.name}</td>
                                    <td class="text-center border border-solid border-[black]">{row.shape}</td>
                                    <td class="text-center border border-solid border-[black]">{row.lenAbrv}</td>
                                </tr>
                            {/each}
                        </tbody>
                    {/if}
                </table>

                <!-- BOUTON POUR PASSER EN ADMIN -->
                <div class="flex justify-center mt-8">
                    <!-- svelte-ignore a11y_consider_explicit_label -->
                    <button class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-500 rounded-full cursor-pointer" on:click={openModifPage}>
                        <!-- Custom Edit Icon SVG -->
                        <svg xmlns="http://www.w3.org/2000/svg" fill="#000000" version="1.1" id="Capa_1" viewBox="0 0 494.936 494.936" class="w-6 h-6 text-gray-800">
                            <g>
                                <g>
                                    <path d="M389.844,182.85c-6.743,0-12.21,5.467-12.21,12.21v222.968c0,23.562-19.174,42.735-42.736,42.735H67.157 c-23.562,0-42.736-19.174-42.736-42.735V150.285c0-23.562,19.174-42.735,42.736-42.735h267.741c6.743,0,12.21-5.467,12.21-12.21 s-5.467-12.21-12.21-12.21H67.157C30.126,83.13,0,113.255,0,150.285v267.743c0,37.029,30.126,67.155,67.157,67.155h267.741 c37.03,0,67.156-30.126,67.156-67.155V195.061C402.054,188.318,396.587,182.85,389.844,182.85z"/>
                                    <path d="M483.876,20.791c-14.72-14.72-38.669-14.714-53.377,0L221.352,229.944c-0.28,0.28-3.434,3.559-4.251,5.396l-28.963,65.069 c-2.057,4.619-1.056,10.027,2.521,13.6c2.337,2.336,5.461,3.576,8.639,3.576c1.675,0,3.362-0.346,4.96-1.057l65.07-28.963 c1.83-0.815,5.114-3.97,5.396-4.25L483.876,74.169c7.131-7.131,11.06-16.61,11.06-26.692 C494.936,37.396,491.007,27.915,483.876,20.791z M466.61,56.897L257.457,266.05c-0.035,0.036-0.055,0.078-0.089,0.107 l-33.989,15.131L238.51,247.3c0.03-0.036,0.071-0.055,0.107-0.09L447.765,38.058c5.038-5.039,13.819-5.033,18.846,0.005 c2.518,2.51,3.905,5.855,3.905,9.414C470.516,51.036,469.127,54.38,466.61,56.897z"/>
                                </g>
                            </g>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- PHOTOS CORRESPONDANTES AUX RECHERCHERS -->
            <div class="flex-1 max-h-[80vh] overflow-y-auto box-border ml-3">
                {#each tools as row, index}
                    <!-- svelte-ignore a11y_click_events_have_key_events -->
                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                    <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                    <img 
                        alt="tool{row.id}" 
                        src={row.src}
                        on:click={()=>showBigPicture(row.src)} 
                        on:mouseover={()=>hoveredCategoryImageIndex = index}
                        on:mouseout={()=>hoveredCategoryImageIndex = null}
                        class="mb-[3px] {selectedCategoryIndex === index ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]' : ''} {hoveredCategoryImageIndex === index && selectedCategoryIndex !== index ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image' : ''}">
                {/each}
            </div>

            <div class="flex-[3] overflow-y-auto box-border m-0 ml-1">
                <!-- PHOTOS DES FOURNISSEURS -->
                <div class="border bg-teal-400 mb-[5px] border-solid border-[black]">
                    <span class="p-1">Photos fournisseurs</span>
                </div>
                <div class="flex h-40 max-w-full overflow-x-auto box-border mb-[15px]">
                   {#each currentSuppliers as row, index}
                       <div class="flex shrink-0 flex-col h-[95%] text-center box-border border mr-[3px] border-solid border-[black]" on:click={()=>showBigPicture(row.src)}>
                           <img 
                               alt="supplier{row.id}" 
                               src={row.src}
                               on:click={()=>showBigPicture(row.src)} 
                               on:mouseover={()=>hoveredSupplierImageIndex = index}
                               on:mouseout={()=>hoveredSupplierImageIndex = null}
                               class="h-4/5 {selectedSupplierIndex === index ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]' : ''} {hoveredSupplierImageIndex === index && selectedSupplierIndex !== index ? 'cursor-pointer border-2 border-solid border-[lightgray]' : ''}"
                           >
                           <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.ref}</div>
                       </div>
                   {/each}
                </div>

                <!-- TABLEAU DES FOURNISSEURS -->
                <div class="suppliers-table">
                    <table class="w-full border-collapse">
                        <thead class="bg-teal-400">
                            <tr>
                                <th class="text-center border border-solid border-[black] w-16">AJOUT</th>
                                <th class="text-center border border-solid border-[black] w-24">REF</th>
                                <th class="text-center border border-solid border-[black] w-32">MARQUE</th>
                                <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
                                <th class="text-center border border-solid border-[black] w-16">PRIX</th>
                                <th class="text-center border border-solid border-[black] w-16">ALT</th>
                                <th class="text-center border border-solid border-[black] w-16">OBS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {#each currentSuppliers as row, index}
                                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                                <tr
                                        class="cursor-pointer"
                                        class:bg-[cornflowerblue]={selectedSupplierIndex === index}
                                        class:bg-[lightgray]={hoveredSupplierIndex === index && selectedSupplierIndex !== index}
                                    on:click={()=>selectSupplier(index)}
                                    on:mouseover={()=> hoveredSupplierIndex = index}
                                    on:mouseout={() => hoveredSupplierIndex = null}
                                >
                                    <td class="green text-center border border-solid border-[black]" on:click={()=>addToOrderPannel(row.ref)}>+</td>
                                    <td class="text-center border border-solid border-[black]">{row.ref}</td>
                                    <td class="text-center border border-solid border-[black]">{row.brand}</td>
                                    <td class="text-center border border-solid border-[black]">{row.description}</td>
                                    <td class="text-center border border-solid border-[black]">{row.price}</td>
                                    <td class="text-center border border-solid border-[black]">{row.alt}</td>
                                    <td class="text-center border border-solid border-[black]">{row.obs}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- PARTIE DU DESSOUS -->
        <div class = "flex-[1] flex flex-col gap-[15px] h-full ml-3 bg-gray-50">
            <div>
                <!-- RECHERCHE PAR COMMANDE ET BOUTTON EXPORTER -->
                <div class="flex flex-row justify-between">
                    <div class="w-1/2 mr-0">
                        <form class="mt-2.5">
                            <label for="order-search" id="order-search-label" class="w-2/5">Rechercher une commande : </label>
                            <input type="text" class="w-1/3 border border-gray-400 rounded p-0.5 border-solid border-[black]" list="commandes" id="order-search" name="order-search" placeholder="Entrez un numéro de commande">
                            <datalist id="commandes">
                                <option value="#123456">
                                <option value="#123457">
                                <option value="#123458">
                                <option value="#123459">
                              </datalist>
                        </form>
                    </div>
                    <div class="mr-4">
                        <button class="border bg-green-600 mt-[10px] p-2.5 rounded-[10px] border-solid border-[none] cursor-pointer" on:click={()=>exportOrderToExcel()}>Exporter</button>
                    </div>
                </div>

                <!-- TABLEAU DES COMMANDES -->
                <div class="w-[80%] mb-[50px]">
                    <table class="w-full border-collapse">
                        <thead class="bg-teal-400">
                            <tr>
                                <th class="text-center border border-solid border-[black]">REF</th>
                                <th class="text-center border border-solid border-[black]">MARQUE</th>
                                <th class="text-center border border-solid border-[black]">GROUPE</th>
                                <th class="text-center border border-solid border-[black]">FONCTION</th>
                                <th class="text-center border border-solid border-[black]">NOM</th>
                                <th class="text-center border border-solid border-[black]">FORME</th>
                                <th class="text-center border border-solid border-[black]">DIMENSION</th>
                                <th class="text-center border border-solid border-[black]">QTE</th>
                                <th class="text-center border border-solid border-[black]">PU HTVA</th>
                                <th class="text-center border border-solid border-[black]">TOTAL HTVA</th>
                            </tr>
                        </thead>
                        <tbody>
                            {#each order as row, index}
                            <tr>
                                <td class="text-center border border-solid border-[black]">{row.ref}</td>
                                <td class="text-center border border-solid border-[black]">{row.brand}</td>
                                <td class="text-center border border-solid border-[black]">{row.group}</td>
                                <td class="text-center border border-solid border-[black]">{row.fct}</td>
                                <td class="text-center border border-solid border-[black]">{row.name}</td>
                                <td class="text-center border border-solid border-[black]">{row.form}</td>
                                <td class="text-center border border-solid border-[black]">{row.dim}</td>
                                <td class="text-center border border-solid border-[black]">{row.qte}</td>
                                <td class="text-center border border-solid border-[black]">{row.pu_htva}</td>
                                <td class="text-center border border-solid border-[black]">{row.total_htva}</td>
                                <td><button class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 border-[none] cursor-pointer">+</button></td>
                                <td><button class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px border-[none] cursor-pointer">-</button></td>
                                <td><button class="text-gray-900 rounded text-sm bg-red-600 w-[20px] h-[20px] border-[none] cursor-pointer">&times;</button></td>
                            </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>

<div class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4" id="big-category-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5 hover:text-[red] cursor-pointer" on:click={(event)=>{event.stopPropagation(); closeBigPicture();}}>&times;</span>
    <img class="h-[300px]" id="big-category" alt="big category">
</div>

<div class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4 text-[white] flex-col gap-[15px]" id="add-order-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5" on:click={(event)=>{event.stopPropagation(); closeAddToOrder();}}>&times;</span>
    <div>AJOUTER référence "{toolToAddRef}" à la commande:</div>
    <div>
        <label for="qte" class="w-2/5">QUANTITE:</label>
        <input type ="number" id="qte" name="qte" class="border border-black rounded p-2 text-black bg-white" bind:value={quantity}>
        <button class="cursor-pointer" pointer on:click={()=>addToOrder()}>AJOUT</button>
    </div>
        
</div>
