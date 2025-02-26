package be.uliege.speam.team03.MDTools.mapper;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.models.User;

public class UserMapper {

   private UserMapper() {
   }
   public static UserDto mapToUserDto(User user) {
      return new UserDto(user.getUserId(), user.getUsername(), user.getEmail(), user.getPassword(), user.getCreatedAt(),
            user.getUpdatedAt(), user.getRoleName(), user.getJobPosition(), user.getWorkplace());
   }

   public static User mapToUser(UserDto userDto) {
      return new User(userDto.getUserId(), userDto.getUsername(), userDto.getEmail(), userDto.getPassword(), userDto.getCreatedAt(),
            userDto.getUpdatedAt(), userDto.getRoleName(), userDto.getJobPosition(), userDto.getWorkplace(), null, null);
   }

}
