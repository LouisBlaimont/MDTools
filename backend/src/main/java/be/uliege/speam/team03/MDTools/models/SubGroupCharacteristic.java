package be.uliege.speam.team03.MDTools.models;

import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "sub_group_characteristic")
public class SubGroupCharacteristic {
    
    @EmbeddedId
    SubGroupCharacteristicKey id;

    @ManyToOne
    @MapsId("subgroupId")
    @JoinColumn(name = "sub_group_id")
    SubGroup subgroup;

    @ManyToOne
    @MapsId("charId")
    @JoinColumn(name = "characteristic_id")
    Characteristic characteristic;

    private Integer order_position;

    public SubGroupCharacteristic(){}

    public SubGroupCharacteristic(SubGroup subgroup, Characteristic characteristic, Integer orderPos){
        this.order_position = orderPos;
        this.subgroup = subgroup;
        this.characteristic = characteristic;
    }

    public SubGroupCharacteristicKey getKey(){
        return this.id;
    }
    public SubGroup getSubGroup(){
        return this.subgroup;
    }
    public Characteristic getCharacteristic(){
        return this.characteristic;
    }
    public Integer getOrderPos(){
        return this.order_position;
    }

    public void setKey(SubGroupCharacteristicKey key){
        this.id = key;
    }
    public void setSubGroup(SubGroup subgroup){
        this.subgroup = subgroup;
    }
    public void setChar(Characteristic characteristic){
        this.characteristic = characteristic;
    }
    public void setOrder(Integer order){
        this.order_position = order;
    }

    
}
