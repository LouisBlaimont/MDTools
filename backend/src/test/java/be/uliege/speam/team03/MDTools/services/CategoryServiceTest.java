package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;

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
            category.setId((long) 1);
            category.setSubGroup(subGroup);
            category.setShape("Round");

            when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
            when(subGroupRepository.findByGroup(group)).thenReturn(List.of(subGroup));
            when(categoryRepository.findAllBySubGroupIn(eq(List.of(subGroup)), any())).thenReturn(List.of(category));
            when(categoryRepository.findCharacteristicVal(1L, "Name")).thenReturn(Optional.of("Scalpel"));
            when(categoryRepository.findCharacteristicVal(1L, "Function")).thenReturn(Optional.of("Cutting"));

            // When
            List<CategoryDTO> result = categoryService.findCategoriesOfGroup(groupName);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Scalpel", result.get(0).getName());
            assertEquals("Cutting", result.get(0).getFunction());
            assertEquals("Round", result.get(0).getShape());
      }

      @Test
      void testFindCategoriesOfGroup_WhenGroupDoesNotExist() {
            // Given
            String groupName = "NonExistentGroup";
            when(groupRepository.findByName(groupName)).thenReturn(Optional.empty());

            // When & Then
            ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
                  categoryService.findCategoriesOfGroup(groupName);
            });

            assertTrue(thrown.getMessage().contains("No group found with the name NonExistentGroup"));
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
            category.setId((long) 1);
            category.setSubGroup(subGroup);
            category.setShape("Round");

            when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(category));
            when(categoryRepository.findCharacteristicVal(1L, "Name")).thenReturn(Optional.of("Scalpel"));
            when(categoryRepository.findCharacteristicVal(1L, "Function")).thenReturn(Optional.of("Cutting"));

            // When
            List<CategoryDTO> result = categoryService.findCategoriesOfSubGroup(subGroupName);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Scalpel", result.get(0).getName());
            assertEquals("Cutting", result.get(0).getFunction());
            assertEquals("Round", result.get(0).getShape());
      }

      @Test
      void testFindCategoriesOfSubGroup_WhenSubGroupDoesNotExist() {
            String subGroupName = "NonExistentSubGroup";
            when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                  categoryService.findCategoriesOfSubGroup(subGroupName);
            });
      }
}
