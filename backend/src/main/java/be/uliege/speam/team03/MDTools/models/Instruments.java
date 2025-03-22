package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
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
    private Supplier supplier;

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
}
