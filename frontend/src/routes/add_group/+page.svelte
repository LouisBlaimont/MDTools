<head><title>Add a group</title></head>

<script>
    function display_char_fields() {
        var n = document.getElementById("number_char").value; 
        let field_name = document.getElementById("field_name");
        let field_to_clone = document.getElementById("field_to_clone");
        let number_field = document.getElementById("number_field");
        const fields = document.getElementById("fields");

        number_field.style.display = "none";
        field_name.style.display = "block";

        for(let i = 0; i < n; i++) {
            const clonedDiv = field_to_clone.cloneNode(true);
            clonedDiv.style.display = "block";
            fields.appendChild(clonedDiv);
        }
    }

    function close_char() {
        const fields = document.getElementById("fields");
        fields.style.display = "none";
        let field_name = document.getElementById("field_name");
        field_name.style.display = "none";
        let number_field = document.getElementById("number_field");
        number_field.style.display = "block";
    }
</script>

<main class="flex flex-col md:flex-row justify-center items-start space-y-8 md:space-y-0 md:space-x-10 px-8 py-16 max-w-screen-xl mx-auto">
    <form class="border-3 rounded-lg w-full md:w-2/3 lg:w-1/2 p-6 mx-auto bg-white shadow-lg">
        <div class="text-teal-500 text-2xl font-bold py-6">
            <span>Formulaire pour ajouter un groupe d'instruments</span>
        </div>
        <div>        
            <label for="id_add_group_name" class="font-semibold text-lg">Nom du groupe:</label>
            <input name="add_group" id="add_group" class="mb-2 w-full p-3 border border-gray-300 rounded-lg" placeholder="Entrez le nom du groupe">   
            
            <div id="number_field" style="display: block;">
                <label for="id_char_group_char" class="font-semibold text-lg">Combien de charactéristiques a le groupe?</label>
                <input type="number" name="number_char" id="number_char" on:keydown={()=>display_char_fields()} class="w-full p-3 border border-gray-300 rounded-lg mb-2" placeholder="Entrez un nombre"> 
            </div>
            
            <div id="field_name" style="display: none;">
                <label for="id_add_char" class="font-semibold text-lg">Entrez les charactéristiques: </label>
            </div>
            <div id="fields"></div>
            <div id="field_to_clone" style="display: none;" class="py-1">
                <input name="char_group" class="w-full p-3 border border-gray-300 rounded-lg mb-2" placeholder="Entrez une charactéristique">      
            </div> 

            <label for="id_add_img" class="font-semibold text-lg">Uploadez une image du groupe</label> 
            <input class="mb-4 block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 dark:text-gray-400 focus:outline-none dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400" id="file_input" type="file">
           
        
            <button class="w-full bg-teal-500 text-white py-3 rounded-lg hover:bg-teal-600 text-lg"><a href="/home_2">Ajouter</a></button>
        </div>    
    </form>
</main>