package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String name;
    private Integer instrCount;
    private Long pictureId;
    private List<SubGroupDTO> subGroups; // Added nested DTOs
}