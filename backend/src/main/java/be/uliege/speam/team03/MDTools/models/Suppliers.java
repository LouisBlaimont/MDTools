package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier")
public class Suppliers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="supplier_id")
    private Integer id;

    @Column(name="supplier_name", nullable = false)
    private String name;

    @Column(name="sold_by_md", nullable = false)
    private Boolean sold_by_md = true;

    @Column(name="closed")
    private Boolean closed;

    public Suppliers() {}

    public Suppliers(String supplierName, Boolean soldByMd, Boolean closed) {
        this.name = supplierName;
        this.sold_by_md = soldByMd;
        this.closed = closed;
    }

    public Integer getSupplierId() {
        return this.id;
    }

    public String getSupplierName() {
        return this.name;
    }

    public Boolean isSoldByMd() {
        return this.sold_by_md;
    }

    public Boolean isClosed() {
        return this.closed;
    }

    public void setSupplierName(String supplierName) {
        this.name = supplierName;
    }

    public void setSoldByMd(Boolean soldByMd) {
        this.sold_by_md = soldByMd;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public void setSupplierId(Integer supplierId) {
        this.id = supplierId;
    }
}
