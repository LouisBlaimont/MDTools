package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.models.Authority;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2UserService oAuth2UserService;

    // Helper method to create a mock ID token
    private OidcIdToken mockIdToken() {
        return new OidcIdToken(
            "mock-token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("sub", "12345")
        );
    }

    @Test
    void processOAuth2User_NewUser_RegistersUser() throws Exception {
        OidcUserInfo userInfo = OidcUserInfo.builder()
                .email("new.user@example.com")
                .claim("name", "New User")
                .build();
        
        OidcUser oidcUser = new DefaultOidcUser(
            Collections.emptyList(),
            mockIdToken(),
            userInfo,
            "sub"
        );

        when(userRepository.findByEmail("new.user@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Method processMethod = OAuth2UserService.class.getDeclaredMethod(
                "processOAuth2User", OidcUserRequest.class, OidcUser.class);
        processMethod.setAccessible(true);
        OidcUser result = (OidcUser) processMethod.invoke(
                oAuth2UserService, mock(OidcUserRequest.class), oidcUser);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("New User", savedUser.getUsername());
        assertEquals("new.user@example.com", savedUser.getEmail());
        assertNotNull(result);
    }

    @Test
    void processOAuth2User_ExistingUser_UpdatesUser() throws Exception {
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setUsername("Existing User");

        OidcUserInfo userInfo = OidcUserInfo.builder()
                .email("existing@example.com")
                .claim("name", "Updated User")
                .build();
        
        OidcUser oidcUser = new DefaultOidcUser(
            Collections.emptyList(),
            mockIdToken(),
            userInfo,
            "sub"
        );

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        Method processMethod = OAuth2UserService.class.getDeclaredMethod(
                "processOAuth2User", OidcUserRequest.class, OidcUser.class);
        processMethod.setAccessible(true);
        OidcUser result = (OidcUser) processMethod.invoke(
                oAuth2UserService, mock(OidcUserRequest.class), oidcUser);

        verify(userRepository).save(existingUser);
        assertEquals("Updated User", existingUser.getUsername());
        assertNotNull(result);
    }

    @Test
    void createOidcUserWithAuthorities_IncludesUserAuthorities() throws Exception {
        User user = new User();
        Set<Authority> authority = new HashSet<>();
        authority.add(new Authority("ROLE_USER"));
        user.setAuthorities(authority);
    
        OidcUser oidcUser = new DefaultOidcUser(
            Collections.emptyList(),
            mockIdToken(),
            OidcUserInfo.builder().claim("test", "test").build(),
            "sub"
        );
    
        Method createMethod = OAuth2UserService.class.getDeclaredMethod(
                "createOidcUserWithAuthorities", OidcUser.class, User.class);
        createMethod.setAccessible(true);
        OidcUser result = (OidcUser) createMethod.invoke(oAuth2UserService, oidcUser, user);
    
        assertTrue(result.getAuthorities().contains((GrantedAuthority) new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals("12345", ((DefaultOidcUser) result).getSubject());
    }
}