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

@ExtendWith(MockitoExtension.class)
public class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

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
    void deleteSupplier_CallsRepositoryDeleteById() {
        // Arrange
        doNothing().when(supplierRepository).deleteById(1);

        // Act
        supplierService.deleteSupplier(1);

        // Assert
        verify(supplierRepository).deleteById(1);
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
}