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

    public GroupCharacteristic(){}

    public GroupCharacteristic(Group group, Characteristic characteristic, Integer orderPos){
        this.order_position = orderPos;
        this.group = group;
        this.characteristic = characteristic;
    }

    public GroupCharacteristicKey getKey(){
        return this.id;
    }
    /*public Group getGroup(){
        return this.group;
    }*/
    public Characteristic getCharacteristic(){
        return this.characteristic;
    }
    public Integer getOrderPos(){
        return this.order_position;
    }

    public void setKey(GroupCharacteristicKey key){
        this.id = key;
    }
    public void setGroup(Group group){
        this.group = group;
    }
    public void setChar(Characteristic characteristic){
        this.characteristic = characteristic;
    }
    public void setOrder(Integer order){
        this.order_position = order;
    }

    
}
