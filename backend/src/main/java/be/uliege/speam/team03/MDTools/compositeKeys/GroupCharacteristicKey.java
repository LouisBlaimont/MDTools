package be.uliege.speam.team03.MDTools.compositeKeys;

import java.io.Serializable;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class GroupCharacteristicKey implements Serializable{
    @Column(name="group_id")
    private Integer groupId;

    @Column(name="characteristic_id")
    private Integer charId;

    public GroupCharacteristicKey(){}
    public GroupCharacteristicKey(Integer groupId, Integer charId){
        this.groupId = groupId;
        this.charId = charId;
    }
    public Integer getGroupId(){
        return this.groupId;
    }
    public Integer getCharId(){
        return this.charId;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass() ) return false;
        GroupCharacteristicKey that = (GroupCharacteristicKey) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(charId, that.charId);

    }
    @Override
    public int hashCode(){
        return Objects.hash(groupId, charId);
    } 
}
