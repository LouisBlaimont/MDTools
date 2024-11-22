package be.uliege.speam.team03.MDTools.DTOs;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
   private Long userId;

   private String username;
   private String email;
   private String password;
   private Timestamp createdAt;
   private Timestamp updatedAt;
   private String roleName;
   private String jobPosition;
   private String workplace;

   public UserDto(Long userId, String username, String email,
         String roleName, String jobPosition, String workplace) {
      this.userId = userId;
      this.username = username;
      this.email = email;
      this.roleName = roleName;
      this.jobPosition = jobPosition;
      this.workplace = workplace;
   }
}
