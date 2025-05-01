package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.InstrumentMapper;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

@ExtendWith(MockitoExtension.class)
public class InstrumentServiceTest {

    @Mock(strictness = Mock.Strictness.LENIENT)
    private InstrumentRepository instrumentRepository;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private SupplierRepository supplierRepository;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private InstrumentMapper instrumentMapper;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private PictureStorageService pictureStorageService;

    @Mock(strictness = Mock.Strictness.LENIENT)
    private CategoryRepository categoryRepository;

    @Mock
    private SupplierService supplierService;

    @Mock
    private SubGroupRepository subGroupRepository;


    @InjectMocks
    private InstrumentService instrumentService;

    private Instruments instrument;
    private InstrumentDTO instrumentDTO;
    private List<Instruments> instruments;
    private List<InstrumentDTO> instrumentDTOs;

    @BeforeEach
    void setUp() {
        // Initialize test data
        instrument = new Instruments();
        instrument.setId((long) 1);
        instrument.setReference("Test Reference");
        instrument.setPrice(100.0f);
        instrument.setObsolete(false);

        instrumentDTO = new InstrumentDTO();
        instrumentDTO.setId((long) 1);
        instrumentDTO.setSupplier("Test Supplier");
        instrumentDTO.setCategoryId((long) 1);
        instrumentDTO.setReference("Test Reference");
        instrumentDTO.setSupplierDescription("Test Description");
        instrumentDTO.setPrice(100.0f);
        instrumentDTO.setObsolete(false);
        instrumentDTO.setPicturesId(null);

        instruments = new ArrayList<>();
        instruments.add(instrument);

        instrumentDTOs = new ArrayList<>();
        instrumentDTOs.add(instrumentDTO);

        // Mock dependencies
        when(pictureStorageService.getPicturesIdByReferenceIdAndPictureType(anyLong(), any(PictureType.class))).thenReturn(new ArrayList<>());
        when(categoryRepository.findById((long) anyInt())).thenReturn(Optional.of(new Category()));
        when(supplierRepository.findById((long) anyInt())).thenReturn(Optional.of(new Supplier()));
        when(instrumentMapper.convertToDTO(any(Instruments.class))).thenReturn(instrumentDTO);
        when(instrumentMapper.convertToEntity(any(InstrumentDTO.class))).thenReturn(instrument);
        when(instrumentRepository.save(any(Instruments.class))).thenReturn(instrument);
        when(instrumentMapper.convertToDTO(any(Instruments.class))).thenReturn(instrumentDTO);
    }

