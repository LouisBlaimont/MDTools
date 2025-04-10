<script> 
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { isEditing, orderItems, ordersNames, reload, selectedCategoryIndex, selectedSupplierIndex, quantity, selectedOrderId, errorMessage, findOrdersNamesStore } from "$lib/stores/searches";  
    import { userId } from "$lib/stores/user_stores.js";  
    import { findOrderItems, addInstrument, getOrders } from "./order_component.js";
    import createOrderModal from "$lib/modals/createOrderModal.svelte";
    import { modals } from "svelte-modals";
    import { apiFetch } from "$lib/utils/fetch.js";
    import { _ } from "svelte-i18n";

    let showOrders = true;

    // Function to toggle the state
    function toggleOrders() {
        showOrders = !showOrders;
    }
    import ExcelJS from "exceljs";
    import FileSaver from "file-saver";

    /**
     * Export the current order items to a styled Excel file.
     */
    async function exportOrderToExcel() {
        const items = get(orderItems);
        const orderId = get(selectedOrderId);
        const orderName = get(ordersNames).find(o => o.id === orderId)?.name;

        if (!items?.length || !orderName) {
            alert("Missing order data");
            return;
        }

        const workbook = new ExcelJS.Workbook();
        const sheet = workbook.addWorksheet("Commande");

        sheet.columns = [
            { header: "Référence", key: "reference", width: 20 },
            { header: "Fournisseur", key: "supplier", width: 20 },
            { header: "Description", key: "description", width: 50 },
            { header: "Quantité", key: "quantity", width: 10 },
            { header: "Prix HTVA", key: "price", width: 15 },
            { header: "Total HTVA", key: "totalHTVA", width: 15 },
            { header: "Montant TVAC", key: "totalTVAC", width: 15 }
        ];

        let totalHTVA = 0;

        for (const item of items) {
            const total = item.totalPrice || 0;
            totalHTVA += total;

            const parts = [
                item.category.groupName,
                item.category.subGroupName,
                item.category.function,
                item.category.name,
                item.category.shape,
                item.category.lenAbrv ? `${item.category.lenAbrv} mm` : null
            ].filter(Boolean);

            const description = parts.join(" ");

            sheet.addRow({
                reference: item.reference,
                supplier: item.supplier,
                description,
                quantity: item.quantity,
                price: item.price,
                totalHTVA: total.toFixed(2),
                totalTVAC: (total * 1.21).toFixed(2)
            });
        }

        const totalRow = sheet.addRow({
            description: "Total",
            totalHTVA: totalHTVA.toFixed(2),
            totalTVAC: (totalHTVA * 1.21).toFixed(2)
        });

        const lastRow = sheet.rowCount;
        const maxCol = sheet.columns.length;

        sheet.eachRow((row, rowNumber) => {
            row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
                if (colNumber > maxCol) return;

                const isHeader = rowNumber === 1;
                const isTotal = rowNumber === lastRow;
                const isTop = isHeader;
                const isBottom = isTotal;
                const isLeft = colNumber === 1;
                const isRight = colNumber === maxCol;

                cell.alignment = {
                    vertical: "middle",
                    horizontal: "center",
                    wrapText: true
                };

                cell.font = { bold: isHeader || isTotal };

                if (isHeader) {
                    cell.font = { bold: true };
                    cell.fill = {
                        type: "pattern",
                        pattern: "solid",
                        fgColor: { argb: "FFD9E1F2" }
                    };
                }

                if (isTotal) {
                    cell.font = { bold: true };
                    cell.fill = {
                        type: "pattern",
                        pattern: "solid",
                        fgColor: { argb: "FFFCE4D6" }
                    };
                }

                cell.border = {
                    top: { style: isTop ? "medium" : "thin" },
                    bottom: { style: isBottom ? "medium" : "thin" },
                    left: { style: isLeft ? "medium" : "thin" },
                    right: { style: isRight ? "medium" : "thin" }
                };
            });
        });

        const buffer = await workbook.xlsx.writeBuffer();
        FileSaver.saveAs(new Blob([buffer]), `commande_${orderName}.xlsx`);
    }


    export async function findOrdersNames(){
        try{
            const response = await apiFetch(`/api/orders/user/${$userId}`);
            if (!response.ok){
                throw new Error(`Failed to fetch orders: ${response.statusText}`); 
            }
            ordersNames.set(await response.json());
        }catch (error){
            console.error(error);
            errorMessage.set(error.message);
        }
        return;
    }

    findOrdersNamesStore.set(findOrdersNames);

    async function removeInstrument(orderId, userId, instrId){
        try{
            const response = await apiFetch(`/api/orders/${orderId}/user/${userId}/remove-instrument/${instrId}`, {
                method : 'DELETE'
            });
            if (!response.ok){
                throw new Error(`Failed to remove instrument : ${response.statusText}`); 
            }
            orderItems.set(await response.json());
        }catch (error){
            console.error(error);
            errorMessage.set(error.message);
        }
        return;     
    }

    onMount(async () => {
        await getOrders();
    });

    function seePreviousOrders(){
    goto("/previous_orders");
    }

