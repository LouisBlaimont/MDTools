<script> 
    import { tools } from "../../tools.js";
    import { suppliers } from "../../suppliers.js";
    import { getOrder } from "../../order.js"; 
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { PUBLIC_API_URL } from "$env/static/public";
    import EditInstrumentButton from "../../routes/searches/EditInstrumentButton.svelte";    
    import { isEditing, order, reload, category_to_addInstrument, categories, selectedCategoryIndex, selectedSupplierIndex, quantity, currentSuppliers, hoveredSupplierImageIndex, hoveredSupplierIndex, toolToAddRef, isAdmin
     } from "$lib/stores/searches";    
    import {startResize, resize, stopResize} from "$lib/resizableUtils.js";
    import { modals } from "svelte-modals";
    import BigPicturesModal from "$lib/modals/BigPicturesModal.svelte";
    
    isAdmin.set(true);

    function selectSupplier(index) {
        selectedSupplierIndex.set(index);
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

    function addToOrderPannel(ref) {
        const pannel = document.getElementById("add-order-pannel");
        const overlay = document.getElementById("overlay");
        toolToAddRef.set(ref);
        pannel.style.display = "flex";
        overlay.style.display = "block";
    }
  
    function closeAddToOrder() {
        const pannel = document.getElementById("add-order-pannel");
        const overlay = document.getElementById("overlay");
        pannel.style.display = "none";
        overlay.style.display = "none";
    }

    function addToOrder() {
        const tool_ref = suppliers[$selectedCategoryIndex][$selectedSupplierIndex].ref;
        const tool_brand = suppliers[$selectedCategoryIndex][$selectedSupplierIndex].brand;
        const tool_group = tools[$selectedCategoryIndex].group;
        const tool_fct = tools[$selectedCategoryIndex].fct;
        const tool_name = tools[$selectedCategoryIndex].name;
        const tool_form = tools[$selectedCategoryIndex].form;
        const tool_dim = tools[$selectedCategoryIndex].dim;
        const tool_qte = Number($quantity);
        const tool_pu_htva = suppliers[$selectedCategoryIndex][$selectedSupplierIndex].price;

        order.update(currentOrder => {
            return addTool(currentOrder, tool_ref, tool_brand, tool_group, tool_fct, tool_name, tool_form, tool_dim, tool_qte, tool_pu_htva);
        });

        closeAddToOrder();
    }

    function addTool(currentOrder, tool_ref, tool_brand, tool_group, tool_fct, tool_name, tool_form, tool_dim, tool_qte, tool_pu_htva) {
        const newTool = {
            id: currentOrder.length + 1, 
            ref: tool_ref, 
            brand: tool_brand, 
            group: tool_group,
            fct: tool_fct, 
            name: tool_name, 
            form: tool_form, 
            dim: tool_dim, 
            qte: tool_qte || 1, 
            pu_htva: tool_pu_htva, 
            total_htva: 3, // You may need to compute this based on qte and pu_htva
        }; 
        return [...currentOrder, newTool]; // Return a new array with the new tool appended
    }

    /**
     * Opens the add instrument page and set the category to the selected category or null if no category is selected
     * @param {number} index
     * @returns {void}
     */
    function openAddInstrumentPage() {
        isAdmin.set(true);
        let id = $categories[$selectedCategoryIndex].id;
        if (id){
            category_to_addInstrument.set(id);
        }
        goto("../../admin/add_instrument");
    }

</script>
<!-- <div
    class="flex shrink-0 flex-col h-[95%] text-center box-border border mr-[3px] border-solid border-[black]"
    role="button"
    tabindex="0"
    on:click={() => showBigPicture(row.src ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}` : "/default/no_picture.png")}
    on:keydown={(e) => e.key === 'Enter' && showBigPicture(row.src ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}` : "/default/no_picture.png")}
>
    <img
        alt="supplier{row.id}"
        src={row.src ? PUBLIC_API_URL + `/api/pictures/${row.pictureId}` : "/default/no_picture.png"}
        on:click={() => modals.open(BigPicturesModal, { initInstrument: row })}
        on:keydown={(e) => e.key === 'Enter' && modals.open(BigPicturesModal, { initInstrument: row })}
        on:mouseover={() => (hoveredSupplierImageIndex.set(index))}
        on:focus={() => (hoveredSupplierImageIndex.set(index))}
        on:mouseout={() => (hoveredSupplierImageIndex.set(null))}
        on:blur={() => (hoveredSupplierImageIndex.set(null))}
        class="h-4/5 {$selectedSupplierIndex === index ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]' : ''} {$hoveredSupplierImageIndex === index && $selectedSupplierIndex !== index ? 'cursor-pointer border-2 border-solid border-[lightgray]' : ''}"
    />
    <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.ref}</div>
