package be.uliege.speam.team03.MDTools.DTOs;

import java.nio.file.attribute.UserPrincipal;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import be.uliege.speam.team03.MDTools.models.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserFullDTO implements OidcUser {

   private Long id;
   private String username;
   private String email;
   private boolean enabled;
   private Collection<? extends GrantedAuthority> authorities;
   private Map<String, Object> attributes;
   private Map<String, Object> claims;
   String tokenValue;
   Instant issuedAt; 
   Instant expiresAt;

   public UserFullDTO() {
   }

   public UserFullDTO(Long id, String username, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.authorities = authorities;
    }

   public static UserFullDTO create(User user) {
      Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

      return new UserFullDTO(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            authorities);
   }

   public static UserFullDTO create(User user, Map<String, Object> attributes) {
      UserFullDTO userFull = UserFullDTO.create(user);
      userFull.setAttributes(attributes);
      return userFull;
   }

   public String getName() {
      return this.username;
   }

   public OidcUserInfo getUserInfo() {
      return new OidcUserInfo(this.claims);
   }

   public OidcIdToken getIdToken() {
      return new OidcIdToken(tokenValue, issuedAt, expiresAt, this.claims);
   }

}
