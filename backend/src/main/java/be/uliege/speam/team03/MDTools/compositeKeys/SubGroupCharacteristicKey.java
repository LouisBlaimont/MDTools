package be.uliege.speam.team03.MDTools.compositeKeys;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class SubGroupCharacteristicKey implements Serializable{
    @Column(name="sub_group_id")
    private Integer subgroupId;

    @Column(name="characteristic_id")
    private Integer charId;

    public SubGroupCharacteristicKey(){}
    public SubGroupCharacteristicKey(Integer subgroupId, Integer charId){
        this.subgroupId = subgroupId;
        this.charId = charId;
    }
    public Integer getSubGroupId(){
        return this.subgroupId;
    }
    public Integer getCharId(){
        return this.charId;
    }
    public void setSubGroupId(Integer subgroupId){
        this.subgroupId = subgroupId;
    }
    public void setCharId(Integer charId){
        this.charId = charId;
    }
    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass() ) return false;
        SubGroupCharacteristicKey that = (SubGroupCharacteristicKey) o;
        return Objects.equals(subgroupId, that.subgroupId) &&
               Objects.equals(charId, that.charId);

    }
    @Override
    public int hashCode(){
        return Objects.hash(subgroupId, charId);
    } 
}
