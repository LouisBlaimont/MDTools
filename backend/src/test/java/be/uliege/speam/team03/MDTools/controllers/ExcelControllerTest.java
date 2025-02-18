package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.services.ExcelImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ExcelController.class)
@TestPropertySource(properties = "security.jwt.secret=your-test-secret")
class ExcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ExcelImportService excelImportService;

    @InjectMocks
    private ExcelController excelController;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(excelController).build();
    }

    @Test
    void handleOptions_ShouldReturnOk() throws Exception {
        mockMvc.perform(options("/api/import/excel"))
                .andExpect(status().isOk());
    }

    @Test
    void receiveJsonData_ShouldReturnBadRequest_WhenPayloadIsEmpty() throws Exception {
        ImportRequestDTO emptyRequest = new ImportRequestDTO();
        emptyRequest.setData(Collections.emptyList());

        mockMvc.perform(post("/api/import/excel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The received JSON is empty."));
    }

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
                        .content(objectMapper.writeValueAsString(validRequest)))
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
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Internal error during import."));
    }
}
