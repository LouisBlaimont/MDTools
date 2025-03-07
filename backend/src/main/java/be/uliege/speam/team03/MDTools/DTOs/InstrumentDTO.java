package be.uliege.speam.team03.MDTools.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private boolean alt;
    private boolean obsolete;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer id;
}
