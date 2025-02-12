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

    // Getters
    public Integer getId() {
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

    // Setters for updating values
    public void setSupplier(Suppliers supplier) {
        this.supplier = supplier;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setSupplierDescription(String supplierDescription) {
        this.supplier_description = supplierDescription;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public void setObsolete(Boolean obsolete) {
        this.obsolete = obsolete;
    }
}
