package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.SupplierMapper;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierPageRepository;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierPageRepository supplierPageRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private SupplierDTO supplierDTO;

    @BeforeEach
    void setUp() {
        supplier = new Supplier();
        supplier.setId((long) 1);
        supplier.setSupplierName("Supplier A");
        supplier.setSoldByMd(true);
        supplier.setClosed(false);

        supplierDTO = new SupplierDTO("Supplier A", (long) 1, true, false);
    }

    @Test
    // Test to verify that a supplier is successfully retrieved by ID when it exists.
    void findSupplierById_ShouldReturnSupplierDTO_WhenSupplierExists() {
        when(supplierRepository.findById((long) 1)).thenReturn(Optional.of(supplier));
        when(supplierMapper.convertToDTO(supplier)).thenReturn(supplierDTO);

        SupplierDTO result = supplierService.findSupplierById((long) 1);

        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
        verify(supplierRepository, times(1)).findById((long) 1);
    }

    @Test
    // Test to verify that an exception is thrown when a supplier is not found by ID.
    void findSupplierById_ShouldReturnNull_WhenSupplierDoesNotExist() {
        when(supplierRepository.findById((long) 1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.findSupplierById((long) 1));
        verify(supplierRepository, times(1)).findById((long) 1);
    }

    @Test
    // Test to verify that a supplier is successfully saved and returned when valid data is provided.
    void saveSupplier_ShouldSaveAndReturnSupplierDTO_WhenValidDataProvided() {
        when(supplierMapper.convertToEntity(supplierDTO)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.convertToDTO(supplier)).thenReturn(supplierDTO);

        SupplierDTO result = supplierService.saveSupplier(supplierDTO);

        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
        verify(supplierRepository, times(1)).save(supplier);
    }

    @Test
    // Test to verify that an exception is thrown when trying to save a supplier with a null name.
    void saveSupplier_ShouldThrowException_WhenSupplierNameIsNull() {
        supplierDTO.setName(null);

        assertThrows(BadRequestException.class, () -> supplierService.saveSupplier(supplierDTO));
        verifyNoInteractions(supplierRepository);
    }

    @Test
    // Test to verify that a list of suppliers is successfully retrieved when suppliers exist.
    void findAllSuppliers_ShouldReturnListOfSupplierDTOs_WhenSuppliersExist() {
        List<Supplier> suppliers = List.of(supplier);
        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.convertToDTOList(suppliers)).thenReturn(List.of(supplierDTO));

        List<SupplierDTO> result = supplierService.findAllSuppliers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(supplierDTO.getName(), result.get(0).getName());
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    // Test to verify that an exception is thrown when no suppliers exist.
    void findAllSuppliers_ShouldThrowException_WhenNoSuppliersExist() {
        when(supplierRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.findAllSuppliers());

        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    // Test to verify that a supplier is successfully deleted when it exists.
    void deleteSupplierById_ShouldDeleteSupplier_WhenSupplierExists() {
        when(supplierRepository.findById((long) 1)).thenReturn(Optional.of(supplier));

        supplierService.deleteSupplierById((long) 1);

        verify(supplierRepository, times(1)).delete(supplier);
    }

    @Test
    // Test to verify that an exception is thrown when trying to delete a non-existent supplier.
    void deleteSupplierById_ShouldThrowException_WhenSupplierDoesNotExist() {
        when(supplierRepository.findById((long) 1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> supplierService.deleteSupplierById((long) 1));
        verify(supplierRepository, times(1)).findById((long) 1);
    }

    @Test
    // Test searching suppliers with a valid search query
    public void testSearchPaginatedSuppliersWithValidQuery() {
        // Create test data
        Supplier supplier1 = new Supplier();
        supplier1.setId(1L);
        supplier1.setSupplierName("Medical Supplies Inc");
        
        Supplier supplier2 = new Supplier();
        supplier2.setId(2L);
        supplier2.setSupplierName("Medical Equipment Ltd");
        
        List<Supplier> supplierList = Arrays.asList(supplier1, supplier2);
        Page<Supplier> supplierPage = new PageImpl<>(supplierList);
        
        // Create corresponding DTOs
        SupplierDTO dto1 = new SupplierDTO();
        dto1.setId(1L);
        dto1.setName("Medical Supplies Inc");
        
        SupplierDTO dto2 = new SupplierDTO();
        dto2.setId(2L);
        dto2.setName("Medical Equipment Ltd");
        
        // Set up mock behavior
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierPageRepository.findBySupplierNameContainingIgnoreCase(eq("Medical"), any(Pageable.class)))
            .thenReturn(supplierPage);
        when(supplierMapper.convertToDTO(supplier1)).thenReturn(dto1);
        when(supplierMapper.convertToDTO(supplier2)).thenReturn(dto2);
        
        // Execute the service method
        Page<SupplierDTO> result = supplierService.searchPaginatedSuppliers("Medical", pageable);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Medical Supplies Inc", result.getContent().get(0).getName());
        assertEquals("Medical Equipment Ltd", result.getContent().get(1).getName());
        
        // Verify that the repository method was called with the correct parameters
        verify(supplierPageRepository).findBySupplierNameContainingIgnoreCase(eq("Medical"), eq(pageable));
        verify(supplierMapper, times(2)).convertToDTO(any(Supplier.class));
    }

    @Test
    // Test searching suppliers with null search query (should delegate to findPaginatedSuppliers)
    public void testSearchPaginatedSuppliersWithNullQuery() {
        // Create test data
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Test Supplier");
        
        List<Supplier> supplierList = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(supplierList);
        
        // Create corresponding DTO
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("Test Supplier");
        
        // Set up mock behavior
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierPageRepository.findAll(pageable)).thenReturn(supplierPage);
        when(supplierMapper.convertToDTO(supplier)).thenReturn(dto);
        
        // Execute the service method
        Page<SupplierDTO> result = supplierService.searchPaginatedSuppliers(null, pageable);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Supplier", result.getContent().get(0).getName());
        
        // Verify that findBySupplierNameContainingIgnoreCase was NOT called
        verify(supplierPageRepository, times(0)).findBySupplierNameContainingIgnoreCase(anyString(), any(Pageable.class));
        // Verify that findAll was called instead
        verify(supplierPageRepository).findAll(pageable);
        verify(supplierMapper).convertToDTO(supplier);
    }

    @Test
    // Test searching suppliers with empty search query (should delegate to findPaginatedSuppliers)
    public void testSearchPaginatedSuppliersWithEmptyQuery() {
        // Create test data
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Test Supplier");
        
        List<Supplier> supplierList = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(supplierList);
        
        // Create corresponding DTO
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("Test Supplier");
        
        // Set up mock behavior
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierPageRepository.findAll(pageable)).thenReturn(supplierPage);
        when(supplierMapper.convertToDTO(supplier)).thenReturn(dto);
        
        // Execute the service method with empty string
        Page<SupplierDTO> result = supplierService.searchPaginatedSuppliers("", pageable);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Supplier", result.getContent().get(0).getName());
        
        // Verify that findBySupplierNameContainingIgnoreCase was NOT called
        verify(supplierPageRepository, times(0)).findBySupplierNameContainingIgnoreCase(anyString(), any(Pageable.class));
        // Verify that findAll was called instead
        verify(supplierPageRepository).findAll(pageable);
        verify(supplierMapper).convertToDTO(supplier);
    }

    @Test
    // Test searching suppliers with whitespace-only query (should delegate to findPaginatedSuppliers)
    public void testSearchPaginatedSuppliersWithWhitespaceQuery() {
        // Create test data
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Test Supplier");
        
        List<Supplier> supplierList = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(supplierList);
        
        // Create corresponding DTO
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("Test Supplier");
        
        // Set up mock behavior
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierPageRepository.findAll(pageable)).thenReturn(supplierPage);
        when(supplierMapper.convertToDTO(supplier)).thenReturn(dto);
        
        // Execute the service method with whitespace string
        Page<SupplierDTO> result = supplierService.searchPaginatedSuppliers("   ", pageable);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Supplier", result.getContent().get(0).getName());
        
        // Verify that findBySupplierNameContainingIgnoreCase was NOT called
        verify(supplierPageRepository, times(0)).findBySupplierNameContainingIgnoreCase(anyString(), any(Pageable.class));
        // Verify that findAll was called instead
        verify(supplierPageRepository).findAll(pageable);
        verify(supplierMapper).convertToDTO(supplier);
    }

    @Test
    // Test searching suppliers with query that has leading/trailing whitespace (should be trimmed)
    public void testSearchPaginatedSuppliersWithQueryTrimming() {
        // Create test data
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Medical");
        
        List<Supplier> supplierList = Arrays.asList(supplier);
        Page<Supplier> supplierPage = new PageImpl<>(supplierList);
        
        // Create corresponding DTO
        SupplierDTO dto = new SupplierDTO();
        dto.setId(1L);
        dto.setName("Medical");
        
        // Set up mock behavior
        Pageable pageable = PageRequest.of(0, 10);
        // Notice how the argument is "Medical" (trimmed), not "  Medical  " (with spaces)
        when(supplierPageRepository.findBySupplierNameContainingIgnoreCase(eq("Medical"), any(Pageable.class)))
            .thenReturn(supplierPage);
        when(supplierMapper.convertToDTO(supplier)).thenReturn(dto);
        
        // Execute the service method with string that has leading/trailing whitespace
        Page<SupplierDTO> result = supplierService.searchPaginatedSuppliers("  Medical  ", pageable);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Medical", result.getContent().get(0).getName());
        
        // Verify that findBySupplierNameContainingIgnoreCase was called with TRIMMED query
        verify(supplierPageRepository).findBySupplierNameContainingIgnoreCase(eq("Medical"), eq(pageable));
        verify(supplierMapper).convertToDTO(supplier);
    }

    @Test
    // Test searching suppliers with query that returns no results
    public void testSearchPaginatedSuppliersWithNoResults() {
        // Create empty page
        Page<Supplier> emptyPage = new PageImpl<>(new ArrayList<>());
        
        // Set up mock behavior
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierPageRepository.findBySupplierNameContainingIgnoreCase(eq("NonExistent"), any(Pageable.class)))
            .thenReturn(emptyPage);
        
        // Execute the service method
        Page<SupplierDTO> result = supplierService.searchPaginatedSuppliers("NonExistent", pageable);
        
        // Verify the results
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        
        // Verify that the repository method was called with the correct parameters
        verify(supplierPageRepository).findBySupplierNameContainingIgnoreCase(eq("NonExistent"), eq(pageable));
        // Verify that mapper was not called since there are no entities to convert
        verifyNoInteractions(supplierMapper);
    }
}
