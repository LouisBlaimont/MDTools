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
    function openSearchPage(){
        goto('/searches');
    }

</script>

<div class="top-bar">
    <img id = "logo_MD" alt = "MDtools logo" src="/MDlogo.jpg" >
    <!-- svelte-ignore a11y_consider_explicit_label -->
    <button class="flex items-center justify-center w-10 h-10 bg-gray-200 hover:bg-yellow-300 rounded-full" on:click={openSearchPage}>
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

<div class = "container">
    <div class ="side-bar">
        <div class="searches">
            <form id="google-search">
                <label for="google-search" id="google-search-label">Recherche par mot(s) clé(s)</label>
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
                        <thead><tr><th>REF</th><th>MARQUE</th><th>DESCRIPTION</th><th>PRIX</th><th>ALT</th><th>OBS</th></tr></thead>
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
                                    <td>{row.ref}</td><td>{row.brand}</td><td>{row.description}</td><td>{row.price}</td><td>{row.alt}</td><td>{row.obs}</td>
                                </tr>
                            {/each}
                        </tbody>
                    </table>
                </div>
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
  
    #logo_MD {
        height: 50px; 
    }

    .container{
        display: flex;
        gap : 5px;
        box-sizing: border-box;
        width: 100%;
        height: 80vh;
    }
    .side-bar{
        margin-left: 5px;
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
        width: 25px;
        height: 25px;
        border-radius: 50%;
    }
    .form-group {
        display: flex;
        align-items: center;
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
        height: 160px;
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
        flex-shrink: 0;
        flex-direction: column;
        height: 95%;
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
    button:hover{
        cursor: pointer;
    }


    
  </style>
