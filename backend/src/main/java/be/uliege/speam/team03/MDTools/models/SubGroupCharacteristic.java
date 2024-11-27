package be.uliege.speam.team03.MDTools.models;

import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sub_group_characteristic")
public class SubGroupCharacteristic {
    
    @EmbeddedId
    SubGroupCharacteristicKey id;

    @ManyToOne
    @MapsId("subGroupId")
    @JoinColumn(name = "sub_group_id")
    SubGroup subGroup;

    @ManyToOne
    @MapsId("charId")
    @JoinColumn(name = "characteristic_id")
    Characteristic characteristic;

    @Column(name="order_position")
    private Integer orderPosition;

    public SubGroupCharacteristic(SubGroup subgroup, Characteristic characteristic, Integer orderPos){
        this.orderPosition = orderPos;
        this.subGroup = subgroup;
        this.characteristic = characteristic;
    }
}
