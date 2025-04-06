package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.mapper.SupplierMapper;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierPageRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @Mock
    private SupplierPageRepository supplierPageRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private SupplierDTO supplierDTO;
    private List<Supplier> suppliers;
    private List<SupplierDTO> supplierDTOs;

    @BeforeEach
    void setUp() {
        // Initialize test data
        supplier = new Supplier();
        supplier.setId(1);
        supplier.setSupplierName("Test Supplier");
        supplier.setSoldByMd(true);
        supplier.setClosed(false);

        supplierDTO = new SupplierDTO("Test Supplier", 1, true, false);

        suppliers = new ArrayList<>();
        suppliers.add(supplier);

        supplierDTOs = new ArrayList<>();
        supplierDTOs.add(supplierDTO);
    }

    @Test
    void findSupplierByName_WhenSupplierExists_ReturnsSupplierDTO() {
        // Arrange
        when(supplierRepository.findBySupplierName("Test Supplier")).thenReturn(Optional.of(supplier));

        // Act
        SupplierDTO result = supplierService.findSupplierByName("Test Supplier");

        // Assert
        assertNotNull(result);
        assertEquals("Test Supplier", result.getName());
        assertEquals(1, result.getId());
        assertTrue(result.isSoldByMd());
        assertFalse(result.isClosed());
    }

    @Test
    void findSupplierByName_WhenSupplierDoesNotExist_ReturnsNull() {
        // Arrange
        when(supplierRepository.findBySupplierName("Nonexistent Supplier")).thenReturn(Optional.empty());

        // Act
        SupplierDTO result = supplierService.findSupplierByName("Nonexistent Supplier");

        // Assert
        assertNull(result);
    }

    @Test
    void findSuppliersByName_WhenSupplierExists_ReturnsListOfSupplierDTOs() {
        // Arrange
        when(supplierRepository.findBySupplierName("Test Supplier")).thenReturn(Optional.of(supplier));

        // Act
        List<SupplierDTO> result = supplierService.findSuppliersByName("Test Supplier");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Supplier", result.get(0).getName());
    }

    @Test
    void findSuppliersByName_WhenSupplierDoesNotExist_ReturnsNull() {
        // Arrange
        when(supplierRepository.findBySupplierName("Nonexistent Supplier")).thenReturn(Optional.empty());

        // Act
        List<SupplierDTO> result = supplierService.findSuppliersByName("Nonexistent Supplier");

        // Assert
        assertNull(result);
    }

    @Test
    void saveSupplier_WithValidSupplier_ReturnsSavedSupplierDTO() {
        // Arrange
        when(supplierMapper.convertToEntity(supplierDTO)).thenReturn(supplier);
        when(supplierRepository.save(supplier)).thenReturn(supplier);
        when(supplierMapper.convertToDTO(supplier)).thenReturn(supplierDTO);

        // Act
        SupplierDTO result = supplierService.saveSupplier(supplierDTO);

        // Assert
        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
        verify(supplierRepository).save(supplier);
    }

    @Test
    void saveSupplier_WithNullName_ThrowsIllegalArgumentException() {
        // Arrange
        SupplierDTO invalidSupplier = new SupplierDTO(null, 1, true, false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.saveSupplier(invalidSupplier);
        });

        assertEquals("Supplier name cannot be null or empty", exception.getMessage());
    }

    @Test
    void saveSupplier_WithEmptyName_ThrowsIllegalArgumentException() {
        // Arrange
        SupplierDTO invalidSupplier = new SupplierDTO("", 1, true, false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.saveSupplier(invalidSupplier);
        });

        assertEquals("Supplier name cannot be null or empty", exception.getMessage());
    }

    @Test
    void findSupplierById_WhenSupplierExists_ReturnsSupplierDTO() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(supplierMapper.convertToDTO(supplier)).thenReturn(supplierDTO);

        // Act
        SupplierDTO result = supplierService.findSupplierById(1);

        // Assert
        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
    }

    @Test
    void findSupplierById_WhenSupplierDoesNotExist_ReturnsNull() {
        // Arrange
        when(supplierRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        SupplierDTO result = supplierService.findSupplierById(999);

        // Assert
        assertNull(result);
    }

    @Test
    void findMaxSupplierId_ReturnsMaxId() {
        // Arrange
        when(supplierRepository.findMaxSupplierId()).thenReturn(5);

        // Act
        Integer result = supplierService.findMaxSupplierId();

        // Assert
        assertEquals(5, result);
    }

    @Test
    void findAllSuppliers_WhenSuppliersExist_ReturnsListOfSupplierDTOs() {
        // Arrange
        when(supplierRepository.findAll()).thenReturn(suppliers);
        when(supplierMapper.convertToDTOList(suppliers)).thenReturn(supplierDTOs);

        // Act
        List<SupplierDTO> result = supplierService.findAllSuppliers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(supplierDTOs, result);
    }

    @Test
    void findAllSuppliers_WhenNoSuppliersExist_ReturnsNull() {
        // Arrange
        when(supplierRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<SupplierDTO> result = supplierService.findAllSuppliers();

        // Assert
        assertNull(result);
    }

    @Test
    void findSupplierByName_WithNullName_ThrowsIllegalArgumentException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.findSupplierByName(null);
        });

        assertEquals("Supplier name cannot be null or empty", exception.getMessage());
    }

    @Test
    void findSuppliersByName_WithEmptyName_ReturnsNull() {
        // Arrange
        when(supplierRepository.findBySupplierName("")).thenReturn(Optional.empty());

        // Act
        List<SupplierDTO> result = supplierService.findSuppliersByName("");

        // Assert
        assertNull(result);
    }

    @Test
    void saveSupplier_WithDuplicateId_ThrowsConflictException() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));

        SupplierDTO duplicateSupplier = new SupplierDTO("Duplicate Supplier", 1, true, false);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.saveSupplier(duplicateSupplier);
        });

        assertEquals("Supplier with this ID already exists", exception.getMessage());
    }

    @Test
    void findAllSuppliers_WhenRepositoryThrowsException_ThrowsRuntimeException() {
        // Arrange
        when(supplierRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            supplierService.findAllSuppliers();
        });

        assertEquals("Database error", exception.getMessage());
    }

    @Test
    void findMaxSupplierId_WhenNoSuppliersExist_ReturnsZero() {
        // Arrange
        when(supplierRepository.findMaxSupplierId()).thenReturn(null);

        // Act
        Integer result = supplierService.findMaxSupplierId();

        // Assert
        assertEquals(0, result);
    }

    @Test
    void updateSupplier_WithPartialData_UpdatesOnlySpecifiedFields() {
        // Arrange
        SupplierDTO partialUpdate = new SupplierDTO(null, 1, false, true);
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        when(supplierMapper.convertToDTO(supplier)).thenReturn(supplierDTO);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // Act
        SupplierDTO result = supplierService.saveSupplier(partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals(supplierDTO.getName(), result.getName());
        assertTrue(result.isClosed());
        assertFalse(result.isSoldByMd());
    }

    @Test
    void findPaginatedSuppliers_WhenSuppliersExist_ReturnsPageOfSupplierDTOs() {
        // Arrange
        Supplier supplier1 = new Supplier();
        supplier1.setId(1);
        supplier1.setSupplierName("Supplier1");
        supplier1.setSoldByMd(true);
        supplier1.setClosed(false);

        Supplier supplier2 = new Supplier();
        supplier2.setId(2);
        supplier2.setSupplierName("Supplier2");
        supplier2.setSoldByMd(false);
        supplier2.setClosed(true);

        Page<Supplier> paginatedSuppliers = new PageImpl<>(Arrays.asList(supplier1, supplier2));

        when(supplierPageRepository.findAll(any(Pageable.class))).thenReturn(paginatedSuppliers);
        when(supplierMapper.convertToDTO(supplier1)).thenReturn(new SupplierDTO("Supplier1", 1, true, false));
        when(supplierMapper.convertToDTO(supplier2)).thenReturn(new SupplierDTO("Supplier2", 2, false, true));

        // Act
        Page<SupplierDTO> result = supplierService.findPaginatedSuppliers(PageRequest.of(0, 2));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Supplier1", result.getContent().get(0).getName());
    }

    @Test
    void deleteSupplierById_WhenSupplierExists_DeletesSuccessfully() {
        // Arrange
        when(supplierRepository.findById(1)).thenReturn(Optional.of(supplier));
        doNothing().when(supplierRepository).delete(supplier);

        // Act
        supplierService.deleteSupplierById(1);

        // Assert
        verify(supplierRepository, times(1)).delete(supplier);
    }

    @Test
    void deleteSupplierById_WhenSupplierDoesNotExist_ThrowsException() {
        // Arrange
        when(supplierRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            supplierService.deleteSupplierById(999);
        });

        assertEquals("Supplier with ID 999 does not exist", exception.getMessage());
    }
}