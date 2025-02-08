package be.uliege.speam.team03.MDTools.models;

import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name="category_characteristic")
public class CategoryCharacteristic {
    
    @EmbeddedId
    CategoryCharacteristicKey id;

    @ManyToOne
    @MapsId("categoryId")
    @JoinColumn(name="category_id")
    Category category;

    @ManyToOne
    @MapsId("charId")
    @JoinColumn(name="characteristic_id")
    Characteristic characteristic;

    @Column(name="characteristic_value")
    private String val;

    @Column(name="value_abreviation")
    private String valAbrev;

    public CategoryCharacteristic(Category category, Characteristic characteristic, String val, String valAbrv){
        this.category = category;
        this.characteristic = characteristic;
        this.val = val;
        this.valAbrev = valAbrv;
    }
    
}
