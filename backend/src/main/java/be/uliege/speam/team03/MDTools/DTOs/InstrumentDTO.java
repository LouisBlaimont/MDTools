package be.uliege.speam.team03.MDTools.DTOs;



import lombok.*;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class InstrumentDTO {
    private String supplier;
    private Integer categoryId;
    private String reference;
    private String supplierDescription;
    private Float price;
    private Boolean obsolete;
    private List<Long> picturesId;
    private Integer id;

}
