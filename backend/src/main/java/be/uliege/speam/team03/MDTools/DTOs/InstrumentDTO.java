package be.uliege.speam.team03.MDTools.DTOs;



import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private Long groupId;
    private Long subGroupId;
    private Long categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private Boolean obsolete;
    private List<Long> picturesId;
    private Long id;

    // Add a constructor matching the test cases
    public InstrumentDTO(String supplier, Long categoryId, String reference, String supplierDescription, Float price, Boolean obsolete, List<Long> picturesId, Long id) {
        this.supplier = supplier;
        this.categoryId = categoryId;
        this.reference = reference;
        this.supplierDescription = supplierDescription;
        this.price = price;
        this.obsolete = obsolete;
        this.picturesId = picturesId;
        this.id = id;
    }
}
