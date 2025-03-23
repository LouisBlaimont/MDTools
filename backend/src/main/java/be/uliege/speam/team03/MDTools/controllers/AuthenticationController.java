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

/**
 * Controller for handling authentication-related API endpoints.
 * Provides functionality to retrieve information about the currently authenticated user.
 * 
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthenticationController {

    /**
     * Handles the "/me" endpoint to retrieve information about the currently authenticated user.
     *
     * @param authentication The authentication object representing the currently authenticated user.
     *                        This is typically an instance of {@link org.springframework.security.oauth2.core.oidc.user.OidcUser}.
     * @return A {@link ResponseEntity} containing a map with the authenticated user's details:
     *         - "email": The email address of the user.
     *         - "name": The full name of the user.
     *         - "roles": A list of roles/authorities assigned to the user.
     *         - "expiresAt": The expiration time of the user's authentication token.
     * @throws BadRequestException If the authentication object is not an instance of {@link org.springframework.security.oauth2.core.oidc.user.OidcUser}.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getAuthenticatedUser(@AuthenticationPrincipal Object authentication)
            throws BadRequestException {
        if (authentication instanceof OidcUser oidcUser) {
            return ResponseEntity.ok(Map.of(
                    "email", oidcUser.getEmail(),
                    "name", oidcUser.getFullName(),
                    "roles", oidcUser.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority).toList(),
                    "expiresAt", oidcUser.getExpiresAt().toString()));
        } else {
            throw new BadRequestException();
        }
    }

}
