package be.uliege.speam.team03.MDTools.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.services.UserService;
import lombok.AllArgsConstructor;

/**
 * This controller implements the API endpoints relative to the users. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
public class UserController {
   private final UserService userService;

   @GetMapping
   @PreAuthorize("@securityService.canAccessUser(#user_id, #email)")
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

   @PreAuthorize("hasRole('WEBMASTER')")
   @PatchMapping("{id}/roles")
   public ResponseEntity<UserDto> updateUserRoles(@PathVariable Long id, @RequestBody List<String> roles) {
      UserDto updatedUser = userService.updateUserRoles(id, roles);
      return ResponseEntity.ok(updatedUser);
   }   

   @PreAuthorize("hasRole('WEBMASTER')")
   @GetMapping("/list")
   public ResponseEntity<List<UserDto>> getAllUser() {
      List<UserDto> users = userService.getAllUsers();
      return new ResponseEntity<>(users, HttpStatus.OK);
   }
}
