package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="supplier")

public class Suppliers {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer supplier_id;

    private String supplier_name;
    private Boolean sold_by_md;
    private Boolean closed;

    public Suppliers(){}
    
    public Suppliers(String supplierName, Boolean soldByMd, Boolean closed){
        this.supplier_name = supplierName;
        this.sold_by_md = soldByMd;
        this.closed = closed;
    }

    public Integer getId(){
        return this.supplier_id;
    }
    public String getName(){
        return this.supplier_name;
    }
    public Boolean isSoldByMd(){
        return this.sold_by_md;
    }
    public Boolean isClosed(){
        return this.closed;
    }
}
