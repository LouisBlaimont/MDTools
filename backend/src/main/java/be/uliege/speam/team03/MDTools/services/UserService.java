package be.uliege.speam.team03.MDTools.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
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

   public Long getUserIdByEmail(String email){
      User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received email: " + email));
      Long id = user.getUserId();
      return id;
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
   public UserDto registerUser(UserDto userDto) {
      String email = userDto.getEmail();
      if (!isValidEmail(email)) {
         throw new BadRequestException("Invalid email address.");
      }
      if (userRepository.findByEmail(email).isPresent()) {
         throw new BadRequestException("User with email " + email + " already exists.");
      }
      String username = userDto.getUsername();
      if (userRepository.findByUsername(username).isPresent()) {
         throw new BadRequestException("User with username " + username + " already exists.");
      }
   
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
   public UserDto updateUser(Object identifier, UserDto userDto) {
      if (identifier == null || (identifier instanceof String && ((String) identifier).isBlank())) {
         throw new BadRequestException("Invalid identifier. Identifier cannot be null or empty.");
      }
      User userToUpdate;
      if (identifier instanceof Long) {
         userToUpdate = userRepository.findById(((Long) identifier))
                 .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + identifier));
     } else if (identifier instanceof String) {
         userToUpdate = userRepository.findByUsername((String) identifier)
                 .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received username: " + identifier));
     } else {
         throw new BadRequestException("Invalid identifier type. Must be a Long (ID) or String (username).");
     }
      String email = userDto.getEmail();
      if (email != null) {
         if (!isValidEmail(email)) {
            throw new BadRequestException("Invalid email address.");
         }
         Optional<User> user = userRepository.findByEmail(email);
         if (user.isPresent() && !Objects.equals(user.get().getUserId(), userToUpdate.getUserId())) {
            throw new BadRequestException("User with email " + email + " already exists.");
         }
         userToUpdate.setEmail(email);
      }
      String newUsername = userDto.getUsername();
      if (newUsername != null) {
         if (newUsername.length() < 1 || newUsername.length() > 30) {
            throw new BadRequestException("Username must be between 1 and 30 characters.");
         }
         if (!newUsername.matches("^[\\p{L}0-9._-]+$")) {
            throw new BadRequestException("Username can only contain letters, numbers, dots, underscores, and hyphens.");
         }
         Optional<User> user = userRepository.findByUsername(newUsername);
         if (user.isPresent() && !Objects.equals(user.get().getUserId(), userToUpdate.getUserId())) {
            throw new BadRequestException("User with username " + newUsername + " already exists.");
         }
      } else {
         newUsername = userToUpdate.getUsername();
      }

      String jobPosition = userDto.getJobPosition();
      if (jobPosition != null) {
         userToUpdate.setJobPosition(jobPosition);
      }
      String workplace = userDto.getWorkplace();
      if (workplace != null) {
         userToUpdate.setWorkplace(workplace);
      }
      String roleName = userDto.getRoleName();
      if (roleName != null) {
         if (roleName.length() < 1 || roleName.length() > 30) {
            throw new BadRequestException("Role name must be between 1 and 30 characters.");
         }
         if (!roleName.matches("^[\\p{L}0-9._-]+$")) {
            throw new BadRequestException("Role name can only contain letters, numbers, dots, underscores, and hyphens.");
         }
      } else {
         roleName = userToUpdate.getRoleName();
      }
      userToUpdate.setRoleName(roleName);

      Boolean enabled = (Boolean) userDto.isEnabled();
      if (enabled && userToUpdate.getAuthorities().isEmpty()) {
         throw new BadRequestException("User must have at least one role to be enabled.");
      }

      List<String> roles = (List<String>) userDto.getRoles();
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
