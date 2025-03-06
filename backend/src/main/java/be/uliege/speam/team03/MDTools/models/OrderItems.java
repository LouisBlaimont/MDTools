package be.uliege.speam.team03.MDTools.models;

import be.uliege.speam.team03.MDTools.compositeKeys.OrderItemsKey;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="order_items")
@NoArgsConstructor
@Getter
@Setter
public class OrderItems {
    @EmbeddedId
    OrderItemsKey id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    Orders order;

    @ManyToOne
    @MapsId("instrumentId")
    @JoinColumn(name = "instrument_id")
    Instruments instrument;

    @Column(name ="quantity")
    private Integer quantity;
    
    public OrderItems(Orders order, Instruments instrument, Integer quantity){
        this.order = order;
        this.instrument = instrument;
        this.quantity = quantity;
    }
}