</div>
{#if $isEditing}
    {#if $isAdmin}
        <button type="button" class="absolute bottom-2 right-6 w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
            +
        </button>
    {/if}
{/if} -->
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
            on:click={() => showBigPicture(row.src? PUBLIC_API_URL + `/api/pictures/${row.pictureId}`
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
                    on:click= {() => modals.open(BigPicturesModal, { initInstrument: row})}
                    on:mouseover={() => (hoveredSupplierImageIndex.set(index))}
                    on:mouseout={() => (hoveredSupplierImageIndex.set(null))}
                    class="h-4/5 {$selectedSupplierIndex === index
                    ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
                    : ''} {$hoveredSupplierImageIndex === index && $selectedSupplierIndex !== index
                    ? 'cursor-pointer border-2 border-solid border-[lightgray]'
                    : ''}"
                />
                <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.ref}</div>
            </div>
            {#if $isEditing}
                {#if $isAdmin}
                    <button class="absolute bottom-2 right-6 w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                        +
                    </button> 
                {/if}
            {/if}
        {/each}
    </div>


    <!-- TABLE OF THE SUPPLIERS -->
    <table data-testid = "suppliers-table" class="w-full border-collapse">
        <thead class="bg-teal-400">
            <tr>
            {#if $isEditing}
                <th class="text-center border border-solid border-[black]"></th>
            {/if}
            <th class="text-center border border-solid border-[black] w-16">AJOUT</th>
            <th class="text-center border border-solid border-[black] w-24">REF</th>
            <th class="text-center border border-solid border-[black] w-32">MARQUE</th>
            <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
            <th class="text-center border border-solid border-[black] w-16">PRIX</th>
            <th class="text-center border border-solid border-[black] w-16">ALT</th>
            <th class="text-center border border-solid border-[black] w-16">OBS</th>
            </tr>
        </thead>
        <tbody>
            {#each $currentSuppliers as row, index}
            <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                <tr
                    class="cursor-pointer"
                    class:bg-[cornflowerblue]={$selectedSupplierIndex === index}
                    class:bg-[lightgray]={$hoveredSupplierIndex === index &&
                    $selectedSupplierIndex !== index}
                    on:click={() => selectedSupplierIndex.set(index)}
                    on:mouseover={() => (hoveredSupplierIndex.set(index))}
                    on:mouseout={() => (hoveredSupplierIndex.set(null))}
                >
                {#if $isEditing}
                    <EditInstrumentButton instrument={row}/>
                {/if}
                <td
                class="green text-center border border-solid border-[black]"
                on:click={() => addToOrderPannel(row.ref)}>+</td
                >
                <td class="text-center border border-solid border-[black]">{row.reference}</td>
                <td class="text-center border border-solid border-[black]">{row.supplier}</td>
                <td class="text-center border border-solid border-[black]">{row.supplierDescription}</td>
                <td class="text-center border border-solid border-[black]">{row.price}</td>
                <td class="text-center border border-solid border-[black]">{row.alt}</td>
                <td class="text-center border border-solid border-[black]">{row.obsolete}</td>
                </tr>
            {/each}
        </tbody>
    </table>
    {#if $isEditing}
       {#if $isAdmin}
            <div class="flex justify-center">
                <button class="mt-4 px-4 py-2 rounded bg-yellow-100 text-black hover:bg-gray-500 transition" on:click={()=>openAddInstrumentPage()}>
                    Ajouter un instrument
                </button>
            </div>
        {/if}
    {/if}
</div>

<div class="hidden fixed w-full h-full bg-[rgba(0,0,0,0)] left-0 top-0" id="overlay"></div>


<div
  class="hidden fixed box-border bg-[rgba(0,0,0,0.8)] justify-center items-center -translate-x-2/4 -translate-y-2/4 p-[50px] rounded-[30px] left-2/4 top-2/4 text-[white] flex-col gap-[15px]"
  id="add-order-pannel"
>
  <!-- svelte-ignore a11y_click_events_have_key_events -->
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <span
    class="absolute text-[white] text-[40px] cursor-pointer transition-[color] duration-[0.3s] right-[15px] top-2.5"
    on:click={(event) => {
      event.stopPropagation();
      closeAddToOrder();
    }}>&times;</span
  >
  <div>AJOUTER référence à la commande:</div>
  <div>
    <label for="qte" class="w-2/5">QUANTITE:</label>
    <input
      type="number"
      id="qte"
      name="qte"
      class="border border-black rounded p-2 text-black bg-white"
      bind:value={$quantity}
    />
    <button class="cursor-pointer" pointer on:click={() => addToOrder()}>AJOUT</button>
  </div>
</div>


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