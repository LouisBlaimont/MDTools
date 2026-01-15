package be.uliege.speam.team03.MDTools.DTOs;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImportProgressDTO {
    private int total;
    private int processed;
    private double progress;
    private Double speed;
    private Integer eta;
    private boolean finished;
}
