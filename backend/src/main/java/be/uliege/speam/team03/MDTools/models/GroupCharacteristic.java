package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_characteristic")
public class GroupCharacteristic {
    @Id
    private Integer group_id;
    @Id
    private Integer characateristic_id;
    private Integer order_position;

    public GroupCharacteristic(){}

    public GroupCharacteristic(Integer groupId, Integer charId, Integer orderPos){
        this.group_id = groupId;
        this.characateristic_id = charId;
        this.order_position = orderPos;
    }

    public Integer getGroupId(){
        return this.group_id;
    }
    public Integer getCharId(){
        return this.characateristic_id;
    }
    public Integer getOrderPos(){
        return this.order_position;
    }
    
}
