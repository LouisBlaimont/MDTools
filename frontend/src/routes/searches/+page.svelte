<script>
    import { tools } from '../../tools.js';
    import { suppliers } from '../../suppliers.js';
    import { getOrder, addTool } from '../../order.js';
    import { goto } from '$app/navigation';

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
        goto('/modifPage2');
    }

</script>

<header class="g-sky-300 py-1 px-1 mb-2 flex justify-end">
    <button class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-500 rounded-full" on:click={openModifPage}>
        <svg xmlns="http://www.w3.org/2000/svg" fill="#000000" version="1.1" id="Capa_1" viewBox="0 0 494.936 494.936" class="w-6 h-6 text-gray-800">
            <g>
                <g>
                    <path d="M389.844,182.85c-6.743,0-12.21,5.467-12.21,12.21v222.968c0,23.562-19.174,42.735-42.736,42.735H67.157c-23.562,0-42.736-19.174-42.736-42.735V150.285c0-23.562,19.174-42.735,42.736-42.735h267.741c6.743,0,12.21-5.467,12.21-12.21s-5.467-12.21-12.21-12.21H67.157C30.126,83.13,0,113.255,0,150.285v267.743c0,37.029,30.126,67.155,67.157,67.155h267.741c37.03,0,67.156-30.126,67.156-67.155V195.061C402.054,188.318,396.587,182.85,389.844,182.85z"/>
                    <path d="M483.876,20.791c-14.72-14.72-38.669-14.714-53.377,0L221.352,229.944c-0.28,0.28-3.434,3.559-4.251,5.396l-28.963,65.069c-2.057,4.619-1.056,10.027,2.521,13.6c2.337,2.336,5.461,3.576,8.639,3.576c1.675,0,3.362-0.346,4.96-1.057l65.07-28.963c1.83-0.815,5.114-3.97,5.396-4.25L483.876,74.169c7.131-7.131,11.06-16.61,11.06-26.692C494.936,37.396,491.007,27.915,483.876,20.791z M466.61,56.897L257.457,266.05c-0.035,0.036-0.055,0.078-0.089,0.107l-33.989,15.131L238.51,247.3c0.03-0.036,0.071-0.055,0.107-0.09L447.765,38.058c5.038-5.039,13.819-5.033,18.846,0.005c2.518,2.51,3.905,5.855,3.905,9.414C470.516,51.036,469.127,54.38,466.61,56.897z"/>
                </g>
            </g>
        </svg>
    </button>
</header>

