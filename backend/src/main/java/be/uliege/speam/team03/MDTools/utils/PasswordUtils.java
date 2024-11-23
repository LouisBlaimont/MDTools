package be.uliege.speam.team03.MDTools.utils;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordUtils {
   private final PasswordEncoder passwordEncoder;

   public PasswordUtils(PasswordEncoder passwordEncoder) {
      this.passwordEncoder = passwordEncoder;
   }

   public String hashPassword(String plainPassword) {
      return passwordEncoder.encode(plainPassword);
   }

   public boolean verifyPassword(String plainPassword, String hashedPassword) {
      return passwordEncoder.matches(plainPassword, hashedPassword);
   }

   public String generateToken() {
      return UUID.randomUUID().toString();
   }
}
