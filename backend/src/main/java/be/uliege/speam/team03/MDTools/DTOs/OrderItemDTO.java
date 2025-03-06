package be.uliege.speam.team03.MDTools.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDTO {
    private Integer orderId;
    private Integer instrumentId;
    private String orderName;
    private String reference;
    private String supplier;
    private CategoryDTO category;
    private Integer quantity;
    private Float price;
    private Float totalPrice;

    public OrderItemDTO(Integer orderId, Integer instrumentId, String orderName, String reference, String supplier, CategoryDTO category, Integer quantity, Float price){
        this.orderId = orderId;
        this.orderName = orderName;
        this.instrumentId = instrumentId;
        this.reference = reference;
        this.supplier = supplier;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
    }

    public void setTotalPrice(){
        if (price !=null && quantity !=null){
            this.totalPrice = price*quantity;
        }
        else{
            this.totalPrice = 0.0f;
        }
    }
}
