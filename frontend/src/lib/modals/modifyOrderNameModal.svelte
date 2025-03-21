<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { ordersNames, userId, selectedOrderId, orderItems, orders } from "$lib/stores/searches";
    import { apiFetch } from "$lib/utils/fetch";

    const {
        isOpen,
        close,
        orderName,
        updateOrderName
    } = $props();

    function handleModifyOrder(){
        const newOrderName = document.getElementById("new-order-name").value.trim();
        const errorNoName = document.getElementById("error-no-name");
        if(newOrderName === ''){
            errorNoName.classList.remove('hidden');
        }else{
            errorNoName.classList.add('hidden');
            modifyOrder(newOrderName);
            
        }
    }

    async function modifyOrder(newOrderName){
        console.log($selectedOrderId);
        const data = {
            orderName : newOrderName,
        };
        return apiFetch(`/api/orders/${$selectedOrderId}`, {
            method : "PATCH",
            headers : { "Content-type" : "application/json"},
            body : JSON.stringify(data),
        })
        .then((response) => {
            if (response.status === 400){
                const errorBadRequest = document.getElementById("error-no-order-for-user");
                errorBadRequest.classList.remove("hidden");
                throw new Error(`Failed to create new order : ${response.status}`);
            }
            else if (response.status === 404){
                throw new Error(`Failed to create new order : ${response.status}`);
            }
            return response.json();
        })
        .then((result) => {
            const errorBadRequest = document.getElementById("error-no-order-for-user");
            errorBadRequest.classList.add("hidden");
            orderItems.set(result);
            ordersNames.update(currentOrdersNames => {
                return currentOrdersNames.map(order =>
                    order.id === $selectedOrderId ? { ...order, name: newOrderName } : order
                );
            });
            orders.update(currentOrders => {
                return currentOrders.map(order =>
                    order.orderName === orderName ? { ...order, orderName: newOrderName, orderItems: result } : order
                );
            });
            updateOrderName(newOrderName);
            close();
        })
        .catch((error) => {
            console.log("Error :", error);
        });
    }

</script>

{#if isOpen}
<div
  class="fixed inset-0 z-10 box-border bg-[rgba(0,0,0,0.8)] flex justify-center items-center p-[50px] rounded-[30px] text-white flex-col gap-[15px]"
  id="add-order-pannel"
  aria-labelledby="modal-title"
  role="dialog"
  aria-modal="true"
>
  <div class="absolute inset-0 bg-gray-500/75 transition-opacity" aria-hidden="true"></div>

    <div class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:w-full sm:max-w-lg lg:max-w-4xl p-6">

        <h2 class="text-lg font-semibold text-gray-900 text-center" id="modal-title">Modifier le nom de la commande "{orderName}":</h2>

        <div class="mt-4">
            <label for="new-order-name" class="block text-sm font-medium text-gray-700">Nouveau nom de la commande :</label>
            <input
                type="text"
                id="new-order-name"
                class="mt-1 mb-2 block w-full border border-gray-300 rounded-md p-2 text-gray-900"
            />
            <span id="error-no-name" class="mb-5 text-red-600 text-sm hidden">Entrez un nom de commande.</span>
            <span id="error-no-order-for-user" class="mb-5 text-red-600 text-sm hidden">Ce nom de commande existe déjà.</span>
        </div>

        <div class="text-right space-x-2">
            <button
                type="button"
                class="inline-block w-auto justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50"
                onclick={() => {close();}}
            >
                Annuler
            </button>

            <button
                type="button"
                class="inline-block w-auto justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500"
                onclick={() => handleModifyOrder()}
            >
                Modifier
            </button>
        </div>
    </div>
</div>


{/if}