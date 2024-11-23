package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.UserAlreadyExistsException;
import be.uliege.speam.team03.MDTools.mapper.UserMapper;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
   private final UserRepository userRepository;
   private final EmailService emailService;
   private final AuthenticationService authenticationService;

   public UserDto createUser(UserDto userDto) throws MailException, UserAlreadyExistsException {
      if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
         throw new UserAlreadyExistsException("User with email " + userDto.getEmail() + " already exists.");
      }
      User user = UserMapper.mapToUser(userDto);
      // Create a token
      user = authenticationService.generateResetToken(user);
      // Send email
      emailService.sendRegistrationEmail(user.getEmail(), user);
      User savedUser = userRepository.save(user);
      return UserMapper.mapToUserDto(savedUser);
   }

   public UserDto getUserByEmail(String email) {
      User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received email: " + email));
      return UserMapper.mapToUserDto(user);
   }

   public UserDto getUserById(Long userId) {
      User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + userId));
      return UserMapper.mapToUserDto(user);
   }

   /**
    * Retrieves a list of all users from the database.
    *
    * @return A list of {@link UserDto} objects representing all users in the
    *         database. This list can be empty.
    */
   public List<UserDto> getAllUsers() {
      List<User> users = userRepository.findAll();
      return users.stream().map(UserMapper::mapToUserDto).collect(Collectors.toList());
   }

   public void updatePassword(Long userId, String password) {
      User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + userId));
      user.setPassword(password);
      userRepository.save(user);
   }
}
