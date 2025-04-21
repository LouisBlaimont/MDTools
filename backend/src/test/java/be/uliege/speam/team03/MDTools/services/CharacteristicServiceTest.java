package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;

@ExtendWith(MockitoExtension.class)
public class CharacteristicServiceTest {

    @Mock
    private CharacteristicRepository characteristicRepository;

    @Mock
    private CategoryCharacteristicRepository categoryCharacteristicRepository;

    @Mock
    private SubGroupRepository subGroupRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CharacteristicService characteristicService;

    private List<Characteristic> mockCharacteristics;
    private List<CategoryCharacteristic> mockCategoryCharacteristics;
    private SubGroup mockSubGroup;
    private List<Category> mockCategories;

    @BeforeEach
    void setUp() {
        // Setup mock characteristics
        mockCharacteristics = Arrays.asList(
                createCharacteristic(1L, "Color"),
                createCharacteristic(2L, "Material"),
                createCharacteristic(3L, "Size"));

        // Setup mock subgroup
        mockSubGroup = new SubGroup();
        mockSubGroup.setId(1L);
        mockSubGroup.setName("TestSubGroup");

        // Setup mock categories
        mockCategories = Arrays.asList(
                createCategory(1L, mockSubGroup),
                createCategory(2L, mockSubGroup),
                createCategory(3L, mockSubGroup));

        // Setup mock category characteristics
        mockCategoryCharacteristics = Arrays.asList(
                createCategoryCharacteristic(1L, 1L, "Color", "Red"),
                createCategoryCharacteristic(1L, 1L, "Color", "Blue"),
                createCategoryCharacteristic(2L, 1L, "Color", "Green"),
                createCategoryCharacteristic(2L, 2L, "Material", "Wood"),
                createCategoryCharacteristic(3L, 2L, "Material", "Metal"),
                createCategoryCharacteristic(3L, 3L, "Size", "Small"),
                createCategoryCharacteristic(2L, 3L, "Size", "Medium"),
                createCategoryCharacteristic(1L, 3L, "Size", "Large"));
    }

