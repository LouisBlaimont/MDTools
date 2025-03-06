package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private Integer id;
    private String supplier;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private boolean alt;
    private boolean obsolete;
    private List<Long> picturesId;
}
