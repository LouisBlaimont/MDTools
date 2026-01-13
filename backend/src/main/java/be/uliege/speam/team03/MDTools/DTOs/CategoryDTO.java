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
public class CategoryDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String groupName;
    private String subGroupName;

    private String externalCode;   // NEW
    private String function;
    private String author;         // NEW
    private String name;
    private String design;         // NEW
    private String shape;

    private String dimOrig;        // NEW
    private String lenAbrv;            // OLD lenAbrv
    private List<Long> picturesId;
}