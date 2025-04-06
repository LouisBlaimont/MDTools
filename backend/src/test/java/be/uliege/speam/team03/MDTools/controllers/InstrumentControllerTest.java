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

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;

@ExtendWith(MockitoExtension.class)
public class InstrumentControllerTest {

    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private InstrumentController instrumentController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(instrumentController).build();
    }

    @Test
    public void testFindInstrumentById() throws Exception {
        InstrumentDTO instrument = new InstrumentDTO();
        instrument.setId(1);
        instrument.setReference("REF001");

        when(instrumentService.findById(1)).thenReturn(instrument);

        mockMvc.perform(get("/api/instrument/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.reference").value("REF001"));

        verify(instrumentService, times(1)).findById(1);
    }

    // @Test
    // public void testAddInstrument() throws Exception {
    //     InstrumentDTO newInstrument = new InstrumentDTO();
    //     newInstrument.setReference("REF001");
    //     newInstrument.setSupplier("Supplier1");
    //     newInstrument.setCategoryId(1);
    //     newInstrument.setPrice(100.0f);

    //     InstrumentDTO savedInstrument = new InstrumentDTO();
    //     savedInstrument.setId(1);
    //     savedInstrument.setReference("REF001");

    //     when(instrumentService.save(any(InstrumentDTO.class))).thenReturn(savedInstrument);

    //     mockMvc.perform(post("/api/instrument")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content("{\"reference\":\"REF001\",\"supplier\":\"Supplier1\",\"categoryId\":1,\"price\":100.0}"))
    //             .andExpect(status().isCreated())
    //             .andExpect(jsonPath("$.id").value(1))
    //             .andExpect(jsonPath("$.reference").value("REF001"));

    //     verify(instrumentService, times(1)).save(any(InstrumentDTO.class));
    // }

    // @Test
    // public void testUpdateInstrument() throws Exception {
    //     InstrumentDTO existingInstrument = new InstrumentDTO();
    //     existingInstrument.setId(1);
    //     existingInstrument.setReference("REF001");

    //     InstrumentDTO updatedInstrument = new InstrumentDTO();
    //     updatedInstrument.setId(1);
    //     updatedInstrument.setReference("REF002");

    //     when(instrumentService.findById(1)).thenReturn(existingInstrument);
    //     when(instrumentService.save(any(InstrumentDTO.class))).thenReturn(updatedInstrument);

    //     mockMvc.perform(patch("/api/instrument/1")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content("{\"reference\":\"REF002\"}"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.id").value(1))
    //             .andExpect(jsonPath("$.reference").value("REF002"));

    //     verify(instrumentService, times(1)).findById(1);
    //     verify(instrumentService, times(1)).save(any(InstrumentDTO.class));
    // }

    @Test
    public void testDeleteInstrument() throws Exception {
        doNothing().when(instrumentService).delete(1);

        mockMvc.perform(delete("/api/instrument/1"))
                .andExpect(status().isNoContent());

        verify(instrumentService, times(1)).delete(1);
    }
}
