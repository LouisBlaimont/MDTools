package be.uliege.speam.team03.MDTools.DTOs;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
   @JsonProperty(access = JsonProperty.Access.READ_ONLY)
   private Long id;

   private String username;
   private String email;

   private List<String> roles;
   private boolean enabled;

   private Timestamp createdAt;
   private Timestamp updatedAt;
}
