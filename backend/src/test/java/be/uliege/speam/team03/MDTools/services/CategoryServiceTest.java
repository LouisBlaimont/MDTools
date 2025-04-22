package be.uliege.speam.team03.MDTools.services;

import java.util.HashMap;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.SubGroupCharacteristic;
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

      @Mock
      private PictureStorageService pictureStorageService;

      @Mock
      private CharacteristicAbbreviationService charValAbbrevService;

      @Mock
      private InstrumentService instrumentService;

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
      @Test
      void testAddCategoryToSubGroup_ShouldThrow_WhenSubGroupNotFound() {
      String subGroupName = "MissingSubGroup";
      Map<String, Object> body = Map.of("Name", "Scalpel");

      when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.addCategoryToSubGroup(body, subGroupName);
      });
      }
      
      @Test
      void testAddCategoryToSubGroup_ShouldThrow_WhenCharacteristicValueIsMissing() {
      String subGroupName = "SurgicalTools";
      Characteristic nameChar = new Characteristic();
      nameChar.setName("Name");

      SubGroup subGroup = new SubGroup();
      subGroup.setName(subGroupName);
      SubGroupCharacteristic sgc = new SubGroupCharacteristic();
      sgc.setCharacteristic(nameChar);
      subGroup.setSubGroupCharacteristics(List.of(sgc));

      when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));

      Map<String, Object> body = new HashMap<>(); // empty body

      assertThrows(BadRequestException.class, () -> {
            categoryService.addCategoryToSubGroup(body, subGroupName);
      });
      }

      @Test
      void testAddCategoryToSubGroup_ShouldThrow_WhenAbbreviationMissing() {
            String subGroupName = "SurgicalTools";

            // Characteristic that triggers abbreviation logic
            Characteristic charac = new Characteristic();
            charac.setId(1L);
            charac.setName("Material");

            Group group = new Group();
            group.setName("Surgical");

            SubGroup subGroup = new SubGroup();
            subGroup.setName(subGroupName);
            subGroup.setGroup(group);

            SubGroupCharacteristic sgc = new SubGroupCharacteristic();
            sgc.setCharacteristic(charac);
            subGroup.setSubGroupCharacteristics(List.of(sgc));

            when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
            when(categoryRepository.save(any())).thenAnswer(invocation -> {
                  Category c = invocation.getArgument(0);
                  c.setId(123L);
                  return c;
            });
            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of());
            when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.empty());

            Map<String, Object> body = new HashMap<>();
            body.put("Material", "Steel");

            BadRequestException ex = assertThrows(BadRequestException.class, () -> {
                  categoryService.addCategoryToSubGroup(body, subGroupName);
            });

            assertTrue(ex.getMessage().contains("Missing value key for the abreviation of Material"));
      }


      @Test
      void testAddCategoryToSubGroup_ShouldThrow_WhenCategoryAlreadyExists() {
            String subGroupName = "SurgicalTools";

            Characteristic charac = new Characteristic();
            charac.setId(1L);
            charac.setName("Name");

            SubGroup subGroup = new SubGroup();
            subGroup.setName(subGroupName);

            SubGroupCharacteristic sgc = new SubGroupCharacteristic();
            sgc.setCharacteristic(charac);
            subGroup.setSubGroupCharacteristics(List.of(sgc));

            Category existingCat = new Category(subGroup);
            existingCat.setId(99L);
            CategoryCharacteristic existingChar = new CategoryCharacteristic(existingCat, charac, "Scalpel");
            existingCat.setCategoryCharacteristic(List.of(existingChar));

            when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
            when(categoryRepository.save(any())).thenAnswer(invocation -> {
                  Category c = invocation.getArgument(0);
                  c.setId(123L);
                  return c;
            });
            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(existingCat));
            when(charValAbbrevService.getAbbreviation("Scalpel")).thenReturn(Optional.of("SC"));

            Map<String, Object> body = new HashMap<>();
            body.put("Name", "Scalpel");
            body.put("Nameabrev", "SC");

            assertThrows(BadRequestException.class, () -> {
                  categoryService.addCategoryToSubGroup(body, subGroupName);
            });
      }

      @Test
      void testAddCategoryToSubGroup_ShouldAddCategorySuccessfully() {
            String subGroupName = "SurgicalTools";

            // Mock characteristic
            Characteristic charac = new Characteristic();
            charac.setId(1L);
            charac.setName("Function");

            // Mock subgroup and group
            Group group = new Group();
            group.setName("Surgical");

            SubGroup subGroup = new SubGroup();
            subGroup.setName(subGroupName);
            subGroup.setGroup(group); 
            SubGroupCharacteristic sgc = new SubGroupCharacteristic();
            sgc.setCharacteristic(charac);
            subGroup.setSubGroupCharacteristics(List.of(sgc));

            when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
            when(categoryRepository.save(any())).thenAnswer(invocation -> {
                  Category c = invocation.getArgument(0);
                  c.setId(123L);
                  return c;
            });

            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of());
            when(charValAbbrevService.getAbbreviation("Cut")).thenReturn(Optional.empty());

            Map<String, Object> body = Map.of(
                  "Function", "Cut",
                  "Functionabrev", "C"
            );

            CategoryDTO result = categoryService.addCategoryToSubGroup(body, subGroupName);

            assertNotNull(result);
            assertEquals(123L, result.getId());
      }

      @Test
      void testAddCategoryToSubGroup_ShouldAddNewAbbreviation() {
          String subGroupName = "Tools";
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Group group = new Group();
          group.setName("General");
      
          SubGroup subGroup = new SubGroup();
          subGroup.setName(subGroupName);
          subGroup.setGroup(group);
          SubGroupCharacteristic sgc = new SubGroupCharacteristic();
          sgc.setCharacteristic(charac);
          subGroup.setSubGroupCharacteristics(List.of(sgc));
      
          when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
          when(categoryRepository.save(any())).thenAnswer(invocation -> {
              Category c = invocation.getArgument(0);
              c.setId(321L);
              return c;
          });
          when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of());
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.empty());
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", "ST"
          );
      
          CategoryDTO result = categoryService.addCategoryToSubGroup(body, subGroupName);
      
          assertNotNull(result);
          assertEquals(321L, result.getId());
      }

      @Test
      void testAddCategoryToSubGroup_ShouldUpdateAbbreviation() {
          String subGroupName = "Tools";
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Group group = new Group();
          group.setName("General");
      
          SubGroup subGroup = new SubGroup();
          subGroup.setName(subGroupName);
          subGroup.setGroup(group);
          SubGroupCharacteristic sgc = new SubGroupCharacteristic();
          sgc.setCharacteristic(charac);
          subGroup.setSubGroupCharacteristics(List.of(sgc));
      
          when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
          when(categoryRepository.save(any())).thenAnswer(invocation -> {
              Category c = invocation.getArgument(0);
              c.setId(789L);
              return c;
          });
          when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of());
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.of("ST"));
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", "ST2" 
          );
      
          CategoryDTO result = categoryService.addCategoryToSubGroup(body, subGroupName);
      
          assertNotNull(result);
          assertEquals(789L, result.getId());
      }
      
      @Test
      void testAddCategoryToSubGroup_ShouldIgnoreEmptyAbbreviation() {
          String subGroupName = "Tools";
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Group group = new Group();
          group.setName("General");
      
          SubGroup subGroup = new SubGroup();
          subGroup.setName(subGroupName);
          subGroup.setGroup(group);
          SubGroupCharacteristic sgc = new SubGroupCharacteristic();
          sgc.setCharacteristic(charac);
          subGroup.setSubGroupCharacteristics(List.of(sgc));
      
          when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
          when(categoryRepository.save(any())).thenAnswer(invocation -> {
              Category c = invocation.getArgument(0);
              c.setId(777L);
              return c;
          });
          when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of());
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.empty());
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", ""
          );
      
          CategoryDTO result = categoryService.addCategoryToSubGroup(body, subGroupName);
      
          assertNotNull(result);
          assertEquals(777L, result.getId());
      }

      @Test
      void testAddCategoryToSubGroup_ShouldThrow_WhenValueIsNotString() {
          String subGroupName = "Tools";
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Group group = new Group();
          group.setName("Test");
      
          SubGroup subGroup = new SubGroup();
          subGroup.setName(subGroupName);
          subGroup.setGroup(group);
          SubGroupCharacteristic sgc = new SubGroupCharacteristic();
          sgc.setCharacteristic(charac);
          subGroup.setSubGroupCharacteristics(List.of(sgc));
      
          when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
          when(categoryRepository.save(any())).thenAnswer(i -> {
              Category c = i.getArgument(0);
              c.setId(1L);
              return c;
          });
      
          Map<String, Object> body = Map.of("Material", 42); 
      
          assertThrows(BadRequestException.class, () -> {
              categoryService.addCategoryToSubGroup(body, subGroupName);
          });
      }
      

      @Test
      void testAddCategoryToSubGroup_ShouldSkipExistingCatWithNullCharacteristics() {
          String subGroupName = "Tools";
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Group group = new Group();
          group.setName("Test");
      
          SubGroup subGroup = new SubGroup();
          subGroup.setName(subGroupName);
          subGroup.setGroup(group);
      
          SubGroupCharacteristic sgc = new SubGroupCharacteristic();
          sgc.setCharacteristic(charac);
          subGroup.setSubGroupCharacteristics(List.of(sgc));
      
          Category existing = new Category(subGroup);
          existing.setId(10L);
          existing.setCategoryCharacteristic(null); // 👈 ici on couvre la branche
      
          when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
          when(categoryRepository.save(any())).thenAnswer(i -> {
              Category c = i.getArgument(0);
              c.setId(11L);
              return c;
          });
      
          when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(existing));
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.empty());
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", "ST"
          );
      
          CategoryDTO result = categoryService.addCategoryToSubGroup(body, subGroupName);
          assertNotNull(result);
      }

      @Test
      void testAddCategoryToSubGroup_ShouldDetectDifferentCategory() {
          String subGroupName = "Tools";
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Group group = new Group();
          group.setName("Test");
      
          SubGroup subGroup = new SubGroup();
          subGroup.setName(subGroupName);
          subGroup.setGroup(group);
      
          SubGroupCharacteristic sgc = new SubGroupCharacteristic();
          sgc.setCharacteristic(charac);
          subGroup.setSubGroupCharacteristics(List.of(sgc));
      
          Category existing = new Category(subGroup);
          existing.setId(10L);
          Characteristic otherChar = new Characteristic();
          otherChar.setId(2L); // different ID
          existing.setCategoryCharacteristic(List.of(new CategoryCharacteristic(existing, otherChar, "Plastic")));
      
          when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
          when(categoryRepository.save(any())).thenAnswer(i -> {
              Category c = i.getArgument(0);
              c.setId(11L);
              return c;
          });
          when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(existing));
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.empty());
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", "ST"
          );
      
          CategoryDTO result = categoryService.addCategoryToSubGroup(body, subGroupName);
          assertNotNull(result);
      }
      
      @Test
      void testUpdateCategory_ShouldThrow_WhenSubGroupNotFound() {
          when(subGroupRepository.findByName("MissingSubGroup")).thenReturn(Optional.empty());
      
          assertThrows(ResourceNotFoundException.class, () ->
              categoryService.updateCategory(1L, "MissingSubGroup", Map.of())
          );
      }

      @Test
      void testUpdateCategory_ShouldThrow_WhenCategoryNotFound() {
          SubGroup sg = new SubGroup();
          sg.setId(1L);
          when(subGroupRepository.findByName("SubGroup")).thenReturn(Optional.of(sg));
          when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
      
          assertThrows(ResourceNotFoundException.class, () ->
              categoryService.updateCategory(1L, "SubGroup", Map.of())
          );
      }
      
      @Test
      void testUpdateCategory_ShouldThrow_WhenSubGroupMismatch() {
          SubGroup subgroup = new SubGroup();
          subgroup.setId(1L);
      
          SubGroup otherGroup = new SubGroup();
          otherGroup.setId(2L);
      
          Category category = new Category(otherGroup);
          category.setId(1L);
      
          when(subGroupRepository.findByName("SubGroup")).thenReturn(Optional.of(subgroup));
          when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
      
          assertThrows(BadRequestException.class, () ->
              categoryService.updateCategory(1L, "SubGroup", Map.of())
          );
      }
      
      @Test
      void testUpdateCategory_ShouldThrow_WhenValueIsNotString() {
          setupBasicUpdateMocks();
      
          Map<String, Object> body = new HashMap<>();
          body.put("Material", 123); // ❌ mauvais type
      
          assertThrows(BadRequestException.class, () ->
              categoryService.updateCategory(1L, "SubGroup", body)
          );
      }

      @Test
      void testUpdateCategory_ShouldThrow_WhenAbbreviationIsNotString() {
          setupBasicUpdateMocks();
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.of("ST"));
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", 123 // ❌ mauvais type
          );
      
          assertThrows(BadRequestException.class, () ->
              categoryService.updateCategory(1L, "SubGroup", body)
          );
      }
      
      @Test
      void testUpdateCategory_ShouldUpdateAndAddAbbreviation() {
          setupBasicUpdateMocks();
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.empty());
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", "ST"
          );
      
          CategoryDTO result = categoryService.updateCategory(1L, "SubGroup", body);
      
          assertNotNull(result);
          assertEquals(1L, result.getId());
      }
      
      @Test
      void testUpdateCategory_ShouldUpdateAbbreviation() {
          setupBasicUpdateMocks();
          when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.of("OLD"));
      
          Map<String, Object> body = Map.of(
              "Material", "Steel",
              "Materialabrev", "NEW"
          );
      
          CategoryDTO result = categoryService.updateCategory(1L, "SubGroup", body);
      
          assertNotNull(result);
          assertEquals(1L, result.getId());
      }
      
      @Test
      void testUpdateCategory_ShouldThrow_WhenCategoryAlreadyExists() {
          SubGroup subgroup = new SubGroup();
          subgroup.setId(1L);
      
          Characteristic charac = new Characteristic();
          charac.setId(1L);
          charac.setName("Material");
      
          Category current = new Category(subgroup);
          current.setId(1L);
          CategoryCharacteristic cc = new CategoryCharacteristic(current, charac, "Steel");
      
          Category existing = new Category(subgroup);
          existing.setId(2L);
          CategoryCharacteristic ecc = new CategoryCharacteristic(existing, charac, "Steel");
          existing.setCategoryCharacteristic(List.of(ecc));
      
          when(subGroupRepository.findByName("SubGroup")).thenReturn(Optional.of(subgroup));
          when(categoryRepository.findById(1L)).thenReturn(Optional.of(current));
          when(categoryCharRepository.findByCategoryId(1L)).thenReturn(List.of(cc));
          when(categoryRepository.findBySubGroup(eq(subgroup), any())).thenReturn(List.of(current, existing));
      
          Map<String, Object> body = Map.of("Material", "Steel");
      
          assertThrows(BadRequestException.class, () ->
              categoryService.updateCategory(1L, "SubGroup", body)
          );
      }
      
      /**
       * Tests that updating a category throws a BadRequestException if another category
       * with the exact same characteristics already exists in the same subgroup.
       */
      private void setupBasicUpdateMocks() {
            SubGroup subgroup = new SubGroup();
            subgroup.setId(1L);
        
            Group group = new Group();
            group.setName("TestGroup");
            subgroup.setGroup(group); 
        
            Characteristic charac = new Characteristic();
            charac.setId(1L);
            charac.setName("Material");
        
            Category category = new Category(subgroup);
            category.setId(1L);
            CategoryCharacteristic cc = new CategoryCharacteristic(category, charac, "Old");
            cc.setCharacteristic(charac);
        
            category.setCategoryCharacteristic(List.of(cc));
        
            when(subGroupRepository.findByName("SubGroup")).thenReturn(Optional.of(subgroup));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(categoryCharRepository.findByCategoryId(1L)).thenReturn(List.of(cc));
            when(categoryRepository.findBySubGroup(eq(subgroup), any())).thenReturn(List.of(category));
            when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        }

        /**
       * Should throw BadRequestException if groupName or subGroupName is missing from body.
       */
      @Test
            void testFindCategoriesByCharacteristics_ShouldThrow_WhenGroupOrSubGroupMissing() {
            Map<String, Object> body = Map.of("groupName", "GroupOnly");

            assertThrows(BadRequestException.class, () ->
                  categoryService.findCategoriesByCharacteristics(body)
            );
      }

      /**
       * Should throw ResourceNotFoundException if group or subgroup does not exist.
       */
      @Test
            void testFindCategoriesByCharacteristics_ShouldThrow_WhenGroupOrSubGroupNotFound() {
            Map<String, Object> body = Map.of("groupName", "TestGroup", "subGroupName", "TestSubGroup");

            when(groupRepository.findByName("TestGroup")).thenReturn(Optional.empty());
            when(subGroupRepository.findByName("TestSubGroup")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () ->
                  categoryService.findCategoriesByCharacteristics(body)
            );
      }

      /**
       * Should return all categories if no filter is applied.
       */
      @Test
            void testFindCategoriesByCharacteristics_ShouldReturnAll_WhenNoFilterApplied() {
            setupBasicGroupSubgroupMocks();

            Category cat = new Category(subGroup);
            cat.setId(1L);

            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(cat));
            when(categoryCharRepository.findByCategoryIds(List.of(1L))).thenReturn(List.of());

            Map<String, Object> body = Map.of(
                  "groupName", "Group", "subGroupName", "SubGroup"
            );

            List<CategoryDTO> result = categoryService.findCategoriesByCharacteristics(body);
            assertEquals(1, result.size());
      }

      /**
       * Should return only categories matching characteristics.
       */
      @Test
      void testFindCategoriesByCharacteristics_ShouldFilterByCharacteristics() {
            setupBasicGroupSubgroupMocks();

            Category cat1 = new Category(subGroup);
            cat1.setId(1L);
            Characteristic c = new Characteristic();
            c.setName("Material");

            CategoryCharacteristic cc = new CategoryCharacteristic(cat1, c, "Steel");

            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(cat1));
            when(categoryCharRepository.findByCategoryIds(List.of(1L))).thenReturn(List.of(cc));

            Map<String, Object> body = Map.of(
                  "groupName", "Group", "subGroupName", "SubGroup",
                  "characteristics", List.of(
                        Map.of("name", "Material", "value", "Steel")
                  )
            );

            List<CategoryDTO> result = categoryService.findCategoriesByCharacteristics(body);
            assertEquals(1, result.size());
      }

      /**
       * Should return categories matching minLength and maxLength filters.
       */
      @Test
            void testFindCategoriesByCharacteristics_ShouldFilterByLengthRange() {
            setupBasicGroupSubgroupMocks();

            Category cat1 = new Category(subGroup);
            cat1.setId(1L);
            Characteristic lengthChar = new Characteristic();
            lengthChar.setName("Length");

            CategoryCharacteristic cc = new CategoryCharacteristic(cat1, lengthChar, "5.5");

            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(cat1));
            when(categoryCharRepository.findByCategoryIds(List.of(1L))).thenReturn(List.of(cc));

            Map<String, Object> body = Map.of(
                  "groupName", "Group", "subGroupName", "SubGroup",
                  "minLength", 5.0, "maxLength", 6.0
            );

            List<CategoryDTO> result = categoryService.findCategoriesByCharacteristics(body);
            assertEquals(1, result.size());
      }
      /**
       * Should return no category if length is out of bounds.
       */
      @Test
            void testFindCategoriesByCharacteristics_ShouldReturnNone_WhenLengthOutsideBounds() {
            setupBasicGroupSubgroupMocks();

            Category cat1 = new Category(subGroup);
            cat1.setId(1L);
            Characteristic lengthChar = new Characteristic();
            lengthChar.setName("Length");

            CategoryCharacteristic cc = new CategoryCharacteristic(cat1, lengthChar, "3.0");

            when(categoryRepository.findBySubGroup(eq(subGroup), any())).thenReturn(List.of(cat1));
            when(categoryCharRepository.findByCategoryIds(List.of(1L))).thenReturn(List.of(cc));

            Map<String, Object> body = Map.of(
                  "groupName", "Group", "subGroupName", "SubGroup",
                  "minLength", 4.0, "maxLength", 10.0
            );

            List<CategoryDTO> result = categoryService.findCategoriesByCharacteristics(body);
            assertTrue(result.isEmpty());
      }

      private final SubGroup subGroup = new SubGroup();

      private void setupBasicGroupSubgroupMocks() {
          subGroup.setId(1L);
          subGroup.setName("SubGroup");
      
          Group group = new Group();
          group.setName("Group");
          subGroup.setGroup(group);
      
          when(groupRepository.findByName("Group")).thenReturn(Optional.of(group));
          when(subGroupRepository.findByName("SubGroup")).thenReturn(Optional.of(subGroup));
      }

      @Test
      void testDeleteCategory_ShouldThrow_WhenCategoryNotFound() {
          when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
      
          assertThrows(ResourceNotFoundException.class, () ->
              categoryService.deleteCategory(1L)
          );
      }
      
      @Test
            void testDeleteCategory_ShouldThrow_WhenInstrumentsExist() {
            Category cat = new Category();
            cat.setId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(instrumentService.findInstrumentsOfCatergory(1L)).thenReturn(List.of(new InstrumentDTO()));

            assertThrows(BadRequestException.class, () ->
                  categoryService.deleteCategory(1L)
            );
      }

      @Test
            void testDeleteCategory_ShouldSucceed_WhenNoInstruments() {
            Category cat = new Category();
            cat.setId(1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(instrumentService.findInstrumentsOfCatergory(1L)).thenReturn(List.of());

            Boolean result = categoryService.deleteCategory(1L);
            assertTrue(result);
      }

      @Test
      void testFindAll_ShouldReturnAllCategories() {
            Group group = new Group();
            group.setName("Surgical");

            SubGroup subGroup = new SubGroup();
            subGroup.setName("Cutting");
            subGroup.setGroup(group); 

            Category cat = new Category();
            cat.setId(1L);
            cat.setSubGroup(subGroup); 

            when(categoryRepository.findAll()).thenReturn(List.of(cat));
            when(categoryRepository.findCharacteristicVal(1L, "Name")).thenReturn(Optional.of("Scalpel"));
            when(categoryRepository.findCharacteristicVal(1L, "Function")).thenReturn(Optional.of("Cutting"));

            List<CategoryDTO> result = categoryService.findAll();
            assertEquals(1, result.size());
      }


      @Test
      void testFindCategoryById_ShouldThrow_WhenNotFound() {
          when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
      
          assertThrows(ResourceNotFoundException.class, () ->
              categoryService.findCategoryById(1L)
          );
      }

      @Test
            void testFindCategoryById_ShouldReturnCharacteristics() {
            Characteristic c = new Characteristic();
            c.setName("Material");

            Category cat = new Category();
            cat.setId(1L);

            CategoryCharacteristic cc = new CategoryCharacteristic(cat, c, "Steel");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(categoryCharRepository.findByCategoryId(1L)).thenReturn(List.of(cc));
            when(charValAbbrevService.getAbbreviation("Steel")).thenReturn(Optional.of("ST"));

            List<CharacteristicDTO> result = categoryService.findCategoryById(1L);

            assertEquals(1, result.size());
            assertEquals("Material", result.get(0).getName());
            assertEquals("Steel", result.get(0).getValue());
            assertEquals("ST", result.get(0).getAbrev());
      }
      @Test
      void testSearchCategory_ShouldReturnMappedDTO() {
            Group group = new Group();
            group.setName("Ortho");

            SubGroup subGroup = new SubGroup();
            subGroup.setGroup(group);

            Category cat = new Category();
            cat.setId(1L);
            cat.setSubGroup(subGroup); 

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
            when(categoryRepository.findCharacteristicVal(1L, "Name")).thenReturn(Optional.of("Clamp"));
            when(categoryRepository.findCharacteristicVal(1L, "Function")).thenReturn(Optional.of("Hold"));

            CategoryDTO dto = categoryService.searchCategory(1L);
            assertNotNull(dto);
            assertEquals("Clamp", dto.getName());
      }

}
