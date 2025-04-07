package be.uliege.speam.team03.MDTools.controllers;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    private AuthenticationController authController;

    @Mock
    private OidcIdToken idToken;

    private OidcUser oidcUser;
    private Object nonOidcUser;

    @BeforeEach
    void setUp() {
        authController = new AuthenticationController();
        
        // Create authorities
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        
        // Create claims for OidcIdToken
        Map<String, Object> claims = Map.of(
            "sub", "12345",
            "email", "test@example.com", 
            "name", "Test User",
            "exp", Instant.now().plusSeconds(3600).getEpochSecond()
        );
        
        // Create OidcIdToken
        idToken = new OidcIdToken(
            "tokenValue", 
            Instant.now(), 
            Instant.now().plusSeconds(3600), 
            claims
        );
        
        // Create OidcUser with proper claims
        oidcUser = new DefaultOidcUser(authorities, idToken);
        
        // Create a non-OidcUser object
        nonOidcUser = new Object();
    }

    @Test
    void testGetAuthenticatedUser_WithOidcUser_ReturnsUserInfo() throws BadRequestException {
        // Act
        ResponseEntity<Map<String, Object>> response = authController.getAuthenticatedUser(oidcUser);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals("Test User", responseBody.get("name"));
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) responseBody.get("roles");
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_USER"));
        assertTrue(roles.contains("ROLE_ADMIN"));
        
        assertNotNull(responseBody.get("expiresAt"));
    }

    @Test
    void testGetAuthenticatedUser_WithNonOidcUser_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            authController.getAuthenticatedUser(nonOidcUser);
        });
    }
    
    @Test
    void testGetAuthenticatedUser_WithNullAuthentication_ThrowsBadRequestException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            authController.getAuthenticatedUser(null);
        });
    }
}