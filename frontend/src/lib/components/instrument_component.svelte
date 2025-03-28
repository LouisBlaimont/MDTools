<script> 
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { isAdmin } from "$lib/stores/user_stores";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { PUBLIC_API_URL } from "$env/static/public";
    import EditInstrumentButton from "../../routes/searches/EditInstrumentButton.svelte";    
    import { isEditing, orderItems, reload, category_to_addInstrument, categories, selectedCategoryIndex, selectedSupplierIndex, quantity, currentSuppliers, hoveredSupplierImageIndex, 
        hoveredSupplierIndex, alternatives, selectedGroup, selectedSubGroup, selectedAlternativeIndex, hoveredAlternativeIndex} from "$lib/stores/searches";   
    import {startResize, resize, stopResize} from "$lib/resizableUtils.js";
    import { modals } from "svelte-modals";
    import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";
    import AddCategoryModal from "$lib/modals/AddCategoryModal.svelte";
    import addInstrumentToOrderModal from "$lib/modals/addInstrumentToOrderModal.svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { apiFetch } from "$lib/utils/fetch";
 

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

    /**
     * Opens the add instrument page and set the category to the selected category or null if no category is selected
     * 
     * @param {number} index - The index of the selected category
     * @returns {void}
     */
    function openAddInstrumentPage() {

        if ($selectedCategoryIndex == null || $selectedCategoryIndex == ""){
            console.log("Categories are not defined");
            category_to_addInstrument.set(null);

            // Open the notification modal
            modals.open(AddCategoryModal, {
                title: "Aucune catégorie sélectionnée",
                message: "Veuillez sélectionner une catégorie ou en créer une nouvelle avant d'ajouter un instrument.",
                onClose: () => {
                    console.log("Notification modal closed");
                }
            });
        } else {
            console.log("Categories are defined");
            category_to_addInstrument.set($categories[$selectedCategoryIndex].id);
            goto("../../admin/add_instrument");
        }
    }

    function seeAllAlternatives(){
        console.log($selectedCategoryIndex);
        if ($selectedCategoryIndex !== null && $selectedCategoryIndex !== "" && $selectedCategoryIndex >= 0){
            goto("/alternatives");
            return
        }
        else {
            toast.push("Veuillez sélectionner une catégorie pour en voir les alternatives.");
            return;
        }
    }

    async function selectAlternative(row, index){
        const categoryId = row.categoryId;
        const instrumentId = row.id;
        window.open(`/searches?group=${encodeURIComponent($selectedGroup)}&subgroup=${encodeURIComponent($selectedSubGroup)}&category=${encodeURIComponent(categoryId)}&instrument=${encodeURIComponent(instrumentId)}`, '_blank');
        return;
    }

</script>

