package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="sub_group_characteristic")
public class SubgroupCharacteristic {
    @Id
    private Integer sub_group_id;

    @Id
    private Integer characteristic_id;

    private String value;
    private String value_abreviation;

    public SubgroupCharacteristic(){}

    public SubgroupCharacteristic(Integer subgroupId, Integer charId, String val, String valAbrv){
        this.sub_group_id = subgroupId;
        this.characteristic_id = charId;
        this.value = val;
        this.value_abreviation = valAbrv;
    }

    public Integer getSubgroupId(){
        return this.sub_group_id;
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
