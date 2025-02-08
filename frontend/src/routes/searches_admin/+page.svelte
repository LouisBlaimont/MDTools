<script>
    import { tools } from '../../tools.js';
    import { suppliers } from '../../suppliers.js';
    import { getOrder, addTool } from '../../order.js';
    import { goto } from '$app/navigation';
    import ModifIcon from '../searches_admin/modifIcon.svelte';

    let hoveredToolIndex = null;
    let hoveredToolImageIndex = null;
    let selectedToolIndex = null;
    let currentSuppliers = [];

    function selectTool(index) {
        selectedToolIndex = index;
        currentSuppliers = suppliers[index] || [];
    }

    let hoveredSupplierIndex = null;
    let hoveredSupplierImageIndex = null;
    let selectedSupplierIndex = null;
    function selectSupplier(index) {
        selectedSupplierIndex = index;
    }

    function showBigPicture(img){
        const pannel = document.getElementById("big-tool-pannel");
        const overlay = document.getElementById("overlay");
        const picture = document.getElementById("big-tool")
        pannel.style.display = "flex";
        overlay.style.display = "block";
        picture.src = img
    }
    function closeBigPicture(){
        const pannel = document.getElementById("big-tool-pannel");
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
        console.log(selectedToolIndex);
        console.log(selectedSupplierIndex);
        console.log(suppliers[selectedToolIndex][selectedSupplierIndex].ref);
        const tool_ref = suppliers[selectedToolIndex][selectedSupplierIndex].ref;
        const tool_brand = suppliers[selectedToolIndex][selectedSupplierIndex].brand;
        const tool_group = tools[selectedToolIndex].group;
        const tool_fct = tools[selectedToolIndex].fct;
        const tool_name = tools[selectedToolIndex].name;
        const tool_form = tools[selectedToolIndex].form;
        const tool_dim = tools[selectedToolIndex].dim;
        const tool_qte = quantity;
        const tool_pu_htva = suppliers[selectedToolIndex][selectedSupplierIndex].price;
        order = addTool(tool_ref, tool_brand, tool_group, tool_fct, tool_name, tool_form, tool_dim, tool_qte, tool_pu_htva);
        closeAddToOrder();
    }
    function exportOrderToExcel(){
        //smth to do with the database I think
    }
    function deleteCharacteristic(id){
        const texte = document.getElementById(id);
        texte.value = "";
    }
    function deleteAllCharacteristics(){
        let texte = document.getElementById("fct");
        texte.value = "";
        texte = document.getElementById("curvature_blade");
        texte.value = "";
        //... continue but i think it is faisable with a loop when the data base gives me the name of the characteristics

    }
    function openModifPage(){
        goto('/searches');
    }

    function openEditPage(toolId) {
        goto(`/instrument_edit/${toolId}`);
    }

    function openAddInstrumentPage() {
        goto('/add_instrument');
    }

