package be.uliege.speam.team03.MDTools.DTOs;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private String reference;
    private String supplierDescription;
    private Float price;
    private boolean alt;
    private boolean obsolete;
}