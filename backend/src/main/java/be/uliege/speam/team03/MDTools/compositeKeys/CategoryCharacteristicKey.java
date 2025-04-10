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
public class CategoryCharacteristicKey implements Serializable {
    @Column(name="category_id")
    private Long categoryId;

    @Column(name="characteristic_id")
    private Long charId;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || this.getClass() != o.getClass()) return false;
        CategoryCharacteristicKey that = (CategoryCharacteristicKey) o;
        return Objects.equals(categoryId, that.categoryId) &&
               Objects.equals(charId, that.charId);
    }
    @Override
    public int hashCode(){
        return Objects.hash(categoryId, charId);
    }
}
