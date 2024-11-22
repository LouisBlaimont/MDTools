package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.Services.UserService;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/user")
public class UserController {
   private UserService userService;

   // Add User REST API
   @PostMapping
   public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
      UserDto savedUser = userService.createUser(userDto);
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
