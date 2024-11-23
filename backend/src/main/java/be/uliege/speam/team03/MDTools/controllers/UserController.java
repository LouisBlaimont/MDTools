package be.uliege.speam.team03.MDTools.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.services.AuthenticationService;
import be.uliege.speam.team03.MDTools.services.EmailService;
import be.uliege.speam.team03.MDTools.services.UserService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
public class UserController {
   private final UserService userService;
   private final AuthenticationService authService;
   private final EmailService emailService;

   // Add User REST API
   @PostMapping
   public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
      if(userDto.getEmail() == null) {
         throw new BadRequestException("Please provide email");
      }
      Pair<String, LocalDateTime> resetToken = authService.generateResetToken();
      UserDto savedUser = userService.createUser(userDto);

      User user = userService.setResetToken(savedUser.getEmail(), resetToken.getFirst(), resetToken.getSecond());
      emailService.sendRegistrationEmail(user.getEmail(), user);

      return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
   }

   @GetMapping
   public ResponseEntity<UserDto> getUser(@RequestParam(required = false) Long user_id,
         @RequestParam(required = false) String email) {

      if (user_id != null) {
         // Fetch user by ID
         UserDto user = userService.getUserById(user_id);
         return ResponseEntity.ok(user);
      } else if (email != null) {
         // Fetch user by email
         UserDto user = userService.getUserByEmail(email);
         return ResponseEntity.ok(user);
      } else {
         throw new BadRequestException("Please provide either user_id or email");
      }
   }

   @GetMapping("/list")
   public ResponseEntity<List<UserDto>> getAllUser() {
      List<UserDto> users = userService.getAllUsers();
      return new ResponseEntity<>(users, HttpStatus.OK);
   }
}
