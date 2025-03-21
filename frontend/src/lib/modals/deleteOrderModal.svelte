<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { ordersNames, selectedOrderId, orderItems, orders } from "$lib/stores/searches";
    import { apiFetch } from "$lib/utils/fetch";

    const {
        isOpen,
        close,
        orderName,
        onDelete
    } = $props();


    async function deleteOrder(newOrderName){
        return apiFetch(`/api/orders/${$selectedOrderId}`, {
            method : "DELETE",
        })
        .then((response) => {
            if (!response.ok){
                throw new Error(`Failed to delete order : ${response.status}`);
            }
            return response.json();
        })
        .then((result) => {
            ordersNames.update(currentOrdersNames => currentOrdersNames.filter(order => order.id !== $selectedOrderId));
            orders.update(currentOrders => currentOrders.filter(order => order.orderName !== orderName));
            selectedOrderId.set(null);
            orderItems.set([]);
            onDelete();
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

        <h2 class="text-lg font-semibold text-gray-900 text-center" id="modal-title">Supprimer la commande "{orderName}":</h2>

        <p class="text-red-700 mt-2">
            Attention, cette action est irréversible.
        </p>

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
                class="inline-block w-auto justify-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-red-500"
                onclick={() => deleteOrder()}
            >
                Supprimer
            </button>
        </div>
    </div>
</div>


{/if}