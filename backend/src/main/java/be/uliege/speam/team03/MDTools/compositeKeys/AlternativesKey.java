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
public class AlternativesKey implements Serializable{
    @Column(name = "instruments_id_1")
    private Long instrId1;

    @Column(name = "instruments_id_2")
    private Long instrId2;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass() ) return false;
        AlternativesKey that = (AlternativesKey) o;
        return Objects.equals(instrId1, that.instrId1) &&
               Objects.equals(instrId2, that.instrId2);

    }
    @Override
    public int hashCode(){
        return Objects.hash(instrId1, instrId2);
    } 

    
}
