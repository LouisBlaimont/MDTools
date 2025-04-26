package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler()) // Add your exception handler if you have one
                .build();
    }

    @Test
    // Test to verify successful retrieval of a user by ID.
    public void testGetUserById() throws Exception {
        // Arrange
        UserDto userDto = createSampleUserDto(1L, "test@example.com", "testUser");
        when(userService.getUserById(1L)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/user")
                .param("user_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testUser"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    // Test to verify behavior when a user is not found by ID.
    public void testGetUserById_NotFound() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User does not exist. Received ID: 999"));

        // Act & Assert
        mockMvc.perform(get("/api/user")
                .param("user_id", "999"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    // Test to verify successful retrieval of a user by email.
    public void testGetUserByEmail() throws Exception {
        // Arrange
        UserDto userDto = createSampleUserDto(1L, "test@example.com", "testUser");
        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/user")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testUser"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    @Test
    // Test to verify behavior when a user is not found by email.
    public void testGetUserByEmail_NotFound() throws Exception {
        // Arrange
        when(userService.getUserByEmail("nonexistent@example.com"))
            .thenThrow(new ResourceNotFoundException("User does not exist. Received email: nonexistent@example.com"));

        // Act & Assert
        mockMvc.perform(get("/api/user")
                .param("email", "nonexistent@example.com"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByEmail("nonexistent@example.com");
    }

    @Test
    // Test to verify behavior when no parameters are provided for user retrieval.
    public void testGetUserBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    // Test to verify successful registration of a new user.
    public void testRegisterUser() throws Exception {
        // Arrange
        UserDto inputDto = createSampleUserDto(null, "new@example.com", "newUser");
        UserDto resultDto = createSampleUserDto(1L, "new@example.com", "newUser");
        
        when(userService.registerUser(any(UserDto.class))).thenReturn(resultDto);

        // Act & Assert
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.username").value("newUser"));

        verify(userService, times(1)).registerUser(any(UserDto.class));
    }

    @Test
    // Test to verify behavior when registering a user with an email that already exists.
    public void testRegisterUser_EmailAlreadyExists() throws Exception {
        // Arrange
        UserDto inputDto = createSampleUserDto(null, "existing@example.com", "newUser");
        
        when(userService.registerUser(any(UserDto.class)))
            .thenThrow(new BadRequestException("User with email existing@example.com already exists."));

        // Act & Assert
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());

        verify(userService, times(1)).registerUser(any(UserDto.class));
    }

    @Test
    // Test to verify successful update of a user by ID.
    public void testUpdateUserById() throws Exception {
        // Arrange
        UserDto inputDto = new UserDto();
        inputDto.setEmail("updated@example.com");
        inputDto.setJobPosition("Developer");
        
        UserDto resultDto = createSampleUserDto(1L, "updated@example.com", "existingUser");
        resultDto.setJobPosition("Developer");
        
        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(resultDto);

        // Act & Assert
        mockMvc.perform(patch("/api/user/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.username").value("existingUser"))
                .andExpect(jsonPath("$.jobPosition").value("Developer"));

        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    // Test to verify successful update of a user by username.
    public void testUpdateUserByUsername() throws Exception {
        // Arrange
        UserDto inputDto = new UserDto();
        inputDto.setEmail("updated@example.com");
        inputDto.setWorkplace("Company XYZ");
        
        UserDto resultDto = createSampleUserDto(1L, "updated@example.com", "existingUser");
        resultDto.setWorkplace("Company XYZ");
        
        when(userService.updateUser(eq("existingUser"), any(UserDto.class))).thenReturn(resultDto);

        // Act & Assert
        mockMvc.perform(patch("/api/user/username/existingUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.username").value("existingUser"))
                .andExpect(jsonPath("$.workplace").value("Company XYZ"));

        verify(userService, times(1)).updateUser(eq("existingUser"), any(UserDto.class));
    }

    @Test
    // Test to verify behavior when updating a user by ID that does not exist.
    public void testUpdateUserById_NotFound() throws Exception {
        // Arrange
        UserDto inputDto = new UserDto();
        inputDto.setEmail("updated@example.com");
        
        when(userService.updateUser(eq(999L), any(UserDto.class)))
            .thenThrow(new ResourceNotFoundException("User does not exist. Received ID: 999"));

        // Act & Assert
        mockMvc.perform(patch("/api/user/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(999L), any(UserDto.class));
    }

    @Test
    // Test to verify successful update of user roles.
    public void testUpdateUserRoles() throws Exception {
        // Arrange
        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_USER");
        UserDto userDto = createSampleUserDto(1L, "test@example.com", "testUser");
        userDto.setRoles(roles);

        when(userService.updateUserRoles(1L, roles)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(patch("/api/user/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(roles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("ROLE_USER"));

        verify(userService, times(1)).updateUserRoles(1L, roles);
    }

    @Test
    // Test to verify successful deletion of a user by username.
    public void testDeleteUser() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser("userToDelete");

        // Act & Assert
        mockMvc.perform(delete("/api/user/userToDelete"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser("userToDelete");
    }

    @Test
    // Test to verify behavior when deleting a user by username that does not exist.
    public void testDeleteUser_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("User does not exist. Received username: nonExistentUser"))
            .when(userService).deleteUser("nonExistentUser");

        // Act & Assert
        mockMvc.perform(delete("/api/user/nonExistentUser"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser("nonExistentUser");
    }

    @Test
    // Test to verify successful retrieval of all users.
    public void testGetAllUsers() throws Exception {
        // Arrange
        UserDto userDto1 = createSampleUserDto(1L, "test1@example.com", "user1");
        UserDto userDto2 = createSampleUserDto(2L, "test2@example.com", "user2");
        List<UserDto> users = Arrays.asList(userDto1, userDto2);

        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("test1@example.com"))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("test2@example.com"))
                .andExpect(jsonPath("$[1].username").value("user2"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    // Test to verify behavior when no users are found.
    public void testGetAllUsers_NoContent() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).getAllUsers();
    }

    // Helper method to create sample user DTOs
    private UserDto createSampleUserDto(Long id, String email, String username) {
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setEmail(email);
        userDto.setUsername(username);
        userDto.setEnabled(true);
        userDto.setRoles(Arrays.asList("ROLE_USER"));
        userDto.setCreatedAt(Timestamp.from(Instant.now()));
        userDto.setUpdatedAt(Timestamp.from(Instant.now()));
        return userDto;
    }
    
    // Mock exception handler class
    public class GlobalExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public org.springframework.http.ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
        
        @ExceptionHandler(BadRequestException.class)
        public org.springframework.http.ResponseEntity<String> handleBadRequestException(BadRequestException ex) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}