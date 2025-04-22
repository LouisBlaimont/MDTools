package be.uliege.speam.team03.MDTools.services;

import java.util.Collection;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Service
@Slf4j
public class OAuth2UserService extends OidcUserService {

   private final UserRepository userRepository;

   @Override
   @SneakyThrows
   public OidcUser loadUser(OidcUserRequest userRequest) {
      log.info("OAuth2UserRequest: " + userRequest.toString());
      OidcUser oidcUser = super.loadUser(userRequest);
      return processOAuth2User(userRequest, oidcUser);
   }

   private OidcUser processOAuth2User(OidcUserRequest userRequest, OidcUser oidcUser) {
      OidcUserInfo oidcUserInfo = oidcUser.getUserInfo();
      Optional<User> userOptional = userRepository.findByEmail(oidcUserInfo.getEmail());
      User user = userOptional
            .map(existingUser -> updateExistingUser(existingUser, oidcUserInfo))
            .orElseGet(() -> registerNewUser(userRequest, oidcUserInfo));
      return createOidcUserWithAuthorities(oidcUser, user);
   }

   private OidcUser createOidcUserWithAuthorities(OidcUser oidcUser, User user) {
      // Assuming user roles or authorities are available. You can change this to reflect your model.
      Collection<GrantedAuthority> authorities = user.getAuthorities();
      
      // Create a new OidcUser with authorities
      return new DefaultOidcUser(
         authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), "sub"
      );
   }

   private User registerNewUser(OidcUserRequest userRequest, OidcUserInfo oidcUserInfo) {
      User user = new User();
      user.setUsername(oidcUserInfo.getFullName());
      user.setEmail(oidcUserInfo.getEmail());
      return userRepository.save(user);
   }

   private User updateExistingUser(User existingUser, OidcUserInfo oidcUserInfo) {
      existingUser.setUsername(oidcUserInfo.getFullName());
      existingUser.setEmail(oidcUserInfo.getEmail());
      return userRepository.save(existingUser);
   }
}
