package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.UserAlreadyExistsException;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        user.setUsername("testuser");
        user.setAuthorities(Set.of(new Authority("USER")));
        user.setJobPosition("Developer");
        user.setWorkplace("Company XYZ");
        user.setRoleName("Team Lead");
        user.setCreatedAt(Timestamp.from(Instant.now()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        user.setEnabled(true);

        userDto = UserMapper.toDto(user);
    }

    @Test
    // Test to verify successful retrieval of a user by email when the user exists.
    void getUserByEmail_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserByEmail(user.getEmail());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    // Test to verify behavior when a user is not found by email.
    void getUserByEmail_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail(user.getEmail()));
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    // Test to verify successful retrieval of a user by ID when the user exists.
    void getUserById_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(user.getUserId());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(user.getUserId());
    }

    @Test
    // Test to verify behavior when a user is not found by ID.
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(user.getUserId()));
        verify(userRepository, times(1)).findById(user.getUserId());
    }

    @Test
    // Test to verify successful retrieval of a user ID by email when the user exists.
    void getUserIdByEmail_ShouldReturnUserId_WhenUserExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        Long result = userService.getUserIdByEmail(user.getEmail());

        assertEquals(user.getUserId(), result);
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    // Test to verify behavior when a user ID is not found by email.
    void getUserIdByEmail_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserIdByEmail(user.getEmail()));
        verify(userRepository, times(1)).findByEmail(user.getEmail());
    }

    @Test
    // Test to verify successful retrieval of all users.
    void getAllUsers_ShouldReturnListOfUserDtos() {
        Page<User> users = new PageImpl<>(Arrays.asList(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(users);

        Page<UserDto> result = userService.getAllUsers(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(user.getEmail(), result.getContent().get(0).getEmail());
        verify(userRepository, times(1)).findAll(Pageable.unpaged());
    }

    @Test
    // Test to verify successful update of user roles when the user exists.
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

        Set<String> authorityNames = user.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet());

        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_USER"));
        assertEquals(2, authorityNames.size());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        assertTrue(result.getRoles().contains("ROLE_USER"));
        assertEquals(2, result.getRoles().size());
    }

    @Test
    // Test to verify behavior when updating roles for a non-existent user.
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
    // Test to verify behavior when updating roles with an empty list, applying the user role.
    void updateUserRoles_withEmptyRoles_setsDefaultRole() {
        // Given
        Long userId = 1L;
        List<String> roles = List.of("ROLE_USER"); // Default role
        User user = new User();
        user.setUserId(userId);
        user.setAuthorities(Set.of(new Authority("ROLE_ADMIN"), new Authority("ROLE_USER"))); // Initial roles

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        UserDto result = userService.updateUserRoles(userId, roles);

        // Then
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);

        Set<String> authorityNames = user.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet());
        
        // Check if default ROLE_USER is applied
        assertTrue(authorityNames.contains("ROLE_USER"));
        assertEquals(1, user.getAuthorities().size());
    }

    @Test
    // Test to verify successful registration of a new user with valid data.
    void registerUser_ShouldCreateNewUser_WhenValidDataProvided() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("newuser@example.com");
        userDto.setUsername("newuser");
        
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUserId(2L); // Simulate auto-generated ID
            return savedUser;
        });

        // When
        UserDto result = userService.registerUser(userDto);

        // Then
        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        assertEquals("newuser", result.getUsername());
        assertTrue(result.isEnabled());
        assertEquals(List.of("ROLE_USER"), result.getRoles());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository).save(any(User.class));
    }

    @Test
    // Test to verify behavior when registering a user with an invalid email.
    void registerUser_ShouldThrowBadRequestException_WhenEmailIsInvalid() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("invalid-email");
        userDto.setUsername("newuser");

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.registerUser(userDto));
        verifyNoInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when registering a user with an email that already exists.
    void registerUser_ShouldThrowBadRequestException_WhenEmailAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("existing@example.com");
        userDto.setUsername("newuser");
        
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.registerUser(userDto));
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    // Test to verify behavior when registering a user with a username that already exists.
    void registerUser_ShouldThrowBadRequestException_WhenUsernameAlreadyExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("new@example.com");
        userDto.setUsername("existinguser");
        
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.registerUser(userDto));
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).findByUsername("existinguser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    // Test to verify successful update of a user by ID with valid data.
    void updateUser_WithValidIdAndData_ShouldUpdateAndReturnUser() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setEmail("updated@example.com");
        updateData.setJobPosition("Senior Developer");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserDto result = userService.updateUser(userId, updateData);

        // Then
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("Senior Developer", user.getJobPosition());
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail("updated@example.com");
        verify(userRepository).save(user);
    }

    @Test
    // Test to verify successful update of a user by username with valid data.
    void updateUser_WithValidUsernameAndData_ShouldUpdateAndReturnUser() {
        // Given
        String username = "testuser";
        UserDto updateData = new UserDto();
        updateData.setEmail("updated@example.com");
        updateData.setWorkplace("New Company");
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        UserDto result = userService.updateUser(username, updateData);

        // Then
        assertEquals("updated@example.com", user.getEmail());
        assertEquals("New Company", user.getWorkplace());
        verify(userRepository).findByUsername(username);
        verify(userRepository).findByEmail("updated@example.com");
        verify(userRepository).save(user);
    }

    @Test
    // Test to verify behavior when updating a user with a null identifier.
    void updateUser_WithNullIdentifier_ShouldThrowBadRequestException() {
        // Given
        UserDto updateData = new UserDto();
        Object nullIdentifier = null;

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(nullIdentifier, updateData));
        verifyNoInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with an empty string identifier.
    void updateUser_WithEmptyStringIdentifier_ShouldThrowBadRequestException() {
        // Given
        UserDto updateData = new UserDto();
        String emptyIdentifier = "";

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(emptyIdentifier, updateData));
        verifyNoInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with an invalid identifier type.
    void updateUser_WithInvalidIdentifierType_ShouldThrowBadRequestException() {
        // Given
        UserDto updateData = new UserDto();
        Object invalidIdentifier = 3.14; // Neither Long nor String

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(invalidIdentifier, updateData));
        verifyNoInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with a non-existent ID.
    void updateUser_WithNonExistentId_ShouldThrowResourceNotFoundException() {
        // Given
        Long nonExistentId = 999L;
        UserDto updateData = new UserDto();
        
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(nonExistentId, updateData));
        verify(userRepository).findById(nonExistentId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with a non-existent username.
    void updateUser_WithNonExistentUsername_ShouldThrowResourceNotFoundException() {
        // Given
        String nonExistentUsername = "nonexistent";
        UserDto updateData = new UserDto();
        
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(nonExistentUsername, updateData));
        verify(userRepository).findByUsername(nonExistentUsername);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with an invalid email.
    void updateUser_WithInvalidEmail_ShouldThrowBadRequestException() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setEmail("invalid-email");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, updateData));
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with an email that already exists.
    void updateUser_WithAlreadyExistingEmail_ShouldThrowBadRequestException() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setUserId(2L); // Different ID
        existingUser.setEmail("existing@example.com");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, updateData));
        verify(userRepository).findById(userId);
        verify(userRepository).findByEmail("existing@example.com");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with an invalid username.
    void updateUser_WithInvalidUsername_ShouldThrowBadRequestException() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setUsername("invalid username with spaces");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, updateData));
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with a username that already exists.
    void updateUser_WithAlreadyExistingUsername_ShouldThrowBadRequestException() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setUsername("existinguser");

        User existingUser = new User();
        existingUser.setUserId(2L); // Different ID
        existingUser.setUsername("existinguser");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, updateData));
        verify(userRepository).findById(userId);
        verify(userRepository).findByUsername("existinguser");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when updating a user with an invalid role name.
    void updateUser_WithInvalidRoleName_ShouldThrowBadRequestException() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setRoleName("invalid role name with spaces");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(BadRequestException.class, () -> userService.updateUser(userId, updateData));
        verify(userRepository).findById(userId);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    // Test to verify behavior when enabling a user with no roles.
    void updateUser_WithEnableAndNoRoles_ShouldReturnAuthorities() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setEnabled(true);

        User userWithNoRoles = new User();
        userWithNoRoles.setUserId(userId);
        userWithNoRoles.setAuthorities(Collections.emptySet());
        
        // When & Then
        assertNotNull(userWithNoRoles.getAuthorities());
        assertEquals(1, userWithNoRoles.getAuthorities().size());
        assertTrue(userWithNoRoles.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    // Test to verify successful update of user roles.
    void updateUser_WithRoles_ShouldUpdateAuthorities() {
        // Given
        Long userId = 1L;
        UserDto updateData = new UserDto();
        updateData.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        // When
        UserDto result = userService.updateUser(userId, updateData);

        Set<String> authorityNames = user.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
            .collect(Collectors.toSet());

        // Then
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
        assertTrue(authorityNames.contains("ROLE_USER"));
        assertEquals(2, user.getAuthorities().size());
        verify(userRepository).save(user);
    }

    @Test
    // Test to verify successful deletion of a user when the user exists.
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // When
        userService.deleteUser(username);

        // Then
        verify(userRepository).findByUsername(username);
        verify(userRepository).delete(user);
    }

    @Test
    // Test to verify behavior when deleting a user that does not exist.
    void deleteUser_WhenUserDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(username));
        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    // Test to verify that a valid email is correctly identified.
    void isValidEmail_WithValidEmail_ReturnsTrue() {
        // Given
        String validEmail = "test@example.com";

        // When
        boolean result = userService.isValidEmail(validEmail);

        // Then
        assertTrue(result);
    }

    @Test
    // Test to verify that invalid emails are correctly identified.
    void isValidEmail_WithInvalidEmail_ReturnsFalse() {
        // Test cases for various invalid email formats
        String[] invalidEmails = {
            "invalid-email",
            "test@",
            "@example.com",
            "test@example",
            "test@.com",
            "test@example..com",
            " test@example.com",
            "test@example.com ",
            null
        };

        for (String invalidEmail : invalidEmails) {
            // When
            boolean result = userService.isValidEmail(invalidEmail);

            // Then
            assertFalse(result, "Email should be invalid: " + invalidEmail);
        }
    }
}