</script>


<div class="flex-[1] flex flex-col gap-[15px] h-full pl-3 bg-gray-50">
    <!-- SEARCH BY ORDER AND EXPORT ORDERS -->
    {#if !showOrders}
        <div class="flex justify-end">
            <button
            on:click={toggleOrders}
            class="border bg-gray-300 mt-[5px] p-1 rounded-[10px] border-solid border-[none] cursor-pointer"
            >
            {showOrders ? '⬆️' : '⬇️'}
            </button>
        </div>
    {/if}
    
    {#if showOrders}
        <div class="flex flex-row justify-between">
            <div class="w-1/2 mr-0">
                <label for="order-search" id="order-search-label" class="w-2/5"
                    >{$_('orders_component.show_orders')}
                </label>
                <select id="commandes"
                class="w-1/3 border border-gray-400 rounded p-0.5 border-solid border-[black]"
                bind:value={$selectedOrderId}
                on:change={(e) => {
                    if($selectedOrderId === "create"){
                        $selectedOrderId = null;
                        modals.open(createOrderModal);
                    }
                    else if($selectedOrderId === "previous_orders"){
                        $selectedOrderId = null;
                        seePreviousOrders();
                    }
                    else{
                        findOrderItems($selectedOrderId);
                    }
                }}
                >
                        <option value="create"  class="bg-orange-200 text-black hover:bg-orange-300">{$_('orders_component.create_order')}</option>
                        <option value="previous_orders"  class="bg-green-200 text-black hover:bg-green-300">{$_('orders_component.see_previous_orders')}</option>
                    {#each $ordersNames as order}
                        <option value={order.id}>{order.name} </option>
                    {/each}

                </select>
            </div>
            <div class="mr-4">
                <button
                class="border bg-green-600 mt-[10px] p-2.5 rounded-[10px] border-solid border-[none] cursor-pointer"
                on:click={() => exportOrderToExcel()}>{$_('orders_component.export')}</button
                >
            </div>
            <div>
                <button class="border bg-gray-300 mt-[5px] p-1 rounded-[10px] border-solid border-[none] cursor-pointer" on:click={toggleOrders}>
                    {showOrders ? '⬆️' : '⬇️'}
                </button>
            </div>
        </div>

    <!-- TABLE OF ORDERS -->
        <div class="w-[80%] mb-[50px]">
            <table class="w-full border-collapse">
                <thead class="bg-teal-400">
                <tr>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.reference')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.brand')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.group')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.function')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.name')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.shape')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.dimension')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.quantity')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.unite_price_excl_vat')}</th>
                    <th class="text-center border border-solid border-[black]">{$_('orders_component.table.total_price_excl_vat')}</th>
                </tr>
                </thead>
                <tbody>
                {#each $orderItems as item, index}
                    <tr>
                    <td class="text-center border border-solid border-[black]">{item.reference}</td>
                    <td class="text-center border border-solid border-[black]">{item.supplier}</td>
                    <td class="text-center border border-solid border-[black]">{item.category.groupName}</td>
                    <td class="text-center border border-solid border-[black]">{item.category.function}</td>
                    <td class="text-center border border-solid border-[black]">{item.category.name}</td>
                    <td class="text-center border border-solid border-[black]">{item.category.shape}</td>
                    <td class="text-center border border-solid border-[black]">{item.category.lenAbrv}</td>
                    <td class="text-center border border-solid border-[black]">{item.quantity}</td>
                    <td class="text-center border border-solid border-[black]">{item.price}</td>
                    <td class="text-center border border-solid border-[black]">{item.totalPrice}</td>
                    <td>
                        <div class = "flex justify-start gap-1">
                            <button
                            class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 border-[none] cursor-pointer"
                            on:click={() => addInstrument(item.instrumentId, item.userId, item.orderId, 1)}
                            >
                            +
                            </button>

                            <button
                            class="text-gray-900 rounded text-sm bg-gray-400 w-[20px] h-[20px] ml-0.5 border-[none] cursor-pointer"
                            on:click={() => addInstrument(item.instrumentId, item.userId, item.orderId, -1)}
                            >
                            -
                            </button>

                            <button
                            class="text-gray-900 rounded text-sm bg-red-600 w-[20px] h-[20px] border-[none] cursor-pointer"
                            on:click={() => removeInstrument(item.orderId, item.userId, item.instrumentId)}
                            >
                            &times;
                            </button>
                        </div>
                    </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    {/if}
   
</div>