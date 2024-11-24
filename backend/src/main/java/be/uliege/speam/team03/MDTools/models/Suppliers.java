package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier")
public class Suppliers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer supplier_id;

    private String supplier_name;
    private Boolean sold_by_md;
    private Boolean closed;

    public Suppliers() {}

    public Suppliers(String supplierName, Boolean soldByMd, Boolean closed) {
        this.supplier_name = supplierName;
        this.sold_by_md = soldByMd;
        this.closed = closed;
    }

    public String getSupplierName() {
        return this.supplier_name;
    }

    public Boolean isSoldByMd() {
        return this.sold_by_md;
    }

    public Boolean isClosed() {
        return this.closed;
    }
}
