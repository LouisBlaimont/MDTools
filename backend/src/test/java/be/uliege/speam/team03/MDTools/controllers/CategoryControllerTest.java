package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

   @Mock
   private CategoryService categoryService;

   @InjectMocks
   private CategoryController categoryController;

   private MockMvc mockMvc;

   @BeforeEach
   void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
   }

   @Test
   void getCategoryFromGroup_ShouldReturnCategories_WhenCategoriesExist() throws Exception {
      // Arrange
      String groupName = "TestGroup";
      Integer id_1 = 1;
      Integer id_2 = 2;
      List<CategoryDTO> categories = Arrays.asList(
            new CategoryDTO(id_1, groupName, "subgroup1", "Name1", "Function1", "Shape1", "lenAbrv1", 1L),
            new CategoryDTO(id_2, groupName, "subgroup2", "Name2", "Function2", "Shape2", "lenAbrv2", 2L));
      when(categoryService.findCategoriesOfGroup(groupName)).thenReturn(categories);

      // Act & Assert
      mockMvc.perform(get("/api/category/group/{groupName}", groupName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Name1"))
            .andExpect(jsonPath("$[1].name").value("Name2"));

      verify(categoryService, times(1)).findCategoriesOfGroup(groupName);
   }

   @Test
   void getCategoryFromGroup_ShouldReturnNotFound_WhenNoCategoriesExist() throws Exception {
      // Arrange
      String groupName = "NonExistentGroup";
      when(categoryService.findCategoriesOfGroup(groupName)).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc.perform(get("/api/category/group/{groupName}", groupName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).findCategoriesOfGroup(groupName);
   }

   @Test
   void getCategoriesFromSubGroup_ShouldReturnCategories_WhenCategoriesExist() throws Exception {
      // Arrange
      String subGroupName = "TestSubGroup";
      Integer id_1 = 1;
      Integer id_2 = 2;
      List<CategoryDTO> categories = Arrays.asList(
            new CategoryDTO(id_1, "Group1", subGroupName, "Name1", "Function1", "Shape1", "lenAbrv1", 1L),
            new CategoryDTO(id_2, "Group2", subGroupName, "Name2", "Function2", "Shape2", "lenAbrv2", 2L));
      when(categoryService.findCategoriesOfSubGroup(subGroupName)).thenReturn(categories);

      // Act & Assert
      mockMvc.perform(get("/api/category/subgroup/{subGroupName}", subGroupName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Name1"))
            .andExpect(jsonPath("$[1].name").value("Name2"));

      verify(categoryService, times(1)).findCategoriesOfSubGroup(subGroupName);
   }

   @Test
   void getCategoriesFromSubGroup_ShouldReturnNotFound_WhenNoCategoriesExist() throws Exception {
      // Arrange
      String subGroupName = "NonExistentSubGroup";
      when(categoryService.findCategoriesOfSubGroup(subGroupName)).thenReturn(Collections.emptyList());

      // Act & Assert
      mockMvc.perform(get("/api/category/subgroup/{subGroupName}", subGroupName)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

      verify(categoryService, times(1)).findCategoriesOfSubGroup(subGroupName);
   }
}