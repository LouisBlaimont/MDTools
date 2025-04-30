<script>
  import { onMount } from "svelte";
  import { getContext } from "svelte";
  import { toast } from "@zerodevx/svelte-toast";
  import { modals } from "svelte-modals";
  import { orderItems, ordersNames, selectedOrderId} from "$lib/stores/searches";
  import { userId } from "$lib/stores/user_stores.js";
  import { addInstrument, findOrderItems} from "../components/order_component.js";
  import { _ } from "svelte-i18n";

  const {
      isOpen,
      close,
      instrument, 
  } = $props();

  let quantity = $state(1);
  const isObsolete = instrument.obsolete;
  const isNotObsolete = !isObsolete;

  // Add dragging functionality
  let posX = $state(0);
  let posY = $state(0);
  let offsetX = 0;
  let offsetY = 0;
  let isDragging = false;

  function startDrag(event) {
      isDragging = true;
      offsetX = event.clientX - posX;
      offsetY = event.clientY - posY;
  }

  function drag(event) {
      if (isDragging) {
          posX = event.clientX - offsetX;
          posY = event.clientY - offsetY;
      }
  }

  function stopDrag() {
      isDragging = false;
  }

  function handleAddInstrument() {
      addInstrument(instrument.id, $userId, $selectedOrderId, quantity);
      close();
  }
</script>

{#if isOpen}
<div
  class="relative z-10"
  aria-labelledby="modal-title"
  role="dialog"
  aria-modal="true"
>
  <!-- svelte-ignore a11y_no_static_element_interactions -->
  <div
      class="fixed inset-0 z-10 flex items-center justify-center"
      on:mousemove={drag}
      on:mouseup={stopDrag}
  >
      
      <div 
          class="bg-white rounded-lg shadow-lg w-2/3 max-h-[80vh] overflow-y-auto relative"
          style="transform: translate({posX}px, {posY}px);"
      >
          <div
              class="p-4 border-b cursor-move bg-gray-200 flex items-center justify-between rounded-t-lg select-none"
              on:mousedown={startDrag}
          >
              <h2 class="text-2xl font-bold text-teal-500 text-center" id="modal-title">
                  {$_('modals.add_to_order.order')} {instrument.reference}
              </h2>
              <!-- Cart/Order Icon -->
              <svg 
                  xmlns="http://www.w3.org/2000/svg" 
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke-width="1.5" 
                  stroke="currentColor" 
                  class="w-6 h-6 text-teal-500"
              >
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
              </svg>
          </div>

          <div class="bg-gray-100 p-6 rounded-b-lg">
              {#if isObsolete}
                  <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4" role="alert">
                      <strong class="font-bold">{$_('modals.add_to_order.obs')}</strong>
                  </div>
              {/if}

              <div class="mb-6">
                  <table class="w-full border-collapse bg-white">
                      <thead class="bg-teal-400">
                          <tr>
                              <th class="text-center border border-solid border-gray-300 p-2">{$_('modals.add_to_order.ref')}</th>
                              <th class="text-center border border-solid border-gray-300 p-2">{$_('modals.add_to_order.brand')}</th>
                              <th class="text-center border border-solid border-gray-300 p-2">{$_('modals.add_to_order.descr')}</th>
                              <th class="text-center border border-solid border-gray-300 p-2">{$_('modals.add_to_order.price')}</th>
                          </tr>
                      </thead>
                      <tbody>
                          <tr>
                              <td class="text-center border border-solid border-gray-300 p-2 text-gray-900">{instrument.reference}</td>
                              <td class="text-center border border-solid border-gray-300 p-2 text-gray-900">{instrument.supplier}</td>
                              <td class="text-center border border-solid border-gray-300 p-2 text-gray-900">{instrument.supplierDescription}</td>
                              <td class="text-center border border-solid border-gray-300 p-2 text-gray-900">{instrument.price}</td>
                          </tr>
                      </tbody>
                  </table>
              </div>
              
              <div class="flex flex-col md:flex-row items-center space-y-4 md:space-y-0 md:space-x-4 mb-6">
                  <div class="flex items-center w-full md:w-auto">
                      <label for="qte" class="text-gray-900 font-semibold text-lg mr-2">{$_('modals.add_to_order.qtt')}</label>
                      <input
                          type="number"
                          id="qte"
                          name="qte"
                          class="border border-gray-300 rounded p-2 text-gray-900 bg-white w-24"
                          bind:value={quantity}
                      />
                  </div>

                  <div class="flex-grow"></div>

                  <div class="flex items-center w-full md:w-auto">
                      <label for="order" class="text-gray-900 font-semibold text-lg mr-2">{$_('modals.add_to_order.order2')}</label>
                      <select 
                          id="order"
                          name="order"
                          class="border border-gray-300 rounded p-2 text-gray-900 bg-white w-64"
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

              <div class="flex justify-end gap-4 mt-6">
                  <button
                      type="button"
                      class="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-700"
                      on:click={() => close()}
                  >
                      {$_('modals.add_to_order.cancel')}
                  </button>
                  
                  {#if isNotObsolete}
                      <button
                          type="button"
                          class="bg-teal-500 text-white px-4 py-2 rounded hover:bg-teal-700"
                          on:click={handleAddInstrument}
                      >
                          {$_('modals.add_to_order.ordering')}
                      </button>
                  {/if}
              </div>
          </div>
      </div>
  </div>
</div>
{/if}