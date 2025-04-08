package be.uliege.speam.team03.MDTools.DTOs;



import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private Long groupId;
    private Long subGroupId;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private Boolean obsolete;
    private List<Long> picturesId;
    private Integer id;

    // Add a constructor matching the test cases
    public InstrumentDTO(String supplier, Integer categoryId, String reference, String supplierDescription, Float price, Boolean obsolete, List<Long> picturesId, Integer id) {
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
