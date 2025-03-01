<script>
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { PUBLIC_API_URL } from "$env/static/public";
    import { isEditing, reload } from "$lib/stores/searches";
    import EditButton from "./EditButton.svelte";
    import EditCategoryButton from "./EditCategoryButton.svelte";



    /**
     * Display the characteristic values of the category at line index in the table.
     * Update categories to have only the selected one.
     * @param index
     */
    async function selectCategoryWithChar(index) {
        selectCategory(index);
        selectedCategoryIndex = index;
        let cat = categories[selectedCategoryIndex];
        let catId = cat.id;
        categories = [cat];

        try{
        const response = await fetch(PUBLIC_API_URL + `/api/category/${catId}`);
        if(!response.ok){
            throw new Error("Failed to fetch characteristics of category");
        }
        const categoryChars = await response.json();
        for (let i = 0; i<categoryChars.length ; i++){
            if (categoryChars[i].name === "Length"){
            const len_val =  categoryChars[i].value.replace(/[^\d.]/g, "");
            document.getElementById(categoryChars[i].name).value = len_val;
            charValues[categoryChars[i].name] = len_val;
            }
            else{
            document.getElementById(categoryChars[i].name).value = categoryChars[i].value;
            charValues[categoryChars[i].name] = categoryChars[i].value;
            }
        }
        }catch(error){
        console.error(error)
        errorMessage = error.message;
        }
        return;

    }

    /**
     * Gets the suppliers of the category given by the line index in the table
     * @param index
     */
    async function selectCategory(index) {
        selectedCategoryIndex = index;

        // selecting the categoryId
        const cat = categories[selectedCategoryIndex]; 
        const categoryId = cat.id;  

        try{
        const response = await fetch(PUBLIC_API_URL + `/api/category/instruments/${categoryId}`);
        if (!response.ok){
            throw new Error("Failed to fetch instruments of category");
        }
        const answer = await response.json();
        currentSuppliers = Array.isArray(answer) ? answer : [answer];
        }catch (error) {
        console.error(error);
        errorMessage = error.message;
        }
        return;
    }

    function showBigPicture(img) {
        const pannel = document.getElementById("big-category-pannel");
        const overlay = document.getElementById("overlay");
        const picture = document.getElementById("big-category");
        pannel.style.display = "flex";
        overlay.style.display = "block";
        picture.src = img;
    }

</script>

<!-- TABLE OF CATEGORIES CORRESPONDING TO RESEARCH  -->
<div class="flex-[3] h-full overflow-y-auto box-border ml-3 resizable" bind:this={div2}>
    <table id="tools-table" data-testid="categories-table" class="w-full border-collapse">
        <thead class="bg-teal-400">
        <tr>
            {#if $isEditing}
            <th class="text-center border border-solid border-[black]"></th>
            {/if}
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
                class:bg-[cornflowerblue]={selectedCategoryIndex === index}
                class:bg-[lightgray]={hoveredCategoryIndex === index &&
                selectedCategoryIndex !== index}
                on:click={() => selectCategory(index)}
                on:dblclick={() => selectCategoryWithChar(index)}
                on:mouseover={() => (hoveredCategoryIndex = index)}
                on:mouseout={() => (hoveredCategoryIndex = null)}
            >
                {#if $isEditing}
                <EditCategoryButton category={row}/>
                {/if}
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


    <!-- PASS IN ADMIN MODE -->
    {#if isAdmin}
        <EditButton />
    {/if}
    <div class="resize-handle" on:mousedown={(e) => startResize(e, div2)}></div>
</div>

<!-- PICTURES CORRESPONDING TO THE CATEGORIES -->
<div class="flex-1 max-h-[80vh] overflow-y-auto box-border ml-3 max-w-[150px] resizable" bind:this={div3}>
    {#each categories as row, index}
        <!-- svelte-ignore a11yå_click_events_have_key_events -->
        <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
        <!-- svelte-ignore a11y_mouse_events_have_key_events -->
        <img
            alt="tool{row.id}"
            src={row.pictureId
            ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
            : "/default/no_picture.png"}
            on:click={() =>
            showBigPicture(
                row.pictureId
                ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
                : "/default/no_picture.png"
            )}
            on:mouseover={() => (hoveredCategoryImageIndex = index)}
            on:mouseout={() => (hoveredCategoryImageIndex = null)}
            class="mb-[3px] {selectedCategoryIndex === index
            ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
            : ''} {hoveredCategoryImageIndex === index && selectedCategoryIndex !== index
            ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image'
            : ''}"
        />
        {#if $isEditing}
            {#if isAdmin}
            <button class="absolute bottom-2 right-6 w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                +
            </button> 
            {/if}
        {/if}
    {/each}
    <div class="resize-handle" on:mousedown={(e) => startResize(e, div3)}></div>
</div>