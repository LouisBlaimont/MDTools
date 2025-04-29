package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.UserService;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
    @Test
    // Test to verify behavior when no parameters are provided for user retrieval.
    public void testGetUserBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @WithMockUser
    @Test
    // Test to verify behavior when registering a user - should return bad request since registration is disabled
    public void testRegisterUser() throws Exception {
        // Arrange
        UserDto inputDto = createSampleUserDto(null, "new@example.com", "newUser");
        
        when(userService.registerUser(any(UserDto.class)))
            .thenThrow(new BadRequestException("Users should be registered through the authentication service"));

        // Act & Assert
        mockMvc.perform(post("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser(roles = "WEBMASTER")
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser(roles = "WEBMASTER")
    @Test
    // Test to verify successful retrieval of all users.
    public void testGetAllUsers() throws Exception {
        // Arrange
        UserDto userDto1 = createSampleUserDto(1L, "test1@example.com", "user1");
        UserDto userDto2 = createSampleUserDto(2L, "test2@example.com", "user2");
        List<UserDto> usersList = Arrays.asList(userDto1, userDto2);
        Page<UserDto> usersPage = new PageImpl<>(usersList);

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(usersPage);

        // Act & Assert
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].email").value("test1@example.com"))
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].email").value("test2@example.com"))
                .andExpect(jsonPath("$.content[1].username").value("user2"));

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @WithMockUser(roles = "WEBMASTER")
    @Test
    // Test to verify behavior when no users are found.
    public void testGetAllUsers_NoContent() throws Exception {
        // Arrange
        Page<UserDto> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    // Test security configuration - we need to verify if the methods have appropriate annotations
    public void testSecurityConfiguration() throws Exception {
        // Instead of directly testing access control, let's verify the controller has the right annotations
        boolean updateUserRolesHasPreAuthorize = false;
        boolean getAllUsersHasPreAuthorize = false;
        
        for (java.lang.reflect.Method method : UserController.class.getDeclaredMethods()) {
            if (method.getName().equals("updateUserRoles")) {
                updateUserRolesHasPreAuthorize = method.isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class);
            }
            if (method.getName().equals("getAllUser")) {
                getAllUsersHasPreAuthorize = method.isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class);
            }
        }
        
        // Verify the methods are properly secured
        org.junit.jupiter.api.Assertions.assertTrue(updateUserRolesHasPreAuthorize, 
            "updateUserRoles method should have @PreAuthorize annotation");
        org.junit.jupiter.api.Assertions.assertTrue(getAllUsersHasPreAuthorize, 
            "getAllUser method should have @PreAuthorize annotation");
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
}