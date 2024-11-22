package be.uliege.speam.team03.MDTools.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.UserMapper;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
   private UserRepository userRepository;

   public UserDto createUser(UserDto userDto) {
      User user = UserMapper.mapToUser(userDto);
      User savedUser = userRepository.save(user);
      return UserMapper.mapToUserDto(savedUser);
   }

   public UserDto getUserByEmail(String email) {
      User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received email: " + email));
      return UserMapper.mapToUserDto(user);
   }

   public UserDto getUserById(Long userId) {
      User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + userId));
      return UserMapper.mapToUserDto(user);
   }

   public List<UserDto> getAllUsers() {
      List<User> users = userRepository.findAll();
      return users.stream().map(UserMapper::mapToUserDto).collect(Collectors.toList());
   }
}
