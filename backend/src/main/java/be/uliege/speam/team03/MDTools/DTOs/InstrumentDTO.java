package be.uliege.speam.team03.MDTools.DTOs;



import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private Timestamp priceDate;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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
