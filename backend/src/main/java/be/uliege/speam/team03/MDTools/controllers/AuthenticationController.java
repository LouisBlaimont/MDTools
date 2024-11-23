package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.services.AuthenticationService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/auth")
public class AuthenticationController {
   private final AuthenticationService authService;
   
   @PostMapping("/forgot-password")
   public ResponseEntity<String> forgotPassword(@RequestBody UserDto userDto) {
      if(userDto.getEmail() == null) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide email");
      }
      authService.forgotPassword(userDto.getEmail());
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
   }
}
