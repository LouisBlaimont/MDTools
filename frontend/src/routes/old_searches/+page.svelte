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

<div class="top-bar">
    <!-- svelte-ignore a11y_consider_explicit_label -->
    <button class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-500 rounded-full" on:click={openModifPage}>
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

<div class = "flex gap-[5px] box-border w-full h-[80vh]">
    <div class ="flex-1 h-full ml-[5px]">
        <div class="flex-1 m-0">
            <form class="flex flex-col w-[90%] mb-2.5">
                <label for="google-search" id="google-search-label">Recherche par mot(s) clé(s)</label>
                <input type="text" id="google-search" name="google-search" placeholder="Entrez un mot clé">
            </form>
            <form class="flex flex-col w-full gap-2.5">
                Recherche par charactéristiques :<br>
                <div class = "flex items-center">
                    <label for="group">Groupe :</label>
                    <input type ="text" id="group" name="group">
                </div>
                <div class = "flex items-center">
                    <label for="name">Nom :</label>
                    <input type="text" id="name" name="name">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("name")}>&times;</button>
                </div>
                <button id="clear-all" on:click={()=>deleteAllCharacteristics()}>Tout effacer</button>
                <div class = "flex items-center">
                    <label for="fct">Fonction :</label>
                    <input type="text" id="fct" name="fct">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("fct")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="curvature_bade">Courbure lame :</label>
                    <input type="text" id="curvature_blade" name="curvature_blade">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("curvature_blade")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="tip_blade">Pointe lame :</label>
                    <input type="text" id="tip_blade" name="tip_blade">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("tip_blade")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="specific_blade">Spécificité lame :</label>
                    <input type="text" id="specific_blade" name="specific_blade">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("specific_blade")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="material">Matière:</label>
                    <input type="text" id="material" name="material">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("material")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="thick">Épaisseur :</label>
                    <input type="text" id="thick" name="thick">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("thick")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="arm">Forme manche :</label>
                    <input type="text" id="arm" name="arm">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("arm")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="rings">Anneaux :</label>
                    <input type="text" id="rings" name="rings">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("rings")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="length">Longueur :</label>
                    <input type="text" id="length" name="length">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("length")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="tolerance">Tolérance :</label>
                    <input type="text" id="tolerance" name="tolerance">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("tolerance")}>&times;</button>
                </div>
                <div class = "flex items-center">
                    <label for="other">Autres :</label>
                    <input type="text" id="other" name="other">
                    <button class="text-[white] bg-[firebrick] w-[25px] h-[25px] ml-0.5 rounded-[50%] border-[none]" on:click={()=>deleteCharacteristic("other")}>&times;</button>
                </div> 
            </form>
        </div>
    </div>
    <div class = "flex-[6] flex flex-col gap-[15px] h-full">
        <div class = "flex gap-[15px] box-border w-full h-[80vh]">
            <div class="flex-[3] h-full overflow-y-auto box-border m-0">
                <table id="tools-table" class="w-full border-collapse">
                    <thead><tr><th>GROUPE</th><th>FONCTION</th><th>NOM</th><th>FORME</th><th>DIMENSION</th></tr></thead>
                    <tbody>
                        {#each tools as row, index}

                            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                            <tr
                                class:selected-row={selectedToolIndex === index}
                                class:hovered-row={hoveredToolIndex === index && selectedToolIndex !== index}
                                on:click={()=>selectTool(index)}
                                on:mouseover={()=> hoveredToolIndex = index}
                                on:mouseout={() => hoveredToolIndex = null}
                            >
                                <td>{row.group}</td><td>{row.fct}</td><td>{row.name}</td><td>{row.form}</td><td>{row.dim}</td>
                            </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
            <div class="flex-1 max-h-full overflow-y-auto box-border m-0">
                {#each tools as row, index}
                    <!-- svelte-ignore a11y_click_events_have_key_events -->
                    <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                    <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                    <img 
                        alt="tool{row.id}" 
                        src={row.src}
                        on:click={()=>showBigPicture(row.src)} 
                        on:mouseover={()=>hoveredToolImageIndex = index}
                        on:mouseout={()=>hoveredToolImageIndex = null}
                        class="image {selectedToolIndex === index ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]' : ''} {hoveredToolImageIndex === index && selectedToolIndex !== index ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image' : ''}">
                {/each}
            </div>

            <div class="flex-[3] max-h-full overflow-y-auto box-border m-0">
                <div class="border bg-[tan] mb-[5px] border-solid border-[black]">Photos fournisseurs</div>
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


                <div class = "suppliers-table">
                    <table class="w-full border-collapse">
                        <thead><tr><th>AJOUT</th><th>REF</th><th>MARQUE</th><th>DESCRIPTION</th><th>PRIX</th><th>ALT</th><th>OBS</th></tr></thead>
                        <tbody>
                            {#each currentSuppliers as row, index}
                                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                                <tr
                                    class:selected-row={selectedSupplierIndex === index}
                                    class:hovered-row={hoveredSupplierIndex === index && selectedSupplierIndex !== index}
                                    on:click={()=>selectSupplier(index)}
                                    on:mouseover={()=> hoveredSupplierIndex = index}
                                    on:mouseout={() => hoveredSupplierIndex = null}
                                >
                                    <td class="text-[green]" on:click={()=>addToOrderPannel(row.ref)}>+</td><td>{row.ref}</td><td>{row.brand}</td><td>{row.description}</td><td>{row.price}</td><td>{row.alt}</td><td>{row.obs}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    
        <div class = "orders">
            <div class = "flex flex-row">
                <div class="w-6/12 mr-0">
                    <form class="my-2.5">
                        <label for="order-search" id="order-search-label">Recherche par nom de commande : </label>
                        <input type="text" id="order-search" name="order-search" >
                    </form>
                </div>
                <div>
                    <button class="text-[white] border bg-[green] mt-[15px] p-2.5 rounded-[10px] border-solid border-[black]" on:click={()=>exportOrderToExcel()}>Exporter</button>
                </div>
            </div>
            <div class="w-[90%] mb-[50px]">
                <table class="w-full border-collapse">
                    <thead><tr><th>REF</th><th>MARQUE</th><th>GROUPE</th><th>FONCTION</th><th>NOM</th><th>FORME</th><th>DIMENSION</th><th>QTE</th><th>PU HTVA</th><th>TOTAL HTVA</th></tr></thead>
                    <tbody>
                        {#each order as row, index}
                        <tr>
                            <td>{row.ref}</td><td>{row.brand}</td><td>{row.group}</td><td>{row.fct}</td><td>{row.name}</td><td>{row.form}</td><td>{row.dim}</td><td>{row.qte}</td><td>{row.pu_htva}</td><td>{row.total_htva}</td>
                        </tr>
                        {/each}
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>

<div class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4" id="big-tool-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5" on:click={(event)=>{event.stopPropagation(); closeBigPicture();}}>&times;</span>
    <img class="h-[300px]" alt="big tool">
</div>

<div class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4" id="add-order-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5" on:click={(event)=>{event.stopPropagation(); closeAddToOrder();}}>&times;</span>
    <div>AJOUTER référence "{toolToAddRef}" à la commande:</div>
    <div>
        <label for="qte">QUANTITE:</label>
        <input type ="number" id="qte" name="qte" class="border border-black rounded p-2 text-black bg-white" bind:value={quantity}>
        <button on:click={()=>addToOrder()}>AJOUT</button>
    </div>
        
</div>

<style>
    div, button, input{
        font-size: 13px;
    }
    .top-bar {
        width: 100%;
        display: flex;
        justify-content: space-between;
        background-color: lightskyblue; 
        padding: 5px 5px;
        box-sizing: border-box;
        margin-bottom : 10px;
    }

    input[type="text"]{
        width : 50%;
        border : 1px solid black;
        padding: 2px;
    }
    label{
        width : 40%;
    }

    #google-search-label{
        width : 100%
    }

    #clear-all{
        width : 90px;
        border: 1px solid black;
        background-color : gray;  
    }
   
   
    th, td{
        text-align: center;
        border: 1px solid black;
    }
    th{
        background-color: tan;
    }
    .tools-pictures img{
        width: 90%;
        margin-bottom: 3px;
    }
  
    .close:hover{
        color:red;
        cursor: pointer;
    }

    .add-tool:hover{
        background-color: green;
        color : white;
        cursor: pointer;
    }
    #add-order-pannel{
        color : white;
        flex-direction: column;
        gap : 15px;
    }
    button:hover{
        cursor: pointer;
    }
    
    .selected-row{
        background-color: cornflowerblue;
    }
    .hovered-row{
        background-color: lightgray;
        cursor: pointer;
    }
    
</style>
