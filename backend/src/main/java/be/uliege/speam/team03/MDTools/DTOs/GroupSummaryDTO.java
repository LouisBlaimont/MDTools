package be.uliege.speam.team03.MDTools.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupSummaryDTO {
    private String name;
    private int instrCount;
    private Long pictureId;
}
