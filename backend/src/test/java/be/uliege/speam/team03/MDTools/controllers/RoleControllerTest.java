package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.services.RoleService;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestSecurityConfig.class)
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    private List<String> authorities;

    

    @BeforeEach
    public void setup() {
        authorities = Arrays.asList("ROLE_ADMIN", "ROLE_USER", "ROLE_WEBMASTER");
        when(roleService.getAllAuthoritiesNames()).thenReturn(authorities);
    }

    @Test
    @WithMockUser(roles = "WEBMASTER")
    public void getAllAuthorities_WithWebmasterRole_ShouldReturnAuthorities() throws Exception {
        mockMvc.perform(get("/api/role/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$[1]").value("ROLE_USER"))
                .andExpect(jsonPath("$[2]").value("ROLE_WEBMASTER"));

        verify(roleService, times(1)).getAllAuthoritiesNames();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllAuthorities_WithUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/role/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(roleService, never()).getAllAuthoritiesNames();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllAuthorities_WithAdminRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/role/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(roleService, never()).getAllAuthoritiesNames();
    }

    @Test
    public void getAllAuthorities_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/role/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(roleService, never()).getAllAuthoritiesNames();
    }
}