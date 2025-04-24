<script>
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { isAdmin } from "$lib/stores/user_stores";
    import { PUBLIC_API_URL } from "$env/static/public";
    import { isEditing, reload, selectedGroup, selectedSubGroup, selectedCategoryIndex,
        hoveredCategoryIndex, charValues, categories, currentSuppliers,showCategories,
        errorMessage, hoveredCategoryImageIndex, alternatives,selectedSupplierIndex,
        findSubGroupsStore,findCharacteristicsStore} from "$lib/stores/searches";
    import EditButton from "../../routes/searches/EditButton.svelte";
    import EditCategoryButton from "../../routes/searches/EditCategoryButton.svelte";
    import { apiFetch } from "$lib/utils/fetch";
    import { modals } from "svelte-modals";
    import addCategoryModal from "$lib/modals/addCategoryModal.svelte";
    import { _ } from "svelte-i18n";
    import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";

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
      selectedSupplierIndex.set("");

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
        let supplierArray = Array.isArray(answer) ? answer : [answer];

        // filtering on the suppliers sold by md
        if (!$isAdmin && !isWebmaster) {
          for (let i = 0; i < supplierArray.length; i++) {
            let supp = supplierArray[i].supplier;
            // getting soldByMd field of the supplier
            const getSupplier = await apiFetch(`/api/supplier/name/${supp}`);
            const gotSupplier = await getSupplier.json();
            if (gotSupplier.soldByMd != true) {
              // removing that line
              for(let j = i; j < supplierArray.length - 1; j++) {
                supplierArray[j] = supplierArray[j+1];
              }
              i--; // checking that line again
              // removing 1 of the actual array size
              supplierArray.splice(supplierArray.length - 1, supplierArray.length);
            }
          }
        }
        currentSuppliers.set(supplierArray);
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
    
    let imageContainerRef;
    let imageRefs = [];
    let findSubGroups = $findSubGroupsStore;
    let findCharacteristics = $findCharacteristicsStore;

    function imageRef(node, index) {
      imageRefs[index] = node;
      return {
          destroy() {
              imageRefs[index] = null;
          }
      };
    }

    let timeout;
    const clickDelay = 200;
    function clickOnAlt(row, index){
        timeout = setTimeout(() => {
            modals.open(BigPicturesModal, {instrument:row, index:index});
        }, clickDelay);
    }
</script>

<div class="flex">
  <div class="flex-[3] max-h-[150vh] box-border ml-3 overflow-y-auto">
    <!-- TABLE OF CATEGORIES CORRESPONDING TO RESEARCH  -->
    <table id="tools-table" data-testid="categories-table" class="w-full border-collapse">
      <thead class="bg-teal-400">
        {#if $showCategories}
          {#if $selectedSubGroup}
            <tr class="bg-white text-teal-400">
              {#if $isEditing}
                <th colspan="2" class="text-center py-2">
                  <button
                    class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
                    onclick={()=>modals.open(addCategoryModal, {fromSearches : true})}
                  >
                  {$_('category_component.admin.button.add_category')}
                  </button>
                </th>
              {/if}
            </tr>
            <tr class="bg-white text-teal-400">
              {#if $isEditing}
                <th colspan="1" class="text-center pb-1">{$selectedGroup}</th>
                <th colspan="1" class="text-center pb-1">{$selectedSubGroup}</th>
              {:else}
                <th colspan="2" class="text-center pb-1">{$selectedGroup}</th>
                <th colspan="2" class="text-center pb-1">{$selectedSubGroup}</th>
              {/if}
            </tr>
          {:else}
          <tr class="bg-white text-teal-400">
            {#if $isEditing && $selectedSubGroup}
              <th colspan="2" class="text-center pb-1">
                <button
                  class="px-3 py-1 rounded bg-yellow-100 text-black hover:bg-gray-500 transition focus:outline-none"
                  onclick={()=>modals.open(addCategoryModal)}
                >
                {$_('category_component.admin.button.add_category')}
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
          {#if $isEditing && $selectedSubGroup}
            <th class="text-center border border-solid border-[black]">{$_('category_component.title.table.group')}</th>
          {/if}
          {#if !$selectedSubGroup}
          <th class="text-center border border-solid border-[black]">{$_('category_component.title.table.subgroup')}</th>
          {/if}
          <th class="text-center border border-solid border-[black]">
            {$_('category_component.title.table.function')}</th>
          <th class="text-center border border-solid border-[black]">{$_('category_component.title.table.name')}</th>
          <th class="text-center border border-solid border-[black]">{$_('category_component.title.table.shape')}</th>
          <th class="text-center border border-solid border-[black]">{$_('category_component.title.table.dimension')}</th>
        </tr>
      </thead>
      {#if $showCategories}
        <tbody>
          {#each $categories as row, index}
            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
            <tr
              class:bg-[cornflowerblue]={$selectedCategoryIndex === index}
              class:bg-[lightgray]={$hoveredCategoryIndex === index &&
                $selectedCategoryIndex !== index}
              onclick={() => selectCategory(index)}
              ondblclick={() => selectCategoryWithChar(index)}
              onmouseover={() => hoveredCategoryIndex.set(index)}
              onmouseout={() => hoveredCategoryIndex.set(null)}
            >
              {#if $isEditing && $selectedSubGroup}
                <EditCategoryButton category={row} />
              {/if}
              {#if !$selectedSubGroup}
              <td class="text-center border border-solid border-[black]">{row.subGroupName}</td>
              {/if}
              <td class="text-center border border-solid border-[black]">{row.function}</td>
              <td class="text-center border border-solid border-[black]">{row.name}</td>
              <td class="text-center border border-solid border-[black]">{row.shape}</td>
              <td class="text-center border border-solid border-[black]">{row.lenAbrv}</td>
            </tr>
          {/each}
        </tbody>
      {/if}
    </table>
  </div>

  <!-- PICTURES OF THE CATEGORIES -->
  <div class="flex-[1] max-h-[150vh] overflow-y-auto ml-3 max-w-[150px]" bind:this={imageContainerRef}>
      <div class="border bg-teal-400 mb-[5px] font-sans text-base py-0.5 px-2">
          <span class="p-1">{$_('category_component.pictures.title')}</span>
      </div>
      {#each $categories as row, index}
          <!-- svelte-ignore a11yå_click_events_have_key_events -->
          <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
          <!-- svelte-ignore a11y_mouse_events_have_key_events -->
          <!-- svelte-ignore a11y_click_events_have_key_events -->
           <div class="flex justify-center items-center border-[1px] border-solid border-[lightgray] mb-0.5">
          <img
              use:imageRef={index}
              alt="tool{row.id}"
              src={row.picturesId && row.picturesId[0]
                    ? PUBLIC_API_URL + `/api/pictures/${row.picturesId[0]}`: "/default/no_picture.png"}
              onclick= {() => modals.open(BigPicturesModal, { instrument: row, index: index , isInstrument: false })}
              onmouseover={() => (hoveredCategoryImageIndex.set(index))}
              onmouseout={() => (hoveredCategoryImageIndex.set(null))}
              class="{$selectedCategoryIndex === index
              ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
              : ''} {$hoveredCategoryImageIndex === index && $selectedCategoryIndex !== index
              ? 'hoveredcursor-pointer border-2 border-solid border-[lightgray]-image'
              : ''}"
          />
        </div>
      {/each}
  </div>
</div>      

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>
