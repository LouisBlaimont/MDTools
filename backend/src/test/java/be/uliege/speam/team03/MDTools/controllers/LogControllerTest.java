package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.uliege.speam.team03.MDTools.DTOs.LogDto;
import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.LogService;

@WebMvcTest(LogController.class)
@Import(TestSecurityConfig.class)
public class LogControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @MockBean
   private LogService logService;

   @Autowired
   private ObjectMapper objectMapper;

   private LogDto logDto1;
   private LogDto logDto2;

   @BeforeEach
   void setUp() {
      logDto1 = new LogDto();
      logDto1.setLogId(1L);
      logDto1.setUserId(101L);
      logDto1.setAction("Login");
      logDto1.setTimestamp("2025-03-22 10:15:30");
      logDto1.setUsername("testUser");

      logDto2 = new LogDto();
      logDto2.setLogId(2L);
      logDto2.setUserId(102L);
      logDto2.setAction("Document Access");
      logDto2.setTimestamp("2025-03-22 11:30:45");
      logDto2.setUsername("testUser2");
   }

   @Test
   @WithMockUser(roles = { "ADMIN" })
   void testCreateLog() throws Exception {
      when(logService.createLog(any(LogDto.class))).thenReturn(logDto1);

      mockMvc.perform(post("/api/logs")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(logDto1)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value(101L))
            .andExpect(jsonPath("$.action").value("Login"))
            .andExpect(jsonPath("$.timestamp").value("2025-03-22 10:15:30.0"));
   }

   @Test
   @WithMockUser(roles = { "ADMIN" })
   void testGetLogById() throws Exception {
      when(logService.getLogById(1L)).thenReturn(logDto1);

      mockMvc.perform(get("/api/logs/1")
            .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.logId").value(1L))
            .andExpect(jsonPath("$.userId").value(101L))
            .andExpect(jsonPath("$.action").value("Login"))
            .andExpect(jsonPath("$.timestamp").value("2025-03-22 10:15:30.0"));
   }

   @Test
   @WithMockUser(roles = { "ADMIN" })
   void testGetAllLogs() throws Exception {
      List<LogDto> logs = Arrays.asList(logDto1, logDto2);
      when(logService.getAllLogs()).thenReturn(logs);

      mockMvc.perform(get("/api/logs/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].logId").value(1L))
            .andExpect(jsonPath("$[0].action").value("Login"))
            .andExpect(jsonPath("$[1].logId").value(2L))
            .andExpect(jsonPath("$[1].action").value("Document Access"));
   }

   @Test
   @WithMockUser(roles = { "ADMIN" })
   void testGetLogByIdNotFound() throws Exception {
      when(logService.getLogById(999L)).thenThrow(new ResourceNotFoundException("Log not found"));

      mockMvc.perform(get("/api/logs/999"))
            .andExpect(status().isNotFound());
   }

   @Test
   @WithMockUser(roles = { "ADMIN" })
   void testCreateLogWithInvalidData() throws Exception {
      LogDto invalidLog = new LogDto();
      // Missing required fields

      mockMvc.perform(post("/api/logs")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidLog)))
            .andExpect(status().isBadRequest());
   }
}