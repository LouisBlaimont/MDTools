<script>
    import {ordersNames, errorMessage, orders, selectedOrderId, orderItems} from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { onMount } from "svelte";
    import createOrderModal from "$lib/modals/createOrderModal.svelte";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";
    import { getOrders } from "$lib/components/order_component.js";

    onMount(async () => {
        await getOrders();
    });

    function getOrderIdFromName(name){
      const selectedOrder = $ordersNames.find(order => order.name === name);
      return selectedOrder ? selectedOrder.id : null;
    }

    function completeOrder(name, items){
      const id = getOrderIdFromName(name);
      selectedOrderId.set(id);
      orderItems.set(items);
      goto("/searches?group=&subgroup=");
    }

    function singleOrderView(name, items){
      const id = getOrderIdFromName(name);
      selectedOrderId.set(id);
      orderItems.set(items);
      goto(`/single_order_view?name=${name}`);
    }
  </script>
  
  <div class="flex justify-center items-center p-4">
    <div class="bg-white shadow-lg rounded-lg p-6 w-full max-w-6xl border border-gray-300 flex flex-col">
      {#each $orders as order}
        <div class="mb-6 border-b pb-4">
          <div class="flex justify-between items-center mb-2">
            <h2 class="text-lg font-bold text-black">{order.orderName}</h2>
            <div class="space-x-2">
              <button 
              class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
              onclick={()=>singleOrderView(order.orderName, order.orderItems)}
              >Voir/modifier commande</button>
              <button 
              class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
              onclick={()=>completeOrder(order.orderName, order.orderItems)}
              >Compléter commande</button>
            </div>
          </div>
          <table class="w-full border border-gray-200 text-sm">
            <thead>
              <tr class="bg-gray-200">
                <th class="p-2 text-left">Référence</th>
                <th class="p-2 text-left">Marque</th>
                <th class="p-2 text-left">Groupe</th>
                <th class="p-2 text-left">Fonction</th>
                <th class="p-2 text-left">Nom</th>
                <th class="p-2 text-left">Forme</th>
                <th class="p-2 text-left">Dimension</th>
                <th class="p-2 text-left">Qte</th>
                <th class="p-2 text-left">Prix HTVA</th>
                <th class="p-2 text-left">Total HTVA</th>
              </tr>
            </thead>
            <tbody>
              {#each order.orderItems.slice(0,2) as item, index}
                <tr class="{index === 1 ? 'opacity-50' : ''} border-t">
                  <td class="p-2">{item.reference}</td>
                  <td class="p-2">{item.supplier}</td>
                  <td class="p-2">{item.category.groupName}</td>
                  <td class="p-2">{item.category.function}</td>
                  <td class="p-2">{item.category.name}</td>
                  <td class="p-2">{item.category.shape}</td>
                  <td class="p-2">{item.category.lenAbrv}</td>
                  <td class="p-2">{item.quantity}</td>
                  <td class="p-2">{item.price}</td>
                  <td class="p-2">{item.totalPrice}</td>
                </tr>
              {/each}
            </tbody>
          </table>
        </div>
      {/each}
      <div class ="mt-4 flex justify-end">
      <button class=" bottom-4 right-4 px-4 py-2 bg-orange-300 text-white rounded-lg hover:bg-orange-400"
      onclick={()=>modals.open(createOrderModal)}>
        Créer une commande
      </button>
      </div>
    </div>
  </div>
  