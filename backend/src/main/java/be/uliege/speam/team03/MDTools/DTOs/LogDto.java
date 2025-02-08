package be.uliege.speam.team03.MDTools.DTOs;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long logId;
    private Long userId;
    private String username;
    private String action;
    private Timestamp timestamp;


}