</script>
<div class="text-[13px]">
    <div class = "flex flex-col gap-[5px] box-border w-full">

        <!-- PARTIE DU DESSUS -->
        <div class="flex-[5] flex flex-row mt-3 h-max-[50vh]">

            <!-- FORM DES RECHERCHES -->
            <div class ="flex-[1.3] h-full ml-[6px] m-0 bg-gray-100 rounded-lg shadow-md">
                <form class="flex flex-col w-[90%] mb-2.5">
                    <label for="google-search" class="font-semibold mt-1">Recherche par mot(s) clé(s):</label>
                    <input type="text" class="border border-gray-400 rounded p-0.5 border-solid border-[black]" id="google-search" name="google-search" placeholder="Entrez un mot clé">
                </form>
                <form class="flex flex-col w-full gap-2.5">
                    <label class="font-semibold">Recherche par caractéristiques:</label>
                    <div class = "flex items-center">
                        <label class="w-2/5" for="group">Groupe:</label>
                        <input type ="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="group" name="group">
                    </div>
                    <div class = "flex items-center">
                        <label class="w-2/5"for="name">Nom:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="name" name="name">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("name")}>&times;</button>
                    </div>
                    <button id="clear-all" class="w-[90px] border border-gray-400 rounded bg-gray-400 border-solid border-[black] rounded-sm" on:click={()=>deleteAllCharacteristics()}>Tout effacer</button>
                    <div class = "flex items-center">
                        <label for="fct" class="w-2/5">Fonction:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="fct" name="fct">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("fct")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="curvature_bade" class="w-2/5">Courbure lame:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="curvature_blade" name="curvature_blade">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("curvature_blade")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="tip_blade" class="w-2/5">Pointe lame:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="tip_blade" name="tip_blade">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("tip_blade")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="specific_blade" class="w-2/5">Spécificité lame:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="specific_blade" name="specific_blade">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("specific_blade")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="material" class="w-2/5">Matière:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="material" name="material">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("material")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="thick" class="w-2/5">Épaisseur:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="thick" name="thick">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("thick")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="arm" class="w-2/5">Forme manche:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="arm" name="arm">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("arm")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="rings" class="w-2/5">Anneaux:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="rings" name="rings">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("rings")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="length" class="w-2/5">Longueur:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="length" name="length">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("length")}>&times;</button>
                    </div>
                    <div class = "flex items-center">
                        <label for="tolerance" class="w-2/5">Tolérance:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="tolerance" name="tolerance">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("tolerance")}>&times;</button>
                    </div>
                    <div class = "flex items-center mb-2">
                        <label for="other" class="w-2/5">Autres:</label>
                        <input type="text" class="w-1/2 border border-gray-400 rounded p-0.5 border-solid border-[black]" id="other" name="other">
                        <button class="text-gray-900 text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 rounded-[50%] border-[none] cursor-pointer" on:click={()=>deleteCharacteristic("other")}>&times;</button>
                    </div> 
                </form>    
            </div>

            <!-- TABLEAU CORRESPONDANT AUX RECHERCHES -->
            <div class="flex-[3] h-full overflow-y-auto box-border ml-3">
                <table id="tools-table" class="w-full border-collapse">
                    <thead class="bg-teal-400">
                        <tr>
                            <th class="text-center border border-solid border-[black]"></th>
                            <th class="text-center border border-solid border-[black]">GROUPE</th>
                            <th class="text-center border border-solid border-[black]">FONCTION</th>
                            <th class="text-center border border-solid border-[black]">NOM</th>
                            <th class="text-center border border-solid border-[black]">FORME</th>
                            <th class="text-center border border-solid border-[black]">DIMENSION</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each tools as row, index}

                            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                            <tr
                                class="cursor-pointer"
                                class:bg-[cornflowerblue]={selectedToolIndex === index}
                                class:bg-[lightgray]={hoveredToolIndex === index && selectedToolIndex !== index}
                                on:click={()=>selectTool(index)}
                                on:mouseover={()=> hoveredToolIndex = index}
                                on:mouseout={() => hoveredToolIndex = null}
                            >
                                <td class="transition-colors duration-300 bg-yellow-400 text-black hover:bg-black hover:text-yellow-500 text-center border border-solid border-[black]">
                                    <ModifIcon></ModifIcon>
                                </td>
                                <td class="text-center border border-solid border-[black]">{row.group}</td>
                                <td class="text-center border border-solid border-[black]">{row.fct}</td>
                                <td class="text-center border border-solid border-[black]">{row.name}</td>
                                <td class="text-center border border-solid border-[black]">{row.form}</td>
                                <td class="text-center border border-solid border-[black]">{row.dim}</td> 
                            </tr>
                        {/each}
                    </tbody>
                </table>
                <div class="flex justify-center">
                    <button class="ml-8 w-7 h-7 bg-yellow-400 text-black text-xl rounded-full mt-2 transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                        +
                    </button>
                </div>

                <!-- BOUTON POUR PASSER EN ADMIN -->
                <div class="flex justify-center mt-8"> 
                    <!-- svelte-ignore a11y_consider_explicit_label -->
                    <button class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-500 rounded-full cursor-pointer" on:click={openModifPage}>
                        <!-- Custom Edit Icon SVG -->
                        <svg xmlns="http://www.w3.org/2000/svg" fill="#000000" height="24" width="24" viewBox="0 0 568.599 568.599" class="w-6 h-6 text-gray-800">
                            <g>
                                <path d="M565.692,147.211L507.96,89.479c-4.08-4.08-10.404-4.08-14.484,0L241.128,342.031L75.276,176.179
                                    c-4.08-4.08-10.404-4.08-14.484,0L3.06,233.911c-4.08,4.08-4.08,10.404,0,14.484l230.724,230.724
                                    c1.836,1.836,4.488,3.06,7.14,3.06s5.304-1.02,7.14-3.06l317.628-317.424C569.568,157.615,569.568,151.291,565.692,147.211z
                                    M241.128,457.495L24.684,241.051l43.248-43.248l165.852,165.852c4.08,4.08,10.404,4.08,14.484,0L500.82,111.103l43.248,43.248
                                    L241.128,457.495z"/>
                                <path d="M497.148,133.543L352.92,277.771c-2.04,2.04-2.04,5.304,0,7.14c1.02,1.02,2.244,1.428,3.672,1.428
                                    c1.428,0,2.652-0.408,3.672-1.428L500.82,144.355l10.812,10.812c2.04,2.04,5.304,2.04,7.14,0c2.04-2.04,2.04-5.304,0-7.14
                                    l-14.484-14.484c-1.02-1.02-2.244-1.428-3.672-1.428C499.188,132.115,498.168,132.523,497.148,133.543z"/>
                            </g>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- PHOTOS CORRESPONDANTES AUX RECHERCHERS -->
            <div class="flex-1 max-h-[80vh] overflow-y-auto box-border ml-3">
                {#each tools as row, index}
                    <div class="relative">
                        <img 
                            alt="tool{row.id}" 
                            src={row.src}
                            on:click={()=>showBigPicture(row.src)} 
                            on:mouseover={()=>hoveredToolImageIndex = index}
                            on:mouseout={()=>hoveredToolImageIndex = null}
                            class="mb-[3px] {selectedToolIndex === index ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]' : ''} {hoveredToolImageIndex === index && selectedToolIndex !== index ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image' : ''}">

                            {#if selectedToolIndex === index}
                            <button class="absolute bottom-2 right-6 w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                                +
                            </button>
                            {/if}
                        </div>
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
                            <div class="flex justify-between items-center box-border p-[3px] border-[black] border-t border-solid">
                                <div>
                                    {row.ref}
                                </div>
                                <div class="mr-2">
                                    {#if selectedSupplierIndex === index}
                                        <button class="w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                                            +
                                        </button>
                                    {/if}
                                </div>
                            </div>
                        </div>
                    {/each}
                </div>

                <!-- TABLEAU DES FOURNISSEURS -->
                <div class="suppliers-table">
                    <table class="w-full border-collapse">
                        <thead class="bg-teal-400">
                            <tr>
                                <th class="text-center border border-solid border-[black]"></th>
                                <th class="text-center border border-solid border-[black]">AJOUT</th>
                                <th class="text-center border border-solid border-[black]">REF</th>
                                <th class="text-center border border-solid border-[black]">MARQUE</th>
                                <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
                                <th class="text-center border border-solid border-[black]">PRIX</th>
                                <th class="text-center border border-solid border-[black]">ALT</th>
                                <th class="text-center border border-solid border-[black]">OBS</th>
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
                                    <td class="transition-colors duration-300 bg-yellow-400 text-black hover:bg-black hover:text-yellow-500 text-center border border-solid border-[black]" on:click={(event) => { event.stopPropagation(); openEditPage(row.id); }}>
                                        <ModifIcon></ModifIcon>
                                    </td>
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
                    <div class="flex justify-center">
                        <button class="w-7 h-7 bg-yellow-400 text-black text-xl rounded-full mt-2 transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer" on:click={()=>openAddInstrumentPage()}>
                            +
                        </button>
                    </div>
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

<div class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4" id="big-tool-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5 hover:text-[red] cursor-pointer" on:click={(event)=>{event.stopPropagation(); closeBigPicture();}}>&times;</span>
    <img class="h-[300px]" id="big-tool" alt="big tool">
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


