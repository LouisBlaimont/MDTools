package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.UserAlreadyExistsException;
import be.uliege.speam.team03.MDTools.mapper.UserMapper;
import be.uliege.speam.team03.MDTools.models.Authority;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setAuthorities(Set.of(new Authority("USER")));

        userDto = UserMapper.toDto(user);
    }

    @Test
    void getUserByEmail_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByEmail(user.getEmail());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void getUserByEmail_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail(user.getEmail()));
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(user.getUserId());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(user.getUserId());
    }

    @Test
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(user.getUserId()));
        verify(userRepository, times(1)).findById(user.getUserId());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDtos() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUserRoles_whenUserExists_updatesRolesAndReturnsDto() {
        // Given
        Long userId = 1L;
        List<String> roles = List.of("ROLE_ADMIN", "ROLE_USER");
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        UserDto result = userService.updateUserRoles(userId, roles);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);

        assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(2, user.getAuthorities().size());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        assertTrue(result.getRoles().contains("ROLE_USER"));
        assertEquals(2, result.getRoles().size());
    }

    @Test
    void updateUserRoles_whenUserNotFound_throwsResourceNotFoundException() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserRoles(userId, List.of("ADMIN")));

        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void updateUserRoles_withEmptyRoles_clearsAuthorities() {
        // Given
        Long userId = 1L;
        List<String> roles = Collections.emptyList();
        User user = new User();
        user.setUserId(userId);
        user.setAuthorities(UserMapper.toAuthorities(List.of("ROLE_ADMIN, ROLE_USER"))); // Initial roles

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        UserDto result = userService.updateUserRoles(userId, roles);

        // Then
        // expected is ROLE_USER
        GrantedAuthority expected = new SimpleGrantedAuthority("ROLE_USER");
        assertTrue(user.getAuthorities().contains(expected));
        assertEquals(1, user.getAuthorities().size());
        assertEquals(roles, result.getRoles());
        verify(userRepository).save(user);
    }
}