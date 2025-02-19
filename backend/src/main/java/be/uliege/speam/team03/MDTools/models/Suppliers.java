package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Suppliers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="supplier_id")
    private Integer id;

    @Column(name = "supplier_name")
    private String supplierName;
    @Column(name = "sold_by_md")
    private Boolean soldByMd;
    @Column(name="closed")
    private Boolean closed;

    public Suppliers() {}

    public Suppliers(String supplierName, Boolean soldByMd, Boolean closed) {
        this.supplierName = supplierName;
        this.soldByMd = soldByMd;
        this.closed = closed;
    }
}
