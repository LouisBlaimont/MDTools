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

   public boolean canAccessUser(Long userId, String email) {
      if (hasRole("WEBMASTER")) {
         return true; // Webmasters can access all users
      }

      log.error("userId: " + userId);
      log.error("email: " + email);

      return (userId != null && isCurrentUser(userId)) || (email != null && isCurrentUserByEmail(email));
   }

   private boolean isCurrentUser(Long userId) {
      String currentUsername = getCurrentUsername();
      return currentUsername != null && currentUsername.equals(userService.getUserById(userId).getUsername());
   }

   private String getCurrentUsername() {
      Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof OidcUser user) {
         log.info(user.getFullName());
         return user.getFullName();
      }
      return null;
   }

   private boolean isCurrentUserByEmail(String email) {
      String currentUsername = getCurrentUsername();
      return currentUsername != null && currentUsername.equals(userService.getUserByEmail(email).getUsername());
   }

   private boolean hasRole(String role) {
      return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
            .stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
   }
}
