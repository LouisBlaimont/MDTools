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
        hoveredSupplierIndex, alternatives, selectedGroup, selectedSubGroup, hoveredAlternativeIndex} from "$lib/stores/searches";   
    import { modals } from "svelte-modals";
    import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";
    import addCategoryModalFromInstrument from "$lib/modals/addCategoryModalFromInstrument.svelte";
    import addInstrumentModal from "$lib/modals/addInstrumentModal.svelte";
    
    function selectSupplier(index) {
        selectedSupplierIndex.set(index);
    }
    import addInstrumentToOrderModal from "$lib/modals/addInstrumentToOrderModal.svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { apiFetch } from "$lib/utils/fetch";
    import DeleteOrderModal from "$lib/modals/deleteOrderModal.svelte";
    import { selectAlternative, removeAlternative } from "./alternatives.js";
    import Icon from "@iconify/svelte";
    
    $: notEditing = !$isEditing;

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

</script>

<div class="flex-[3] overflow-y-auto box-border m-0 ml-1">
    <!-- PICTURES OF THE INSTRUMENTS -->
    <div class="border bg-teal-400 mb-[5px] font-sans text-base py-0.5 px-2">
        <span class="">Fournisseurs</span>
    </div>
    <div class="flex h-40 max-w-full overflow-x-auto box-border mb-[15px]">
        {#each $currentSuppliers as row, index}
            <!-- svelte-ignore a11y_click_events_have_key_events -->
            <!-- svelte-ignore a11y_no_static_element_interactions -->
            <div
            class="flex shrink-0 flex-col h-[95%] max-w-[150px] text-center box-border border mr-[3px] border-solid border-[black]"
            >
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <!-- svelte-ignore a11y_no_noninteractive_element_interactions -->
                <img
                    alt="supplier{row.id}"
                    src={row.picturesId && row.picturesId[0]
                    ? PUBLIC_API_URL + `/api/pictures/${row.picturesId[0]}`
                    : "/default/no_picture.png"}
                    onclick= {() => modals.open(BigPicturesModal, { instrument: row, index: index })}
                    onmouseover={() => (hoveredSupplierImageIndex.set(index))}
                    onmouseout={() => (hoveredSupplierImageIndex.set(null))}
                    class="h-4/5 {$selectedSupplierIndex === index
                    ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
                    : ''} {$hoveredSupplierImageIndex === index && $selectedSupplierIndex !== index
                    ? 'cursor-pointer border-2 border-solid border-[lightgray]'
                    : ''}"
                />
                <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.reference}</div>
            </div>
        {/each}
    </div>


    <!-- TABLE OF THE SUPPLIERS -->
    <table data-testid = "suppliers-table" class="w-full border-collapse">
        <thead class="bg-teal-400">
            <tr class="bg-white text-teal-400">
                <th colspan="2"></th>
                <th colspan="3" class="text-center py-2">Instruments</th>
            </tr>
            <tr class="bg-teal-400">
                {#if $isEditing}
                  <th class="text-center border border-solid border-[black] w-10"></th>
                {/if}
                {#if notEditing}
                    <th class="text-center border border-solid border-[black] w-5 overflow-hidden">
                    <div class="flex justify-center items-center">
                        <Icon icon="material-symbols:shopping-basket-outline" width="24" height="24" />
                    </div>
                    </th>
                {/if}
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
                    class="cursor-pointer {row.obsolete ? 'bg-red-500' : ''}" 
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
                {#if !$isEditing}
                    <td
                    class="text-center border border-solid border-[black]"
                    onclick= {() => modals.open(addInstrumentToOrderModal, { instrument: row})}>+</td
                    >
                {/if}
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
                <button
                    class="mt-4 px-4 py-2 rounded bg-yellow-100 text-black hover:bg-gray-500 transition"
                    onclick={() => {
                    const selectedCategory = $selectedCategoryIndex != null ? $categories[$selectedCategoryIndex] : null;
                    modals.open(addInstrumentModal, { initInstrument: null, initCategory: selectedCategory });
                    }}
                >
                     Ajouter un instrument
                </button>
            </div>
        {/if}
    {/if}

    <!-- TABLE OF THE ALTERNATIVES -->
    <table class="table-fixed w-full border-collapse mt-4">
        <thead>
            <tr class="bg-white text-teal-400">
                <th colspan="2" class="text-center py-2">
                    <button class="bg-blue-500 text-white py-1 px-3 rounded hover:bg-blue-700 focus:outline-none"
                    onclick={()=>seeAllAlternatives()}>
                        Voir plus
                    </button>
                </th>
                <th colspan="3" class="text-center py-2">Alternatives</th>
            </tr>
            <tr class="bg-teal-400">
            {#if $isEditing}
                <th class="text-center border border-solid border-[black]"></th>
            {/if}
            {#if notEditing}
            <th class="border border-solid border-black">
                <div class="flex justify-center items-center">
                    <Icon icon="material-symbols:shopping-basket-outline" width="24" height="24" />
                </div>
            </th>            
            {/if}
            <th class="text-center border border-solid border-[black]">REF</th>
            <th class="text-center border border-solid border-[black]">MARQUE</th>
            <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
            <th class="text-center border border-solid border-[black]">PRIX</th>
            </tr>
        </thead>
        <tbody>
            {#each $alternatives.slice(0,2) as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr
                    class="cursor-pointer {index === 1 ? 'opacity-50' : ''} {row.obsolete ? 'bg-red-500' : ''}"
                    class:bg-[lightgray]={$hoveredAlternativeIndex === index}
                    ondblclick={() => selectAlternative(row, index)}
                    onmouseover={() => (hoveredAlternativeIndex.set(index))}
                    onmouseout={() => (hoveredAlternativeIndex.set(null))}
                >
                {#if $isEditing}
                    <td 
                    class="text-center border border-solid border-[black]"
                    onclick={() => removeAlternative(row.id)}
                    >
                    <span class="{row.obsolete ? 'text-white' : 'text-red-500'}">&times;</span>
                    </td>
                {/if}
                {#if notEditing}
                    <td
                    class="text-center border border-solid border-[black]"
                    onclick= {() => modals.open(addInstrumentToOrderModal, { instrument: row})}>+</td
                    >
                {/if}
                <td class="text-center border border-solid border-[black]" onclick= {() => modals.open(BigPicturesModal, { initInstrument: row})} >{row.reference}</td>
                <td class="text-center border border-solid border-[black]" onclick= {() => modals.open(BigPicturesModal, { initInstrument: row})}>{row.supplier}</td>
                <td class="text-center border border-solid border-[black]" onclick= {() => modals.open(BigPicturesModal, { initInstrument: row})}>{row.supplierDescription}</td>
                <td class="text-center border border-solid border-[black]" onclick= {() => modals.open(BigPicturesModal, { initInstrument: row})}>{row.price}</td>
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

