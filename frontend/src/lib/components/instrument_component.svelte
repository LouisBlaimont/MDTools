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

    

    function selectSupplier(index) {
        selectedSupplierIndex = index;
    }

    function showBigPicture(img) {
        const pannel = document.getElementById("big-category-pannel");
        const overlay = document.getElementById("overlay");
        const picture = document.getElementById("big-category");
        pannel.style.display = "flex";
        overlay.style.display = "block";
        picture.src = img;
    }

    function addToOrderPannel(ref) {
        const pannel = document.getElementById("add-order-pannel");
        const overlay = document.getElementById("overlay");
        toolToAddRef = ref;
        pannel.style.display = "flex";
        overlay.style.display = "block";
    }
    
    function openAddInstrumentPage() {
        goto('/admin/add_instrument');
    }  


</script>

<div class="flex-[3] overflow-y-auto box-border m-0 ml-1">
<!-- PCITURES OF THE SUPPLIERS -->
    <div class = "resizable" bind:this={div4}>
        <div class="border bg-teal-400 mb-[5px] border-solid border-[black]">
            <span class="p-1">Photos fournisseurs</span>
        </div>
        <div class="flex h-40 max-w-full overflow-x-auto box-border mb-[15px]">
            {#each currentSuppliers as row, index}
                <div
                class="flex shrink-0 flex-col h-[95%] text-center box-border border mr-[3px] border-solid border-[black]"
                on:click={() => showBigPicture(row.src)}
                >
                <img
                    alt="supplier{row.id}"
                    src={row.src}
                    on:click={() => showBigPicture(row.src)}
                    on:mouseover={() => (hoveredSupplierImageIndex = index)}
                    on:mouseout={() => (hoveredSupplierImageIndex = null)}
                    class="h-4/5 {selectedSupplierIndex === index
                    ? 'cursor-pointer border-2 border-solid border-[cornflowerblue]'
                    : ''} {hoveredSupplierImageIndex === index && selectedSupplierIndex !== index
                    ? 'cursor-pointer border-2 border-solid border-[lightgray]'
                    : ''}"
                />
                <div class="box-border p-[3px] border-t-[black] border-t border-solid">{row.ref}</div>
                </div>
                {#if $isEditing}
                    {#if isAdmin}
                        <button class="absolute bottom-2 right-6 w-5 h-5 bg-yellow-400 text-black text-lg rounded-full flex items-center justify-center transition-colors duration-300 hover:bg-black hover:text-yellow-500 cursor-pointer">
                            +
                        </button> 
                    {/if}
                {/if}
            {/each}
        </div>
        <div class="resize-handle" on:mousedown={(e) => startResize(e, div4)}></div>
    </div>

<!-- TABLE OF THE SUPPLIERS -->
    <div class="suppliers-table resizable" bind:this={div5}>
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
                {#each currentSuppliers as row, index}
                <!-- svelte-ignore a11y_mouse_events_have_key_events -->
                    <tr
                        class="cursor-pointer"
                        class:bg-[cornflowerblue]={selectedSupplierIndex === index}
                        class:bg-[lightgray]={hoveredSupplierIndex === index &&
                        selectedSupplierIndex !== index}
                        on:click={() => selectSupplier(index)}
                        on:mouseover={() => (hoveredSupplierIndex = index)}
                        on:mouseout={() => (hoveredSupplierIndex = null)}
                    >
                    {#if $isEditing}
                    <EditCategoryButton category={row}/>
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
            {#if isAdmin}
                <div class="flex justify-center">
                <button class="mt-4 px-4 py-2 rounded bg-yellow-100 text-black hover:bg-gray-500 transition" on:click={()=>openAddInstrumentPage()}>
                    Add an instrument
                </button>
                </div>
            {/if}
        {/if}
        <div class="resize-handle" on:mousedown={(e) => startResize(e, div5)}></div>
    </div>
</div>