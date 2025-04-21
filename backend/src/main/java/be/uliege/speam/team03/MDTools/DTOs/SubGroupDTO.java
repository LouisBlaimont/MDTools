package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubGroupDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String name;
    private Long groupId;
    private List<SubGroupCharacteristicDTO> subGroupCharacteristics;
    private Integer instrCount;
    private List<Long> categoriesId;
    private Long pictureId;
}