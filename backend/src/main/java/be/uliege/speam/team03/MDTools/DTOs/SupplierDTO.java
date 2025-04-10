package be.uliege.speam.team03.MDTools.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SupplierDTO {
    private String name;
    private Long id;
    private boolean soldByMd;
    private boolean closed;


}
