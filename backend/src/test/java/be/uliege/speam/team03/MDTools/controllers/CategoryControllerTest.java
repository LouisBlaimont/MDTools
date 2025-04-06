package be.uliege.speam.team03.MDTools.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.GroupMapper;
import be.uliege.speam.team03.MDTools.mapper.SubGroupMapper;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import be.uliege.speam.team03.MDTools.services.GroupService;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SubGroupService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

      @Mock
      private CategoryService categoryService;

   @Mock
   private InstrumentService instrumentService;

   @Mock
   private GroupService groupService;

   @Mock
   private SubGroupService subGroupService;

   @InjectMocks
   private CategoryController categoryController;

      @Mock
      private GroupService groupService;
   @BeforeEach
   void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
   }

   @Test
   void getCategoryFromGroup_ShouldReturnNotFound_WhenNoCategoriesExist() throws Exception {
      // Arrange
      String groupName = "NonExistentGroup";
      when(categoryService.findCategoriesOfGroup(eq(groupName), any(Pageable.class))).thenReturn(Page.empty());

            // Act & Assert
            mockMvc.perform(get("/api/category/group/{groupName}", groupName)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value("Name1"))
                        .andExpect(jsonPath("$[1].name").value("Name2"));


      verify(categoryService, times(1)).findCategoriesOfGroup(eq(groupName), any(Pageable.class));
   }

   @Test
   void getCategoryFromGroup_ShouldReturnPageOfCategories() throws Exception {
      // Arrange
      String groupName = "testGroup";
      List<CategoryDTO> categoryList = Arrays.asList(
         new CategoryDTO(1, groupName, "subgroup1", "Name1", "Function1", "Shape1", "lenAbrv1", 1L),
         new CategoryDTO(2, groupName, "subgroup2", "Name2", "Function2", "Shape2", "lenAbrv2", 2L));

      Page<CategoryDTO> categoryPage = new PageImpl<>(categoryList, PageRequest.of(0, 20), categoryList.size());
      when(categoryService.findCategoriesOfGroup(eq(groupName), any(Pageable.class))).thenReturn(categoryPage);

      // Act & Assert
      mockMvc.perform(get("/api/category/group/{groupName}", groupName)
            .param("page", "0")
            .param("size", "20")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value("Name1"))
            .andExpect(jsonPath("$.content[1].id").value(2L))
            .andExpect(jsonPath("$.content[1].name").value("Name2"));
   }

   @Test
   void getCategoriesFromSubGroup_ShouldReturnNotFound_WhenNoCategoriesExist() throws Exception {
      // Arrange
      String subGroupName = "NonExistentSubGroup";
      when(categoryService.findCategoriesOfSubGroup(eq(subGroupName), any(Pageable.class))).thenReturn(Page.empty());

            // Act & Assert
            mockMvc.perform(get("/api/category/subgroup/{subGroupName}", subGroupName)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].name").value("Name1"))
                        .andExpect(jsonPath("$[1].name").value("Name2"));

      verify(categoryService, times(1)).findCategoriesOfSubGroup(eq(subGroupName), any(Pageable.class));
   }

   @Test
   void setGroupPicture_ShouldReturnUpdatedCategory_WhenCategoryExists() throws Exception {
      // Arrange
      Long categoryId = 1L;
      CategoryDTO updatedCategory = new CategoryDTO(1, "Group1", "SubGroup1", "UpdatedName", "Function1",
            "Shape1", "lenAbrv1", categoryId);
      MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes());

      when(categoryService.setCategoryPicture(eq(categoryId), any(MultipartFile.class)))
            .thenReturn(updatedCategory);

      // Act & Assert
      mockMvc.perform(multipart("/api/category/{id}/picture", categoryId)
            .file(mockFile))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("UpdatedName"));

      verify(categoryService, times(1)).setCategoryPicture(eq(categoryId), any(MultipartFile.class));
   }

   @Test
   void setGroupPicture_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
      // Arrange
      Long nonExistentCategoryId = 999L;
      MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes());

      when(categoryService.setCategoryPicture(eq(nonExistentCategoryId), any(MultipartFile.class)))
            .thenThrow(new ResourceNotFoundException("Category not found"));

      // Act & Assert
      mockMvc.perform(multipart("/api/category/{id}/picture", nonExistentCategoryId)
            .file(mockFile))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).setCategoryPicture(eq(nonExistentCategoryId), any(MultipartFile.class));
   }

   @Test
   void searchCategoriesByCharacteristics_ShouldReturnCategories_WhenMatchingCategoriesExist() throws Exception {
      // Arrange
      Map<String, Object> characteristics = new HashMap<>();
      characteristics.put("shape", "Round");
      characteristics.put("function", "Cutting");

      List<CategoryDTO> matchingCategories = Arrays.asList(
            new CategoryDTO(1, "Group1", "SubGroup1", "Name1", "Cutting", "Round", "lenAbrv1", 1L),
            new CategoryDTO(2, "Group2", "SubGroup2", "Name2", "Cutting", "Round", "lenAbrv2", 2L));

      when(categoryService.findCategoriesByCharacteristics(any(Map.class))).thenReturn(matchingCategories);

      // Convert the map to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBody = objectMapper.writeValueAsString(characteristics);

      // Act & Assert
      mockMvc.perform(post("/api/category/search/by-characteristics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].function").value("Cutting"))
            .andExpect(jsonPath("$[0].shape").value("Round"))
            .andExpect(jsonPath("$[1].function").value("Cutting"))
            .andExpect(jsonPath("$[1].shape").value("Round"));

      verify(categoryService, times(1)).findCategoriesByCharacteristics(any(Map.class));
   }

   @Test
   void searchCategoriesByCharacteristics_ShouldReturnNotFound_WhenNoMatchingCategoriesExist() throws Exception {
      // Arrange
      Map<String, Object> characteristics = new HashMap<>();
      characteristics.put("shape", "NonExistentShape");

      when(categoryService.findCategoriesByCharacteristics(any(Map.class))).thenReturn(Collections.emptyList());

      // Convert the map to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBody = objectMapper.writeValueAsString(characteristics);

      // Act & Assert
      mockMvc.perform(post("/api/category/search/by-characteristics")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).findCategoriesByCharacteristics(any(Map.class));
   }

   @Test
   void getCategoryFromId_ShouldReturnCharacteristics_WhenCategoryExists() throws Exception {
      // Arrange
      Integer categoryId = 1;
      List<CharacteristicDTO> characteristics = Arrays.asList(
            new CharacteristicDTO("Material", "Steel", "ST"),
            new CharacteristicDTO("Color", "Silver", "SL"));

      when(categoryService.findCategoryById(categoryId)).thenReturn(characteristics);

      // Act & Assert
      mockMvc.perform(get("/api/category/{id}", categoryId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Material"))
            .andExpect(jsonPath("$[0].value").value("Steel"))
            .andExpect(jsonPath("$[0].abrev").value("ST"))
            .andExpect(jsonPath("$[1].name").value("Color"))
            .andExpect(jsonPath("$[1].value").value("Silver"))
            .andExpect(jsonPath("$[1].abrev").value("SL"));

      verify(categoryService, times(1)).findCategoryById(categoryId);
   }

   @Test
   void getCategoryFromId_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
      // Arrange
      Integer nonExistentCategoryId = 999;

      when(categoryService.findCategoryById(nonExistentCategoryId)).thenReturn(null);

      // Act & Assert
      mockMvc.perform(get("/api/category/{id}", nonExistentCategoryId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).findCategoryById(nonExistentCategoryId);
   }

   @Test
   void getInstrumentsFromCategory_ShouldReturnNotFound_WhenNoInstrumentsExist() throws Exception {
      // Arrange
      Integer categoryId = 999;

      when(instrumentService.findInstrumentsOfCatergory(categoryId)).thenReturn(null);

      // Act & Assert
      mockMvc.perform(get("/api/category/instruments/{id}", categoryId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

      verify(instrumentService, times(1)).findInstrumentsOfCatergory(categoryId);
   }

   @Test
   void addCategory_ShouldReturnNewCategory_WhenAllParametersAreValid() throws Exception {
      // Arrange
      Integer subGroupId = 1;
      Integer groupId = 5;
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("name", "New Category");
      requestBody.put("function", "Test Function");
      requestBody.put("shape", "Test Shape");
      requestBody.put("lenAbrv", "TC");

      // The DTO returned by the service after initial creation
      CategoryDTO initialCategory = new CategoryDTO(10, null, null, "New Category", "Test Function", "Test Shape",
            "TC", null);

      // The group and subgroup objects returned by their respective services
      Group group = new Group();
      group.setName("Test Group");

      SubGroup subGroup = new SubGroup();
      subGroup.setName("Test SubGroup");
      subGroup.setGroup(group);
      group.setSubGroups(Arrays.asList(subGroup));
      GroupDTO groupDTO = GroupMapper.toDto(group);
      SubGroupDTO subGroupDTO = SubGroupMapper.toDto(subGroup);

      // The final saved category with all fields populated
      CategoryDTO savedCategory = new CategoryDTO(10, "Test Group", "Test SubGroup", "New Category",
            "Test Function", "Test Shape", "TC", 10L);

      when(categoryService.addCategoryToSubGroup(any(Map.class), eq(subGroupId))).thenReturn(initialCategory);
      when(groupService.findGroupById(groupId)).thenReturn(groupDTO);
      when(subGroupService.findSubGroupById(subGroupId)).thenReturn(subGroupDTO);
      when(categoryService.save(any(CategoryDTO.class))).thenReturn(savedCategory);

      // Convert the map to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBodyJson = objectMapper.writeValueAsString(requestBody);

      // Act & Assert
      mockMvc.perform(post("/api/category/group/{groupId}/subgroup/{id}/add", groupId, subGroupId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBodyJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.groupName").value("Test Group"))
            .andExpect(jsonPath("$.subGroupName").value("Test SubGroup"))
            .andExpect(jsonPath("$.name").value("New Category"))
            .andExpect(jsonPath("$.function").value("Test Function"))
            .andExpect(jsonPath("$.shape").value("Test Shape"))
            .andExpect(jsonPath("$.lenAbrv").value("TC"));

      verify(categoryService, times(1)).addCategoryToSubGroup(any(Map.class), eq(subGroupId));
      verify(groupService, times(1)).findGroupById(groupId);
      verify(subGroupService, times(1)).findSubGroupById(subGroupId);
      verify(categoryService, times(1)).save(any(CategoryDTO.class));
   }

   @Test
   void addCategory_ShouldReturnNotFound_WhenGroupDoesNotExist() throws Exception {
      // Arrange
      Integer subGroupId = 1;
      Integer nonExistentGroupId = 999;
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("name", "New Category");

      CategoryDTO initialCategory = new CategoryDTO(10, null, null, "New Category", "Test Function", "Test Shape",
            "TC", null);

      when(categoryService.addCategoryToSubGroup(any(Map.class), eq(subGroupId))).thenReturn(initialCategory);
      when(groupService.findGroupById(nonExistentGroupId)).thenReturn(null);

      // Convert the map to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBodyJson = objectMapper.writeValueAsString(requestBody);

      // Act & Assert
      mockMvc.perform(post("/api/category/group/{groupId}/subgroup/{id}/add", nonExistentGroupId, subGroupId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBodyJson))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).addCategoryToSubGroup(any(Map.class), eq(subGroupId));
      verify(groupService, times(1)).findGroupById(nonExistentGroupId);
      verify(subGroupService, never()).findSubGroupById(anyInt());
      verify(categoryService, never()).save(any(CategoryDTO.class));
   }

   @Test
   void addCategory_ShouldReturnNotFound_WhenSubGroupDoesNotExist() throws Exception {
      // Arrange
      Integer nonExistentSubGroupId = 999;
      Integer groupId = 5;
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("name", "New Category");

      CategoryDTO initialCategory = new CategoryDTO(10, null, null, "New Category", "Test Function", "Test Shape",
            "TC", null);
      Group group = new Group();
      group.setName("Test Group");

      SubGroup subGroup = new SubGroup();
      subGroup.setName("Test SubGroup");
      subGroup.setGroup(group);
      group.setSubGroups(Arrays.asList(subGroup));

      GroupDTO groupDTO = GroupMapper.toDto(group);

      when(categoryService.addCategoryToSubGroup(any(Map.class), eq(nonExistentSubGroupId)))
            .thenReturn(initialCategory);
      when(groupService.findGroupById(groupId)).thenReturn(groupDTO);
      when(subGroupService.findSubGroupById(nonExistentSubGroupId)).thenReturn(null);

      // Convert the map to JSON
      ObjectMapper objectMapper = new ObjectMapper();
      String requestBodyJson = objectMapper.writeValueAsString(requestBody);

      // Act & Assert
      mockMvc.perform(post("/api/category/group/{groupId}/subgroup/{id}/add", groupId, nonExistentSubGroupId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBodyJson))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).addCategoryToSubGroup(any(Map.class), eq(nonExistentSubGroupId));
      verify(groupService, times(1)).findGroupById(groupId);
      verify(subGroupService, times(1)).findSubGroupById(nonExistentSubGroupId);
      verify(categoryService, never()).save(any(CategoryDTO.class));
   }
}