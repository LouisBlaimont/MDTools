package be.uliege.speam.team03.MDTools.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.services.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserService userService;

    @Value("${app.allowed-origin}")
    private String frontendUrl;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }
    
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
            UserDto user = userService.getUserByEmail(oidcUser.getEmail());

            if (user == null) {
                throw new BadRequestException("User not found in the database.");
            }

            return ResponseEntity.ok(Map.of(
                "id", user.getId() != null ? user.getId() : "",
                "username", user.getUsername() != null ? user.getUsername() : "",
                "name", oidcUser.getFullName(),
                "email", oidcUser.getEmail(),
                "jobPosition", user.getJobPosition() != null ? user.getJobPosition() : "",
                "workplace", user.getWorkplace() != null ? user.getWorkplace() : "",
                "roleName", user.getRoleName() != null ? user.getRoleName() : "",
                "roles", oidcUser.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority).toList(),
                "expiresAt", oidcUser.getExpiresAt().toString()
            ));
        }

        throw new BadRequestException();
    }

    @GetMapping("/logout-url")
    public ResponseEntity<Map<String, String>> getLogoutUrl(@AuthenticationPrincipal OidcUser oidcUser) {
        String issuer = oidcUser.getIdToken().getIssuer().toString();
        String idToken = oidcUser.getIdToken().getTokenValue();

        String endSessionEndpoint = issuer + "/protocol/openid-connect/logout";
        String postLogoutRedirect = frontendUrl + "/?logout=success";

        String logoutUrl = endSessionEndpoint
            + "?id_token_hint=" + URLEncoder.encode(idToken, StandardCharsets.UTF_8)
            + "&post_logout_redirect_uri=" + URLEncoder.encode(postLogoutRedirect, StandardCharsets.UTF_8);

        return ResponseEntity.ok(Map.of("logoutUrl", logoutUrl));
    }
}
