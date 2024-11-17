package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "instruments")
public class Instruments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer instrument_id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Suppliers supplier;

    @ManyToOne
    @JoinColumn(name = "sub_group_id")
    private SubGroups subGroup;

    private String reference;
    private String supplier_description;
    private Float price;
    private Boolean obsolete;

    public Instruments() {}

    public Instruments(Suppliers supplier, SubGroups subGroup, String reference, String supplierDescription, Float price, Boolean obsolete) {
        this.supplier = supplier;
        this.subGroup = subGroup;
        this.reference = reference;
        this.supplier_description = supplierDescription;
        this.price = price;
        this.obsolete = obsolete;
    }

    public Suppliers getSupplier() {
        return this.supplier;
    }

    public SubGroups getSubGroup() {
        return this.subGroup;
    }

    public String getReference() {
        return this.reference;
    }

    public String getSupplierDescription() {
        return this.supplier_description;
    }

    public Float getPrice() {
        return this.price;
    }

    public Boolean isObsolete() {
        return this.obsolete;
    }
}
