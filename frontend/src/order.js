let order = [{id:"1", ref :"AA1000301", brand : "MAGONOVUM", group : "MANCHE DE BISTOURI", fct :"", name :"", form:"3" ,dim : "LG 125", qte :"2", pu_htva : "15.44", total_htva :"30.88" },];
export function getOrder(){
    return order;
}
export function addTool(tool_ref, tool_brand, tool_group, tool_fct, tool_name, tool_form, tool_dim, tool_qte, tool_pu_htva){
    const newTool = {
        id : order.length +1, 
        ref : tool_ref, 
        brand : tool_brand, 
        group : tool_group,
        fct : tool_fct, 
        name : tool_name, 
        form : tool_form, 
        dim : tool_dim, 
        qte : tool_qte || 1, 
        pu_htva : tool_pu_htva, 
        total_htva : "3", //not good !!
    }; 
    order.push(newTool);
    return order;
}