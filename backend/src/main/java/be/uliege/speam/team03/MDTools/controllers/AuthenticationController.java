package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.LoginRequestDto;
import be.uliege.speam.team03.MDTools.DTOs.LoginResponseDto;
import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.UnauthorizedException;
import be.uliege.speam.team03.MDTools.services.AuthenticationService;
import be.uliege.speam.team03.MDTools.services.EmailService;
import be.uliege.speam.team03.MDTools.services.UserDetailsServiceImpl;
import be.uliege.speam.team03.MDTools.utils.JwtTokenUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/auth")
public class AuthenticationController {
   private final AuthenticationService authService;
   private final EmailService emailService;

   private final AuthenticationManager authenticationManager;
   private final JwtTokenUtils jwtTokenProvider; // Replace with your JWT implementation
   private final UserDetailsServiceImpl userDetailsService;

   @PostMapping("/forgot-password")
   public ResponseEntity<String> forgotPassword(@RequestBody UserDto userDto) {
      if (userDto.getEmail() == null) {
         return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please provide email");
      }
      authService.forgotPassword(userDto.getEmail());
      return ResponseEntity.ok("Email sent successfully");
   }

   @PostMapping("/login")
   public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
      try {
         // Authenticate user
         Authentication authentication = authenticationManager.authenticate(
               new UsernamePasswordAuthenticationToken(
                     loginRequest.getEmail(),
                     loginRequest.getPassword()));

         SecurityContextHolder.getContext().setAuthentication(authentication);

         // Generate JWT Token
         String token = jwtTokenProvider.generateToken(authentication);

         return ResponseEntity.ok(new LoginResponseDto(token));
      } catch (AuthenticationException ex) {
         throw new UnauthorizedException("Invalid email/password supplied");
      }
   }

}
