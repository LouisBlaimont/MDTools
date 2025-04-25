package be.uliege.speam.team03.MDTools.controllers;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SupplierService;

@ExtendWith(MockitoExtension.class)
public class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    @Mock
    private InstrumentService instrumentService;

    @InjectMocks
    private SupplierController supplierController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supplierController).build();
    }

    @Test
    public void testGetInstrumentsOfSupplierSuccess() throws Exception {
        SupplierDTO supplierDto = new SupplierDTO();
        supplierDto.setId((long) 1);
        supplierDto.setName("Supplier A");

        when(supplierService.findSupplierById((long) 1)).thenReturn(supplierDto);

        mockMvc.perform(get("/api/supplier/1/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supplier A"));

        verify(supplierService, times(1)).findSupplierById((long) 1);
    }

    @Test
    public void testGetInstrumentsOfSupplierNotFound() throws Exception {
        when(instrumentService.findInstrumentsBySupplierId((1L))).thenThrow(new ResourceNotFoundException("Supplier with ID 1 not found"));

        mockMvc.perform(get("/api/supplier/1/instruments"))
                .andExpect(status().isNotFound());

        verify(instrumentService, times(1)).findInstrumentsBySupplierId((long) 1);
    }

    @Test
    public void testGetInstrumentsOfSupplierWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/supplier/invalid/instruments"))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).findSupplierById(any(Long.class));
    }

    @Test
    public void testGetSupplierByIdSuccess() throws Exception {
        SupplierDTO supplierDto = new SupplierDTO();
        supplierDto.setId((long) 1);
        supplierDto.setName("Supplier A");

        when(supplierService.findSupplierById((long) 1)).thenReturn(supplierDto);

        mockMvc.perform(get("/api/supplier/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supplier A"));

        verify(supplierService, times(1)).findSupplierById((long) 1);
    }

    @Test
    public void testGetSupplierByIdNotFound() throws Exception {
        when(supplierService.findSupplierById((long) 1)).thenThrow(new ResourceNotFoundException("Supplier with ID 1 not found"));

        mockMvc.perform(get("/api/supplier/1"))
                .andExpect(status().isNotFound());

        verify(supplierService, times(1)).findSupplierById((long) 1);
    }

    @Test
    public void testGetSupplierByIdWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/supplier/invalid"))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).findSupplierById(any(Long.class));
    }
    
    @Test
    public void testGetAllSuppliersSuccess() throws Exception {
        SupplierDTO supplierDto1 = new SupplierDTO();
        supplierDto1.setId((long) 1);
        supplierDto1.setName("Supplier A");

        SupplierDTO supplierDto2 = new SupplierDTO();
        supplierDto2.setId((long) 2);
        supplierDto2.setName("Supplier B");

        List<SupplierDTO> suppliers = Arrays.asList(supplierDto1, supplierDto2);

        when(supplierService.findAllSuppliers()).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Supplier A"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Supplier B"));

        verify(supplierService, times(1)).findAllSuppliers();
    }

    @Test
    public void testGetAllSuppliersEmptyList() throws Exception {
        List<SupplierDTO> suppliers = Arrays.asList();

        when(supplierService.findAllSuppliers()).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(supplierService, times(1)).findAllSuppliers();
    }

    // @Test
    // public void testGetPaginatedSuppliersSuccess() throws Exception {
    //     SupplierDTO supplierDto1 = new SupplierDTO();
    //     supplierDto1.setId((long) 1);
    //     supplierDto1.setName("Supplier A");

    //     SupplierDTO supplierDto2 = new SupplierDTO();
    //     supplierDto2.setId((long) 2);
    //     supplierDto2.setName("Supplier B");

    //     Page<SupplierDTO> suppliers = new org.springframework.data.domain.PageImpl<>(Arrays.asList(supplierDto1, supplierDto2));

    //     when(supplierService.findPaginatedSuppliers(any())).thenReturn(suppliers);

    //     mockMvc.perform(get("/api/supplier?page=0&size=10"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$[0].id").value(1))
    //             .andExpect(jsonPath("$[0].name").value("Supplier A"))
    //             .andExpect(jsonPath("$[1].id").value(2))
    //             .andExpect(jsonPath("$[1].name").value("Supplier B"));

    //     verify(supplierService, times(1)).findPaginatedSuppliers(any());
    // }

    @Test
    public void testAddSupplierSuccess() throws Exception {
        SupplierDTO supplierDto = new SupplierDTO();
        supplierDto.setId((long) 1);
        supplierDto.setName("Supplier A");

        when(supplierService.saveSupplier(any(SupplierDTO.class))).thenReturn(supplierDto);

        mockMvc.perform(post("/api/supplier")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Supplier A\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supplier A"));

        verify(supplierService, times(1)).saveSupplier(any(SupplierDTO.class));
    }

    // @Test
    // public void testAddSupplierBadRequest() throws Exception {
    //     mockMvc.perform(post("/api/supplier")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content("{\"name\": \"\"}"))
    //             .andExpect(status().isBadRequest());

    //     verify(supplierService, times(1)).saveSupplier(any(SupplierDTO.class));
    // }

    @Test
    public void testUpdateSupplierSuccess() throws Exception {
        SupplierDTO supplierDto = new SupplierDTO();
        supplierDto.setId((long) 1);
        supplierDto.setName("Updated Supplier");

        when(supplierService.findSupplierById((long) 1)).thenReturn(supplierDto);
        when(supplierService.saveSupplier(any(SupplierDTO.class))).thenReturn(supplierDto);

        mockMvc.perform(patch("/api/supplier/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Supplier\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Supplier"));

        verify(supplierService, times(1)).findSupplierById((long) 1);
        verify(supplierService, times(1)).saveSupplier(any(SupplierDTO.class));
    }

    @Test
    public void testUpdateSupplierNotFound() throws Exception {
        when(supplierService.findSupplierById((long) 1)).thenThrow(new ResourceNotFoundException("Supplier with ID 1 not found"));

        mockMvc.perform(patch("/api/supplier/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Supplier\"}"))
                .andExpect(status().isNotFound());

        verify(supplierService, times(1)).findSupplierById((long) 1);
    }

    @Test
    public void testDeleteSupplierSuccess() throws Exception {
        doNothing().when(supplierService).deleteSupplierById((long) 1);

        mockMvc.perform(delete("/api/supplier/1"))
                .andExpect(status().isNoContent());

        verify(supplierService, times(1)).deleteSupplierById((long) 1);
    }

    @Test
    public void testDeleteSupplierNotFound() throws Exception {
        doNothing().when(supplierService).deleteSupplierById((long) 1);

        mockMvc.perform(delete("/api/supplier/1"))
                .andExpect(status().isNoContent());

        verify(supplierService, times(1)).deleteSupplierById((long) 1);
    }
}
