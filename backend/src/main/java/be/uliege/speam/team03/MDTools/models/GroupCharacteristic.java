package be.uliege.speam.team03.MDTools.models;

import be.uliege.speam.team03.MDTools.compositeKeys.GroupCharacteristicKey;
import jakarta.persistence.*;

@Entity
@Table(name = "group_characteristic")
public class GroupCharacteristic {
    
    @EmbeddedId
    GroupCharacteristicKey id;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    Group group;

    @ManyToOne
    @MapsId("charId")
    @JoinColumn(name = "characteristic_id")
    Characteristic characteristic;

    private Integer order_position;

    @Transient
    private String charName;

    public GroupCharacteristic(){}

    public GroupCharacteristic(Group group, Characteristic characteristic, Integer orderPos){
        this.order_position = orderPos;
        this.group = group;
        this.characteristic = characteristic;
    }

    public Characteristic getCharacteristic(){
        return this.characteristic;
    }
    public Integer getOrderPos(){
        return this.order_position;
    }
    
}
