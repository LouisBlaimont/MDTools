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
    void findByReference_WhenInstrumentExists_ReturnsInstrumentDTO() {
        // Arrange
        when(instrumentRepository.findByReference("Test Reference")).thenReturn(Optional.of(instrument));
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);


        // Act
        InstrumentDTO result = instrumentService.findByReference("Test Reference");

        // Assert
        assertNotNull(result);
        assertEquals("Test Reference", result.getReference());
    }

    @Test
    void findByReference_WhenInstrumentDoesNotExist_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findByReference("Nonexistent Reference")).thenReturn(Optional.empty());

        // Act
        InstrumentDTO result = instrumentService.findByReference("Nonexistent Reference");

        // Assert
        assertNull(result);
    }

    @Test
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
    void findInstrumentsByReference_WhenInstrumentExists_ReturnsListOfInstrumentDTO() {
        Category category = new Category();
        category.setId((long) 1);
        category.setSubGroup(new SubGroup());
        category.getSubGroup().setGroup(new Group());
        category.getSubGroup().getGroup().setId(1L);
        instrument.setCategory(category);

        // Arrange
        instrument.setSupplier(new Supplier());
        when(instrumentRepository.findByReference("Test Reference")).thenReturn(Optional.of(instrument));
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);

        // Act
        List<InstrumentDTO> result = instrumentService.findInstrumentsByReference("Test Reference");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Reference", result.get(0).getReference());
    }

    @Test
    void findInstrumentsByReference_WhenInstrumentDoesNotExist_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findByReference("Nonexistent Reference")).thenReturn(Optional.empty());

        // Act
        List<InstrumentDTO> result = instrumentService.findInstrumentsByReference("Nonexistent Reference");

        // Assert
        assertNull(result);
    }

    @Test
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
            "obsolete", true
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
    void updateInstrument_WithNonexistentId_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findById(999L)).thenReturn(Optional.empty());

        Map<String, Object> updateData = Map.of("reference", "Nonexistent Reference");

        // Act
        InstrumentDTO result = instrumentService.updateInstrument(updateData, (long) 999);

        // Assert
        assertNull(result);
    }
}
