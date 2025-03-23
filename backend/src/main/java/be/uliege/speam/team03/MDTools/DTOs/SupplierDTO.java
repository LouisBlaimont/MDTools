package be.uliege.speam.team03.MDTools.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SupplierDTO {
    private String name;
    private Integer id;
    private boolean soldByMd;
    private boolean closed;


}
