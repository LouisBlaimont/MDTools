package be.uliege.speam.team03.MDTools.services;

import java.util.List;
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
   private final UserRepository userRepository;

   public UserDto getUserByEmail(String email) {
      User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received email: " + email));
      return UserMapper.toDto(user);
   }

   public UserDto getUserById(Long userId) {
      User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + userId));
      return UserMapper.toDto(user);
   }

   /**
    * Retrieves a list of all users from the database.
    *
    * @return A list of {@link UserDto} objects representing all users in the
    *         database. This list can be empty.
    */
   public List<UserDto> getAllUsers() {
      List<User> users = userRepository.findAll();
      return users.stream().map(UserMapper::toDto).toList();
   }

   public UserDto updateUserRoles(Long userId, List<String> roles) {
      User userToUpdate = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + userId));
      userToUpdate.setAuthorities(UserMapper.toAuthorities(roles));
      userRepository.save(userToUpdate);
      return UserMapper.toDto(userToUpdate);
   }
}
