package be.uliege.speam.team03.MDTools.services;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class SecurityService {

   private final UserService userService;

   /**
    * Checking if the user can access a certain user 
    * @param userId id of the user
    * @param email email of the user
    * @return boolean (access or not)
    */
   public boolean canAccessUser(Long userId, String email) {
      if (hasRole("WEBMASTER")) {
         return true; // Webmasters can access all users
      }

      log.error("userId: " + userId);
      log.error("email: " + email);

      return (userId != null && isCurrentUser(userId)) || (email != null && isCurrentUserByEmail(email));
   }

   /**
    * cheching if a user if the current user
    * @param userId id of the user to check
    * @return boolean (is current user or not)
    */
   private boolean isCurrentUser(Long userId) {
      String currentUsername = getCurrentUsername();
      return currentUsername != null && currentUsername.equals(userService.getUserById(userId).getUsername());
   }

   /**
    * retrieves the username of the current user
    * @return  username
    */
   private String getCurrentUsername() {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof OidcUser user) {
         log.info(user.getFullName());
         return user.getFullName();
      }
      return null;
   }

   /**
    * Checking if a user is the current user based on an email
    * @param email email of the user to compare to current user
    * @return boolean (is current or not)
    */
   private boolean isCurrentUserByEmail(String email) {
      String currentUsername = getCurrentUsername();
      return currentUsername != null && currentUsername.equals(userService.getUserByEmail(email).getUsername());
   }

   /**
    * Checking if a user has a certain role
    * @param role role to search
    * @return boolean (has role or not)
    */
   private boolean hasRole(String role) {
      return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
   }
}
