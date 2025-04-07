package be.uliege.speam.team03.MDTools.services;

import java.time.Instant;
import java.util.Optional;
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.*;
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

   /**
    * Registers a new user in the database.
    *
    * @param user The user to register.
    * @return The registered user.
    */
   public UserDto registerUser(Map<String, Object> body) {
      String email = (String) body.get("email");
      if (!isValidEmail(email)) {
         throw new BadRequestException("Invalid email address.");
      }
      if (userRepository.findByEmail(email).isPresent()) {
         throw new BadRequestException("User with email " + email + " already exists.");
      }
      String username = (String) body.get("username");
      if (userRepository.findByUsername(username).isPresent()) {
         throw new BadRequestException("User with username " + username + " already exists.");
      }
      UserDto userDto = new UserDto();
      userDto.setEmail(email);
      userDto.setUsername(username);
      userDto.setRoles(List.of("ROLE_USER"));
      userDto.setEnabled(true);

      Timestamp currentTimestamp = Timestamp.from(Instant.now());
      userDto.setCreatedAt(currentTimestamp);
      userDto.setUpdatedAt(currentTimestamp);

      User newUser = UserMapper.toEntity(userDto);
      userRepository.save(newUser);
      return UserMapper.toDto(newUser);
   }

   /**
    * Updates a user in the database.
    *
    * @param identifier The identifier of the user to update (can be an id or a username).
    * @param body   The new values of the user.
    * @return The updated user.
    */
   public UserDto updateUser(Object identifier, Map<String, Object> body) {
      User userToUpdate;
      if (identifier instanceof Long) {
         userToUpdate = userRepository.findById((Long) identifier)
                 .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + identifier));
     } else if (identifier instanceof String) {
         userToUpdate = userRepository.findByUsername((String) identifier)
                 .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received username: " + identifier));
     } else {
         throw new BadRequestException("Invalid identifier type. Must be a Long (ID) or String (username).");
     }
      
      String email = (String) body.get("email");
      if (email != null) {
         if (!isValidEmail(email)) {
            throw new BadRequestException("Invalid email address.");
         }
         Optional<User> user = userRepository.findByEmail(email);
         if (user.isPresent() && user.get().getUserId() != userToUpdate.getUserId()) {
            throw new BadRequestException("User with email " + email + " already exists.");
         }
         userToUpdate.setEmail(email);
      }
      String newUsername = (String) body.get("username");
      if (newUsername != null) {
         Optional<User> user = userRepository.findByUsername(newUsername);
         if (user.isPresent() && user.get().getUserId() != userToUpdate.getUserId()) {
            throw new BadRequestException("User with username " + newUsername + " already exists.");
         }
         userToUpdate.setUsername(newUsername);
      }
      Boolean enabled = (Boolean) body.get("enabled");
      if (enabled != null) {
         userToUpdate.setEnabled(enabled);
      }

      @SuppressWarnings("unchecked")
      List<String> roles = (List<String>) body.get("roles");
      if (roles != null) {
         userToUpdate.setAuthorities(UserMapper.toAuthorities(roles));
      }

      userToUpdate.setUpdatedAt(Timestamp.from(Instant.now()));

      userRepository.save(userToUpdate);
      return UserMapper.toDto(userToUpdate);
   }

   /**
    * Deletes a user from the database.
    *
    * @param userName The ID of the user to delete.
    */
   public void deleteUser(String userName) {
      User userToDelete = userRepository.findByUsername(userName)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received username: " + userName));
      userRepository.delete(userToDelete);
   }

   /**
    * Checks if an email is valid.
    *
    * @param email The email to check.
    * @return True if the email is valid, false otherwise.
    */
   protected boolean isValidEmail(String email) {
      // Define the regex for a valid email
      String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
  
      // Compile the regex
      Pattern pattern = Pattern.compile(emailRegex);
  
      // Match the email against the regex
      Matcher matcher = pattern.matcher(email);
      return matcher.matches();
  }
}
