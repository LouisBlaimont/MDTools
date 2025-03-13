package be.uliege.speam.team03.MDTools.controllers;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationController {
   

   @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getAuthenticatedUser(@AuthenticationPrincipal Object authentication) throws BadRequestException {
        if (authentication instanceof OidcUser oidcUser) {
            return ResponseEntity.ok(Map.of(
                "email", oidcUser.getEmail(),
                "name", oidcUser.getFullName(),
                "roles", oidcUser.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList(),
                "expiresAt", oidcUser.getExpiresAt().toString()
            ));
        } else {
            throw new BadRequestException();
        }
    }

}
