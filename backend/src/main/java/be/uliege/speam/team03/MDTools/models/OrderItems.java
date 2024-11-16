package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="order_items")
public class OrderItems {
    
    @Id
    Integer order_id;

    @Id
    Integer instrument_id;

    Integer quantity;

    public OrderItems(){}

    public OrderItems(Integer orderId, Integer instrID, Integer quantity){
        this.order_id = orderId;
        this.instrument_id = instrID;
        this.quantity = quantity;
    }

    public Integer getOrderId(){
        return this.order_id;
    }

    public Integer getInstrId(){
        return this.instrument_id;
    }

    public Integer getQuantity(){
        return this.quantity;
    }
}
