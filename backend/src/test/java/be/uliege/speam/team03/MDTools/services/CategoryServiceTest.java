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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

        List<Category> categories = List.of(category);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
        when(subGroupRepository.findByGroup(group)).thenReturn(List.of(subGroup));
        when(categoryRepository.findAllBySubGroupIn(List.of(subGroup), pageable)).thenReturn(categoryPage);
        when(categoryRepository.findCharacteristicVal(1L, "Name")).thenReturn(Optional.of("Scalpel"));
        when(categoryRepository.findCharacteristicVal(1L, "Function")).thenReturn(Optional.of("Cutting"));

        // When
        Page<CategoryDTO> result = categoryService.findCategoriesOfGroup(groupName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Scalpel", result.getContent().get(0).getName());
        assertEquals("Cutting", result.getContent().get(0).getFunction());
        assertEquals("Round", result.getContent().get(0).getShape());
    }

    @Test
    void testFindCategoriesOfGroup_WhenGroupDoesNotExist() {
        // Given
        String groupName = "NonExistentGroup";
        Pageable pageable = PageRequest.of(0, 10);
        when(groupRepository.findByName(groupName)).thenReturn(Optional.empty());

        // When
        Page<CategoryDTO> result = categoryService.findCategoriesOfGroup(groupName, pageable);

        // Then
        assertTrue(result.isEmpty());
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

        List<Category> categories = List.of(category);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
        when(categoryRepository.findBySubGroup(subGroup, pageable)).thenReturn(categoryPage);
        when(categoryRepository.findCharacteristicVal(1L, "Name")).thenReturn(Optional.of("Scalpel"));
        when(categoryRepository.findCharacteristicVal(1L, "Function")).thenReturn(Optional.of("Cutting"));

        // When
        Page<CategoryDTO> result = categoryService.findCategoriesOfSubGroup(subGroupName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Scalpel", result.getContent().get(0).getName());
        assertEquals("Cutting", result.getContent().get(0).getFunction());
        assertEquals("Round", result.getContent().get(0).getShape());
    }

    @Test
    void testFindCategoriesOfSubGroup_WhenSubGroupDoesNotExist() {
        // Given
        String subGroupName = "NonExistentSubGroup";
        Pageable pageable = PageRequest.of(0, 10);
        when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());

        // When
        Page<CategoryDTO> result = categoryService.findCategoriesOfSubGroup(subGroupName, pageable);

        // Then
        assertTrue(result.isEmpty());
    }
}