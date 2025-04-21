package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
import org.springframework.context.annotation.Import;

import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.AlternativesRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class AlternativeServiceTest {

    @Mock
    private AlternativesRepository alternativesRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private PictureStorageService pictureStorageService;

    private AlternativeService alternativeService;

    private Instruments instrument1;
    private Instruments instrument2;
    private Instruments instrument3;
    private Supplier supplier1;
    private Supplier supplier2;
    private Category category1;
    private SubGroup subGroup1;
    private Group group1;
    private Alternatives alternative1;
    private List<Alternatives> alternatives1;
    private List<Alternatives> alternatives2;
    private List<AlternativeReferenceDTO> alternativeReferenceDTOs;

    @BeforeEach
    public void setup() {
        // Initialize the service with mocks
        alternativeService = new AlternativeService(
                alternativesRepository,
                supplierRepository,
                categoryRepository,
                instrumentRepository,
                pictureStorageService);

        // Create test data
        group1 = new Group();
        group1.setId(1L);
        group1.setName("Test Group");

        subGroup1 = new SubGroup();
        subGroup1.setId(1L);
        subGroup1.setName("Test SubGroup");
        subGroup1.setGroup(group1);

        category1 = new Category();
        category1.setId(1L);
        category1.setSubGroup(subGroup1);

        supplier1 = new Supplier();
        supplier1.setId(1L);
        supplier1.setSupplierName("Medicon"); // Special supplier for sorting
        supplier1.setSoldByMd(true);

        supplier2 = new Supplier();
        supplier2.setId(2L);
        supplier2.setSupplierName("Other Supplier");
        supplier2.setSoldByMd(false);

        instrument1 = new Instruments();
        instrument1.setId(1L);
        instrument1.setSupplierDescription("Instrument 1");
        instrument1.setReference("REF001");
        instrument1.setSupplier(supplier1);
        instrument1.setCategory(category1);

        instrument2 = new Instruments();
        instrument2.setId(2L);
        instrument2.setSupplierDescription("Instrument 2");
        instrument2.setReference("REF002");
        instrument2.setSupplier(supplier2);
        instrument2.setCategory(category1);

        instrument3 = new Instruments();
        instrument3.setId(3L);
        instrument3.setSupplierDescription("Instrument 3");
        instrument3.setReference("REF003");
        instrument3.setSupplier(supplier1);
        instrument3.setCategory(category1);

        alternative1 = new Alternatives(instrument1, instrument2);

        alternatives1 = new ArrayList<>();
        alternatives1.add(alternative1);

        alternatives2 = new ArrayList<>();

        alternativeReferenceDTOs = new ArrayList<>();
        AlternativeReferenceDTO altRef = new AlternativeReferenceDTO();
        altRef.setReference_1("REF001");
        altRef.setReference_2("REF002");
        alternativeReferenceDTOs.add(altRef);
    }

    @Test
    public void testFindAlternatives_Success() {
        // Given
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument1));
        when(alternativesRepository.findById_InstrId1(1L)).thenReturn(alternatives1);
        when(alternativesRepository.findById_InstrId2(1L)).thenReturn(new ArrayList<>());

        // When
        List<Instruments> result = alternativeService.findAlternatives(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(instrument2.getId(), result.get(0).getId());
        verify(instrumentRepository, times(1)).findById(1L);
        verify(alternativesRepository, times(1)).findById_InstrId1(1L);
        verify(alternativesRepository, times(1)).findById_InstrId2(1L);
    }

    @Test
    public void testFindAlternatives_InstrumentNotFound() {
        // Given
        when(instrumentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativeService.findAlternatives(999L);
        });
        verify(instrumentRepository, times(1)).findById(999L);
    }

    @Test
    public void testFindAlternativesUser_Success() {
        // Given
        List<Instruments> alternatives = new ArrayList<>();
        alternatives.add(instrument2);
        alternatives.add(instrument3);

        // Mock the findAlternatives method using spy
        AlternativeService spyService = spy(alternativeService);
        doReturn(alternatives).when(spyService).findAlternatives(1L);

        // When
        List<InstrumentDTO> result = spyService.findAlternativesUser(1L);

        // Then
        // Only instrument3 should remain since instrument2's supplier is not sold by MD
        assertEquals(1, result.size());
    }

    @Test
    public void testFindAlternativesAdmin_Success() {
        // Given
        List<Instruments> alternatives = new ArrayList<>();
        alternatives.add(instrument2);
        alternatives.add(instrument3);

        // Mock the findAlternatives method using spy
        AlternativeService spyService = spy(alternativeService);
        doReturn(alternatives).when(spyService).findAlternatives(1L);

        // When
        List<InstrumentDTO> result = spyService.findAlternativesAdmin(1L);

        // Then
        // Both instruments should remain for admin
        assertEquals(2, result.size());
    }

    @Test
    public void testFindAlternativesOfCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        List<Instruments> categoryInstruments = new ArrayList<>();
        categoryInstruments.add(instrument1);
        when(instrumentRepository.findByCategory(category1)).thenReturn(Optional.of(categoryInstruments));

        when(alternativesRepository.findById_InstrId1(1L)).thenReturn(alternatives1);
        when(alternativesRepository.findById_InstrId2(1L)).thenReturn(new ArrayList<>());

        // When
        List<Instruments> result = alternativeService.findAlternativesOfCategory(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(instrument2.getId(), result.get(0).getId());
    }

    @Test
    public void testFindAlternativesOfCategory_CategoryNotFound() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativeService.findAlternativesOfCategory(999L);
        });
    }

    @Test
    public void testFindAlternativesOfCategory_NoInstruments() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(instrumentRepository.findByCategory(category1)).thenReturn(Optional.empty());

        // When
        List<Instruments> result = alternativeService.findAlternativesOfCategory(1L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFindAlternativesOfCategoryUser_Success() {
        // Given
        List<Instruments> alternatives = new ArrayList<>();
        alternatives.add(instrument2); // Not sold by MD
        alternatives.add(instrument3); // Sold by MD

        // Mock the findAlternativesOfCategory method using spy
        AlternativeService spyService = spy(alternativeService);
        doReturn(alternatives).when(spyService).findAlternativesOfCategory(1L);

        // When
        List<InstrumentDTO> result = spyService.findAlternativesOfCategoryUser(1L);

        // Then
        // Only instrument3 should remain since instrument2's supplier is not sold by MD
        assertEquals(1, result.size());
    }

    @Test
    public void testFindAlternativesOfCategoryAdmin_Success() {
        // Given
        List<Instruments> alternatives = new ArrayList<>();
        alternatives.add(instrument2);
        alternatives.add(instrument3);

        // Mock the findAlternativesOfCategory method using spy
        AlternativeService spyService = spy(alternativeService);
        doReturn(alternatives).when(spyService).findAlternativesOfCategory(1L);

        // When
        List<InstrumentDTO> result = spyService.findAlternativesOfCategoryAdmin(1L);

        // Then
        // Both instruments should remain for admin
        assertEquals(2, result.size());
    }

    @Test
    public void testFindAllAlternativesReferences_Success() {
        // Given
        when(alternativesRepository.findAllAlternativesReferences()).thenReturn(alternativeReferenceDTOs);

        // When
        List<AlternativeReferenceDTO> result = alternativeService.findAllAlternativesReferences();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REF001", result.get(0).getReference_1());
        assertEquals("REF002", result.get(0).getReference_2());
    }

    @Test
    public void testRemoveAlternativeFromCategory_Success() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));

        List<Instruments> categoryInstruments = new ArrayList<>();
        categoryInstruments.add(instrument1);
        when(instrumentRepository.findByCategory(category1)).thenReturn(Optional.of(categoryInstruments));

        when(alternativesRepository.findById_InstrId1(1L)).thenReturn(alternatives1);
        when(alternativesRepository.findById_InstrId2(1L)).thenReturn(new ArrayList<>());

        // When
        Boolean result = alternativeService.removeAlternativeFromCategory(1L, 2L);

        // Then
        assertTrue(result);
        verify(alternativesRepository, times(1)).delete(any(Alternatives.class));
    }

    @Test
    public void testRemoveAlternativeFromCategory_CategoryOrInstrumentNotFound() {
        // Given
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativeService.removeAlternativeFromCategory(999L, 2L);
        });
    }

    @Test
    public void testRemoveAlternativeFromCategory_NoInstrumentsInCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));
        when(instrumentRepository.findByCategory(category1)).thenReturn(Optional.empty());

        // When
        Boolean result = alternativeService.removeAlternativeFromCategory(1L, 2L);

        // Then
        assertFalse(result);
    }

    @Test
    public void testAddAlternative_Success() {
        // Given
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));
        when(alternativesRepository.findByInstr1AndInstr2(instrument1, instrument2)).thenReturn(Optional.empty());

        // Mock the findAlternativesOfCategoryAdmin method using spy
        AlternativeService spyService = spy(alternativeService);
        List<InstrumentDTO> expectedResult = new ArrayList<>();
        doReturn(expectedResult).when(spyService).findAlternativesOfCategoryAdmin(1L);

        // When
        List<InstrumentDTO> result = spyService.addAlternative(1L, 2L);

        // Then
        assertEquals(expectedResult, result);
        verify(alternativesRepository, times(1)).save(any(Alternatives.class));
    }

    @Test
    public void testAddAlternative_InstrumentNotFound() {
        // Given
        when(instrumentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativeService.addAlternative(999L, 2L);
        });
    }

    @Test
    public void testAddAlternative_SameSupplier() {
        // Given
        Instruments instr1WithSupplier1 = new Instruments();
        instr1WithSupplier1.setId(1L);
        instr1WithSupplier1.setSupplier(supplier1);
        instr1WithSupplier1.setCategory(category1);

        Instruments instr2WithSupplier1 = new Instruments();
        instr2WithSupplier1.setId(2L);
        instr2WithSupplier1.setSupplier(supplier1);
        instr2WithSupplier1.setCategory(category1);

        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instr1WithSupplier1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instr2WithSupplier1));

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            alternativeService.addAlternative(1L, 2L);
        });
    }

    @Test
    public void testAddAlternative_DifferentGroups() {
        // Given
        Group group2 = new Group();
        group2.setId(2L);

        SubGroup subGroup2 = new SubGroup();
        subGroup2.setId(2L);
        subGroup2.setGroup(group2);

        Category category2 = new Category();
        category2.setId(2L);
        category2.setSubGroup(subGroup2);

        Instruments instr1 = new Instruments();
        instr1.setId(1L);
        instr1.setSupplier(supplier1);
        instr1.setCategory(category1);

        Instruments instr2 = new Instruments();
        instr2.setId(2L);
        instr2.setSupplier(supplier2);
        instr2.setCategory(category2);

        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instr1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instr2));

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            alternativeService.addAlternative(1L, 2L);
        });
    }

    @Test
    public void testAddAlternative_AlternativeAlreadyExists() {
        // Given
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));
        when(alternativesRepository.findByInstr1AndInstr2(instrument1, instrument2))
                .thenReturn(Optional.of(alternative1));

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            alternativeService.addAlternative(1L, 2L);
        });
    }

    @Test
    public void testRemoveAlternative_Success() {
        // Given
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));
        when(alternativesRepository.findByInstr1AndInstr2(instrument1, instrument2))
                .thenReturn(Optional.of(alternative1));

        // Mock the findAlternativesOfCategoryAdmin method using spy
        AlternativeService spyService = spy(alternativeService);
        List<InstrumentDTO> expectedResult = new ArrayList<>();
        doReturn(expectedResult).when(spyService).findAlternativesOfCategoryAdmin(1L);

        // When
        List<InstrumentDTO> result = spyService.removeAlternative(1L, 2L);

        // Then
        assertEquals(expectedResult, result);
        verify(alternativesRepository, times(1)).delete(alternative1);
    }

    @Test
    public void testRemoveAlternative_ReverseOrder() {
        // Given
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));
        when(alternativesRepository.findByInstr1AndInstr2(instrument1, instrument2))
                .thenReturn(Optional.empty());
        when(alternativesRepository.findByInstr1AndInstr2(instrument2, instrument1))
                .thenReturn(Optional.of(alternative1));

        // Mock the findAlternativesOfCategoryAdmin method using spy
        AlternativeService spyService = spy(alternativeService);
        List<InstrumentDTO> expectedResult = new ArrayList<>();
        doReturn(expectedResult).when(spyService).findAlternativesOfCategoryAdmin(1L);

        // When
        List<InstrumentDTO> result = spyService.removeAlternative(1L, 2L);

        // Then
        assertEquals(expectedResult, result);
        verify(alternativesRepository, times(1)).delete(alternative1);
    }

    @Test
    public void testRemoveAlternative_InstrumentNotFound() {
        // Given
        when(instrumentRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            alternativeService.removeAlternative(999L, 2L);
        });
    }

    @Test
    public void testRemoveAlternative_AlternativeNotFound() {
        // Given
        when(instrumentRepository.findById(1L)).thenReturn(Optional.of(instrument1));
        when(instrumentRepository.findById(2L)).thenReturn(Optional.of(instrument2));
        when(alternativesRepository.findByInstr1AndInstr2(any(), any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> {
            alternativeService.removeAlternative(1L, 2L);
        });
    }
}