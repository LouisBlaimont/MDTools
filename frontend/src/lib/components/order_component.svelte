<script> 
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { get } from "svelte/store";
    import { isEditing, orderItems, ordersNames, reload, selectedCategoryIndex, selectedSupplierIndex, selectedOrderId, errorMessage, findOrdersNamesStore } from "$lib/stores/searches";  
    import { findOrderItems, addInstrument, getOrders, exportOrderToExcel, findOrdersNames } from "./order_component.js";
    import createOrderModal from "$lib/modals/createOrderModal.svelte";
    import { modals } from "svelte-modals";
    import { apiFetch } from "$lib/utils/fetch.js";
    import { _ } from "svelte-i18n";

    let showOrders = true;

    // Function to toggle the state
    function toggleOrders() {
        showOrders = !showOrders;
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


<!-- SEARCH BY ORDER AND EXPORT ORDERS -->
<div class="flex-[1] flex flex-col gap-[15px] h-full pl-3 bg-gray-50">
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
                class="w-1/4 ml-2 p-1 bg-transparent text-slate-700 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-teal-500 focus:border-transparent"
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
                        {#if !order.isExported}
                            <option value={order.id}>{order.name} </option>
                        {/if}
                    {/each}

                </select>
            </div>
            <div class="mr-4">
                <button
                class="border bg-green-600 mt-[10px] p-2.5 rounded-[10px] border-solid border-[none] cursor-pointer"
                on:click={() => exportOrderToExcel()}>{$_('orders_component.export.button')}</button
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