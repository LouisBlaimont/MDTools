package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupDTO {
    private Long id;
    private String name;
    private Integer instrCount;
    private Long pictureId;
    private List<SubGroupDTO> subGroups; // Added nested DTOs
}