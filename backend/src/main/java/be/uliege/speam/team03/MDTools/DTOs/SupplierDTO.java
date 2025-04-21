package be.uliege.speam.team03.MDTools.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private boolean soldByMd;
    private boolean closed;


}
