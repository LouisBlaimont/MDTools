package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubGroupDTO {
    private Long id;
    private String name;
    private Long groupId;
    private List<String> subGroupCharacteristics;
    private Integer instrCount = 0;
    private List<Long> categoriesId;
    private Long pictureId;
}