<script>
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { isAdmin } from "$lib/stores/user_stores";
    import { PUBLIC_API_URL } from "$env/static/public";
    import {
        isEditing,
        reload,
        selectedGroup,
        selectedSubGroup,
        selectedCategoryIndex,
        hoveredCategoryIndex,
        charValues,
        categories,
        currentSuppliers,
        showCategories,
        errorMessage,
        hoveredCategoryImageIndex,
        alternatives,
        selectedSupplierIndex,
        findSubGroupsStore,
        findCharacteristicsStore
    } from "$lib/stores/searches";
    import EditButton from "../../routes/searches/EditButton.svelte";
    import EditCategoryButton from "../../routes/searches/EditCategoryButton.svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { modals } from "svelte-modals";
    import addCategoryModal from "$lib/modals/addCategoryModal.svelte";
    import { _ } from "svelte-i18n";

    /**
     * Display the characteristic values of the category at line index in the table.
     * Update categories to have only the selected one.
     * @param index
     */
    async function selectCategoryWithChar(index) {
        selectCategory(index);
        selectedCategoryIndex.set(index);
        let cat = $categories[$selectedCategoryIndex];
        let catId = $categories[$selectedCategoryIndex].id;
        categories.set([cat]);

        try {
        const response = await apiFetch(`/api/category/${catId}`);
        if (!response.ok) {
            throw new Error("Failed to fetch characteristics of category");
        }
        const categoryChars = await response.json();

        charValues.update((currentValues) => {
            let updatedValues = { ...currentValues }; // Clone current object

            for (let i = 0; i < categoryChars.length; i++) {
            let key = categoryChars[i].name;
            let value = categoryChars[i].value;

            if (key === "Length") {
                value = value.replace(/[^\d.]/g, "");
            }

            const element = document.getElementById(key);
            if (element) {
                element.value = value;
            }

            updatedValues[key] = value;
            }
            return updatedValues;
        });
        } catch (error) {
        console.log(error);
        errorMessage.set(error.message);
        }
        return;
    }

    /**
     * Gets the suppliers of the category given by the line index in the table
     * @param index
     */
    async function selectCategory(index) {
        currentSuppliers.set([]);
        alternatives.set([]);
        selectedCategoryIndex.set(index);
        // Scroll the corresponding image into view
        if (imageRefs[index] instanceof HTMLElement) {
            imageRefs[index].scrollIntoView({ behavior: "smooth", block: "nearest" });
        } else {
            console.warn(`Element at index ${index} is not available or not a valid HTMLElement.`);
        }

      // selecting the categoryId
      const cat = $categories[$selectedCategoryIndex];
      const categoryId = $categories[$selectedCategoryIndex].id;

        try{
            const response = await apiFetch(`/api/category/instruments/${categoryId}`);
            let response2;
            if($isAdmin){
                response2 = await apiFetch(`/api/alternatives/admin/category/${categoryId}`);
        }
        else{
            response2 = await apiFetch(`/api/alternatives/user/category/${categoryId}`);
        }
        if (!response.ok){
            throw new Error("Failed to fetch instruments of category");
        }
        const answer = await response.json();
        currentSuppliers.set(Array.isArray(answer) ? answer : [answer]);
        if (!response2.ok){
            return;
        }
        const answer2 = await response2.json();
        alternatives.set(Array.isArray(answer2)? answer2 : [answer2]);
        }catch (error) {
            console.error(error);
            errorMessage.set(error.message);
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

    function closeBigPicture() {
        const pannel = document.getElementById("big-category-pannel");
        const overlay = document.getElementById("overlay");
        pannel.style.display = "none";
        overlay.style.display = "none";
    }
    
    function openAddCategoryPage() {
        if($selectedGroup == null){
            console.log("Groups are not defined");
            return;
        } 
        else if($selectedSubGroup == null){
            console.log("SubGroups are not defined");
            return;
        }
        else {
            console.log("Groups and subgroups are defined");
            goto("../../admin/add_category");
        }
    }
    let imageContainerRef;
    let imageRefs = [];
    let findSubGroups = $findSubGroupsStore;
    let findCharacteristics = $findCharacteristicsStore;

    function registerImageRef(el, index) {
        if (el) {
            imageRefs[index] = el; // Store the element in the array
        }
    }

</script>
<div class="flex">
    <div class="flex-[3] max-h-[150vh] box-border ml-3 overflow-y-auto">
        <!-- TABLE OF CATEGORIES CORRESPONDING TO SEARCH  -->
        <div class="border bg-teal-400 mb-[5px] font-sans text-base py-0.5 px-2">
            <span class="">{$_('category_component.title.instruments_types')}</span>
        </div>
        <table id="tools-table" data-testid="categories-table" class="w-full border-collapse table-fixed">
            <thead class="bg-teal-400">
                {#if $showCategories}
                    {#if $selectedSubGroup}
                        <tr class="bg-white text-teal-400">
                            {#if $isEditing}
                            <th colspan="2" class="text-center py-2">
                                <button
                                class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
                                on:click={() => openAddCategoryPage()}
                                >
                                Ajouter
                                </button>
                            </th>
                            <th colspan="1" class="text-center pb-1">{$selectedGroup}</th>
                            <th colspan="1" class="text-center pb-1">{$selectedSubGroup}</th>
                        {:else}
                        <th colspan="2" class="text-center pb-1">{$selectedGroup}</th>
                        <th colspan="2" class="text-center pb-1">{$selectedSubGroup}</th>
                        {/if}
                    </tr>
                    {:else}
                <tr class="bg-white text-teal-400">
                {#if $isEditing}
                    <th colspan="2" class="text-center pb-1">
                    <button
                        class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
                        on:click={() => openAddCategoryPage()}
                    >
                        Ajouter
                    </button>
                    </th>
                    <th colspan="3" class="text-center pb-1">{$selectedGroup}</th>
                {:else}
                    <th colspan="5" class="text-center pb-1">{$selectedGroup}</th>
                {/if}
                </tr>
                {/if}
            {/if}
                <tr>
                    {#if $isEditing}
                        <th class="text-center border border-solid border-[black] w-8 overflow-hidden"></th>
                    {/if}
                    {#if !$selectedSubGroup}
                        <th class="text-center border border-solid border-[black] overflow-hidden">{$_('category_component.title.table.subgroup')}</th>
                    {/if}
                    <th class="text-center border border-solid border-[black] w-14 overflow-hidden">{$_('category_component.title.table.function')}</th>
                    <th class="text-center border border-solid border-[black] w-12 overflow-hidden">{$_('category_component.title.table.name')}</th>
                    <th class="text-center border border-solid border-[black] w-8 overflow-hidden">{$_('category_component.title.table.shape')}</th>
                    <th class="text-center border border-solid border-[black] w-6 overflow-hidden">{$_('category_component.title.table.dimension')}</th>
                </tr>
            </thead>
            {#if $showCategories}
                <tbody>
                    {#each $categories as row, index}
                        <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                        <tr
                            class:bg-[cornflowerblue]={$selectedCategoryIndex === index}
                            class:bg-[lightgray]={$hoveredCategoryIndex === index && $selectedCategoryIndex !== index}
                            on:click={() => selectCategory(index)}
                            on:dblclick={() => selectCategoryWithChar(index)}
                            on:mouseover={() => (hoveredCategoryIndex.set(index))}
                            on:mouseout={() => (hoveredCategoryIndex.set(null))}
                        >
                            {#if $isEditing}
                                <EditCategoryButton category={row}/>
                            {/if}
                            {#if !$selectedSubGroup}
                                <td 
                                class="text-center border border-solid border-[black] truncate max-w-[120px] overflow-hidden text-ellipsis whitespace-nowrap"
                                title="{row.subGroupName}"
                            >
                                {row.subGroupName}
                            </td>
                            {/if}
                            <td 
                                class="text-center border border-solid border-[black] truncate max-w-[120px] overflow-hidden text-ellipsis whitespace-nowrap"
                                title="{row.function}"
                            >
                                {row.function}
                            </td>
                            <td 
                                class="text-center border border-solid border-[black] truncate max-w-[150px] overflow-hidden text-ellipsis whitespace-nowrap"
                                title="{row.name}"
                            >
                                {row.name}
                            </td>
                            <td 
                                class="text-center border border-solid border-[black] truncate max-w-[150px] overflow-hidden text-ellipsis whitespace-nowrap"
                                title="{row.shape}"
                            >
                                {row.shape}
                            </td>
                            <td class="text-center border border-solid border-[black] overflow-hidden">{row.lenAbrv}</td>
                        </tr>
                    {/each}
                </tbody>
            {/if}
        </table>
        
    <!-- PASS IN ADMIN MODE -->
    {#if $isEditing}
    {#if $isAdmin}
            <div class="flex justify-center">
                <button 
                    class="mt-4 px-4 py-2 rounded bg-yellow-100 text-black hover:bg-gray-500 transition" 
                    on:click={()=>modals.open(addCategoryModal)}
                >
                {$_('category_component.admin.button.add_category')}
                </button>
            </div>
        {/if}
    {/if}
    </div>

    <!-- PICTURES CORRESPONDING TO THE CATEGORIES -->
    <div class="flex-[1] max-h-[150vh] overflow-y-auto ml-3 max-w-[150px]" bind:this={imageContainerRef}>
        <div class="border bg-teal-400 mb-[5px] font-sans text-base py-0.5 px-2">
            <span class="p-1">{$_('category_component.pictures.title')}</span>
        </div>
        {#each $categories as row, index}
            <!-- svelte-ignore a11yå_click_events_have_key_events -->
            <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
            <!-- svelte-ignore a11y_click_events_have_key_events -->
            <img
                alt="tool{row.id}"
                src={row.pictureId ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}` : "/default/no_picture.png"}
                bind:this={imageRefs[index]}
                ref={el => registerImageRef(el, index)}
                on:click={() =>
                showBigPicture(
                    row.pictureId
                    ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
                    : "/default/no_picture.png"
                )}
                on:mouseover={() => (hoveredCategoryImageIndex.set(index))}
                on:mouseout={() => (hoveredCategoryImageIndex.set(null))}
                class="mb-[3px] {$selectedCategoryIndex === index
                ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
                : ''} {$hoveredCategoryImageIndex === index && $selectedCategoryIndex !== index
                ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image'
                : ''}"
            />
            {#if $isEditing}
                {#if $isAdmin}
                <button class="absolute bottom-2 right-6 w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                    +
                </button> 
                {/if}
            {/if}
        {/each}
    </div>    
</div>    

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>

<div
class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4"
id="big-category-pannel"
>
<!-- svelte-ignore a11y_click_events_have_key_events -->
<!-- svelte-ignore a11y_no_static_element_interactions -->
<span
    class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5 hover:text-[red] cursor-pointer"
    on:click={(event) => {
    event.stopPropagation();
    closeBigPicture();
    }}>&times;</span
>
<img class="h-[300px]" id="big-category" alt="big category" />
</div>
