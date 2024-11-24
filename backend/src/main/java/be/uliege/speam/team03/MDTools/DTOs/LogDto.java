package be.uliege.speam.team03.MDTools.DTOs;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogDto {
    private Long logId;
    private Long userId;
    private String username;
    private String action;
    private Timestamp timestamp;


}
