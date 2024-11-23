package be.uliege.speam.team03.MDTools.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
   private final PasswordUtils passwordUtils;
   private final UserService userService;

   @Value("${security.token.expiry-hours}")
   private int tokenExpiryHours;

   public boolean authenticate(String plainPassword, String storedPswdFingeprint) {
      return passwordUtils.verifyPassword(plainPassword, storedPswdFingeprint);
   }

   public User forgotPassword(String email) {
      Pair<String, LocalDateTime> resetToken = generateResetToken();
      return userService.setResetToken(email, resetToken.getFirst(), resetToken.getSecond());
   }


   /**
    * Updates the password of a user in the database.
    *
    * @param plainPassword The new password in plain text. This will be hashed
    *                      before being saved to the database.
    * @param userId        The unique identifier of the user whose password needs
    *                      to be updated.
    *
    * @throws ResourceNotFoundException If the user with the given ID does not
    *                                   exist in the database.
    */
   public void overwritePassword(String plainPassword, Long userId) {
      // Hash the password
      String hashedPassword = passwordUtils.hashPassword(plainPassword);
      // Save hashedPassword to the database
      userService.updatePassword(userId, hashedPassword);
   }

   public Pair<String, LocalDateTime> generateResetToken() {
      String token = passwordUtils.generateToken();
      LocalDateTime expiration = LocalDateTime.now().plusHours(tokenExpiryHours);
      return Pair.of(token, expiration);
   }
   
}
