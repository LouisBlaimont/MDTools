package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "instruments")
public class Instruments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "instrument_id")
    private Integer instrument_id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Suppliers supplier;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String reference;
    private String supplier_description;
    private Float price;
    private Boolean obsolete;

    public Instruments() {}

    public Instruments(Suppliers supplier, Category category, String reference, String supplierDescription, Float price, Boolean obsolete) {
        this.supplier = supplier;
        this.category = category;
        this.reference = reference;
        this.supplier_description = supplierDescription;
        this.price = price;
        this.obsolete = obsolete;
    }
    public Integer getInstrument_id() {
        return this.instrument_id;
    }

    public Suppliers getSupplier() {
        return this.supplier;
    }

    public Category getCategory() {
        return this.category;
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
