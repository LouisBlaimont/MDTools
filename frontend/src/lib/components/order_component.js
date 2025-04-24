import { get } from "svelte/store";
import { orderItems, orders, ordersNames,errorMessage, selectedOrderId } from "$lib/stores/searches";
import { userId } from "$lib/stores/user_stores.js"; 
import { apiFetch } from "$lib/utils/fetch";
import ExcelJS from "exceljs";
import FileSaver from "file-saver";
import confetti from "canvas-confetti";

/**
 * Gets every order
 */
export async function findOrdersNames(){
    try{
        const response = await apiFetch(`/api/orders/user/${get(userId)}`);
        if (!response.ok){
            throw new Error(`Failed to fetch orders: ${response.statusText}`); 
        }
        ordersNames.set(await response.json());
    }catch (error){
        console.error(error);
    }
    return;
}
/**
 * Get every order with their name and instruments
 */
export async function getOrders(){
    let newOrders = []
    if(!(get(ordersNames))){
        await findOrdersNames();
    }
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
            id : order.id,
            orderName : order.name,
            orderItems : orderInstr,
            isExported : order.isExported,
            creationDate : order.creationDate,
            exportDate : order.exportDate
        }
        newOrders.push(orderDTO);
    }
    newOrders.sort((a,b) =>{
        if(a.isExported !== b.isExported){
            return a.isExported ? 1 : -1;
        }
        if(!a.isExported){
            return new Date(b.creationDate) - new Date(a.creationDate);
        }
        return new Date(b.exportDate) - new Date(a.exportDate);
    })
    orders.set(newOrders);
}
/**
 * Gets the instruments of a specific order
 * @param {*} id  The id of the order
 */
export async function findOrderItems(id){
    try{
        const response = await apiFetch(`/api/orders/${id}`);
        if (!response.ok){
            throw new Error(`Failed to fetch order items: ${response.statusText}`); 
        }
        orderItems.set(await response.json());
    }catch (error){
        console.error(error);
    }
    return;
}

/**
 * Adds instrument to order
 * @param {*} instrId The id of the instrument to add
 * @param {*} userId  The id of the user to which the order belongs
 * @param {*} orderId  The id of the order
 * @param {*} quantity The quantity of the instrument within the order
 */
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

/**
* Export the current order items to a styled Excel file.
*/
export async function exportOrderToExcel() {
    if(Math.floor(Math.random() * 11) === 0){
        confetti({
            particleCount: 100,
            spread: 70,
            origin: { y: 0.9, x: 0.73 },
        });
    }

   const items = get(orderItems);
   const orderId = get(selectedOrderId);
   const orderName = get(ordersNames).find(o => o.id === orderId)?.name;

   if (!items?.length || !orderName) {
       alert("Missing order data");
       return;
   }

   const workbook = new ExcelJS.Workbook();
   const sheet = workbook.addWorksheet("Commande");

   sheet.columns = [
       { header: "Référence", key: "reference", width: 20 },
       { header: "Fournisseur", key: "supplier", width: 20 },
       { header: "Description", key: "description", width: 50 },
       { header: "Quantité", key: "quantity", width: 10 },
       { header: "Prix HTVA", key: "price", width: 15 },
       { header: "Total HTVA", key: "totalHTVA", width: 15 },
       { header: "Montant TVAC", key: "totalTVAC", width: 15 }
   ];

   for (const item of items) {
       const total = item.totalPrice || 0;

       const parts = [
           item.category.groupName,
           item.category.subGroupName,
           item.category.function,
           item.category.name,
           item.category.shape,
           item.category.lenAbrv ? `${item.category.lenAbrv} mm` : null
       ].filter(Boolean);

       const description = parts.join(" ");

       sheet.addRow({
           reference: item.reference,
           supplier: item.supplier,
           description,
           quantity: item.quantity,
           price: item.price,
           totalHTVA: total.toFixed(2),
           totalTVAC: (total * 1.21).toFixed(2)
       });
   }

   const lastRow = sheet.rowCount;
   const maxCol = sheet.columns.length;

   sheet.eachRow((row, rowNumber) => {
       row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
           if (colNumber > maxCol) return;

           const isHeader = rowNumber === 1;
           const isTotal = rowNumber === lastRow;
           const isTop = isHeader;
           const isBottom = isTotal;
           const isLeft = colNumber === 1;
           const isRight = colNumber === maxCol;

           cell.alignment = {
               vertical: "middle",
               horizontal: "center",
               wrapText: true
           };

           cell.font = { bold: isHeader || isTotal };

           if (isHeader) {
               cell.font = { bold: true };
               cell.fill = {
                   type: "pattern",
                   pattern: "solid",
                   fgColor: { argb: "FFD9E1F2" }
               };
           }

           if (isTotal) {
               cell.font = { bold: true };
               cell.fill = {
                   type: "pattern",
                   pattern: "solid",
                   fgColor: { argb: "FFFCE4D6" }
               };
           }

           cell.border = {
               top: { style: isTop ? "medium" : "thin" },
               bottom: { style: isBottom ? "medium" : "thin" },
               left: { style: isLeft ? "medium" : "thin" },
               right: { style: isRight ? "medium" : "thin" }
           };
       });
   });

   const buffer = await workbook.xlsx.writeBuffer();
   FileSaver.saveAs(new Blob([buffer]), `commande_${orderName}.xlsx`);
   try {
    const response = await apiFetch(`/api/orders/${get(selectedOrderId)}/exported`, {
    method: "PATCH", 
    headers : { "Content-type" : "application/json"},
    });
    if(!response.ok){
        throw new Error(`Failed to export order : ${response.status}`);
    }
    ordersNames.set(await response.json());
    await getOrders();
    }catch(error){
        console.error(error);
    }


}