<script>
    import { orders, selectedOrderId, orderItems} from "$lib/stores/searches";
    import { userId } from "$lib/stores/user_stores";
    import { locale } from "svelte-i18n";
    import { onMount } from "svelte";
    import createOrderModal from "$lib/modals/createOrderModal.svelte";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";
    import { getOrders } from "$lib/components/order_component.js";
    import { _ } from "svelte-i18n";

    onMount(async () => {
        await getOrders();
    });

    function completeOrder(id, items){
      selectedOrderId.set(id);
      orderItems.set(items);
      goto("/searches?group=&subgroup=");
    }

    function singleOrderView(id, items){
      selectedOrderId.set(id);
      orderItems.set(items);
      goto(`/single_order_view?id=${encodeURIComponent(id)}`);
    }
  </script>
  
  <div class="flex justify-center items-center p-4">
    <div class="bg-white shadow-lg rounded-lg p-6 w-full max-w-6xl border border-gray-300 flex flex-col max-h-[80vh] overflow-y-auto">
      <div class ="mb-2 flex justify-end border-b">
        <button class="mb-1 bottom-4 right-4 px-4 py-2 bg-orange-300 text-white rounded-lg hover:bg-orange-400"
        onclick={()=>modals.open(createOrderModal)}>
          {$_('order_pages.all.create')}
        </button>
      </div>
      {#each $orders as order}
        <div class="mt-6 border-b pb-4">
          <div class="flex justify-between items-center mb-2">
            <h2 class="text-lg font-bold text-black">
              {order.orderName}
              {#if !order.isExported}
              : {$_('order_pages.dates.creationDate')} {new Intl.DateTimeFormat($locale).format(new Date(order.creationDate))}
              {:else}
              : {$_('order_pages.dates.exportDate')} {new Intl.DateTimeFormat($locale).format(new Date(order.exportDate))}
              {/if}
            </h2>
            <div class="space-x-2">
              <button 
              class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
              onclick={()=>singleOrderView(order.id, order.orderItems)}
              >{$_('order_pages.all.view_edit')}</button>
              {#if !order.isExported}
                <button 
                class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
                onclick={()=>completeOrder(order.id, order.orderItems)}
                >{$_('order_pages.all.complete')}</button>
              {/if}
            </div>
          </div>
          <table class="w-full border border-gray-200 text-sm">
            <thead>
              <tr class="bg-gray-200">
                <th class="p-2 text-left">{$_('orders_component.table.reference')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.brand')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.group')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.function')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.name')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.shape')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.dimension')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.quantity')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.unite_price_excl_vat')}</th>
                <th class="p-2 text-left">{$_('orders_component.table.total_price_excl_vat')}</th>
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
    </div>
  </div>
  