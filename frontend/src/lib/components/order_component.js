import { get } from 'svelte/store';
import { orderItems, orders, ordersNames,errorMessage } from "$lib/stores/searches";
import { apiFetch } from "$lib/utils/fetch";

export async function getOrders(){
    let newOrders = []
    const ordersNamesValue = get(ordersNames);
    for (const order of ordersNamesValue){
        let orderInstr = [];
        try{
            const response = await apiFetch(`/api/orders/${order.id}`);
            if (!response.ok){
                throw new Error(`Failed to fetch order items: ${response.statusText}`); 
            }
            orderInstr = await response.json();
        }catch (error){
            console.error(error);
            errorMessage.set(error.message);
        }
        const orderDTO = {
            orderName : order.name,
            orderItems : orderInstr
        }
        newOrders.push(orderDTO);
    }
    orders.set(newOrders);
}

export async function findOrderItems(id){
    try{
        const response = await apiFetch(`/api/orders/${id}`);
        if (!response.ok){
            throw new Error(`Failed to fetch order items: ${response.statusText}`); 
        }
        orderItems.set(await response.json());
    }catch (error){
        console.error(error);
        errorMessage.set(error.message);
    }
    return;
}

export async function addInstrument(instrId, userId,orderId, quantity){
    const data = {
        instrId : instrId,
        userId : userId,
        orderId : orderId,
        quantity : quantity,
    };

    return apiFetch("/api/orders/add-instrument", {
        method : "POST",
        headers : { "Content-type" : "application/json"},
        body : JSON.stringify(data),
    })
    .then((response) =>{
        if (!response.ok){
            throw new Error(`Failed to add instrument to order : ${response.status}`);
        }
        return response.json();
    })
    .then((result) => {
        orderItems.set(result);
    })
    .catch((error) => {
        console.log("Error :", error);
    });
}