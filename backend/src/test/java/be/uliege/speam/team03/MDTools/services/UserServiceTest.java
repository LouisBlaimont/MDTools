package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.UserAlreadyExistsException;
import be.uliege.speam.team03.MDTools.mapper.UserMapper;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;

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
        user.setPassword("password");

        userDto = UserMapper.mapToUserDto(user);
    }

    @Test
    void createUser_ShouldReturnUserDto_WhenUserDoesNotExist() throws MailException, UserAlreadyExistsException {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowUserAlreadyExistsException_WhenUserExists() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
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
    void updatePassword_ShouldUpdatePassword_WhenUserExists() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        userService.updatePassword(user.getUserId(), "newPassword");

        assertEquals("newPassword", user.getPassword());
        verify(userRepository, times(1)).findById(user.getUserId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updatePassword_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword(user.getUserId(), "newPassword"));
        verify(userRepository, times(1)).findById(user.getUserId());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setResetToken_ShouldSetResetTokenAndExpiration_WhenUserExists() {
        String resetToken = "resetToken";
        LocalDateTime resetTokenExpiration = LocalDateTime.now();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userService.setResetToken(user.getEmail(), resetToken, resetTokenExpiration);

        assertNotNull(result);
        assertEquals(resetToken, result.getResetToken());
        assertEquals(resetTokenExpiration, result.getResetTokenExpiration());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void setResetToken_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        String resetToken = "resetToken";
        LocalDateTime resetTokenExpiration = LocalDateTime.now();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.setResetToken(user.getEmail(), resetToken, resetTokenExpiration));
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }
}