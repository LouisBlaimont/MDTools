package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "instruments")
public class Instruments {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer instrument_id;
    private Integer supplier_id;
    private Integer sub_group_id;
    private String reference;
    private String supplier_description;
    private Float price;
    private Boolean obsolete;

    public Instruments(){}
    
    public Instruments(Integer supplierId, Integer subGroupId, String reference, String supplierDes, Float price, Boolean obsolete){
        this.supplier_id = supplierId;
        this.sub_group_id = subGroupId;
        this.reference = reference;
        this.supplier_description = supplierDes;
        this.price = price;
        this.obsolete = obsolete;
    }

    public Integer getId(){
        return this.instrument_id;
    }

    public Integer getSupId(){
        return this.supplier_id;
    }
    public Integer getGroupId(){
        return this.sub_group_id;
    }
    public String getRef(){
        return this.reference;
    }
    public String getDescription(){
        return this.supplier_description;
    }
    public Float getPrice(){
        return this.price;
    }
    public Boolean isObsolete(){
        return this.obsolete;
    }
}
