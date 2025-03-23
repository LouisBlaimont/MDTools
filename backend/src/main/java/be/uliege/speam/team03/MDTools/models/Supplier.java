package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "supplier")
public class Supplier {

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

    /**
     * Constructor for the Suppliers class.
     * 
     * @param supplierName the name of the supplier
     * @param soldByMd whether the supplier is sold by MD
     * @param closed whether the supplier is closed
     * 
     * @return a new Suppliers object
     */
    public Supplier(String supplierName, Boolean soldByMd, Boolean closed) {
        this.supplierName = supplierName;
        this.soldByMd = soldByMd;
        this.closed = closed;
    }
}
