package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    @Column(name = "supplier_description")
    private String supplierDescription;
    
    private Float price;
    private Boolean obsolete;

    public Instruments() {}

    public Instruments(Suppliers supplier, Category category, String reference, String supplierDescription, Float price, Boolean obsolete) {
        this.supplier = supplier;
        this.category = category;
        this.reference = reference;
        this.supplierDescription = supplierDescription;
        this.price = price;
        this.obsolete = obsolete;
    }
}
