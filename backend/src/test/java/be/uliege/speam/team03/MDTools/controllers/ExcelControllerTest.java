package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.services.ExcelImportService;
import be.uliege.speam.team03.MDTools.utils.JwtTokenUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ExcelController.class)
@TestPropertySource(properties = "security.jwt.secret=your-test-secret")
class ExcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  
    private ExcelImportService excelImportService;

    @MockBean  
    private JwtTokenUtils jwtTokenUtils;

    @MockBean  
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void handleOptions_ShouldReturnOk() throws Exception {
        mockMvc.perform(options("/api/import/excel").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void receiveJsonData_ShouldReturnBadRequest_WhenPayloadIsEmpty() throws Exception {
        ImportRequestDTO emptyRequest = new ImportRequestDTO();
        emptyRequest.setData(Collections.emptyList());

        mockMvc.perform(post("/api/import/excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The received JSON is empty."));
    }

    @WithMockUser(username = "testuser", roles = {"USER"})
    @Test
    void receiveJsonData_ShouldReturnOk_WhenValidPayload() throws Exception {
        ImportRequestDTO validRequest = new ImportRequestDTO();
        validRequest.setImportType("Importer un sous-groupe");
        validRequest.setGroupName("Group A");
        validRequest.setSubGroupName("SubGroup B");
        validRequest.setData(Collections.singletonList(Collections.singletonMap("key", "value")));

        doNothing().when(excelImportService).processImport(any(ImportRequestDTO.class));

        mockMvc.perform(post("/api/import/excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(csrf())) // Ensure CSRF token is included
                .andExpect(status().isOk())
                .andExpect(content().string("Data imported successfully!"));
    }


    @Test
    void receiveJsonData_ShouldReturnInternalServerError_WhenExceptionOccurs() throws Exception {
        ImportRequestDTO validRequest = new ImportRequestDTO();
        validRequest.setImportType("Importer un sous-groupe");
        validRequest.setGroupName("Group A");
        validRequest.setSubGroupName("SubGroup B");
        validRequest.setData(Collections.singletonList(Collections.singletonMap("key", "value")));

        doThrow(new RuntimeException("Test Exception")).when(excelImportService).processImport(any(ImportRequestDTO.class));

        mockMvc.perform(post("/api/import/excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest))
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal error during import."));
    }

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                .csrf(csrf -> csrf.disable())  // Disable CSRF in test environment
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
        }
    }
}