    @Test
    // Test to verify that an instrument is successfully retrieved by reference when it exists.
    void findByReference_WhenInstrumentExists_ReturnsInstrumentDTO() {
        // Arrange
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.of(instrument));
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);


        // Act
        InstrumentDTO result = instrumentService.findByReference("Test Reference");

        // Assert
        assertNotNull(result);
        assertEquals("Test Reference", result.getReference());
    }

    @Test
    // Test to verify that null is returned when an instrument does not exist by reference.
    void findByReference_WhenInstrumentDoesNotExist_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findByReferenceIgnoreCase("Nonexistent Reference")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            instrumentService.findByReference("Nonexistent Reference");
        });
    }

    @Test
    // Test to verify that an instrument is successfully retrieved by ID when it exists.
    void findById_WhenInstrumentExists_ReturnsInstrumentDTO() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument));
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);

        // Act
        InstrumentDTO result = instrumentService.findById( 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    // Test to verify that an exception is thrown when an instrument does not exist by ID.
    void findById_WhenInstrumentDoesNotExist_ThrowsResourceNotFoundException() {
        // Arrange
        when(instrumentRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            instrumentService.findById(999L);
        });

        assertTrue(exception.getMessage().contains("Instrument not found with ID: 999"));
    }

    @Test
    // Test to verify that a list of instruments is successfully retrieved when instruments exist.
    void findAll_WhenInstrumentsExist_ReturnsListOfInstrumentDTOs() {
        // Arrange
        when(instrumentRepository.findAll()).thenReturn(instruments);
        when(instrumentMapper.convertToDTO(instruments)).thenReturn(instrumentDTOs);

        // Act
        List<InstrumentDTO> result = instrumentService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
    }

    @Test
    // Test to verify that an empty list is returned when no instruments exist.
    void findAll_WhenNoInstrumentsExist_ReturnsEmptyList() {
        // Arrange
        when(instrumentRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<InstrumentDTO> result = instrumentService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    // Test to verify that an instrument is successfully saved and returned when valid data is provided.
    void saveInstrument_WithValidInstrument_ReturnsSavedInstrumentDTO() {
        // Arrange
        when(instrumentMapper.convertToEntity(instrumentDTO)).thenReturn(instrument);
        when(instrumentRepository.save(instrument)).thenReturn(instrument);
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);

        // Act
        InstrumentDTO result = instrumentService.save(instrumentDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Reference", result.getReference());
        verify(instrumentRepository).save(instrument);
    }

    @Test
    // Test to verify that an exception is thrown when trying to save an instrument with a null reference.
    void saveInstrument_WithNullReference_ThrowsIllegalArgumentException() {
        // Arrange
        InstrumentDTO invalidInstrument = new InstrumentDTO();
        invalidInstrument.setReference(null);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            instrumentService.save(invalidInstrument);
        });

        assertEquals("Reference is required to identify an instrument", exception.getMessage());
    }

    @Test
    // Test to verify that an instrument is successfully deleted when it exists.
    void deleteInstrument_CallsRepositoryDeleteById() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument));
        doNothing().when(instrumentRepository).delete(instrument);

        // Act
        instrumentService.delete( 1L);

        // Assert
        verify(instrumentRepository).delete(instrument);
    }

    @Test
    // Test to verify that an exception is thrown when trying to delete a non-existent instrument.
    void deleteInstrument_WithNonexistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(instrumentRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            instrumentService.delete((long) 999);
        });

        assertTrue(exception.getMessage().contains("Instrument not found with id: 999"));
    }

    @Test
    // Test to verify that a list of instruments is successfully retrieved by reference when they exist.
    void findInstrumentsByReference_WhenInstrumentExists_ReturnsListOfInstrumentDTO() {
        Category category = new Category();
        category.setId((long) 1);
        category.setSubGroup(new SubGroup());
        category.getSubGroup().setGroup(new Group());
        category.getSubGroup().getGroup().setId(1L);
        instrument.setCategory(category);

        // Arrange
        instrument.setSupplier(new Supplier());
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.of(instrument));
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);

        // Act
        List<InstrumentDTO> result = instrumentService.findInstrumentsByReference("Test Reference");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
    }

    @Test
    // Test to verify that null is returned when no instruments exist by reference.
    void findInstrumentsByReference_WhenInstrumentDoesNotExist_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findByReferenceIgnoreCase("Nonexistent Reference")).thenReturn(Optional.empty());

        // Act
        List<InstrumentDTO> result = instrumentService.findInstrumentsByReference("Nonexistent Reference");

        // Assert
        assertNull(result);
    }

    @Test
    // Test to verify that an instrument is successfully updated with valid data.
    void updateInstrument_WithValidData_ReturnsUpdatedInstrumentDTO() {
        // Arrange
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument));
        when(instrumentRepository.save(any(Instruments.class))).thenAnswer(invocation -> {
            Instruments updatedInstrument = invocation.getArgument(0);
            updatedInstrument.setReference("Updated Reference");
            updatedInstrument.setPrice(200.0f);
            updatedInstrument.setObsolete(true);
            return updatedInstrument;
        });
        when(instrumentMapper.convertToDTO(any(Instruments.class))).thenAnswer(invocation -> {
            Instruments updatedInstrument = invocation.getArgument(0);
            InstrumentDTO updatedDTO = new InstrumentDTO();
            updatedDTO.setReference(updatedInstrument.getReference());
            updatedDTO.setPrice(updatedInstrument.getPrice());
            updatedDTO.setObsolete(updatedInstrument.getObsolete());
            return updatedDTO;
        });

        Map<String, Object> updateData = Map.of(
            "reference", "Updated Reference",
            "price", 200.0f,
            "obsolete", true,
            "categoryId", 1L
        );

        // Act
        InstrumentDTO result = instrumentService.updateInstrument(updateData, (long) 1);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Reference", result.getReference());
        assertEquals(200.0f, result.getPrice());
        assertTrue(result.getObsolete());
    }

    @Test
    // Test to verify that an exception is thrown when trying to update a non-existent instrument.
    void updateInstrument_WithNonexistentId_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> updateData = Map.of("reference", "Nonexistent Reference");

        // Act
        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            instrumentService.updateInstrument(updateData, 999L);
        });
        
        assertTrue(exception.getMessage().contains("Instrument not found with ID: 999"));
    }

    @Test
    void addInstrument_WithValidData_ReturnsSavedInstrumentDTO() {
        // Arrange
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.empty());
        when(supplierService.findSupplierByName("Test Supplier")).thenReturn(new SupplierDTO());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(new Category()));
        when(instrumentMapper.convertToEntity(instrumentDTO)).thenReturn(instrument);
        when(instrumentRepository.save(instrument)).thenReturn(instrument);
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);

        // Act
        InstrumentDTO result = instrumentService.addInstrument(instrumentDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Test Reference", result.getReference());
    }


    @Test
    void addInstrument_MissingReference_ThrowsBadRequest() {
        instrumentDTO.setReference(null);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });
        assertTrue(exception.getMessage().contains("Reference is required to identify an instrument"));
    }

    @Test
    void addInstrument_MissingSupplier_ThrowsBadRequest() {
        instrumentDTO.setSupplier(null);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });
        assertTrue(exception.getMessage().contains("Supplier name cannot be null or empty"));

    }

    @Test
    void addInstrument_MissingCategoryId_ThrowsBadRequest() {
        instrumentDTO.setCategoryId(null);
        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });
        assertTrue(exception.getMessage().contains("Category ID is required to identify an instrument"));

    }

    @Test
    void addInstrument_DuplicateReference_ThrowsBadRequest() {
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.of(instrument));

        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });

        assertTrue(exception.getMessage().contains("An instrument with this reference already exists."));
    }

    @Test
    void addInstrument_UnknownSupplier_ThrowsBadRequest() {
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.empty());
        when(supplierRepository.findBySupplierName("Test Supplier")).thenReturn(Optional.empty());

        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });

        assertTrue(exception.getMessage().contains("Supplier not found: Test Supplier"));
    }

    @Test
    void addInstrument_UnknownCategory_ThrowsBadRequest() {
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.empty());
        when(supplierService.findSupplierByName("Test Supplier")).thenReturn(new SupplierDTO());
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());


        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });

        assertTrue(exception.getMessage().contains("Category not found with ID: 1"));

    }

    @Test
    void addInstrument_NegativePrice_ThrowsBadRequest() {
        instrumentDTO.setPrice(-10F);
        when(instrumentRepository.findByReferenceIgnoreCase("Test Reference")).thenReturn(Optional.empty());

        Exception exception = assertThrows(BadRequestException.class, () -> {
            instrumentService.addInstrument(instrumentDTO);
        });

        assertTrue(exception.getMessage().contains("Price cannot be negative."));
    }

    @Test
    void findInstrumentsOfCategory_WhenCategoryExistsAndHasInstruments_ReturnsInstrumentDTOs() {
        // Arrange
        Category category = new Category();
        SubGroup subGroup = new SubGroup();
        Group group = new Group();
        group.setId(1L);
        subGroup.setGroup(group);
        category.setSubGroup(subGroup);

        Supplier supplier = new Supplier();
        supplier.setSupplierName("Test Supplier");

        instrument.setCategory(category);
        instrument.setSupplier(supplier);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(instrumentRepository.findByCategory(category)).thenReturn(Optional.of(List.of(instrument)));
        when(pictureStorageService.getPicturesIdByReferenceIdAndPictureType(anyLong(), any()))
            .thenReturn(new ArrayList<>());

        // Act
        List<InstrumentDTO> result = instrumentService.findInstrumentsOfCatergory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
        assertEquals("Test Supplier", result.get(0).getSupplier());
    }


    @Test
    void findInstrumentsOfCategory_WhenCategoryExistsButNoInstruments_ReturnsNull() {
        // Arrange
        Category category = new Category();
        SubGroup subGroup = new SubGroup();
        Group group = new Group();
        group.setId(1L);
        subGroup.setGroup(group);
        category.setSubGroup(subGroup);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(instrumentRepository.findByCategory(category)).thenReturn(Optional.empty());

        // Act
        List<InstrumentDTO> result = instrumentService.findInstrumentsOfCatergory(1L);

        // Assert
        assertNull(result);
    }

    @Test
    void findInstrumentsOfCategory_WhenCategoryDoesNotExist_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            instrumentService.findInstrumentsOfCatergory(999L);
        });

        assertTrue(exception.getMessage().contains("Category with the id 999 doesn't exist"));
    }

    @Test
    void findInstrumentsBySubGroup_WhenSubGroupExists_ReturnsInstrumentDTOs() {
        SubGroup subGroup = new SubGroup();
        subGroup.setName("SubGroup A");

        Category category = new Category();
        category.setSubGroup(subGroup);

        List<Category> categories = List.of(category);

        when(subGroupRepository.findByName("SubGroup A")).thenReturn(Optional.of(subGroup));
        when(categoryRepository.findBySubGroup(subGroup, Sort.by("subGroupName", "id"))).thenReturn(categories);
        when(instrumentRepository.findByCategory(category)).thenReturn(Optional.of(instruments));
        when(instrumentMapper.convertToDTO(instruments)).thenReturn(instrumentDTOs);

        List<InstrumentDTO> result = instrumentService.findInstrumentsBySubGroup("SubGroup A");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
    }

    @Test
    void findInstrumentsBySubGroup_WhenSubGroupNotFound_ThrowsException() {
        when(subGroupRepository.findByName("Unknown")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            instrumentService.findInstrumentsBySubGroup("Unknown");
        });

        assertTrue(exception.getMessage().contains("SubGroup not found with name: Unknown"));
    }

    @Test
    void findInstrumentsBySupplierName_WhenSupplierExists_ReturnsInstrumentDTOs() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);
        supplier.setSupplierName("Test Supplier");

        when(supplierRepository.findBySupplierName("Test Supplier")).thenReturn(Optional.of(supplier));
        when(instrumentRepository.findBySupplierId(1L)).thenReturn(Optional.of(instruments));
        when(instrumentMapper.convertToDTO(instruments)).thenReturn(instrumentDTOs);

        List<InstrumentDTO> result = instrumentService.findInstrumentsBySupplierName("Test Supplier");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
    }

    @Test
    void findInstrumentsBySupplierName_WhenSupplierNotFound_ThrowsException() {
        when(supplierRepository.findBySupplierName("Unknown")).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            instrumentService.findInstrumentsBySupplierName("Unknown");
        });

        assertTrue(exception.getMessage().contains("Supplier not found with name: Unknown"));
    }

    @Test
    void findInstrumentsBySupplierId_WhenSupplierExists_ReturnsInstrumentDTOs() {
        Supplier supplier = new Supplier();
        supplier.setId(1L);

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(instrumentRepository.findBySupplierId(1L)).thenReturn(Optional.of(instruments));
        when(instrumentMapper.convertToDTO(instruments)).thenReturn(instrumentDTOs);

        List<InstrumentDTO> result = instrumentService.findInstrumentsBySupplierId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
    }

    @Test
    void findInstrumentsBySupplierId_WhenSupplierNotFound_ReturnsNull() {
        when(supplierRepository.findById(999L)).thenReturn(Optional.empty());

        List<InstrumentDTO> result = instrumentService.findInstrumentsBySupplierId(999L);

        assertNull(result);
    }
}
