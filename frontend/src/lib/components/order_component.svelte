<script> 
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { onMount } from "svelte";
    import { preventDefault } from "svelte/legacy";
    import { get } from "svelte/store";
    import { isEditing, orderItems, ordersNames, reload, selectedCategoryIndex, selectedSupplierIndex, quantity, selectedOrderId, errorMessage, findOrdersNamesStore, userId } from "$lib/stores/searches";    
    import { findOrderItems, addInstrument, getOrders } from "./order_component.js";
    import createOrderModal from "$lib/modals/createOrderModal.svelte";
    import { modals } from "svelte-modals";
    import { apiFetch } from "$lib/utils/fetch.js";

    let showOrders = true;

    // Function to toggle the state
    function toggleOrders() {
        showOrders = !showOrders;
    }

    function exportOrderToExcel() {
        //smth to do with the database I think
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
                    >Afficher une commande :
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
                        <option value="create"  class="bg-orange-200 text-black hover:bg-orange-300">Créer une commande</option>
                        <option value="previous_orders"  class="bg-green-200 text-black hover:bg-green-300">Voir commandes précédentes</option>
                    {#each $ordersNames as order}
                        <option value={order.id}>{order.name} </option>
                    {/each}

                </select>
            </div>
            <div class="mr-4">
                <button
                class="border bg-green-600 mt-[10px] p-2.5 rounded-[10px] border-solid border-[none] cursor-pointer"
                on:click={() => exportOrderToExcel()}>Exporter</button
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