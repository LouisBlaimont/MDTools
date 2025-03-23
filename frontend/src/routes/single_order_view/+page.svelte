<script>
    import { ordersNames, orderItems} from "$lib/stores/searches";
    import { onMount } from "svelte";
    import ModifyOrderNameModal from "$lib/modals/modifyOrderNameModal.svelte";
    import DeleteOrderModal from "$lib/modals/deleteOrderModal.svelte";
    import { modals } from "svelte-modals";
    import { goto } from "$app/navigation";
    import { page } from "$app/stores";

    let orderName = "";
    let successDelete = "";
    $:{
        orderName = $page.url.searchParams.get("name");
    }
    function completeOrder(){
      goto("/searches?group=&subgroup=");
    }
    function deleteOrder(){
      successDelete = "Commande supprimée avec succès.";
    }
  </script>
  
  <div class="flex justify-center items-center p-4">
    <div class="bg-white shadow-lg rounded-lg p-6 w-full max-w-6xl border border-gray-300 flex flex-col">

        {#if successDelete}
          <p class="text-red-600 font-semibold text-center">{successDelete}</p>
        {:else}
        <div class="mb-6 border-b pb-4">
          <div class="flex justify-between items-center mb-2">
            <h2 class="text-lg font-bold text-black">{orderName}</h2>
            <div class="space-x-2">
              <button class="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600">Exporter</button>
              <button 
              class="bg-amber-500 text-white px-4 py-2 rounded hover:bg-amber-600"
              onclick={()=> modals.open(ModifyOrderNameModal, {orderName, updateOrderName: (newName) => orderName = newName})}
              >Modifier nom</button>
              <button 
              class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
              onclick={()=>completeOrder()}
              >Compléter</button>
              <button 
              class="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
              onclick={()=> modals.open(DeleteOrderModal, {orderName, onDelete:deleteOrder})}
              >Supprimer</button>
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
        {/if}
    </div>
  </div>
  