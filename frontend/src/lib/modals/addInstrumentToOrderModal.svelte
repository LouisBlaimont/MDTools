<script>
    import { getContext } from "svelte";
    import { toast } from "@zerodevx/svelte-toast";
    import { modals } from "svelte-modals";
    import { orderItems, ordersNames, quantity, selectedOrderId} from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores.js";
    import { addInstrument, findOrderItems} from "../components/order_component.js";

    const {
        isOpen,
        close,
        instrument, 
    } = $props();

    function handleAddInstrument(){
        addInstrument(instrument.id, $userId, $selectedOrderId, $quantity);
        close();
    }
    const isObsolete = instrument.obsolete;
    const isNotObsolete = !isObsolete;

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

    <h2 class="text-lg font-semibold text-gray-900 text-center" id="modal-title">Commander référence {instrument.reference} :</h2>

    {#if isObsolete}
      <h4 class="text-lg font-semibold text-red-500 text-center">L'instrument est obsolète, impossible de le commander.</h4>
    {/if}

    <div class="max-w-4xl mx-auto mt-4" >
        <table class="w-full border-collapse">
            <thead class="bg-teal-400">
                <tr>
                <th class="text-center border border-solid border-[black] w-24">REF</th>
                <th class="text-center border border-solid border-[black] w-32">MARQUE</th>
                <th class="text-center border border-solid border-[black]">DESCRIPTION</th>
                <th class="text-center border border-solid border-[black] w-16">PRIX</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td class="text-center border border-solid border-[black] text-black">{instrument.reference}</td>
                    <td class="text-center border border-solid border-[black] text-black">{instrument.supplier}</td>
                    <td class="text-center border border-solid border-[black] text-black">{instrument.supplierDescription}</td>
                    <td class="text-center border border-solid border-[black] text-black">{instrument.price}</td>
                </tr>
            </tbody>
        </table>
        
        <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
            <div class="flex flex-col items-center space-y-4">
            <div class="flex flex-col md:flex-row items-center w-full space-y-2 md:space-y-0 md:space-x-4 ">

            <div class = "flex items-center space-x-1 w-full md:w-auto">
                <label for="qte" class="text-gray-900">Quantité:</label>
                <input
                type="number"
                id="qte"
                name="qte"
                class="border border-gray-300 rounded p-2 text-gray-900 bg-white w-1/2"
                bind:value={$quantity}
                />
            </div>

            <div class="flex-grow"></div>

            <div class = "flex items-center space-x-1 w-full md:w-auto">
                <label for="order" class="text-gray-900">Commande:</label>
                <select 
                    id="order"
                    name="order"
                    class="border border-gray-300 rounded p-2 text-gray-900 bg-white w-1/2"
                    bind:value={$selectedOrderId}
                >
                    {#if $selectedOrderId === null}
                        <option value="---" disabled selected>---</option>
                        {#each $ordersNames as order}
                            <option value={order.id}>{order.name}</option>  
                        {/each}
                    {:else}
                        {#each $ordersNames as order}
                            {#if order.id === $selectedOrderId}
                                <option value={order.id} selected>{order.name}</option>
                            {:else}
                                <option value={order.id}>{order.name}</option>
                            {/if}  
                        {/each}
                    {/if}
                </select>
            </div>

          </div>
        </div>
      </div>
    </div>

      <div class="text-right space-x-2">
        <button
          type="button"
          class="inline-block w-auto justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50"
          onclick={() => close()}
        >
          Annuler
        </button>
        {#if isNotObsolete}
          <button
            type="button"
            class="inline-block w-auto justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-blue-500"
            onclick={handleAddInstrument}
          >
            Commander
          </button>
        {/if}
      </div>
    </div>
</div>


{/if}