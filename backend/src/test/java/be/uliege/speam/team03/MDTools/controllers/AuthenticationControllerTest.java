package be.uliege.speam.team03.MDTools.controllers;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.services.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authController;

    @Mock
    private OidcIdToken idToken;

    @Mock
    private UserService userService;

    private OidcUser oidcUser;
    private Object nonOidcUser;

    @BeforeEach
    void setUp() {
        
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
            "jobPosition", "Developer",
            "workplace", "Company XYZ",
            "roleName", "Admin",
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
        // Arrange
        when(userService.getUserByEmail("test@example.com")).thenReturn(
            new UserDto(
                123L, 
                "Test User", 
                "test@example.com", 
                "Developer", 
                "Company XYZ", 
                "Admin", 
                List.of("ROLE_USER", "ROLE_ADMIN"), 
                true, 
                Timestamp.valueOf("2025-01-01 12:00:00"), 
                Timestamp.valueOf("2025-04-01 12:00:00")
            )
        );

        // Act
        ResponseEntity<Map<String, Object>> response = authController.getAuthenticatedUser(oidcUser);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(123L, responseBody.get("id"));
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals("Test User", responseBody.get("name"));
        assertEquals("Developer", responseBody.get("jobPosition"));
        assertEquals("Company XYZ", responseBody.get("workplace"));
        assertEquals("Admin", responseBody.get("roleName"));

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