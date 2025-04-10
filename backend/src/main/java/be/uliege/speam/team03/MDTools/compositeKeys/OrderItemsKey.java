package be.uliege.speam.team03.MDTools.compositeKeys;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class OrderItemsKey implements Serializable{
    @Column(name="order_id")
    private Long orderId;

    @Column(name="instrument_id")
    private Long instrumentId;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass() ) return false;
        OrderItemsKey that = (OrderItemsKey) o;
        return Objects.equals(orderId, that.orderId) &&
               Objects.equals(instrumentId, that.instrumentId);

    }
    @Override
    public int hashCode(){
        return Objects.hash(orderId, instrumentId);
    } 
}
