package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="category_characteristic")
public class CategoryCharacteristic {
    @Id
    private Integer category_id;

    @Id
    private Integer characteristic_id;

    private String value;
    private String value_abreviation;

    public CategoryCharacteristic(){}

    public CategoryCharacteristic(Integer categoryId, Integer charId, String val, String valAbrv){
        this.category_id = categoryId;
        this.characteristic_id = charId;
        this.value = val;
        this.value_abreviation = valAbrv;
    }

    public Integer getCategoryId(){
        return this.category_id;
    }
    public Integer getCharId(){
        return this.characteristic_id;
    }
    public String getVal(){
        return this.value;
    }
    public String getAbrv(){
        return this.value_abreviation;
    }
    
}
