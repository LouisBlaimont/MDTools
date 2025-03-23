package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
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

import be.uliege.speam.team03.MDTools.DTOs.UserDto;
import be.uliege.speam.team03.MDTools.services.UserService;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testGetUserById() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");

        when(userService.getUserById(1L)).thenReturn(userDto);

        mockMvc.perform(get("/api/user")
                .param("user_id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void testGetUserByEmail() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("test@example.com");

        when(userService.getUserByEmail("test@example.com")).thenReturn(userDto);

        mockMvc.perform(get("/api/user")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    @Test
    public void testGetUserBadRequest() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUserRoles() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setRoles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"));

        when(userService.updateUserRoles(1L, Arrays.asList("ROLE_ADMIN", "ROLE_USER"))).thenReturn(userDto);

        mockMvc.perform(patch("/api/user/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"ROLE_ADMIN\", \"ROLE_USER\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("ROLE_USER"));

        verify(userService, times(1)).updateUserRoles(1L, Arrays.asList("ROLE_ADMIN", "ROLE_USER"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        UserDto userDto1 = new UserDto();
        userDto1.setId(1L);
        userDto1.setEmail("test1@example.com");

        UserDto userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setEmail("test2@example.com");

        List<UserDto> users = Arrays.asList(userDto1, userDto2);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/user/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].email").value("test1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].email").value("test2@example.com"));

        verify(userService, times(1)).getAllUsers();
    }
}