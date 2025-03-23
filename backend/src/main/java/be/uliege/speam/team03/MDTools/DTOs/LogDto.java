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

    public void setTimestamp(String timestamp) {
        this.timestamp = Timestamp.valueOf(timestamp);
    }

    public String getTimestamp() {
        if (timestamp == null) {
            return null;
        }
        // Format the timestamp to a string representation
        return timestamp.toString();
    }


}