<div class="flex-[3] overflow-y-auto box-border m-0 ml-1">
    <!-- PICTURES OF THE INSTRUMENTS -->
    <div class="border bg-teal-400 mb-[5px] border-solid border-[black]">
        <span class="p-1">Photos des fournisseurs</span>
    </div>
    <div class="flex h-40 max-w-full overflow-x-auto box-border mb-[15px]">
        {#each $currentSuppliers as row, index}
            <!-- svelte-ignore a11y_click_events_have_key_events -->
            <!-- svelte-ignore a11y_no_static_element_interactions -->
            <div
            class="flex shrink-0 flex-col h-[95%] text-center box-border border mr-[3px] border-solid border-[black]"
            onclick={() => showBigPicture(row.src? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
                : "/default/no_picture.png"
            )}
            >
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                <img
                    alt="supplier{row.id}"
                    src={row.src
                    ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
                    : "/default/no_picture.png"}
                    onclick= {() => modals.open(BigPicturesModal, { initInstrument: row})}
                    onmouseover={() => (hoveredSupplierImageIndex.set(index))}
                    onmouseout={() => (hoveredSupplierImageIndex.set(null))}
                    class="h-4/5 {$selectedSupplierIndex === index
                    ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
                    : ''} {$hoveredSupplierImageIndex === index && $selectedSupplierIndex !== index
                    ? 'cursor-pointer border-2 border-solid border-[lightgray]'
                    : ''}"
                />
                <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.ref}</div>
            </div>
        {/each}
    </div>


    <!-- TABLE OF THE SUPPLIERS -->
    <table data-testid = "suppliers-table" class="w-full border-collapse">
        <thead class="bg-teal-400">
            <tr class="bg-white text-teal-400">
                <th colspan="4" class="text-center py-2">Instruments</th>
            </tr>
            <tr class="bg-teal-400">
                {#if $isEditing}
                  <th class="text-center border border-solid border-[black] w-10"></th>
                {/if}
                <th class="text-center border border-solid border-[black] w-5 overflow-hidden">AJOUT</th>
                <th class="text-center border border-solid border-[black] w-14 overflow-hidden">REF</th>
                <th class="text-center border border-solid border-[black] w-14 overflow-hidden">MARQUE</th>
                <th class="text-center border border-solid border-[black] w-18 overflow-hidden">DESCRIPTION</th> 
                <th class="text-center border border-solid border-[black] w-9 overflow-hidden">PRIX</th>
                <th class="text-center border border-solid border-[black] w-8 overflow-hidden">ALT</th>
                <th class="text-center border border-solid border-[black] w-8 overflow-hidden">OBS</th>
              </tr>
              
        </thead>
        <tbody>
            {#each $currentSuppliers as row, index}
            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr
                    class="cursor-pointer"
                    class:bg-[cornflowerblue]= {$selectedSupplierIndex === index}
                    class:bg-[lightgray]={$hoveredSupplierIndex === index &&
                    $selectedSupplierIndex !== index}
                    onclick={() => selectedSupplierIndex.set(index)}
                    onmouseover={() => (hoveredSupplierIndex.set(index))}
                    onmouseout={() => (hoveredSupplierIndex.set(null))}
                >
                {#if $isEditing}
                    <EditInstrumentButton instrument={row}/>
                {/if}
                <td
                class="green text-center border border-solid border-[black]"
                onclick= {() => modals.open(addInstrumentToOrderModal, { instrument: row})}>+</td
                >
                <td 
                class="text-center border border-solid border-[black] truncate max-w-[100px] min-w-0 overflow-hidden text-ellipsis whitespace-nowrap"
                title="{row.reference}"
                >
                    {row.reference}
                </td> 
                <td 
                class="text-center border border-solid border-[black] truncate max-w-[100px] min-w-0 overflow-hidden text-ellipsis whitespace-nowrap"
                title="{row.supplier}"
                >
                    {row.supplier}
                </td> 
                <td 
                class="text-center border border-solid border-[black] truncate max-w-[150px] min-w-0 text-ellipsis whitespace-nowrap" title="{row.supplierDescription}"
                >
                    {row.supplierDescription}
                </td>                
                <td class="text-center border border-solid border-[black] overflow-hidden">{row.price}</td>
                <td class="text-center border border-solid border-[black] overflow-hidden">{row.alt ? 'Yes' : 'No'}</td>
                <td class="text-center border border-solid border-[black] overflow-hidden">{row.obsolete ? 'Yes' : 'No'}</td>
                </tr>
            {/each}
        </tbody>
    </table>
    {#if $isEditing}
       {#if $isAdmin}
            <div class="flex justify-center">
                <button class="mt-4 px-4 py-2 rounded bg-yellow-100 text-black hover:bg-gray-500 transition" onclick={()=>openAddInstrumentPage()}>
                    Ajouter un instrument
                </button>
            </div>
        {/if}
    {/if}

    <!-- TABLE OF THE ALTERNATIVES -->
    <table class="w-full border-collapse mt-4">
        <thead>
            <tr class="bg-white text-teal-400">
                <th colspan="4" class="text-center py-2">Alternatives</th>
                <th colspan="2" class="text-center py-2">
                    <button class="bg-blue-500 text-white py-1 px-3 rounded hover:bg-blue-700 focus:outline-none"
                    onclick={()=>seeAllAlternatives()}>
                        Voir plus
                    </button>
                </th>
            </tr>
            <tr class="bg-teal-400">
            {#if $isEditing}
                <th class="text-center border border-solid border-[black]"></th>
            {/if}
            <th class="text-center border border-solid border-[black] w-16">AJOUT</th>
            <th class="text-center border border-solid border-[black] w-24">REF</th>
            <th class="text-center border border-solid border-[black] w-32">MARQUE</th>
            <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
            <th class="text-center border border-solid border-[black] w-16">PRIX</th>
            <th class="text-center border border-solid border-[black] w-16">OBS</th>
            </tr>
        </thead>
        <tbody>
            {#each $alternatives.slice(0,2) as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr
                    class="cursor-pointer {index === 1 ? 'opacity-50' : ''}"
                    class:bg-[cornflowerblue]= {$selectedAlternativeIndex === index}
                    class:bg-[lightgray]={$hoveredAlternativeIndex === index &&
                    $selectedAlternativeIndex !== index}
                    onclick={() => selectedAlternativeIndex.set(index)}
                    ondblclick={() => selectAlternative(row, index)}
                    onmouseover={() => (hoveredAlternativeIndex.set(index))}
                    onmouseout={() => (hoveredAlternativeIndex.set(null))}
                >
                {#if $isEditing}
                    <EditInstrumentButton instrument={row}/>
                {/if}
                <td
                class="green text-center border border-solid border-[black]"
                onclick= {() => modals.open(addInstrumentToOrderModal, { instrument: row})}>+</td
                >
                <td class="text-center border border-solid border-[black]">{row.reference}</td>
                <td class="text-center border border-solid border-[black]">{row.supplier}</td>
                <td class="text-center border border-solid border-[black]">{row.supplierDescription}</td>
                <td class="text-center border border-solid border-[black]">{row.price}</td>
                <td class="text-center border border-solid border-[black]">{row.obsolete}</td>
                </tr>
            {/each}
        </tbody>
    </table>

</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>

<div
  class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4"
  id="big-category-pannel"
>
  <span
    class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5 hover:text-[red] cursor-pointer"
    onclick={(event) => {
      event.stopPropagation();
      closeBigPicture();
    }}>&times;</span
  >
  <img class="h-[300px]" id="big-category" alt="big category" />
</div>