<!-- Flex column pour la partie au dessus et la commande -->
<div class="flex flex-col h-screen">
    <!-- Flex row pour les 4 éléments du dessus de la page -->
     <div class="basis-3/4 grow-0 shrink-0 h-fit">
        <div class="flex flex-6 gap-4">

            <!-- RECHERCHES -->
            <div class="ml-2 bg-gray-100 rounded-lg shadow-md flex flex-1 h-fit">
                <form id="google-search" class="flex flex-col gap-2 w-full">
                    <label for="google-search" class="font-semibold text-lg mt-2">Recherche par mot(s) clé(s) :</label>
                    <input type="text" id="google-search" name="google-search" placeholder="Entrez un mot clé" class="border border-slate-500 p-0.5 w-3/4">
                    <div class="font-semibold text-lg">Recherche par charactéristiques :<br>
                    </div>                    
                    <div class="flex items-center">
                        <label for="group" class="mr-2 w-1/5">Groupe :</label>
                        <input type="text" id="group" name="group" class="border border-slate-500 p-0.5">
                    </div>
                    <div class="flex items-center">
                        <label for="name" class="mr-2 w-1/5">Nom :</label>
                        <input type="text" id="name" name="name" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("name")}>&times;</button>
                    </div>
                    <div class="flex justify-center">
                    <button id="clear-all" class="w-40 border bg-gray-300 my-5" on:click={()=>deleteAllCharacteristics()}>Tout effacer</button>
                    </div>
                    <div class="flex items-center">
                        <label for="fct" class="mr-2 w-1/5">Fonction :</label>
                        <input type="text" id="fct" name="fct" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("fct")}>&times;</button>
                    </div>    
                    <div class="flex items-center">
                        <label for="curvature_bade" class="mr-2 w-1/5">Courbure lame :</label>
                        <input type="text" id="curvature_blade" name="curvature_blade" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("curvature_blade")}>&times;</button>
                    </div>     
                    <div class="flex items-center">
                        <label for="tip_blade" class="mr-2 w-1/5">Pointe lame :</label>
                        <input type="text" id="tip_blade" name="tip_blade" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("tip_blade")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="specific_blade" class="mr-2 w-1/5">Spécificité lame:</label>
                        <input type="text" id="specific_blade" name="specific_blade" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("specific_blade")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="material" class="mr-2 w-1/5">Matière:</label>
                        <input type="text" id="material" name="material" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("material")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="thick" class="mr-2 w-1/5">Épaisseur :</label>
                        <input type="text" id="thick" name="thick" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("thick")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="arm" class="mr-2 w-1/5">Forme manche :</label>
                        <input type="text" id="arm" name="arm" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("arm")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="rings" class="mr-2 w-1/5">Anneaux :</label>
                        <input type="text" id="rings" name="rings" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("rings")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="length" class="mr-2 w-1/5">Longueur :</label>
                        <input type="text" id="length" name="length" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("length")}>&times;</button>
                    </div>
                    <div class="flex items-center">
                        <label for="tolerance" class="mr-2 w-1/5">Tolérance :</label>
                        <input type="text" id="tolerance" name="tolerance" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("tolerance")}>&times;</button>
                    </div>
                    <div class="flex items-center mb-5">
                        <label for="other" class="mr-2 w-1/5">Autres :</label>
                        <input type="text" id="other" name="other" class="border border-slate-500 p-0.5">
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>deleteCharacteristic("other")}>&times;</button>
                    </div> 
                </form>
            </div>

            <!-- TABLEAU RESULTATS RECHERCHES -->
            <div class="overflow-y-auto border-collapse text-left flex flex-3 h-fit">
                <table id="tools-table" class="w-full">
                    <thead>
                        <tr>
                            <th class="bg-teal-400 border-b border border-slate-500">GROUPE</th>
                            <th class="bg-teal-400 border-b border border-slate-500">FONCTION</th>
                            <th class="bg-teal-400 border-b border border-slate-500">NOM</th>
                            <th class="bg-teal-400 border-b border border-slate-500">FORME</th>
                            <th class="bg-teal-400 border-b border border-slate-500">DIMENSION</th>
                        </tr>
                    </thead>
                    <tbody class="text-left">
                        {#each tools as row, index}

                            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                            <tr class="bg-white border-b border border-slate-500"
                                class:selected-row={selectedToolIndex === index}
                                class:hovered-row={hoveredToolIndex === index && selectedToolIndex !== index}
                                on:click={()=>selectTool(index)}
                                on:mouseover={()=> hoveredToolIndex = index}
                                on:mouseout={() => hoveredToolIndex = null}
                            >
                                <td class="bg-white border-b border border-slate-500 px-1">{row.group}</td>
                                <td class="bg-white border-b border border-slate-500 px-1">{row.fct}</td>
                                <td class="bg-white border-b border border-slate-500 px-1">{row.name}</td>
                                <td class="bg-white border-b border border-slate-500 px-1">{row.form}</td>
                                <td class="bg-white border-b border border-slate-500 px-1">{row.dim}</td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>

            <!-- PHOTOS RESULTATS RECHERCHES -->
            <div class="flex flex-wrap overflow-auto flex flex-1 h-fit w-auto">
                {#each tools as row, index}
                    <img 
                        alt="tool{row.id}" 
                        src={row.src}
                        on:click={()=>showBigPicture(row.src)} 
                        on:mouseover={()=>hoveredToolImageIndex = index}
                        on:mouseout={()=>hoveredToolImageIndex = null}
                        class="w-1/2 h-3/8 image {selectedToolIndex === index ? 'selected-image' : ''} {hoveredToolImageIndex === index && selectedToolIndex !== index ? 'hovered-image' : ''}">
                {/each}
            </div>

            <div class="flex flex-3">
                <div class="w-full">
                    <!-- Flex column pour les deux objets fournisseurs-->
                    <div class="flex flex-col overflow-y-auto box-border m-0 max-h-full">
                        
                        <!-- PHOT0S FOURNISSEURS -->
                        <div>
                            <div class="text-center mb-2 bg-teal-400 border-b border border-slate-500">Photos fournisseurs</div>
                            <div class="flex overflow-x-auto mb-2">
                                {#each currentSuppliers as row, index}
                                <div class="flex-shrink-0 flex flex-col text-center border w-28 h-full" on:click={()=>showBigPicture(row.src)}>
                                    <img 
                                        alt="supplier{row.id}" 
                                        src={row.src}
                                        on:click={()=>showBigPicture(row.src)} 
                                        on:mouseover={()=>hoveredSupplierImageIndex = index}
                                        on:mouseout={()=>hoveredSupplierImageIndex = null}
                                        class="image {selectedSupplierIndex === index ? 'selected-image' : ''} {hoveredSupplierImageIndex === index && selectedSupplierIndex !== index ? 'hovered-image' : ''}"
                                    >
                                    <div class="flex-shrink-0 flex flex-col text-center border w-28 h-full">{row.ref}</div>
                                </div>
                                {/each}
                            </div>
                        </div>

                        <!-- TABLEAU FOURNISSEURS -->
                        <div>
                            <table class="flex-none overflow-y-auto box-border border-collapse border border-slate-500 text-center w-full">
                                <thead>
                                    <tr class="bg-yellow-300 text-left">
                                        <th class="bg-teal-400 border-b border border-slate-500">AJOUT</th>
                                        <th class="bg-teal-400 border-b border border-slate-500">REF</th>
                                        <th class="bg-teal-400 border-b border border-slate-500">MARQUE</th>
                                        <th class="bg-teal-400 border-b border border-slate-500">DESCRIPTION</th>
                                        <th class="bg-teal-400 border-b border border-slate-500">PRIX</th>
                                        <th class="bg-teal-400 border-b border border-slate-500">ALT</th>
                                        <th class="bg-teal-400 border-b border border-slate-500">OBS</th>
                                    </tr>
                                </thead>
                                <tbody class="text-left">
                                    {#each currentSuppliers as row, index}
                                    <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                                        <tr class="bg-white border-b border border-slate-500"
                                            class:selected-row={selectedSupplierIndex === index}
                                            class:hovered-row={hoveredSupplierIndex ===   index && selectedSupplierIndex !== index}
                                            on:click={()=>selectSupplier(index)}
                                            on:mouseover={()=> hoveredSupplierIndex = index}
                                            on:mouseout={() => hoveredSupplierIndex = null}
                                        >
                                            <td class="add-tool" on:click={()=>addToOrderPannel(row.ref)}>+</td>
                                            <td class="bg-white border-b border border-slate-500 px-1">{row.ref}</td>
                                            <td class="bg-white border-b border border-slate-500 px-1">{row.brand}</td>
                                            <td class="bg-white border-b border border-slate-500 px-1">{row.description}</td>
                                            <td class="bg-white border-b border border-slate-500 px-1">{row.price}</td>
                                            <td class="bg-white border-b border border-slate-500 px-1">{row.alt}</td>
                                            <td class="bg-white border-b border border-slate-500 px-1">{row.obs}</td>
                                        </tr>
                                    {/each}
                                </tbody>
                            </table>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- COMMANDE/SET -->
    <div class="basis-1/4 grow-0 shrink-0 w-full mt-6 container mx-auto bg-gray-50 p-6 shadow-lg">
        <div class="mb-5">
            <div class="flex items-center space-x-6">
                <div>
                  <span class="text-teal-600 font-semibold text-2xl">Set/commande</span>
                </div>
                <div> 
                    <label for="id_ref" class="font-semibold text-lg">Rechercher une commande:</label> 
                    <div class= "flex flex-row"> 
                        <div>   
                        <input list="commandes" name="commandes" placeholder="Entrez un numéro de commande"
                                class="w-[350px] p-3 border border-gray-300 rounded-lg focus:ring-teal-500 focus:border-teal-500 text-lg">
                        <datalist id="commandes">
                            <option value="#123456">
                            <option value="#123457">
                            <option value="#123458">
                            <option value="#123459">
                        </datalist>
                        </div>
                        <div class="flex justify-end">
                            <button class="bg-teal-500 text-white py-3 px-6 rounded-lg hover:bg-teal-600 text-lg">Rechercher</button>
                        </div>
                    </div>
                </div>  
                <button id="export-order" class="bg-teal-500 text-white py-3 px-6 rounded-lg hover:bg-teal-600 text-lg" on:click={()=>exportOrderToExcel()}>Exporter</button>
            </div>
              
        </div>
        <div class="overflow-y-auto">
            <table class="w-3/4 border-collapse">
                <thead>
                    <tr class="bg-yellow-300 text-center">
                        <th class="bg-teal-400 border-b border border-slate-500">REF</th>
                        <th class="bg-teal-400 border-b border border-slate-500">MARQUE</th>
                        <th class="bg-teal-400 border-b border border-slate-500">GROUPE</th>
                        <th class="bg-teal-400 border-b border border-slate-500">FONCTION</th>
                        <th class="bg-teal-400 border-b border border-slate-500">NOM</th>
                        <th class="bg-teal-400 border-b border border-slate-500">FORME</th>
                        <th class="bg-teal-400 border-b border border-slate-500">DIMENSION</th>
                        <th class="bg-teal-400 border-b border border-slate-500">QTE</th>
                        <th class="bg-teal-400 border-b border border-slate-500">PU HTVA</th>
                        <th class="bg-teal-400 border-b border border-slate-500">TOTAL HTVA</th>
                    </tr>
                </thead>
                <tbody>
                    {#each order as row, index}
                        <tr>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.ref}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.brand}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.group}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.fct}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.name}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.form}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.dim}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.qte}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.pu_htva}</td>
                            <td class="bg-white border-b border border-slate-500 px-2">{row.total_htva}</td>
                        </tr>
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>modifyToolQt("tip_blade")}>+</button>
                        <button class="ml-1 bg-red-600 text-white w-6 h-6 rounded-full" on:click={()=>modifyToolQt("tip_blade")}>-</button>
                    {/each}
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- HIDDEN, FOR THE "DYNAMISM/STYLE" OF THE PAGE -->
<div id="overlay" class="fixed inset-0 bg-black bg-opacity-50 hidden"></div>

<div id="big-tool-pannel" class="fixed inset-0 flex justify-center items-center bg-white p-4 hidden">
    <span class="absolute top-2 right-2 text-2xl font-bold cursor-pointer" on:click={(event)=>{event.stopPropagation(); closeBigPicture();}}>&times;</span>
    <img id="big-tool" alt="big tool">
</div>

<div id="add-order-pannel" class="fixed inset-0 flex justify-center items-center bg-white p-4 hidden">
    <span class="absolute top-2 right-2 text-2xl font-bold cursor-pointer" on:click={(event)=>{event.stopPropagation(); closeAddToOrder();}}>&times;</span>
    <div>AJOUTER référence "{toolToAddRef}" à la commande:</div>
    <div>
        <label for="qte">QUANTITE:</label>
        <input type ="number" id="qte" name="qte" class="border border-black rounded p-2 text-black bg-white" bind:value={quantity}>
        <button on:click={()=>addToOrder()}>AJOUT</button>
    </div>
</div>


<style>
     .selected-image{
        border : 2px solid cornflowerblue;
        cursor : pointer;
    }
    .hovered-image{
        border : 2px solid lightgray;
        cursor: pointer;
    }
  </style>
