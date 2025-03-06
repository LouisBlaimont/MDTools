<script> 
    import { tools } from "../../tools.js";
    import { suppliers } from "../../suppliers.js";
    import { getOrder, addTool } from "../../order.js";
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { PUBLIC_API_URL } from "$env/static/public";
    import { isEditing, order, reload, selectedCategoryIndex, selectedSupplierIndex, quantity } from "$lib/stores/searches";    
    import {startResize, resize, stopResize} from "$lib/resizableUtils.js";
    
    let resizing = null;
    let startX, startY, startWidth, startHeight;
    let div4;

    function exportOrderToExcel() {
        //smth to do with the database I think
    }

    console.log($order);

</script>


<div class="flex-[1] flex flex-col gap-[15px] h-full pl-3 bg-gray-50">
    <!-- SEARCH BY ORDER AND EXPORT ORDERS -->
    <div class="flex flex-row justify-between">
        <div class="w-1/2 mr-0">
            <form class="mt-2.5">
            <label for="order-search" id="order-search-label" class="w-2/5"
                >Rechercher une commande :
            </label>
            <input
                type="text"
                class="w-1/3 border border-gray-400 rounded p-0.5 border-solid border-[black]"
                list="commandes"
                id="order-search"
                name="order-search"
                placeholder="Entrez un numéro de commande"
            />
            <datalist id="commandes">
                <option value="#123456"> </option><option value="#123457"> </option><option
                value="#123458"
                >
                </option><option value="#123459"> </option></datalist
            >
            </form>
        </div>
        <div class="mr-4">
            <button
            class="border bg-green-600 mt-[10px] p-2.5 rounded-[10px] border-solid border-[none] cursor-pointer"
            on:click={() => exportOrderToExcel()}>Exporter</button
            >
        </div>
    </div>

    <!-- TABLE OF ORDERS -->
    <div class="w-[80%] mb-[50px]">
        <table class="w-full border-collapse">
            <thead class="bg-teal-400">
            <tr>
                <th class="text-center border border-solid border-[black]">REF</th>
                <th class="text-center border border-solid border-[black]">MARQUE</th>
                <th class="text-center border border-solid border-[black]">GROUPE</th>
                <th class="text-center border border-solid border-[black]">FONCTION</th>
                <th class="text-center border border-solid border-[black]">NOM</th>
                <th class="text-center border border-solid border-[black]">FORME</th>
                <th class="text-center border border-solid border-[black]">DIMENSION</th>
                <th class="text-center border border-solid border-[black]">QTE</th>
                <th class="text-center border border-solid border-[black]">PU HTVA</th>
                <th class="text-center border border-solid border-[black]">TOTAL HTVA</th>
            </tr>
            </thead>
            <tbody>
            {#each $order as row, index}
                <tr>
                <td class="text-center border border-solid border-[black]">{row.ref}</td>
                <td class="text-center border border-solid border-[black]">{row.brand}</td>
                <td class="text-center border border-solid border-[black]">{row.group}</td>
                <td class="text-center border border-solid border-[black]">{row.fct}</td>
                <td class="text-center border border-solid border-[black]">{row.name}</td>
                <td class="text-center border border-solid border-[black]">{row.form}</td>
                <td class="text-center border border-solid border-[black]">{row.dim}</td>
                <td class="text-center border border-solid border-[black]">{row.qte}</td>
                <td class="text-center border border-solid border-[black]">{row.pu_htva}</td>
                <td class="text-center border border-solid border-[black]">{row.total_htva}</td>
                <td
                    ><button
                    class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 border-[none] cursor-pointer"
                    >+</button
                    ></td
                >
                <td
                    ><button
                    class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px] border-[none] cursor-pointer"
                    >-</button
                    ></td
                >
                <td
                    ><button
                    class="text-gray-900 rounded text-sm bg-red-600 w-[20px] h-[20px] border-[none] cursor-pointer"
                    >&times;</button
                    ></td
                >
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
   
</div>