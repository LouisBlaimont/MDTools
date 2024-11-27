package be.uliege.speam.team03.MDTools.compositeKeys;

import java.io.Serializable;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Embeddable
public class SubGroupCharacteristicKey implements Serializable{
    @Column(name="sub_group_id")
    private Integer subGroupId;

    @Column(name="characteristic_id")
    private Integer charId;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass() ) return false;
        SubGroupCharacteristicKey that = (SubGroupCharacteristicKey) o;
        return Objects.equals(subGroupId, that.subGroupId) &&
               Objects.equals(charId, that.charId);

    }
    @Override
    public int hashCode(){
        return Objects.hash(subGroupId, charId);
    } 
}
