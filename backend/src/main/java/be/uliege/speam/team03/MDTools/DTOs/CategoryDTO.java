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
public class CategoryDTO {
    private Long id;
    private String groupName;
    private String subGroupName;
    private String name;
    private String function;
    private String shape;
    private String lenAbrv;
    private Long pictureId;
}