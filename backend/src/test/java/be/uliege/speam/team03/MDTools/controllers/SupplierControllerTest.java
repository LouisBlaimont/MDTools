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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
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
        mockMvc = MockMvcBuilders.standaloneSetup(supplierController)
                .setControllerAdvice(new ResponseStatusExceptionHandler())
                .build();
    }

    @Test
    // Test to verify successful retrieval of instruments for a supplier.
    public void testGetInstrumentsOfSupplierSuccess() throws Exception {
        // Create sample instrument DTOs
        InstrumentDTO instrument1 = new InstrumentDTO();
        instrument1.setId(1L);
        instrument1.setReference("Instrument 1");
        
        List<InstrumentDTO> instruments = Arrays.asList(instrument1);
        
        // Mock the instrumentService to return these instruments
        when(instrumentService.findInstrumentsBySupplierId(1L)).thenReturn(instruments);
        
        mockMvc.perform(get("/api/supplier/1/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reference").value("Instrument 1"));
        
        // Verify the service method was called
        verify(instrumentService, times(1)).findInstrumentsBySupplierId(1L);
    }

    @Test
    // Test to verify behavior when instruments for a supplier are not found.
    public void testGetInstrumentsOfSupplierNotFound() throws Exception {
        when(instrumentService.findInstrumentsBySupplierId((1L))).thenThrow(new ResourceNotFoundException("Supplier with ID 1 not found"));

        mockMvc.perform(get("/api/supplier/1/instruments"))
                .andExpect(status().isNotFound());

        verify(instrumentService, times(1)).findInstrumentsBySupplierId((long) 1);
    }

    @Test
    // Test to verify behavior when an invalid supplier ID is provided for instruments retrieval.
    public void testGetInstrumentsOfSupplierWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/supplier/invalid/instruments"))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).findSupplierById(any(Long.class));
    }

    @Test
    // Test to verify successful retrieval of a supplier by ID.
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
    // Test to verify behavior when a supplier is not found by ID.
    public void testGetSupplierByIdNotFound() throws Exception {
        when(supplierService.findSupplierById((long) 1)).thenThrow(new ResourceNotFoundException("Supplier with ID 1 not found"));

        mockMvc.perform(get("/api/supplier/1"))
                .andExpect(status().isNotFound());

        verify(supplierService, times(1)).findSupplierById((long) 1);
    }

    @Test
    // Test to verify behavior when an invalid supplier ID is provided for retrieval.
    public void testGetSupplierByIdWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/supplier/invalid"))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).findSupplierById(any(Long.class));
    }
    
    @Test
    // Test to verify successful retrieval of all suppliers.
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
    // Test to verify behavior when no suppliers are found.
    public void testGetAllSuppliersEmptyList() throws Exception {
        List<SupplierDTO> suppliers = Arrays.asList();

        when(supplierService.findAllSuppliers()).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(supplierService, times(1)).findAllSuppliers();
    }

    @Test
    // Test to verify successful addition of a new supplier.
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

    @Test
    // Test to verify successful update of an existing supplier.
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
    // Test to verify behavior when updating a non-existent supplier.
    public void testUpdateSupplierNotFound() throws Exception {
        when(supplierService.findSupplierById((long) 1)).thenThrow(new ResourceNotFoundException("Supplier with ID 1 not found"));

        mockMvc.perform(patch("/api/supplier/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Supplier\"}"))
                .andExpect(status().isNotFound());

        verify(supplierService, times(1)).findSupplierById((long) 1);
    }

    @Test
    // Test to verify successful deletion of a supplier.
    public void testDeleteSupplierSuccess() throws Exception {
        doNothing().when(supplierService).deleteSupplierById((long) 1);

        mockMvc.perform(delete("/api/supplier/1"))
                .andExpect(status().isNoContent());

        verify(supplierService, times(1)).deleteSupplierById((long) 1);
    }

    @Test
    // Test to verify behavior when deleting a non-existent supplier.
    public void testDeleteSupplierNotFound() throws Exception {
        doNothing().when(supplierService).deleteSupplierById((long) 1);

        mockMvc.perform(delete("/api/supplier/1"))
                .andExpect(status().isNoContent());

        verify(supplierService, times(1)).deleteSupplierById((long) 1);
    }

    @Test
    // Test to verify successful retrieval of paginated suppliers.
    public void testGetPaginatedSuppliersSuccess() throws Exception {
        SupplierDTO supplierDto1 = new SupplierDTO();
        supplierDto1.setId(1L);
        supplierDto1.setName("Supplier A");

        SupplierDTO supplierDto2 = new SupplierDTO();
        supplierDto2.setId(2L);
        supplierDto2.setName("Supplier B");

        List<SupplierDTO> supplierList = Arrays.asList(supplierDto1, supplierDto2);
        
        // Create a proper Page object with all the details
        Page<SupplierDTO> suppliers = new PageImpl<>(
            supplierList,
            PageRequest.of(0, 10),
            supplierList.size()
        );

        // Use a more specific matcher for Pageable
        when(supplierService.findPaginatedSuppliers(any(Pageable.class))).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier?page=0&size=10"))
                .andExpect(status().isOk())
                // Check the structure matches what you're seeing in your API
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Supplier A"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Supplier B"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(supplierService, times(1)).findPaginatedSuppliers(any(Pageable.class));
    }

    @Test
    // Test to verify behavior when adding a supplier with a bad request body.
    public void testAddSupplierBadRequest() throws Exception {
        // This test assumes your controller validates that request body is not null
        mockMvc.perform(post("/api/supplier")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).saveSupplier(any(SupplierDTO.class));
    }

    @Test
    // Test to verify successful retrieval of a supplier by name.
    public void testFindSupplierByNameSuccess() throws Exception {
        SupplierDTO supplierDto = new SupplierDTO();
        supplierDto.setId(1L);
        supplierDto.setName("Supplier A");

        when(supplierService.findSupplierByName("Supplier A")).thenReturn(supplierDto);

        mockMvc.perform(get("/api/supplier/name/Supplier A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Supplier A"));

        verify(supplierService, times(1)).findSupplierByName("Supplier A");
    }

    @Test
    // Test to verify behavior when a supplier is not found by name.
    public void testFindSupplierByNameNotFound() throws Exception {
        when(supplierService.findSupplierByName("Nonexistent")).thenThrow(
                new ResourceNotFoundException("Supplier with name Nonexistent not found"));

        mockMvc.perform(get("/api/supplier/name/Nonexistent"))
                .andExpect(status().isNotFound());

        verify(supplierService, times(1)).findSupplierByName("Nonexistent");
    }

    @Test
    // Test to verify successful partial update of a supplier.
    public void testPartialUpdateSupplierSuccess() throws Exception {
        SupplierDTO existingSupplier = new SupplierDTO();
        existingSupplier.setId(1L);
        existingSupplier.setName("Original Name");
        existingSupplier.setSoldByMd(false);
        existingSupplier.setClosed(false);

        SupplierDTO updatedSupplier = new SupplierDTO();
        updatedSupplier.setId(1L);
        updatedSupplier.setName("Original Name"); // Name unchanged
        updatedSupplier.setSoldByMd(true); // Changed
        updatedSupplier.setClosed(true); // Changed

        when(supplierService.findSupplierById(1L)).thenReturn(existingSupplier);
        when(supplierService.saveSupplier(any(SupplierDTO.class))).thenReturn(updatedSupplier);

        mockMvc.perform(patch("/api/supplier/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"soldByMd\": true, \"closed\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Original Name"))
                .andExpect(jsonPath("$.soldByMd").value(true))
                .andExpect(jsonPath("$.closed").value(true));

        verify(supplierService, times(1)).findSupplierById(1L);
        verify(supplierService, times(1)).saveSupplier(any(SupplierDTO.class));
    }

    @Test
    // Test to verify behavior when updating a supplier with an invalid ID format.
    public void testUpdateSupplierWithInvalidId() throws Exception {
        mockMvc.perform(patch("/api/supplier/invalid")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Supplier\"}"))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).findSupplierById(any(Long.class));
        verify(supplierService, times(0)).saveSupplier(any(SupplierDTO.class));
    }

    @Test
    // Test to verify successful retrieval of instruments for a supplier.
    public void testGetInstrumentsOfSupplierReturnsInstruments() throws Exception {
        // Create some sample instrument DTOs
        InstrumentDTO instrument1 = new InstrumentDTO();
        instrument1.setId(1L);
        instrument1.setReference("Instrument 1");
        
        InstrumentDTO instrument2 = new InstrumentDTO();
        instrument2.setId(2L);
        instrument2.setReference("Instrument 2");
        
        List<InstrumentDTO> instruments = Arrays.asList(instrument1, instrument2);
        
        when(instrumentService.findInstrumentsBySupplierId(1L)).thenReturn(instruments);
        
        mockMvc.perform(get("/api/supplier/1/instruments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].reference").value("Instrument 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].reference").value("Instrument 2"));
                
        verify(instrumentService, times(1)).findInstrumentsBySupplierId(1L);
    }

    @Test
    // Test to verify behavior when deleting a supplier with an invalid ID format.
    public void testDeleteSupplierWithInvalidId() throws Exception {
        mockMvc.perform(delete("/api/supplier/invalid"))
                .andExpect(status().isBadRequest());

        verify(supplierService, times(0)).deleteSupplierById(any(Long.class));
    }

    @Test
    // Test to verify successful search for suppliers with query parameter
    public void testSearchSuppliersWithQuerySuccess() throws Exception {
        SupplierDTO supplierDto1 = new SupplierDTO();
        supplierDto1.setId(1L);
        supplierDto1.setName("ABC Medical");

        SupplierDTO supplierDto2 = new SupplierDTO();
        supplierDto2.setId(2L);
        supplierDto2.setName("ABC Surgical");

        List<SupplierDTO> supplierList = Arrays.asList(supplierDto1, supplierDto2);
        
        Page<SupplierDTO> suppliers = new PageImpl<>(
            supplierList,
            PageRequest.of(0, 10),
            supplierList.size()
        );

        when(supplierService.searchPaginatedSuppliers("ABC", PageRequest.of(0, 10))).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/search?query=ABC&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("ABC Medical"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("ABC Surgical"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(supplierService, times(1)).searchPaginatedSuppliers("ABC", PageRequest.of(0, 10));
    }

    @Test
    // Test to verify search without providing a query parameter (should return all suppliers)
    public void testSearchSuppliersWithNoQueryParameter() throws Exception {
        SupplierDTO supplierDto1 = new SupplierDTO();
        supplierDto1.setId(1L);
        supplierDto1.setName("Supplier A");

        SupplierDTO supplierDto2 = new SupplierDTO();
        supplierDto2.setId(2L);
        supplierDto2.setName("Supplier B");

        List<SupplierDTO> supplierList = Arrays.asList(supplierDto1, supplierDto2);
        
        Page<SupplierDTO> suppliers = new PageImpl<>(
            supplierList,
            PageRequest.of(0, 10),
            supplierList.size()
        );

        when(supplierService.searchPaginatedSuppliers(null, PageRequest.of(0, 10))).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/search?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Supplier A"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Supplier B"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(supplierService, times(1)).searchPaginatedSuppliers(null, PageRequest.of(0, 10));
    }

    @Test
    // Test to verify search with custom pagination parameters
    public void testSearchSuppliersWithCustomPagination() throws Exception {
        SupplierDTO supplierDto1 = new SupplierDTO();
        supplierDto1.setId(1L);
        supplierDto1.setName("Supplier A");

        List<SupplierDTO> supplierList = Arrays.asList(supplierDto1);
        
        // Create a page with custom pagination (page 2, size 5)
        Page<SupplierDTO> suppliers = new PageImpl<>(
            supplierList,
            PageRequest.of(2, 5),
            11 // Total elements (to have multiple pages)
        );

        when(supplierService.searchPaginatedSuppliers("Test", PageRequest.of(2, 5))).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/search?query=Test&page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Supplier A"))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalPages").value(3)); // 11 items with size 5 = 3 pages

        verify(supplierService, times(1)).searchPaginatedSuppliers("Test", PageRequest.of(2, 5));
    }

    @Test
    // Test to verify search with no results
    public void testSearchSuppliersWithNoResults() throws Exception {
        List<SupplierDTO> emptyList = Arrays.asList();
        
        Page<SupplierDTO> emptyPage = new PageImpl<>(
            emptyList,
            PageRequest.of(0, 10),
            0
        );

        when(supplierService.searchPaginatedSuppliers("NonExistent", PageRequest.of(0, 10))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/supplier/search?query=NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(supplierService, times(1)).searchPaginatedSuppliers("NonExistent", PageRequest.of(0, 10));
    }

    @Test
    // Test to verify search with an empty string query
    public void testSearchSuppliersWithEmptyQuery() throws Exception {
        SupplierDTO supplierDto1 = new SupplierDTO();
        supplierDto1.setId(1L);
        supplierDto1.setName("Supplier A");

        SupplierDTO supplierDto2 = new SupplierDTO();
        supplierDto2.setId(2L);
        supplierDto2.setName("Supplier B");

        List<SupplierDTO> supplierList = Arrays.asList(supplierDto1, supplierDto2);
        
        Page<SupplierDTO> suppliers = new PageImpl<>(
            supplierList,
            PageRequest.of(0, 10),
            supplierList.size()
        );

        when(supplierService.searchPaginatedSuppliers("", PageRequest.of(0, 10))).thenReturn(suppliers);

        mockMvc.perform(get("/api/supplier/search?query="))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Supplier A"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].name").value("Supplier B"))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(supplierService, times(1)).searchPaginatedSuppliers("", PageRequest.of(0, 10));
    }
}
