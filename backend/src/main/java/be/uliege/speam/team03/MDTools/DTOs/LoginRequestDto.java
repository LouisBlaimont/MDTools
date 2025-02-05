package be.uliege.speam.team03.MDTools.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LoginRequestDto {
   String email;
   String password;
}
