<script>
    import { ordersNames, orderItems, orders, selectedOrderId} from "$lib/stores/searches";
    import { locale } from "svelte-i18n";
    import { onMount } from "svelte";
    import ModifyOrderNameModal from "$lib/modals/modifyOrderNameModal.svelte";
    import DeleteOrderModal from "$lib/modals/deleteOrderModal.svelte";
    import { exportOrderToExcel, getOrders, findOrderItems } from "$lib/components/order_component.js";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";
    import { _ } from "svelte-i18n";
    
    let dataLoaded = false;

    onMount(async () => {
      if(!$orders || !$ordersNames){        
        await getOrders();
      }
      dataLoaded = true;
    });

    let orderName = ""; 
    let isExported;
    let creationDate;
    let exportDate;
    $: if(dataLoaded) {
        const orderId = $page.url.searchParams.get("id");
        const order = $ordersNames.find(order => order.id === Number(orderId));
        selectedOrderId.set(order.id);
        if(!$orderItems){
          findOrderItems(order.id);
        }
        orderName = order.name;
        isExported = order.isExported;
        creationDate = new Intl.DateTimeFormat($locale).format(new Date(order.creationDate));
        exportDate = new Intl.DateTimeFormat($locale).format(new Date(order.exportDate));
    }
    function completeOrder(){
      goto("/searches?group=&subgroup=");
    }
  </script>
  
  <div class="flex justify-center items-center p-4">
    <div class="bg-white shadow-lg rounded-lg p-6 w-full max-w-6xl border border-gray-300 flex flex-col">
      <div class="mb-6 border-b pb-4">
        <div class="flex justify-between items-center mb-2">
          <h2 class="text-lg font-bold text-black">
            {orderName}
            {#if !isExported}
            : {$_('order_pages.dates.creationDate')} {creationDate}
            {:else}
            : {$_('order_pages.dates.exportDate')} {exportDate}
            {/if}
          </h2>
          {#if !isExported}
            <div class="space-x-2">
              <button class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
              onclick={()=>exportOrderToExcel()}
              >{$_('order_pages.single.export')}</button>
              <button 
              class="bg-amber-500 text-white px-4 py-2 rounded hover:bg-amber-600"
              onclick={()=> modals.open(ModifyOrderNameModal, {orderName})}
              >{$_('order_pages.single.edit')}</button>
              <button 
              class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
              onclick={()=>completeOrder()}
              >{$_('order_pages.single.complete')}</button>
              <button 
              class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
              onclick={()=> modals.open(DeleteOrderModal, {orderName})}
              >{$_('order_pages.single.delete')}</button>
            </div>
          {/if}
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
            {#each $orderItems as item, index}
              <tr class="border-t">
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
    </div>
  </div>
  