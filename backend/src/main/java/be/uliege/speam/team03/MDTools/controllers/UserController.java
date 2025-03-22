package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
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

   /**
    * Retrieves a user from the database.
    *
    * @param user_id The ID of the user to retrieve.
    * @param email The email of the user to retrieve.
    * @return The user with the given ID or email.
    */
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

   /**
    * Registers a new user in the database.
    *
    * @param body The user to register.
    * @return The registered user.
    */
   @PostMapping("/add")
   @ResponseStatus(HttpStatus.CREATED)
   public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> body) {
      UserDto newUser = userService.registerUser(body);
      if (newUser == null) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
      }
      return ResponseEntity.ok(newUser);
   }

   @PatchMapping("{username}")
   @ResponseStatus(HttpStatus.OK)
   public ResponseEntity<?> updateUser(@PathVariable String username, @RequestBody Map<String, Object> body) {
      UserDto updatedUser = userService.updateUser(username, body);
      if (updatedUser == null) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User does not exist");
      }
      return ResponseEntity.ok(updatedUser);
   }

   /**
    * Deletes a user from the database.
    *
    * @param id The ID of the user to delete.
    */
   @DeleteMapping("{name}")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void deleteUser(@PathVariable String name) {
      userService.deleteUser(name);
   }

   /**
    * Updates the roles of a user.
    *
    * @param id The ID of the user to update.
    * @param roles The new roles of the user.
    * @return The updated user.
    */
   @PreAuthorize("hasRole('WEBMASTER')")
   @PatchMapping("{id}/roles")
   public ResponseEntity<UserDto> updateUserRoles(@PathVariable Long id, @RequestBody List<String> roles) {
      UserDto updatedUser = userService.updateUserRoles(id, roles);
      return ResponseEntity.ok(updatedUser);
   }   

   /**
    * Retrieves a list of all users from the database.
    *
    * @return A list of {@link UserDto} objects representing all users in the
    *         database. This list can be empty.
    */
   @PreAuthorize("hasRole('WEBMASTER')")
   @GetMapping("/list")
   public ResponseEntity<List<UserDto>> getAllUser() {
      List<UserDto> users = userService.getAllUsers();
      return new ResponseEntity<>(users, HttpStatus.OK);
   }
}
