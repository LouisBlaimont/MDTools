package be.uliege.speam.team03.MDTools.mapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.models.Authority;

public class UserMapper {

   private UserMapper() {
   }

   public static UserDto toDto(User user) {
      if (user == null) {
         return null;
      }

      UserDto userDto = new UserDto();
      userDto.setId(user.getUserId());
      userDto.setUsername(user.getUsername());
      userDto.setEmail(user.getEmail());
      userDto.setRoles(user.getRoles());
      userDto.setJobPosition(user.getJobPosition());
      userDto.setWorkplace(user.getWorkplace());
      userDto.setRoleName(user.getRoleName());
      userDto.setEnabled(user.isEnabled());
      userDto.setCreatedAt(user.getCreatedAt());
      userDto.setUpdatedAt(user.getUpdatedAt());


      return userDto;
   }

   public static User toEntity(UserDto userDto) {
      if (userDto == null) {
         return null;
      }

      User user = new User();
      user.setUserId(userDto.getId());
      user.setUsername(userDto.getUsername());
      user.setEmail(userDto.getEmail());
      user.setJobPosition(userDto.getJobPosition());
      user.setWorkplace(userDto.getWorkplace());
      user.setRoleName(userDto.getRoleName());
      user.setAuthorities(toAuthorities(userDto.getRoles()));
      user.setEnabled(userDto.isEnabled());
      user.setCreatedAt(userDto.getCreatedAt());
      user.setUpdatedAt(userDto.getUpdatedAt());

      return user;
   }

   public static Set<Authority> toAuthorities(List<String> roles) {
      return roles.stream().map(Authority::new).collect(Collectors.toSet());
   }
}
