package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ExcelImportServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private SubGroupRepository subGroupRepository;

    @Mock
    private CharacteristicRepository characteristicRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryCharacteristicRepository categoryCharacteristicRepository;

    @InjectMocks
    private ExcelImportService excelImportService;

    private SubGroup existingSubGroup;
    private Characteristic existingCharacteristic;
    private Suppliers existingSupplier;
    private Instruments existingInstrument;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Use an existing subgroup with initialized list
        existingSubGroup = new SubGroup("Scalpels", new Group("Surgical Instruments"));
        existingSubGroup.setSubGroupCharacteristics(new ArrayList<>()); // Prevent NullPointerException

        // Use an existing characteristic
        existingCharacteristic = new Characteristic("Blade Size");

        // Use an existing supplier
        existingSupplier = new Suppliers("NewMed", false, false);

        existingInstrument = new Instruments(existingSupplier, null, "SC123", "Old description", 100.0f, false);
    }

    @Test
    void testProcessSubGroupImport_WithExistingSubGroup() {
        // Given
        when(subGroupRepository.findByName("Scalpels")).thenReturn(Optional.of(existingSubGroup));

        List<Map<String, Object>> data = List.of(Map.of("reference", "SC123", "Blade Size", "15"));

        // When
        excelImportService.processSubGroupImport("Surgical Instruments", "Scalpels", data);

        // Then
        verify(subGroupRepository, times(1)).findByName("Scalpels");
    }

    @Test
    void testProcessInstrumentRow_WithExistingSubGroup() {
        // Given
        Map<String, Object> row = Map.of("reference", "SC123", "Blade Size", "15");
        when(instrumentRepository.findByReference("SC123")).thenReturn(Optional.empty());
        when(characteristicRepository.findByName("Blade Size")).thenReturn(Optional.of(existingCharacteristic));

        // When
        excelImportService.processInstrumentRow(row, existingSubGroup, Set.of("reference", "Blade Size"), List.of("Blade Size"));

        // Then
        verify(instrumentRepository, times(1)).save(any(Instruments.class));
        verify(categoryRepository, atMost(1)).save(any());
    }

    @Test
    void testProcessInstrumentRow_UpdateExistingInstrument() {
        // Given
        Map<String, Object> row = Map.of("reference", "SC123", "Blade Size", "20");
        Instruments existingInstrument = new Instruments();
        existingInstrument.setReference("SC123");
        existingInstrument.setSupplierDescription("Old description");

        when(instrumentRepository.findByReference("SC123")).thenReturn(Optional.of(existingInstrument));

        // When
        excelImportService.processInstrumentRow(row, existingSubGroup, Set.of("reference", "Blade Size"), List.of("Blade Size"));

        // Then
        verify(instrumentRepository, times(1)).save(existingInstrument);
    }

    @Test
    void testGetOrCreateSupplier_ExistingSupplier() {
        // Given
        when(supplierRepository.findAll()).thenReturn(List.of(existingSupplier));
        Map<String, Object> row = Map.of("supplier_name", "newmed");

        // When
        Suppliers supplier = excelImportService.getOrCreateSupplier(row, Set.of("supplier_name"));

        // Then
        assertEquals(existingSupplier, supplier);
        verify(supplierRepository, never()).save(any());
    }

    @Test
    void testCreateNewCategory_UsesExistingCharacteristic() {
        // Given
        Category existingCategory = new Category(existingSubGroup);
        when(categoryRepository.findBySubGroup(existingSubGroup)).thenReturn(Optional.of(List.of(existingCategory)));
        when(characteristicRepository.findByName("Blade Size")).thenReturn(Optional.of(existingCharacteristic));

        Map<String, String> instrumentCharacteristics = Map.of("Blade Size", "15");
        Set<String> availableColumns = Set.of("Blade Size");
        Map<String, Object> row = Map.of("Blade Size", "15");

        // When
        Category resultCategory = excelImportService.createNewCategory(existingSubGroup, instrumentCharacteristics, availableColumns, row);

        // Then
        assertEquals(existingCategory.getId(), resultCategory.getId()); // Compare IDs, not object references
        verify(categoryRepository, atMost(1)).save(any());
    }

    @Test
    void testNormalizeString() {
        // Given
        String input = "Éxámple Test";

        // When
        String normalized = excelImportService.normalizeString(input);

        // Then
        assertEquals("example test", normalized);
    }

    @Test
    void testGetBooleanValue() {
        // Given
        Map<String, Object> row = Map.of("obsolete", "yes");

        // When
        Boolean result = excelImportService.getBooleanValue(row, Set.of("obsolete"), "obsolete");

        // Then
        assertTrue(result);
    }

    @Test
    void testGetBooleanValue_DefaultFalse() {
        // Given
        Map<String, Object> row = new HashMap<>();

        // When
        Boolean result = excelImportService.getBooleanValue(row, Set.of(), "obsolete");

        // Then
        assertFalse(result);
    }

    @Test
    void testProcessSubGroupImport_Complete() {
        List<Map<String, Object>> data = List.of(
                Map.of("reference", "SC123", "Blade Size", "15"),
                Map.of("reference", "SC124", "Blade Size", "10")
        );

        when(subGroupRepository.findByName("Scalpels")).thenReturn(Optional.of(existingSubGroup));
        when(instrumentRepository.findByReference(anyString())).thenReturn(Optional.empty());
        when(characteristicRepository.findByName("Blade Size")).thenReturn(Optional.of(existingCharacteristic));

        excelImportService.processSubGroupImport("Surgical Instruments", "Scalpels", data);

        verify(instrumentRepository, times(2)).save(any(Instruments.class));
    }

    @Test
    void testProcessInstrumentRow_NullValues() {
        // Given
        Map<String, Object> row = new HashMap<>(); // Use HashMap instead of Map.of() to allow null values
        row.put("reference", "SC125");
        row.put("Blade Size", null);
    
        when(instrumentRepository.findByReference("SC125")).thenReturn(Optional.empty());
        when(subGroupRepository.findByName("Scalpels")).thenReturn(Optional.of(existingSubGroup));
    
        // When
        excelImportService.processInstrumentRow(
                row, 
                existingSubGroup, 
                Set.of("reference", "Blade Size"), 
                List.of("Blade Size")
        );
    
        // Then
        verify(instrumentRepository, times(1)).save(any(Instruments.class));
    }
}
