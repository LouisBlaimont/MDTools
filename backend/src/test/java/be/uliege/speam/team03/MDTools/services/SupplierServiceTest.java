package be.uliege.speam.team03.MDTools.services;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
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
}
