package be.uliege.speam.team03.MDTools.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.utils.PasswordUtils;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AuthenticationService {
   private final PasswordUtils passwordUtils;
   private final UserService userService;

   @Value("${security.token.expiry-hours}")
   private int tokenExpiryHours;

   public boolean authenticate(String plainPassword, String storedPswdFingeprint) {
      return passwordUtils.verifyPassword(plainPassword, storedPswdFingeprint);
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

   public User generateResetToken(User user) {
      String token = passwordUtils.generateToken();
      user.setResetToken(token);
      user.setResetTokenExpiration(LocalDateTime.now().plusHours(tokenExpiryHours)); // Token valid for 24 hours
      return user;
   }
   
}
