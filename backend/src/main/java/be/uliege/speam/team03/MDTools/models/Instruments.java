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
    @Column(name = "instrument_id")
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL) // Cascade the save operation
    @JoinColumn(name = "supplier_id")
    private Suppliers supplier;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "reference")
    private String reference;

    @Column(name = "supplier_description")
    private String supplierDescription;

    @Column(name = "price")
    private Float price;

    @Column(name = "obsolete")
    private Boolean obsolete;
    //private Boolean alt;

    public Instruments() {}

    public Instruments(Suppliers supplier, Category category, String reference, String supplierDescription, Float price, Boolean obsolete){//, Boolean alt) {
        this.supplier = supplier;
        this.category = category;
        this.reference = reference;
        this.supplierDescription = supplierDescription;
        this.price = price;
        this.obsolete = obsolete;
        //this.alt = alt;
    }
}
