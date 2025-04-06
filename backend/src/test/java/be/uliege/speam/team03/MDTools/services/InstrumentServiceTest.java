package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.mapper.InstrumentMapper;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.repositories.PictureRepository;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;

@ExtendWith(MockitoExtension.class)
public class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private InstrumentMapper instrumentMapper;

    @Mock
    private PictureStorageService pictureStorageService;

    @Mock
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
        instrument.setId(1);
        instrument.setReference("Test Reference");
        instrument.setPrice(100.0f);
        instrument.setObsolete(false);

        instrumentDTO = new InstrumentDTO();
        instrumentDTO.setId(1);
        instrumentDTO.setSupplier("Test Supplier");
        instrumentDTO.setCategoryId(1);
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
        lenient().when(pictureStorageService.getPicturesIdByReferenceIdAndPictureType(anyLong(), any(PictureType.class))).thenReturn(new ArrayList<>());
        lenient().when(categoryRepository.findById(anyInt())).thenReturn(Optional.of(new Category()));
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
        when(instrumentRepository.findById(1)).thenReturn(Optional.of(instrument));
        when(instrumentMapper.convertToDTO(instrument)).thenReturn(instrumentDTO);

        // Act
        InstrumentDTO result = instrumentService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    void findById_WhenInstrumentDoesNotExist_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        InstrumentDTO result = instrumentService.findById(999);

        // Assert
        assertNull(result);
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
        when(instrumentRepository.findById(1)).thenReturn(Optional.of(instrument));
        doNothing().when(instrumentRepository).delete(instrument);

        // Act
        instrumentService.delete(1);

        // Assert
        verify(instrumentRepository).delete(instrument);
    }

    @Test
    void deleteInstrument_WithNonexistentId_ThrowsResourceNotFoundException() {
        // Arrange
        when(instrumentRepository.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            instrumentService.delete(999);
        });

        assertEquals("Instrument not found with id: 999", exception.getMessage());
    }

    @Test
    void findInstrumentsByReference_WhenInstrumentExists_ReturnsListOfInstrumentDTO() {
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
        when(instrumentRepository.findById(1)).thenReturn(Optional.of(instrument));
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
        InstrumentDTO result = instrumentService.updateInstrument(updateData, 1);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Reference", result.getReference());
        assertEquals(200.0f, result.getPrice());
        assertTrue(result.getObsolete());
    }

    @Test
    void updateInstrument_WithNonexistentId_ReturnsNull() {
        // Arrange
        when(instrumentRepository.findById(999)).thenReturn(Optional.empty());

        Map<String, Object> updateData = Map.of("reference", "Nonexistent Reference");

        // Act
        InstrumentDTO result = instrumentService.updateInstrument(updateData, 999);

        // Assert
        assertNull(result);
    }
}
