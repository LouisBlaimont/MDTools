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
    public void testGetAllSuppliers() throws Exception {
        SupplierDTO supplier1 = new SupplierDTO("Supplier1", 1, true, false);
        SupplierDTO supplier2 = new SupplierDTO("Supplier2", 2, false, true);

        List<SupplierDTO> suppliers = Arrays.asList(supplier1, supplier2);

        when(supplierService.findAllSuppliers()).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Supplier1"))
                .andExpect(jsonPath("$[1].name").value("Supplier2"));

        verify(supplierService, times(1)).findAllSuppliers();
    }

    @Test
    public void testGetSupplierById() throws Exception {
        SupplierDTO supplier = new SupplierDTO("Supplier1", 1, true, false);

        when(supplierService.findSupplierById(1)).thenReturn(supplier);

        mockMvc.perform(get("/api/supplier/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Supplier1"));

        verify(supplierService, times(1)).findSupplierById(1);
    }

    @Test
    public void testAddSupplier() throws Exception {
        SupplierDTO newSupplier = new SupplierDTO("Supplier1", null, true, false);
        SupplierDTO savedSupplier = new SupplierDTO("Supplier1", 1, true, false);

        when(supplierService.saveSupplier(any(SupplierDTO.class))).thenReturn(savedSupplier);

        mockMvc.perform(post("/api/supplier")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Supplier1\",\"soldByMd\":true,\"closed\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supplier1"));

        verify(supplierService, times(1)).saveSupplier(any(SupplierDTO.class));
    }

    @Test
    public void testUpdateSupplier() throws Exception {
        SupplierDTO existingSupplier = new SupplierDTO("Supplier1", 1, true, false);
        SupplierDTO updatedSupplier = new SupplierDTO("UpdatedSupplier", 1, false, true);

        when(supplierService.findSupplierById(1)).thenReturn(existingSupplier);
        when(supplierService.saveSupplier(any(SupplierDTO.class))).thenReturn(updatedSupplier);

        mockMvc.perform(patch("/api/supplier/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"UpdatedSupplier\",\"soldByMd\":false,\"closed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedSupplier"));

        verify(supplierService, times(1)).findSupplierById(1);
        verify(supplierService, times(1)).saveSupplier(any(SupplierDTO.class));
    }
}
