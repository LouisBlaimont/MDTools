package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.repositories.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExcelImportServiceTest {

    @Mock private InstrumentRepository instrumentRepository;
    @Mock private SubGroupRepository subGroupRepository;
    @Mock private CharacteristicRepository characteristicRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryCharacteristicRepository categoryCharacteristicRepository;
    @Mock private CharacteristicAbbreviationService abbreviationService;
    @Mock private AlternativesRepository alternativesRepository;

    @InjectMocks private ExcelImportService excelImportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessImport_SubGroup() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("SubGroup");
        dto.setGroupName("Group A");
        dto.setSubGroupName("Subgroup A");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF123");
        dto.setData(List.of(row));

        SubGroup mockSubGroup = new SubGroup();
        mockSubGroup.setName("Subgroup A");
        mockSubGroup.setSubGroupCharacteristics(List.of());

        when(subGroupRepository.findByName("Subgroup A")).thenReturn(Optional.of(mockSubGroup));
        when(instrumentRepository.findByReferenceIgnoreCase("ref123")).thenReturn(Optional.empty());

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(subGroupRepository, times(1)).findByName("Subgroup A");
        verify(instrumentRepository, times(1)).save(any(Instruments.class));
    }

    @Test
    void testProcessImport_NonCategorized_CreatesNewInstrument() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("NonCategorized");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF001");
        dto.setData(List.of(row));

        when(instrumentRepository.findByReferenceIgnoreCase("ref001")).thenReturn(Optional.empty());

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, times(1)).save(any(Instruments.class));
    }

    @Test
    void testProcessImport_NonCategorized_SkipsEmptyReference() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("NonCategorized");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "   "); // Vide après trim
        dto.setData(List.of(row));

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, never()).save(any());
    }
    @Test
    void testProcessImport_NonCategorized_UpdatesExistingInstrument() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("NonCategorized");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF123");
        row.put("supplier_description", "New Desc");
        dto.setData(List.of(row));

        Instruments existingInstrument = new Instruments();
        existingInstrument.setReference("ref123");
        existingInstrument.setSupplierDescription("Old Desc");

        when(instrumentRepository.findByReferenceIgnoreCase("ref123")).thenReturn(Optional.of(existingInstrument));

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, times(1)).save(existingInstrument);
    }

    @Test
    void testProcessImport_Catalogue_SupplierNull_ShouldStillSaveInstruments() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Catalogue");
        dto.setSupplier("MissingSupplier");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF001");
        dto.setData(List.of(row));

        when(supplierRepository.findAll()).thenReturn(List.of());

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, atLeastOnce()).save(any()); // supplier was null, but instrument saved
    }

    @Test
    void testProcessImport_Catalogue_SetsObsoleteWhenMissingFromImport() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Catalogue");
        dto.setSupplier("MySupplier");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF001");
        dto.setData(List.of(row));

        Supplier supplier = new Supplier();
        supplier.setSupplierName("MySupplier");

        Instruments oldInstrument = new Instruments();
        oldInstrument.setReference("ref002");
        oldInstrument.setObsolete(false);
        oldInstrument.setSupplier(supplier);

        when(supplierRepository.findAll()).thenReturn(List.of(supplier));
        when(instrumentRepository.findAllBySupplier(supplier)).thenReturn(List.of(oldInstrument));
        when(instrumentRepository.findByReferenceIgnoreCase("ref001")).thenReturn(Optional.empty());

        // Act
        excelImportService.processImport(dto);

        // Assert
        assertTrue(oldInstrument.getObsolete());
        verify(instrumentRepository, times(1)).save(oldInstrument); // updated to obsolete
    }
    @Test
    void testProcessImport_Catalogue_ReactivatesInstrumentIfPresent() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Catalogue");
        dto.setSupplier("MySupplier");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF001");
        dto.setData(List.of(row));

        Supplier supplier = new Supplier();
        supplier.setSupplierName("MySupplier");

        Instruments existing = new Instruments();
        existing.setReference("ref001");
        existing.setObsolete(true);
        existing.setSupplier(supplier);

        when(supplierRepository.findAll()).thenReturn(List.of(supplier));
        when(instrumentRepository.findAllBySupplier(supplier)).thenReturn(List.of(existing));
        when(instrumentRepository.findByReferenceIgnoreCase("ref001")).thenReturn(Optional.of(existing));

        // Act
        excelImportService.processImport(dto);

        // Assert
        assertFalse(existing.getObsolete());
        verify(instrumentRepository, times(1)).save(existing);
    }

    @Test
    void testProcessImport_Catalogue_SavesNewInstrument() {
        // Arrange
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Catalogue");
        dto.setSupplier("MySupplier");

        Map<String, Object> row = new HashMap<>();
        row.put("reference", "REF003");
        dto.setData(List.of(row));

        Supplier supplier = new Supplier();
        supplier.setSupplierName("MySupplier");

        when(supplierRepository.findAll()).thenReturn(List.of(supplier));
        when(instrumentRepository.findByReferenceIgnoreCase("ref003")).thenReturn(Optional.empty());

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, atLeastOnce()).save(any(Instruments.class));
    }

    @Test
    void testProcessImport_Alternatives_IgnoreEmptyOrSameRefs() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Alternatives");

        Map<String, Object> invalid1 = Map.of("ref_1", "REF1", "ref_2", "");      // empty
        Map<String, Object> invalid2 = Map.of("ref_1", "REF2", "ref_2", "REF2");  // same
        Map<String, Object> invalid3 = new HashMap<>();
        invalid3.put("ref_1", null);
        invalid3.put("ref_2", "REF3");
        
        dto.setData(List.of(invalid1, invalid2, invalid3));

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, never()).save(any());
        verify(alternativesRepository, never()).save(any());
    }

    @Test
    void testProcessImport_Alternatives_CreatesInstrumentsIfMissing() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Alternatives");

        Map<String, Object> row = Map.of("ref_1", "REF_A", "ref_2", "REF_B");
        dto.setData(List.of(row));

        when(instrumentRepository.findByReferenceIgnoreCase("ref_a")).thenReturn(Optional.empty());
        when(instrumentRepository.findByReferenceIgnoreCase("ref_b")).thenReturn(Optional.empty());
        when(alternativesRepository.existsById(any())).thenReturn(false);

        Instruments createdA = new Instruments(); createdA.setId(1L); createdA.setReference("ref_a");
        Instruments createdB = new Instruments(); createdB.setId(2L); createdB.setReference("ref_b");

        when(instrumentRepository.save(any())).thenReturn(createdA, createdB);

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, times(2)).save(any(Instruments.class));
        verify(alternativesRepository, times(1)).save(any());
    }

    @Test
    void testProcessImport_Alternatives_SkipIfRelationExists() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Alternatives");

        Map<String, Object> row = Map.of("ref_1", "REF_X", "ref_2", "REF_Y");
        dto.setData(List.of(row));

        Instruments a = new Instruments(); a.setId(10L); a.setReference("ref_x");
        Instruments b = new Instruments(); b.setId(20L); b.setReference("ref_y");

        when(instrumentRepository.findByReferenceIgnoreCase("ref_x")).thenReturn(Optional.of(a));
        when(instrumentRepository.findByReferenceIgnoreCase("ref_y")).thenReturn(Optional.of(b));
        when(alternativesRepository.existsById(any())).thenReturn(true); // simulate AB or BA exists

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(alternativesRepository, never()).save(any());
    }

    @Test
    void testProcessImport_Alternatives_SavesNewRelation() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Alternatives");

        Map<String, Object> row = Map.of("ref_1", "REF_M", "ref_2", "REF_N");
        dto.setData(List.of(row));

        Instruments m = new Instruments(); m.setId(100L); m.setReference("ref_m");
        Instruments n = new Instruments(); n.setId(200L); n.setReference("ref_n");

        when(instrumentRepository.findByReferenceIgnoreCase("ref_m")).thenReturn(Optional.of(m));
        when(instrumentRepository.findByReferenceIgnoreCase("ref_n")).thenReturn(Optional.of(n));
        when(alternativesRepository.existsById(any())).thenReturn(false);

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(alternativesRepository, times(1)).save(any());
    }
    @Test
    void testProcessImport_Crossref_EmptyRowIgnored() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Crossref");

        Map<String, Object> row = new HashMap<>();
        row.put("col1", null);
        row.put("col2", "");

        dto.setData(List.of(row));

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, never()).save(any());
    }
    @Test
    void testProcessImport_Crossref_ApplySharedCategoryToOthers() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Crossref");

        Map<String, Object> row = Map.of("reference1", "REF1", "reference2", "REF2");
        dto.setData(List.of(row));

        Category sharedCategory = new Category();

        Instruments instr1 = new Instruments();
        instr1.setReference("ref1");
        instr1.setCategory(sharedCategory);

        Instruments instr2 = new Instruments();
        instr2.setReference("ref2");
        instr2.setCategory(null);

        when(instrumentRepository.findByReferenceIgnoreCase("ref1")).thenReturn(Optional.of(instr1));
        when(instrumentRepository.findByReferenceIgnoreCase("ref2")).thenReturn(Optional.of(instr2));
        when(supplierRepository.findAll()).thenReturn(List.of());

        ArgumentCaptor<Instruments> captor = ArgumentCaptor.forClass(Instruments.class);

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, atLeastOnce()).save(captor.capture());
        List<Instruments> saved = captor.getAllValues();

        boolean ref2Updated = saved.stream()
            .anyMatch(instr -> "ref2".equals(instr.getReference()) &&
                            sharedCategory.equals(instr.getCategory()));

        assertTrue(ref2Updated, "Instrument with reference 'ref2' and shared category not saved");
    }


    @Test
    void testProcessImport_Crossref_CreatesInstrumentIfMissing() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Crossref");

        Map<String, Object> row = Map.of("reference1", "REF1", "reference2", "REF2");
        dto.setData(List.of(row));

        when(instrumentRepository.findByReferenceIgnoreCase("ref1")).thenReturn(Optional.empty());
        when(instrumentRepository.findByReferenceIgnoreCase("ref2")).thenReturn(Optional.empty());
        when(supplierRepository.findAll()).thenReturn(List.of());

        ArgumentCaptor<Instruments> captor = ArgumentCaptor.forClass(Instruments.class);

        // Act
        excelImportService.processImport(dto);

        // Assert: should save 2 instruments
        verify(instrumentRepository, times(2)).save(captor.capture());

        List<Instruments> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(instr -> instr.getReference().equalsIgnoreCase("ref1")));
        assertTrue(saved.stream().anyMatch(instr -> instr.getReference().equalsIgnoreCase("ref2")));
    }


    @Test
    void testProcessImport_Crossref_AddsSupplierIfMissing() {
        ImportRequestDTO dto = new ImportRequestDTO();
        dto.setImportType("Crossref");

        Map<String, Object> row = new HashMap<>();
        row.put("reference1", "REF_X");
        row.put("supplier", "MySupplier"); // champ classique
        dto.setData(List.of(row));

        Instruments instr = new Instruments();
        instr.setReference("ref_x");
        instr.setSupplier(null);

        Supplier supplier = new Supplier();
        supplier.setSupplierName("MySupplier");

        when(instrumentRepository.findByReferenceIgnoreCase("ref_x")).thenReturn(Optional.of(instr));
        when(supplierRepository.findAll()).thenReturn(List.of(supplier));

        ArgumentCaptor<Instruments> captor = ArgumentCaptor.forClass(Instruments.class);

        // Act
        excelImportService.processImport(dto);

        // Assert
        verify(instrumentRepository, atLeastOnce()).save(captor.capture());
        List<Instruments> savedInstruments = captor.getAllValues();

        boolean found = savedInstruments.stream()
            .anyMatch(instrSaved -> "ref_x".equals(instrSaved.getReference()) &&
                                    supplier.equals(instrSaved.getSupplier()));

        assertTrue(found, "Instrument with reference 'ref_x' and supplier 'MySupplier' not saved");
    }

    @Test
    void testGetOrCreateSupplier_CreatesNewIfNotFound() {
        // Arrange
        Map<String, Object> row = Map.of("supplier", "NewSupplier");
        Set<String> availableColumns = Set.of("supplier");

        when(supplierRepository.findAll()).thenReturn(List.of());

        ArgumentCaptor<Supplier> captor = ArgumentCaptor.forClass(Supplier.class);

        // Act
        Supplier created = excelImportService.getOrCreateSupplier(row, availableColumns, null);

        // Assert
        assertNotNull(created);
        verify(supplierRepository).save(captor.capture());

        Supplier saved = captor.getValue();
        assertEquals("newsupplier", saved.getSupplierName());
    }

    @Test
    void testUpdateExistingInstrument_UpdatesCategoryIfDifferent() {
        // Arrange
        Instruments instrument = new Instruments();
        instrument.setReference("ref001");
        instrument.setCategory(new Category()); // ancienne catégorie

        SubGroup subGroup = new SubGroup();
        subGroup.setName("Test Subgroup");

        // Nouvelle catégorie que retournera getOrCreateCategory
        Category newCategory = new Category();
        newCategory.setId(123L);

        Map<String, Object> row = Map.of("reference", "REF001");
        Set<String> availableColumns = Set.of("reference");
        List<String> characteristics = List.of();

        when(categoryRepository.findBySubGroup(any(), any())).thenReturn(List.of());
        when(characteristicRepository.findByName(any())).thenReturn(Optional.empty());

        // Act
        boolean updated = excelImportService.updateExistingInstrument(
                instrument, row, subGroup, availableColumns, characteristics, true);

        // Assert
        assertTrue(updated, "Instrument should be marked as updated due to category change");
        assertNotNull(instrument.getCategory());
    }
    @Test
    void testMatchCategory_ReturnsTrueWhenAllCharacteristicsMatch() {
        // Arrange
        Category category = new Category();
        category.setId(1L);

        Map<String, String> instrumentCharacteristics = Map.of(
            "Color", "Blue",
            "Size", "Large"
        );

        when(categoryRepository.findCharacteristicVal(1L, "Color")).thenReturn(Optional.of("Blue"));
        when(categoryRepository.findCharacteristicVal(1L, "Size")).thenReturn(Optional.of("Large"));

        // Act
        boolean result = excelImportService.matchCategory(category, instrumentCharacteristics);

        // Assert
        assertTrue(result);
    }
    @Test
    void testGetBooleanValue_TrueAndFalseStrings() {
        Map<String, Object> row = new HashMap<>();
        row.put("obsolete", "oui");

        Set<String> availableColumns = Set.of("obsolete");

        boolean value = excelImportService.getBooleanValue(row, availableColumns, "obsolete");
        assertTrue(value);

        row.put("obsolete", "non");
        value = excelImportService.getBooleanValue(row, availableColumns, "obsolete");
        assertFalse(value);
    }
    @Test
    void testCleanString_RemovesQuotesAndAccentsAndTrims() throws Exception {
        String raw = "\" Éléphant \"";

        // Appel indirect via méthode privée : utilise la reflection
        var method = ExcelImportService.class.getDeclaredMethod("cleanString", Object.class);
        method.setAccessible(true);

        String result = (String) method.invoke(excelImportService, raw);

        assertEquals("elephant", result);
    }
    @Test
    void testNormalizeString_RemovesAccentsAndLowercases() {
        String raw = "ÉléGANT";

        String result = excelImportService.normalizeString(raw);

        assertEquals("elegant", result);
    }
    @Test
    void testGetSoldByMdValue_ReturnsTrueWhenOui() throws Exception {
        Map<String, Object> row = Map.of("sold_by_md", "oui");
        Set<String> availableColumns = Set.of("sold_by_md");

        Boolean result = excelImportService.getSoldByMdValue(row, availableColumns);

        assertTrue(result);
    }

    @Test
    void testGetSoldByMdValue_ReturnsFalseWhenEmptyOrMissing() {
        Map<String, Object> row1 = Map.of("sold_by_md", "");
        Map<String, Object> row2 = new HashMap<>(); // key missing
        Set<String> availableColumns = Set.of("sold_by_md");

        assertFalse(excelImportService.getSoldByMdValue(row1, availableColumns));
        assertFalse(excelImportService.getSoldByMdValue(row2, availableColumns));
    }

    @Test
    void testGetClosedValue_ReturnsTrueWhenObsolete() {
        Map<String, Object> row = Map.of("closed", "Obsolete");
        Set<String> availableColumns = Set.of("closed");

        Boolean result = excelImportService.getClosedValue(row, availableColumns);

        assertTrue(result);
    }

    @Test
    void testGetClosedValue_ReturnsFalseWhenEmptyOrMissing() {
        Map<String, Object> row1 = Map.of("closed", "");
        Map<String, Object> row2 = new HashMap<>();
        Set<String> availableColumns = Set.of("closed");

        assertFalse(excelImportService.getClosedValue(row1, availableColumns));
        assertFalse(excelImportService.getClosedValue(row2, availableColumns));
    }
    @Test
    void testGetPrice_ReturnsFloatValue_WhenNumberProvided() {
        Map<String, Object> row = Map.of("price", 42.5f);
        Set<String> availableColumns = Set.of("price");

        Float result = excelImportService.getPrice(row, availableColumns);

        assertEquals(42.5f, result);
    }

    @Test
    void testGetPrice_ReturnsZero_WhenColumnMissing() {
        Map<String, Object> row = Map.of("other", 123);
        Set<String> availableColumns = Set.of("other");

        Float result = excelImportService.getPrice(row, availableColumns);

        assertEquals(0.0f, result);
    }

    @Test
    void testGetPrice_ReturnsZero_WhenValueNotNumber() {
        Map<String, Object> row = Map.of("price", "notANumber");
        Set<String> availableColumns = Set.of("price");

        Float result = excelImportService.getPrice(row, availableColumns);

        assertEquals(0.0f, result);
    }
    @Test
    void testGetObsoleteValue_ReturnsTrue_WhenOui() {
        Map<String, Object> row = Map.of("obsolete", "oui");
        Set<String> availableColumns = Set.of("obsolete");

        Boolean result = excelImportService.getObsoleteValue(row, availableColumns);

        assertTrue(result);
    }

    @Test
    void testGetObsoleteValue_ReturnsFalse_WhenFaux() {
        Map<String, Object> row = Map.of("obsolete", "faux");
        Set<String> availableColumns = Set.of("obsolete");

        Boolean result = excelImportService.getObsoleteValue(row, availableColumns);

        assertFalse(result);
    }

    @Test
    void testGetObsoleteValue_ReturnsFalse_WhenColumnMissing() {
        Map<String, Object> row = new HashMap<>();
        Set<String> availableColumns = Set.of();

        Boolean result = excelImportService.getObsoleteValue(row, availableColumns);

        assertFalse(result);
    }
    @Test
    void testExtractCharacteristics_ReturnsCorrectMap() {
        Map<String, Object> row = new HashMap<>();
        row.put("Length", "12 cm");
        row.put("Color", "Blue");
        row.put("Unused", "ignore me");

        List<String> characteristics = List.of("Length", "Color");

        Map<String, String> result = excelImportService.extractCharacteristics(row, characteristics);

        assertEquals(2, result.size());
        assertEquals("12 cm", result.get("Length"));
        assertEquals("blue", result.get("Color"));
    }

    @Test
    void testExtractCharacteristics_MissingValueReturnsEmptyString() {
        Map<String, Object> row = new HashMap<>();
        row.put("Length", null);

        List<String> characteristics = List.of("Length");

        Map<String, String> result = excelImportService.extractCharacteristics(row, characteristics);

        assertEquals(1, result.size());
        assertEquals("", result.get("Length"));
    }

    @Test
    void testMatchCategory_ReturnsTrue_WhenAllValuesMatch() {
        Category category = new Category();
        category.setId(1L);

        Map<String, String> instrumentCharacteristics = Map.of(
            "material", "steel",
            "color", "black"
        );

        when(categoryRepository.findCharacteristicVal(1L, "material")).thenReturn(Optional.of("steel"));
        when(categoryRepository.findCharacteristicVal(1L, "color")).thenReturn(Optional.of("black"));

        boolean result = excelImportService.matchCategory(category, instrumentCharacteristics);

        assertTrue(result);
    }

    @Test
    void testMatchCategory_ReturnsFalse_WhenOneMismatch() {
        Category category = new Category();
        category.setId(1L);

        Map<String, String> instrumentCharacteristics = Map.of(
            "material", "steel",
            "color", "white"  // mismatch here
        );

        when(categoryRepository.findCharacteristicVal(1L, "material")).thenReturn(Optional.of("steel"));
        when(categoryRepository.findCharacteristicVal(1L, "color")).thenReturn(Optional.of("black"));

        boolean result = excelImportService.matchCategory(category, instrumentCharacteristics);

        assertFalse(result);
    }

    @Test
    void testMatchCategory_ReturnsFalse_WhenMissingCharacteristic() {
        Category category = new Category();
        category.setId(1L);

        Map<String, String> instrumentCharacteristics = Map.of(
            "material", "steel",
            "length", "10 cm"
        );

        when(categoryRepository.findCharacteristicVal(1L, "material")).thenReturn(Optional.of("steel"));
        when(categoryRepository.findCharacteristicVal(1L, "length")).thenReturn(Optional.empty()); // missing

        boolean result = excelImportService.matchCategory(category, instrumentCharacteristics);

        assertFalse(result);
    }

    
    @Test
    void testGetBooleanValue_WithTrueStrings() {
        Map<String, Object> row = new HashMap<>();
        row.put("obsolete", "oui");
        Set<String> availableColumns = Set.of("obsolete");
    
        assertTrue(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    
        row.put("obsolete", "yes");
        assertTrue(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    
        row.put("obsolete", "true");
        assertTrue(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    
        row.put("obsolete", "marque propre");
        assertTrue(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    }
    
    @Test
    void testGetBooleanValue_WithFalseStrings() {
        Map<String, Object> row = new HashMap<>();
        row.put("obsolete", "non");
        Set<String> availableColumns = Set.of("obsolete");
    
        assertFalse(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    
        row.put("obsolete", "no");
        assertFalse(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    
        row.put("obsolete", "nee");
        assertFalse(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    }
    
    @Test
    void testGetBooleanValue_WithInvalidString() {
        Map<String, Object> row = new HashMap<>();
        row.put("obsolete", "random");
        Set<String> availableColumns = Set.of("obsolete");
    
        assertFalse(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    }
    
    @Test
    void testGetBooleanValue_WithBooleanInput() {
        Map<String, Object> row = new HashMap<>();
        row.put("obsolete", true);
        Set<String> availableColumns = Set.of("obsolete");
    
        assertTrue(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    
        row.put("obsolete", false);
        assertFalse(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    }
    
    @Test
    void testGetBooleanValue_KeyMissing() {
        Map<String, Object> row = new HashMap<>();
        Set<String> availableColumns = Set.of(); // clé absente
    
        assertFalse(excelImportService.getBooleanValue(row, availableColumns, "obsolete"));
    }
    
    @Test
    void testGetSoldByMdValue_DefaultsToFalseWhenMissingOrEmpty() {
        Map<String, Object> row = new HashMap<>();
        Set<String> availableColumns = Set.of();
    
        assertFalse(excelImportService.getSoldByMdValue(row, availableColumns));
    
        row.put("sold_by_md", "");
        availableColumns = Set.of("sold_by_md");
        assertFalse(excelImportService.getSoldByMdValue(row, availableColumns));
    }
    
    @Test
    void testGetSoldByMdValue_ReturnsCorrectValue() {
        Map<String, Object> row = new HashMap<>();
        row.put("sold_by_md", "oui");
        Set<String> availableColumns = Set.of("sold_by_md");
    
        assertTrue(excelImportService.getSoldByMdValue(row, availableColumns));
    }
  
    @Test
    void testGetClosedValue_DefaultsToFalseWhenMissingOrEmpty() {
        Map<String, Object> row = new HashMap<>();
        Set<String> availableColumns = Set.of();
    
        assertFalse(excelImportService.getClosedValue(row, availableColumns));
    
        row.put("closed", "");
        availableColumns = Set.of("closed");
        assertFalse(excelImportService.getClosedValue(row, availableColumns));
    }
    
    @Test
    void testGetClosedValue_ReturnsCorrectValue() {
        Map<String, Object> row = new HashMap<>();
        row.put("closed", "yes");
        Set<String> availableColumns = Set.of("closed");
    
        assertTrue(excelImportService.getClosedValue(row, availableColumns));
    }
    @Test
    void testCreateNewCategory_WithNewCharacteristicAndAbbreviation() {
        // Arrange
        SubGroup subGroup = new SubGroup();
        subGroup.setId(1L);

        Map<String, String> characteristics = Map.of("Color", "Red");
        Set<String> columns = Set.of("abbreviation_Color");
        Map<String, Object> row = Map.of("abbreviation_Color", "R");

        when(characteristicRepository.findByName("Color")).thenReturn(Optional.empty());
        when(abbreviationService.getAbbreviation("Red")).thenReturn(Optional.empty());

        Characteristic createdChar = new Characteristic("Color");
        createdChar.setId(10L);
        when(characteristicRepository.save(any())).thenReturn(createdChar);

        // Act
        Category newCategory = excelImportService.createNewCategory(subGroup, characteristics, columns, row);

        // Assert
        assertNotNull(newCategory);
        verify(categoryRepository, times(1)).save(newCategory);
        verify(characteristicRepository, times(1)).save(any(Characteristic.class));
        verify(categoryCharacteristicRepository, times(1)).save(any(CategoryCharacteristic.class));
        verify(abbreviationService, times(1)).addAbbreviation("Red", "R");
    }

    @Test
    void testCreateNewCategory_ExistingCharacteristicWithAbbreviation() {
        // Arrange
        SubGroup subGroup = new SubGroup();
        subGroup.setId(1L);

        Characteristic existingChar = new Characteristic("Color");
        existingChar.setId(10L);

        Map<String, String> characteristics = Map.of("Color", "Red");
        Set<String> columns = Set.of("abbreviation_Color");
        Map<String, Object> row = Map.of("abbreviation_Color", "R");

        when(characteristicRepository.findByName("Color")).thenReturn(Optional.of(existingChar));
        when(categoryCharacteristicRepository.findByCharacteristicId(10L)).thenReturn(List.of());
        when(abbreviationService.getAbbreviation("Red")).thenReturn(Optional.empty());

        // Act
        Category newCategory = excelImportService.createNewCategory(subGroup, characteristics, columns, row);

        // Assert
        assertNotNull(newCategory);
        verify(categoryCharacteristicRepository, times(1)).save(any());
        verify(abbreviationService, times(1)).addAbbreviation("Red", "R");
    }

    @Test
    void testCreateNewCategory_ExistingCharAndValueWithAbbreviationAlreadySet() {
        // Arrange
        SubGroup subGroup = new SubGroup();
        subGroup.setId(1L);

        Characteristic existingChar = new Characteristic("Color");
        existingChar.setId(10L);

        CategoryCharacteristic existingCC = new CategoryCharacteristic();
        existingCC.setVal("Red");

        Map<String, String> characteristics = Map.of("Color", "Red");
        Set<String> columns = Set.of("abbreviation_Color");
        Map<String, Object> row = Map.of("abbreviation_Color", "R");

        when(characteristicRepository.findByName("Color")).thenReturn(Optional.of(existingChar));
        when(categoryCharacteristicRepository.findByCharacteristicId(10L)).thenReturn(List.of(existingCC));
        when(abbreviationService.getAbbreviation("Red")).thenReturn(Optional.of("R"));

        // Act
        Category newCategory = excelImportService.createNewCategory(subGroup, characteristics, columns, row);

        // Assert
        assertNotNull(newCategory);
        verify(abbreviationService, never()).addAbbreviation(any(), any());
    }
}
