package be.uliege.speam.team03.MDTools.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

   /**
    * Retrieves a user based on their email.
    *
    * @param email The email of the user to retrieve.
    * @return A {@link UserDto} representing the user with the given email.
    * @throws ResourceNotFoundException if the user does not exist.
    */
   public UserDto getUserByEmail(String email) {
      User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received email: " + email));
      return UserMapper.toDto(user);
   }

   /**
    * Retrieves a user based on their ID.
    *
    * @param userId The ID of the user to retrieve.
    * @return A {@link UserDto} representing the user with the given ID.
    * @throws ResourceNotFoundException if the user does not exist.
    */
   public UserDto getUserById(Long userId) {
      User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + userId));
      return UserMapper.toDto(user);
   }

   /**
    * Retrieves the ID of a user based on their email.
    *
    * @param email The email of the user.
    * @return The ID of the user with the given email.
    * @throws ResourceNotFoundException if the user does not exist.
    */
   public Long getUserIdByEmail(String email){
      User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received email: " + email));
      Long id = user.getUserId();
      return id;
   }

   /**
    * Retrieves a list of all users from the database.
    *
    * @return A list of {@link UserDto} objects representing all users in the database.
    */
   public Page<UserDto> getAllUsers(Pageable pageable) {
      return userRepository.findAll(pageable).map(UserMapper::toDto);
   }

   /**
    * Updates the roles of a user in the database.
    *
    * @param userId The ID of the user to update.
    * @param roles The new roles to assign to the user.
    * @return A {@link UserDto} representing the updated user.
    * @throws ResourceNotFoundException if the user does not exist.
    */
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
    * @param userDto The data of the user to register.
    * @return A {@link UserDto} representing the registered user.
    * @throws BadRequestException if the email or username is invalid or already exists.
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
    * @param identifier The identifier of the user to update (can be an ID or a username).
    * @param userDto The new data for the user.
    * @return A {@link UserDto} representing the updated user.
    * @throws BadRequestException if the identifier is invalid or if the email/username already exists.
    * @throws ResourceNotFoundException if the user does not exist.
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
         if (!newUsername.matches("^[\\p{L}0-9._-]+$")) {
            throw new BadRequestException("Username can only contain letters, numbers, dots, underscores, and hyphens.");
         }
         Optional<User> user = userRepository.findByUsername(newUsername);
         if (user.isPresent() && !Objects.equals(user.get().getUserId(), userToUpdate.getUserId())) {
            throw new BadRequestException("User with username " + newUsername + " already exists.");
         }
         userToUpdate.setUsername(newUsername);
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
         if (!roleName.matches("^[\\p{L}0-9._-]+$|^$")) {
            throw new BadRequestException("Role name can only contain letters, numbers, dots, underscores, and hyphens or be empty.");
         }
      } else {
         roleName = userToUpdate.getRoleName();
      }
      userToUpdate.setRoleName(roleName);

      Boolean enabled = (Boolean) userDto.isEnabled();
      if (enabled == null || !enabled) {
         enabled = false;
      } else {
         enabled = true;
      }
      userToUpdate.setEnabled(enabled);

      List<String> roles = (List<String>) userDto.getRoles();
      if (roles != null) {
         userToUpdate.setAuthorities(UserMapper.toAuthorities(roles));
      }

      if (enabled && userToUpdate.getAuthorities().isEmpty()) {
         throw new BadRequestException("User must have at least one role to be enabled.");
      }

      userToUpdate.setUpdatedAt(Timestamp.from(Instant.now()));
      userRepository.save(userToUpdate);
      return UserMapper.toDto(userToUpdate);
   }

   /**
     * Find paginated users based on search query and pagination information.
     * 
     * @param searchQuery the search query to filter users by name
     * @param pageable the pagination information
     * @return a paginated list of UserDTOs matching the search query
     */
    public Page<UserDto> searchPaginatedUsers(String searchQuery, Pageable pageable) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return getAllUsers(pageable);
        }
        
        return userRepository.findByUsernameContainingIgnoreCase(searchQuery.trim(), pageable)
                .map(UserMapper::toDto);
    }

   /**
    * Deletes a user from the database.
    *
    * @param userName The username of the user to delete.
    * @throws ResourceNotFoundException if the user does not exist.
    */
   public void deleteUser(String userName) {
      User userToDelete = userRepository.findByUsername(userName)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received username: " + userName));
      userRepository.delete(userToDelete);
   }

   /**
    * Checks if an email is valid.
    *
    * @param email The email to validate.
    * @return True if the email is valid, false otherwise.
    */
   protected boolean isValidEmail(String email) {
      if (email == null || email.isEmpty()) {
         return false;
      }

      if (email != email.trim()) {
         return false;
      }

      // Define the regex for a valid email
      String emailRegex = "^[a-zA-Z0-9](?!.*\\.\\.)[a-zA-Z0-9._%+-]*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
  
      // Compile the regex
      Pattern pattern = Pattern.compile(emailRegex);
  
      // Match the email against the regex
      Matcher matcher = pattern.matcher(email);
      return matcher.matches();
  }
}
