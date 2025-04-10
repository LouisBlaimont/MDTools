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
import be.uliege.speam.team03.MDTools.services.SupplierService;

@ExtendWith(MockitoExtension.class)
public class SupplierControllerTest {

    @Mock
    private SupplierService supplierService;

    @InjectMocks
    private SupplierController supplierController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(supplierController).build();
    }

    @Test
    public void testGetSupplierById() throws Exception {
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
    public void testGetAllSuppliers() throws Exception {
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
    public void testAddSupplier() throws Exception {
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

    @Test
    public void testUpdateSupplier() throws Exception {
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
    public void testDeleteSupplier() throws Exception {
        doNothing().when(supplierService).deleteSupplierById((long) 1);

        mockMvc.perform(delete("/api/supplier/1"))
                .andExpect(status().isNoContent());

        verify(supplierService, times(1)).deleteSupplierById((long) 1);
    }
}