    @Test
    void getAllCharacteristicNames_ReturnsAllNames() {
        // Arrange
        when(characteristicRepository.findAll()).thenReturn(mockCharacteristics);

        // Act
        List<String> result = characteristicService.getAllCharacteristicNames();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("Color", "Material", "Size")));
        verify(characteristicRepository, times(1)).findAll();
    }

    @Test
    void getAllCharacteristicNames_ReturnsEmptyList_WhenNoCharacteristicsExist() {
        // Arrange
        when(characteristicRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<String> result = characteristicService.getAllCharacteristicNames();

        // Assert
        assertTrue(result.isEmpty());
        verify(characteristicRepository, times(1)).findAll();
    }

    @Test
    void getPossibleValuesOfChar_ReturnsDistinctValues_ForValidCharAndSubGroup() throws ResourceNotFoundException {
        // Arrange
        String charName = "Color";
        String subGroupName = "TestSubGroup";
        Characteristic mockCharacteristic = mockCharacteristics.get(0); // Color characteristic

        when(characteristicRepository.findByName(charName)).thenReturn(Optional.of(mockCharacteristic));
        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(mockSubGroup));
        when(categoryRepository.findBySubGroup(eq(mockSubGroup), any())).thenReturn(mockCategories);
        when(categoryCharacteristicRepository.findByCharacteristicAndCategoryIn(
                eq(mockCharacteristic), eq(mockCategories)))
                .thenReturn(mockCategoryCharacteristics.stream()
                        .filter(cc -> cc.getCharacteristic().getName().equals(charName))
                        .collect(Collectors.toList()));

        // Act
        List<String> result = characteristicService.getPossibleValuesOfChar(charName, subGroupName);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList("Red", "Blue", "Green")));
        verify(characteristicRepository, times(1)).findByName(charName);
        verify(subGroupRepository, times(1)).findByName(subGroupName);
        verify(categoryRepository, times(1)).findBySubGroup(eq(mockSubGroup), any());
        verify(categoryCharacteristicRepository, times(1))
                .findByCharacteristicAndCategoryIn(eq(mockCharacteristic), eq(mockCategories));
    }

    @Test
    void getPossibleValuesOfChar_ThrowsException_WhenCharacteristicNotFound() {
        // Arrange
        String charName = "NonExistentChar";
        String subGroupName = "TestSubGroup";

        when(characteristicRepository.findByName(charName)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            characteristicService.getPossibleValuesOfChar(charName, subGroupName);
        });

        verify(characteristicRepository, times(1)).findByName(charName);
        verify(subGroupRepository, never()).findByName(subGroupName);
        verify(categoryRepository, never()).findBySubGroup(any(), any());
        verify(categoryCharacteristicRepository, never()).findByCharacteristicAndCategoryIn(any(), any());
    }

    @Test
    void getPossibleValuesOfChar_ThrowsException_WhenSubGroupNotFound() {
        // Arrange
        String charName = "Color";
        String subGroupName = "NonExistentSubGroup";
        Characteristic mockCharacteristic = mockCharacteristics.get(0); // Color characteristic

        when(characteristicRepository.findByName(charName)).thenReturn(Optional.of(mockCharacteristic));
        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            characteristicService.getPossibleValuesOfChar(charName, subGroupName);
        });

        verify(characteristicRepository, times(1)).findByName(charName);
        verify(subGroupRepository, times(1)).findByName(subGroupName);
        verify(categoryRepository, never()).findBySubGroup(any(), any());
        verify(categoryCharacteristicRepository, never()).findByCharacteristicAndCategoryIn(any(), any());
    }

    @Test
    void getPossibleValuesOfChar_ReturnsEmptyList_WhenNoValuesExistForCharInSubGroup()
            throws ResourceNotFoundException {
        // Arrange
        String charName = "Color";
        String subGroupName = "TestSubGroup";
        Characteristic mockCharacteristic = mockCharacteristics.get(0); // Color characteristic

        when(characteristicRepository.findByName(charName)).thenReturn(Optional.of(mockCharacteristic));
        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(mockSubGroup));
        when(categoryRepository.findBySubGroup(eq(mockSubGroup), any())).thenReturn(mockCategories);
        when(categoryCharacteristicRepository.findByCharacteristicAndCategoryIn(
                eq(mockCharacteristic), eq(mockCategories)))
                .thenReturn(Collections.emptyList());

        // Act
        List<String> result = characteristicService.getPossibleValuesOfChar(charName, subGroupName);

        // Assert
        assertTrue(result.isEmpty());
        verify(characteristicRepository, times(1)).findByName(charName);
        verify(subGroupRepository, times(1)).findByName(subGroupName);
        verify(categoryRepository, times(1)).findBySubGroup(eq(mockSubGroup), any());
        verify(categoryCharacteristicRepository, times(1))
                .findByCharacteristicAndCategoryIn(eq(mockCharacteristic), eq(mockCategories));
    }

    @Test
    void getPossibleValuesOfChar_ReturnsSortedDistinctValues() throws ResourceNotFoundException {
        // Arrange
        String charName = "Size";
        String subGroupName = "TestSubGroup";
        Characteristic mockCharacteristic = mockCharacteristics.get(2); // Size characteristic

        when(characteristicRepository.findByName(charName)).thenReturn(Optional.of(mockCharacteristic));
        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(mockSubGroup));
        when(categoryRepository.findBySubGroup(eq(mockSubGroup), any())).thenReturn(mockCategories);

        // Create list with duplicates and unsorted values
        List<CategoryCharacteristic> unsortedCharValues = Arrays.asList(
                createCategoryCharacteristic(1L, 3L, "Size", "Large"),
                createCategoryCharacteristic(2L, 3L, "Size", "Medium"),
                createCategoryCharacteristic(3L, 3L, "Size", "Small"),
                createCategoryCharacteristic(1L, 3L, "Size", "Large") // Duplicate
        );

        when(categoryCharacteristicRepository.findByCharacteristicAndCategoryIn(
                eq(mockCharacteristic), eq(mockCategories)))
                .thenReturn(unsortedCharValues);

        // Act
        List<String> result = characteristicService.getPossibleValuesOfChar(charName, subGroupName);

        // Assert
        assertEquals(3, result.size()); // Should be distinct
        // Assuming the service sorts alphabetically
        assertEquals("Large", result.get(0));
        assertEquals("Medium", result.get(1));
        assertEquals("Small", result.get(2));
    }

    // Helper methods to create test objects
    private Characteristic createCharacteristic(Long id, String name) {
        Characteristic characteristic = new Characteristic();
        characteristic.setId(id);
        characteristic.setName(name);
        return characteristic;
    }

    private Category createCategory(Long id, SubGroup subGroup) {
        Category category = new Category();
        category.setId(id);
        category.setSubGroup(subGroup);
        return category;
    }

    private CategoryCharacteristic createCategoryCharacteristic(Long categoryId, Long characteristicId,
            String characteristicName, String value) {
        CategoryCharacteristic cc = new CategoryCharacteristic();
        Characteristic characteristic = createCharacteristic(characteristicId, characteristicName);
        Category category = createCategory(categoryId, mockSubGroup);
        cc.setCharacteristic(characteristic);
        cc.setCategory(category);
        cc.setVal(value);
        return cc;
    }
}