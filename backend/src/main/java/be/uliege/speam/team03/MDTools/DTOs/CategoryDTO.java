package be.uliege.speam.team03.MDTools.DTOs;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CategoryDTO {
    private Integer id;
    private String groupName;
    private String subGroupName;
    private String name;
    private String function;
    private String shape;
    private String lenAbrv;
    private Long pictureId;
}