package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
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
    * Retrieves a user from the database by ID or email.
    *
    * @param user_id The ID of the user to retrieve (optional).
    * @param email The email of the user to retrieve (optional).
    * @return The user with the given ID or email, or a BadRequestException if neither is provided.
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
    * @param dto The user data to register.
    * @return The registered user, or a BAD_REQUEST response if the user already exists.
    */
   @PostMapping
   @ResponseStatus(HttpStatus.CREATED)
   public ResponseEntity<Void> registerUser(@RequestBody UserDto dto) {
      // UserDto newUser = userService.registerUser(dto);
      // if (newUser == null) {
      //    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
      // }
      // return ResponseEntity.ok(newUser);
      throw new BadRequestException("Users should be registered through the authentication service");
   }

   /**
    * Updates a user in the database by username.
    *
    * @param username The username of the user to update.
    * @param userDto The new user data.
    * @return The updated user, or a BAD_REQUEST response if the user does not exist.
    */
   @PatchMapping("username/{username}")
   @ResponseStatus(HttpStatus.OK)
   public ResponseEntity<UserDto> updateUser(@PathVariable String username, @RequestBody UserDto userDto) {
      UserDto updatedUser = userService.updateUser(username, userDto);
      return ResponseEntity.ok(updatedUser);
   }

   /**
    * Updates a user in the database by ID.
    *
    * @param id The ID of the user to update.
    * @param userDto The new user data.
    * @return The updated user, or a BAD_REQUEST response if the user does not exist.
    */
   @PatchMapping("{id}")
   @ResponseStatus(HttpStatus.OK)
   public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
      UserDto updatedUser = userService.updateUser(id, userDto);
      return ResponseEntity.ok(updatedUser);
   }

   /**
    * Deletes a user from the database by username.
    *
    * @param name The username of the user to delete.
    */
   @DeleteMapping("{name}")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void deleteUser(@PathVariable String name) {
      userService.deleteUser(name);
   }

   /**
    * Updates the roles of a user in the database.
    *
    * @param id The ID of the user to update.
    * @param roles The new roles to assign to the user.
    * @return The updated user with the new roles.
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
    * @return A list of all users, or an empty list if no users exist.
    */
   @PreAuthorize("hasRole('WEBMASTER')")
   @GetMapping("/list")
   public ResponseEntity<Page<UserDto>> getAllUser(@SortDefault(sort = "username", 
  direction = Direction.ASC) @PageableDefault(page = 0) Pageable pageable) {
      return ResponseEntity.ok(userService.getAllUsers(pageable));
   }
}
