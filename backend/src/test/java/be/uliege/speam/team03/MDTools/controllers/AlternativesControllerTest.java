package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
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

import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.AlternativeService;

@WebMvcTest(AlternativesController.class)
@Import(TestSecurityConfig.class)
public class AlternativesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlternativeService alternativeService;

    private List<InstrumentDTO> instrumentDTOs;
    private List<AlternativeReferenceDTO> alternativeReferenceDTOs;

    @BeforeEach
    public void setup() {
        // Initialize test data
        instrumentDTOs = new ArrayList<>();

        InstrumentDTO instrument1 = new InstrumentDTO();
        instrument1.setId(1L);
        instrument1.setReference("REF001");
        instrument1.setSupplierDescription("Instrument 1");

        InstrumentDTO instrument2 = new InstrumentDTO();
        instrument2.setId(2L);
        instrument2.setReference("REF002");
        instrument2.setSupplierDescription("Instrument 2");

        instrumentDTOs.add(instrument1);
        instrumentDTOs.add(instrument2);

        // Alternative references
        alternativeReferenceDTOs = new ArrayList<>();

        AlternativeReferenceDTO altRef = new AlternativeReferenceDTO();
        altRef.setReference_1("REF001");
        altRef.setReference_2("REF002");

        alternativeReferenceDTOs.add(altRef);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfInstrUser() throws Exception {
        // Given
        when(alternativeService.findAlternativesUser(1L)).thenReturn(instrumentDTOs);

        // When & Then
        mockMvc.perform(get("/api/alternatives/user/instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reference").value("REF001"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].reference").value("REF002"));

        verify(alternativeService, times(1)).findAlternativesUser(1L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfInstrUser_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.findAlternativesUser(anyLong()))
                .thenThrow(new ResourceNotFoundException("Instrument not found"));

        // When & Then
        mockMvc.perform(get("/api/alternatives/user/instrument/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfInstrAdmin() throws Exception {
        // Given
        when(alternativeService.findAlternativesAdmin(1L)).thenReturn(instrumentDTOs);

        // When & Then
        mockMvc.perform(get("/api/alternatives/admin/instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(alternativeService, times(1)).findAlternativesAdmin(1L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfInstrAdmin_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.findAlternativesAdmin(anyLong()))
                .thenThrow(new ResourceNotFoundException("Instrument not found"));

        // When & Then
        mockMvc.perform(get("/api/alternatives/admin/instrument/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfCategoryUser() throws Exception {
        // Given
        when(alternativeService.findAlternativesOfCategoryUser(1L)).thenReturn(instrumentDTOs);

        // When & Then
        mockMvc.perform(get("/api/alternatives/user/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(alternativeService, times(1)).findAlternativesOfCategoryUser(1L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfCategoryUser_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.findAlternativesOfCategoryUser(anyLong()))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(get("/api/alternatives/user/category/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfCategoryAdmin() throws Exception {
        // Given
        when(alternativeService.findAlternativesOfCategoryAdmin(1L)).thenReturn(instrumentDTOs);

        // When & Then
        mockMvc.perform(get("/api/alternatives/admin/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(alternativeService, times(1)).findAlternativesOfCategoryAdmin(1L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAlternativesOfCategoryAdmin_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.findAlternativesOfCategoryAdmin(anyLong()))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        // When & Then
        mockMvc.perform(get("/api/alternatives/admin/category/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testGetAllAlternatives() throws Exception {
        // Given
        when(alternativeService.findAllAlternativesReferences()).thenReturn(alternativeReferenceDTOs);

        // When & Then
        mockMvc.perform(get("/api/alternatives/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].reference_1").value("REF001"))
                .andExpect(jsonPath("$[0].reference_2").value("REF002"));

        verify(alternativeService, times(1)).findAllAlternativesReferences();
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testDeleteAlternativeFromCategory() throws Exception {
        // Given
        when(alternativeService.removeAlternativeFromCategory(1L, 2L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/alternatives/2/category/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(alternativeService, times(1)).removeAlternativeFromCategory(1L, 2L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testDeleteAlternativeFromCategory_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.removeAlternativeFromCategory(anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("Resource not found"));

        // When & Then
        mockMvc.perform(delete("/api/alternatives/999/category/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testAddAlternative() throws Exception {
        // Given
        when(alternativeService.addAlternative(1L, 2L)).thenReturn(instrumentDTOs);

        // When & Then
        mockMvc.perform(post("/api/alternatives")
                .param("instr1", "1")
                .param("instr2", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(alternativeService, times(1)).addAlternative(1L, 2L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testAddAlternative_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.addAlternative(anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("Instrument not found"));

        // When & Then
        mockMvc.perform(post("/api/alternatives")
                .param("instr1", "999")
                .param("instr2", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testAddAlternative_BadRequest() throws Exception {
        // Given
        when(alternativeService.addAlternative(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("Alternatives can't have the same supplier"));

        // When & Then
        mockMvc.perform(post("/api/alternatives")
                .param("instr1", "1")
                .param("instr2", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testDeleteAlternative() throws Exception {
        // Given
        when(alternativeService.removeAlternative(1L, 2L)).thenReturn(instrumentDTOs);

        // When & Then
        mockMvc.perform(delete("/api/alternatives/2/instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(alternativeService, times(1)).removeAlternative(1L, 2L);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testDeleteAlternative_ResourceNotFound() throws Exception {
        // Given
        when(alternativeService.removeAlternative(anyLong(), anyLong()))
                .thenThrow(new ResourceNotFoundException("Instrument not found"));

        // When & Then
        mockMvc.perform(delete("/api/alternatives/999/instrument/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = { "USER" })
    public void testDeleteAlternative_BadRequest() throws Exception {
        // Given
        when(alternativeService.removeAlternative(anyLong(), anyLong()))
                .thenThrow(new BadRequestException("This alternative doesn't exist"));

        // When & Then
        mockMvc.perform(delete("/api/alternatives/2/instrument/1"))
                .andExpect(status().isBadRequest());
    }
}