package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class CategoryServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private SubGroupRepository subGroupRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CharacteristicRepository characteristicRepository;

    @Mock
    private CategoryCharacteristicRepository categoryCharRepository;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindCategoriesOfGroup_WhenGroupExists() {
        // Given
        String groupName = "TestGroup";
        Group group = new Group();
        group.setName(groupName);

        SubGroup subGroup = new SubGroup();
        subGroup.setGroup(group);
        subGroup.setName("TestSubGroup");

        Category category = new Category();
        category.setId(1);
        category.setSubGroup(subGroup);
        category.setShape("Round");

        when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
        when(subGroupRepository.findByGroup(group)).thenReturn(List.of(subGroup));
        when(categoryRepository.findBySubGroupIn(List.of(subGroup))).thenReturn(Optional.of(List.of(category)));
        when(categoryRepository.findCharacteristicVal(1, "Name")).thenReturn(Optional.of("Scalpel"));
        when(categoryRepository.findCharacteristicVal(1, "Function")).thenReturn(Optional.of("Cutting"));
        when(categoryRepository.findCharacteristicValAbrv(1, "Length")).thenReturn(Optional.of("L"));

        // When
        List<CategoryDTO> result = categoryService.findCategoriesOfGroup(groupName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Scalpel", result.get(0).getName());
        assertEquals("Cutting", result.get(0).getFunction());
        assertEquals("Round", result.get(0).getShape());
        assertEquals("L", result.get(0).getLenAbrv());
    }

    @Test
    void testFindCategoriesOfGroup_WhenGroupDoesNotExist() {
        // Given
        String groupName = "NonExistentGroup";
        when(groupRepository.findByName(groupName)).thenReturn(Optional.empty());

        // When
        List<CategoryDTO> result = categoryService.findCategoriesOfGroup(groupName);

        // Then
        assertNull(result);
    }

    @Test
    void testFindCategoriesOfSubGroup_WhenSubGroupExists() {
        // Given
        String subGroupName = "TestSubGroup";
        Group group = new Group();
        group.setName("TestGroup");

        SubGroup subGroup = new SubGroup();
        subGroup.setName(subGroupName);
        subGroup.setGroup(group);

        Category category = new Category();
        category.setId(1);
        category.setSubGroup(subGroup);
        category.setShape("Round");

        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
        when(categoryRepository.findBySubGroup(subGroup)).thenReturn(Optional.of(List.of(category)));
        when(categoryRepository.findCharacteristicVal(1, "Name")).thenReturn(Optional.of("Scalpel"));
        when(categoryRepository.findCharacteristicVal(1, "Function")).thenReturn(Optional.of("Cutting"));
        when(categoryRepository.findCharacteristicValAbrv(1, "Length")).thenReturn(Optional.of("L"));

        // When
        List<CategoryDTO> result = categoryService.findCategoriesOfSubGroup(subGroupName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Scalpel", result.get(0).getName());
        assertEquals("Cutting", result.get(0).getFunction());
        assertEquals("Round", result.get(0).getShape());
        assertEquals("L", result.get(0).getLenAbrv());
    }

    @Test
    void testFindCategoriesOfSubGroup_WhenSubGroupDoesNotExist() {
        // Given
        String subGroupName = "NonExistentSubGroup";
        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());

        // When
        List<CategoryDTO> result = categoryService.findCategoriesOfSubGroup(subGroupName);

        // Then
        assertNull(result);
    }
}
