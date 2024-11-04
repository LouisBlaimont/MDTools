<script>
    import { tools } from '../../tools.js';
    import { suppliers } from '../../suppliers.js';
    import { getOrder, addTool } from '../../order.js';

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


</script>

<div class="top-bar">
    <img id = "logo_MD" alt = "MDtools logo" src="/MDlogo.jpg" >
</div>

<div class = "container">
    <div class ="side-bar">
        <div class="searches">
            <form id="google-search">
                <label for="google-search" id="google-search-label">Recherche par mot clé(s)</label>
                <input type="text" id="google-search" name="google-search" placeholder="Entrez un mot clé">
            </form>
            <form id="char-search">
                Recherche par charactéristiques :<br>
                <div class = "form-group">
                    <label for="group">Groupe :</label>
                    <input type ="text" id="group" name="group">
                </div>
                <div class = "form-group">
                    <label for="name">Nom :</label>
                    <input type="text" id="name" name="name">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("name")}>&times;</button>
                </div>
                <button id="clear-all" on:click={()=>deleteAllCharacteristics()}>Tout effacer</button>
                <div class = "form-group">
                    <label for="fct">Fonction :</label>
                    <input type="text" id="fct" name="fct">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("fct")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="curvature_bade">Courbure lame :</label>
                    <input type="text" id="curvature_blade" name="curvature_blade">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("curvature_blade")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="tip_blade">Pointe lame :</label>
                    <input type="text" id="tip_blade" name="tip_blade">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("tip_blade")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="specific_blade">Spécificité lame :</label>
                    <input type="text" id="specific_blade" name="specific_blade">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("specific_blade")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="material">Matière:</label>
                    <input type="text" id="material" name="material">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("material")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="thick">Épaisseur :</label>
                    <input type="text" id="thick" name="thick">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("thick")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="arm">Forme manche :</label>
                    <input type="text" id="arm" name="arm">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("arm")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="rings">Anneaux :</label>
                    <input type="text" id="rings" name="rings">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("rings")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="length">Longueur :</label>
                    <input type="text" id="length" name="length">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("length")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="tolerance">Tolérance :</label>
                    <input type="text" id="tolerance" name="tolerance">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("tolerance")}>&times;</button>
                </div>
                <div class = "form-group">
                    <label for="other">Autres :</label>
                    <input type="text" id="other" name="other">
                    <button class="delete_char" on:click={()=>deleteCharacteristic("other")}>&times;</button>
                </div> 
            </form>
        </div>
    </div>
    <div class = "main-content">
        <div class = "tools-suppliers">
            <div class="tools">
                <table id="tools-table">
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
            <div class="tools-pictures">
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
                        class="image {selectedToolIndex === index ? 'selected-image' : ''} {hoveredToolImageIndex === index && selectedToolIndex !== index ? 'hovered-image' : ''}">
                {/each}
            </div>

            <div class="suppliers">
                <div id="supplier-pictures-text">Photos fournisseurs</div>
                <div class="suppliers-pictures">
                    {#each currentSuppliers as row, index}
                        <!-- svelte-ignore a11y_click_events_have_key_events -->
                        <!-- svelte-ignore a11y_no_static_element_interactions -->
                        <div class="suppliers-picture-group" on:click={()=>showBigPicture(row.src)}>
                            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                            <img 
                                alt="supplier{row.id}" 
                                src={row.src}
                                on:click={()=>showBigPicture(row.src)} 
                                on:mouseover={()=>hoveredSupplierImageIndex = index}
                                on:mouseout={()=>hoveredSupplierImageIndex = null}
                                class="image {selectedSupplierIndex === index ? 'selected-image' : ''} {hoveredSupplierImageIndex === index && selectedSupplierIndex !== index ? 'hovered-image' : ''}"
                            >
                            <div class="reference">{row.ref}</div>
                        </div>
                    {/each}
                </div>


                <div class = "suppliers-table">
                    <table>
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
                                    <td class="add-tool" on:click={()=>addToOrderPannel(row.ref)}>+</td><td>{row.ref}</td><td>{row.brand}</td><td>{row.description}</td><td>{row.price}</td><td>{row.alt}</td><td>{row.obs}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    
        <div class = "orders">
            <div class = "form-export-order">
                <div class="form-order">
                    <form id="order-search">
                        <label for="order-search" id="order-search-label">Recherche par nom de commande : </label>
                        <input type="text" id="order-search" name="order-search" >
                    </form>
                </div>
                <div>
                    <button id="export-order" on:click={()=>exportOrderToExcel()}>Exporter</button>
                </div>
            </div>
            <div class="order-table">
                <table>
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

<div class="overlay" id="overlay"></div>

<div class="big-pannel" id="big-tool-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="close" on:click={(event)=>{event.stopPropagation(); closeBigPicture();}}>&times;</span>
    <img id="big-tool" alt="big tool">
</div>

<div class="big-pannel" id="add-order-pannel">
    <!-- svelte-ignore a11y_click_events_have_key_events -->
    <!-- svelte-ignore a11y_no_static_element_interactions -->
    <span class="close" on:click={(event)=>{event.stopPropagation(); closeAddToOrder();}}>&times;</span>
    <div>AJOUTER référence "{toolToAddRef}" à la commande:</div>
    <div>
        <label for="qte">QUANTITE:</label>
        <input type ="number" id="qte" name="qte" bind:value={quantity}>
        <button on:click={()=>addToOrder()}>AJOUT</button>
    </div>
        
</div>

<style>
    div, button, input{
        font-size: 13px;
    }
    .top-bar {
        width: 100%;
        background-color: lightskyblue; 
        padding: 5px 5px;
        box-sizing: border-box;
        margin-bottom : 10px;
    }
  
    #logo_MD {
        height: 50px; 
    }

    .container{
        display: flex;
        gap : 15px;
        box-sizing: border-box;
        width: 100%;
        height: 80vh;
    }
    .side-bar{
        flex : 1;
        height : 100%;
    }
    #google-search{
        display: flex;
        flex-direction: column;
        margin-bottom: 10px;
        width : 90%;
    }

    #char-search{
        display: flex;
        flex-direction: column;
        width: 100%;
        gap: 10px;
    }
    .delete_char{
        margin-left: 2px;
        color : white;
        background-color: firebrick;
        border : none;
        border-radius: 50%;
    }
    .form-group {
        display: flex;
        align-items: center;
    }

    input[type="text"]{
        width : 50%;
    }
    label{
        width : 40%;
    }
    #google-search-label{
        width : 100%
    }
    #clear-all{
        width : 90px
    }
    .main-content{
        flex: 6;
        display: flex;
        flex-direction: column;
        gap: 15px;
        height: 100%;
    }
    .tools-suppliers{
        display: flex;
        gap : 15px;
        box-sizing: border-box;
        width: 100%;
        height: 80vh;
    }
    .searches{
        flex : 1;
        margin: 0;
    }
    .tools{
        flex : 3;
        margin: 0;
        height: 100%; 
        overflow-y: auto; 
        box-sizing: border-box;
    }
    .tools-pictures{
        flex : 1;
        margin: 0;
        max-height: 100%; 
        overflow-y: auto; 
        box-sizing: border-box;
    }
    .suppliers{
        flex : 3;
        margin: 0;
        max-height: 100%; 
        overflow-y: auto; 
        box-sizing: border-box;
    }
    .suppliers-pictures{
        display: flex;
        height: 140px;
        margin-bottom: 15px;
        max-width : 100%; 
        overflow-x: auto; 
        box-sizing: border-box;
    }
    
    table{
        width : 100%;
        border-collapse: collapse;
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
    .suppliers-picture-group{
        display: flex;
        flex-direction: column;
        height: 100%;
        text-align: center;
        box-sizing: border-box;
        border : 1px solid black;
        margin-right : 3px;
    }
    .suppliers-picture-group .reference{
        border-top: 1px solid black;
        padding: 3px;
        box-sizing: border-box;
    }
    .suppliers-picture-group img{
        height: 80%;
    }
    
    #order-search{
        margin-top: 10px;
        margin-bottom: 10px;
    }
    .order-table{
        width: 90%;
        margin-bottom: 50px;
    }

    .big-pannel{
        display: none;
        position: fixed;
        padding : 50px;
        box-sizing: border-box;
        background-color: rgba(0,0,0,0.8);
        justify-content: center;
        align-items: center;
        top : 50%;
        left : 50%;
        transform: translate(-50%, -50%);
        border-radius: 30px;
    }
    .overlay {
        display: none; 
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0); 
    }
    #big-tool{
        height: 300px;
    }
    .close{
        position: absolute;
        top : 10px;
        right : 15px;
        color : white;
        font-size: 40px;
        cursor: pointer;
        transition: color 0.3s;
    }
    .close:hover{
        color:red;
        cursor: pointer;
    }

    .selected-row{
        background-color: cornflowerblue;
    }
    .hovered-row{
        background-color: lightgray;
        cursor: pointer;
    }
    .selected-image{
        border : 2px solid cornflowerblue;
        cursor : pointer;
    }
    .hovered-image{
        border : 2px solid lightgray;
        cursor: pointer;
    }

    #supplier-pictures-text{
        border: 1px solid black;
        background-color: tan;
        margin-bottom: 5px;
    }
    .add-tool{
        color:green;
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
    #export-order{
        color:white;
        border : 1px solid black;
        background-color: green;
        padding : 10px;
        border-radius: 10px;
        margin-top: 15px; 
    }
    .form-export-order{
        display: flex;
        flex-direction: row;
    }
    .form-order{
        width : 50%;
        margin-right : 0px;
    }


    
  </style>
