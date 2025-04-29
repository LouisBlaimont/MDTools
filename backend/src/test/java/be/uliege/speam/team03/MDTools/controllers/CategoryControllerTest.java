package be.uliege.speam.team03.MDTools.controllers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import be.uliege.speam.team03.MDTools.services.GroupService;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import be.uliege.speam.team03.MDTools.services.SubGroupService;

@ExtendWith(MockitoExtension.class)
public class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private InstrumentService instrumentService;

    @Mock 
    private PictureStorageService pictureStorageService;

    @Mock
    private GroupService groupService;

    @Mock
    private SubGroupService subGroupService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController).build();
    }

    @Test
    public void testFindAllCategories() throws Exception {
        // Arrange
        List<CategoryDTO> categories = Arrays.asList(
                createCategoryDTO((long) 1, "Category1", "SubGroup1", "Group1"),
                createCategoryDTO((long) 2, "Category2", "SubGroup2", "Group2"));
        
        when(categoryService.findAll()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/category/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Category1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Category2"));

        verify(categoryService, times(1)).findAll();
    }

//     @Test
//     public void testGetCategoryFromGroup() throws Exception {
//         // Arrange
//         List<CategoryDTO> categories = Arrays.asList(
//                 createCategoryDTO(1, "Category1", "SubGroup1", "Group1"),
//                 createCategoryDTO(2, "Category2", "SubGroup1", "Group1"));
        
//         Page<CategoryDTO> page = new PageImpl<>(categories);
        
//         when(categoryService.findCategoriesOfGroup(eq("Group1"), any(Pageable.class))).thenReturn(page);

//         // Act & Assert
//         mockMvc.perform(get("/api/category/group/Group1"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content[0].id").value(1))
//                 .andExpect(jsonPath("$.content[0].name").value("Category1"))
//                 .andExpect(jsonPath("$.content[1].id").value(2))
//                 .andExpect(jsonPath("$.content[1].name").value("Category2"));

//         verify(categoryService, times(1)).findCategoriesOfGroup(eq("Group1"), any(Pageable.class));
//     }

//     @Test
//     public void testGetCategoryFromGroupNotFound() throws Exception {
//         // Arrange
//         when(categoryService.findCategoriesOfGroup(eq("NonExistentGroup"), any(Pageable.class)))
//                 .thenThrow(new ResourceNotFoundException("No categories found for the group name: NonExistentGroup"));

//         // Act & Assert
//         mockMvc.perform(get("/api/category/group/NonExistentGroup"))
//                 .andExpect(status().isNotFound());

//         verify(categoryService, times(1)).findCategoriesOfGroup(eq("NonExistentGroup"), any(Pageable.class));
//     }

//     @Test
//     public void testGetCategoriesFromSubGroup() throws Exception {
//         // Arrange
//         List<CategoryDTO> categories = Arrays.asList(
//                 createCategoryDTO(1, "Category1", "SubGroup1", "Group1"),
//                 createCategoryDTO(2, "Category2", "SubGroup1", "Group1"));
        
//         Page<CategoryDTO> page = new PageImpl<>(categories);
        
//         when(categoryService.findCategoriesOfSubGroup(eq("SubGroup1"), any(Pageable.class))).thenReturn(page);

//         // Act & Assert
//         mockMvc.perform(get("/api/category/subgroup/SubGroup1"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.content[0].id").value(1))
//                 .andExpect(jsonPath("$.content[0].name").value("Category1"))
//                 .andExpect(jsonPath("$.content[1].id").value(2))
//                 .andExpect(jsonPath("$.content[1].name").value("Category2"));

//         verify(categoryService, times(1)).findCategoriesOfSubGroup(eq("SubGroup1"), any(Pageable.class));
//     }

//     @Test
//     public void testGetCategoriesFromSubGroupNotFound() throws Exception {
//         // Arrange
//         when(categoryService.findCategoriesOfSubGroup(eq("NonExistentSubGroup"), any(Pageable.class)))
//                 .thenThrow(new ResourceNotFoundException("No categories found for the subgroup name:NonExistentSubGroup"));

//         // Act & Assert
//         mockMvc.perform(get("/api/category/subgroup/NonExistentSubGroup"))
//                 .andExpect(status().isNotFound());

//         verify(categoryService, times(1)).findCategoriesOfSubGroup(eq("NonExistentSubGroup"), any(Pageable.class));
//     }

    @Test
    public void testAddCategory() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Material", "Wood");
        requestBody.put("Size", "10");

        String subGroupName = "SubGroup1";
        CategoryDTO newCategory = createCategoryDTO(3L, "NewCategory", "SubGroup1", "Group1");

        when(categoryService.addCategoryToSubGroup(requestBody, subGroupName)).thenReturn(newCategory);

        // Act & Assert
        mockMvc.perform(post("/api/category/subgroup/" + subGroupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("NewCategory"))
                .andExpect(jsonPath("$.groupName").value("Group1"))
                .andExpect(jsonPath("$.subGroupName").value("SubGroup1"));

        verify(categoryService, times(1)).addCategoryToSubGroup(requestBody, subGroupName);
    }

    @Test
    public void testAddCategorySubGroupNotFound() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("Size", "10");

        String subGroupName = "NonExistentSubGroup";

        when(categoryService.addCategoryToSubGroup(requestBody, subGroupName))
                .thenThrow(new ResourceNotFoundException("SubGroup not found"));

        // Act & Assert
        mockMvc.perform(post("/api/category/subgroup/" + subGroupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).addCategoryToSubGroup(requestBody, subGroupName);
    }

//     @Test
//     public void testSetCategoryPicture() throws Exception {
//         // Arrange
//         CategoryDTO updatedCategory = createCategoryDTO(1, "Category1", "SubGroup1", "Group1");
//         updatedCategory.setPictureId(1L);

//         MockMultipartFile file = new MockMultipartFile(
//                 "file", "category1.jpg", "image/jpeg", "test image content".getBytes());

//         when(categoryService.setCategoryPicture(1L, file)).thenReturn(updatedCategory);

//         // Act & Assert
//         mockMvc.perform(multipart("/api/category/1/picture")
//                 .file(file))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.id").value(1))
//                 .andExpect(jsonPath("$.name").value("Category1"))
//                 .andExpect(jsonPath("$.pictureUrl").value("http://example.com/category1.jpg"));

//         verify(categoryService, times(1)).setCategoryPicture(1L, file);
//     }

    @Test
    public void testSearchCategoriesByCharacteristics() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("material", "Wood");
        requestBody.put("size", "Large");

        List<CategoryDTO> matchingCategories = Arrays.asList(
                createCategoryDTO((long) 1, "Category1", "SubGroup1", "Group1"),
                createCategoryDTO((long) 2, "Category2", "SubGroup1", "Group1"));

        when(categoryService.findCategoriesByCharacteristics(requestBody)).thenReturn(matchingCategories);

        // Act & Assert
        mockMvc.perform(post("/api/category/search/by-characteristics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Category1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Category2"));

        verify(categoryService, times(1)).findCategoriesByCharacteristics(requestBody);
    }

    @Test
    public void testSearchCategoriesByCharacteristicsNotFound() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("material", "Unobtainium");

        when(categoryService.findCategoriesByCharacteristics(requestBody))
                .thenThrow(new ResourceNotFoundException("No categories found for the given characteristics"));

        // Act & Assert
        mockMvc.perform(post("/api/category/search/by-characteristics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findCategoriesByCharacteristics(requestBody);
    }

      // @Test
      // void getCategoryFromId_ShouldReturnCharacteristics_WhenCategoryExists() throws Exception {
      //       // Arrange
      //       Long categoryId = 1;
      //       List<CharacteristicDTO> characteristics = Arrays.asList(
      //             new CharacteristicDTO("Material", "Steel", "ST"),
      //             new CharacteristicDTO("Color", "Silver", "SL"));

      //       when(categoryService.findCategoryById(categoryId)).thenReturn(characteristics);

      //       // Act & Assert
      //       mockMvc.perform(get("/api/category/{id}", categoryId)
      //             .contentType(MediaType.APPLICATION_JSON))
      //             .andExpect(status().isOk())
      //             .andExpect(jsonPath("$[0].name").value("Material"))
      //             .andExpect(jsonPath("$[0].value").value("Steel"))
      //             .andExpect(jsonPath("$[0].abrev").value("ST"))
      //             .andExpect(jsonPath("$[1].name").value("Color"))
      //             .andExpect(jsonPath("$[1].value").value("Silver"))
      //             .andExpect(jsonPath("$[1].abrev").value("SL"));

      //       verify(categoryService, times(1)).findCategoryById(categoryId);
      // }
      // @Test
      // void getCategoryFromId_ShouldReturnNotFound_WhenCategoryDoesNotExist() throws Exception {
      //       // Arrange
      //       Long nonExistentCategoryId = 999;

      //       when(categoryService.findCategoryById(nonExistentCategoryId)).thenReturn(null);

      //       // Act & Assert
      //       mockMvc.perform(get("/api/category/{id}", nonExistentCategoryId)
      //             .contentType(MediaType.APPLICATION_JSON))
      //             .andExpect(status().isNotFound());

      //       verify(categoryService, times(1)).findCategoryById(nonExistentCategoryId);
      // }

    @Test
    public void testGetCategoryFromIdNotFound() throws Exception {
        // Arrange
        when(categoryService.findCategoryById(999L))
        .thenThrow(new ResourceNotFoundException("No category with ID 999 found"));

        // Act & Assert
        mockMvc.perform(get("/api/category/999"))
                .andExpect(status().isNotFound());

        verify(categoryService, times(1)).findCategoryById((long) 999);
    }

//     @Test
//     public void testGetInstrumentsFromCategory() throws Exception {
//         // Arrange
//         List<InstrumentDTO> instruments = Arrays.asList(
//                 createInstrumentDTO(1, "Instrument1"),
//                 createInstrumentDTO(2, "Instrument2"));

//         when(instrumentService.findInstrumentsOfCatergory(1)).thenReturn(instruments);

//         // Act & Assert
//         mockMvc.perform(get("/api/category/instruments/1"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$[0].id").value(1))
//                 .andExpect(jsonPath("$[0].ref").value("Instrument1"))
//                 .andExpect(jsonPath("$[1].id").value(2))
//                 .andExpect(jsonPath("$[1].ref").value("Instrument2"));

//         verify(instrumentService, times(1)).findInstrumentsOfCatergory(1);
//     }

    @Test
    public void testGetInstrumentsFromCategoryNotFound() throws Exception {
        // Arrange
        when(instrumentService.findInstrumentsOfCatergory(999L))
        .thenThrow(new ResourceNotFoundException("Category with the id 999 doesn't exist"));


        // Act & Assert
        mockMvc.perform(get("/api/category/instruments/999"))
                .andExpect(status().isNotFound());

        verify(instrumentService, times(1)).findInstrumentsOfCatergory((long) 999);
    }

//     @Test
//     public void testUpdateCategory() throws Exception {
//         // Arrange
//         List<CharacteristicDTO> characteristics = Arrays.asList(
//                 createCharacteristicDTO("1cm", "Material", "Metal"),
//                 createCharacteristicDTO("2L", "Size", "Medium"));

//         when(categoryService.updateCategoryCharacteristics(1, characteristics)).thenReturn(characteristics);

//         // Act & Assert
//         mockMvc.perform(patch("/api/category/1")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(characteristics)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$[0].id").value(1))
//                 .andExpect(jsonPath("$[0].name").value("Material"))
//                 .andExpect(jsonPath("$[0].value").value("Metal"))
//                 .andExpect(jsonPath("$[1].id").value(2))
//                 .andExpect(jsonPath("$[1].name").value("Size"))
//                 .andExpect(jsonPath("$[1].value").value("Medium"));

//         verify(categoryService, times(1)).updateCategoryCharacteristics(1, characteristics);
//     }

    @Test
    public void testGetCharacteristicValuesFromCategory() throws Exception {
        List<CharacteristicDTO> characteristics = List.of(
            new CharacteristicDTO("Material", "Wood", "WD"),
            new CharacteristicDTO("Size", "Large", "LG"),
            new CharacteristicDTO("Weight", "Heavy", "HV")
        );

        when(categoryService.findCategoryById((long)1)).thenReturn(characteristics);

        mockMvc.perform(get("/api/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Material"))
                .andExpect(jsonPath("$[0].value").value("Wood"))
                .andExpect(jsonPath("$[0].abrev").value("WD"))
                .andExpect(jsonPath("$[1].name").value("Size"))
                .andExpect(jsonPath("$[1].value").value("Large"))
                .andExpect(jsonPath("$[1].abrev").value("LG"))
                .andExpect(jsonPath("$[2].name").value("Weight"))
                .andExpect(jsonPath("$[2].value").value("Heavy"))
                .andExpect(jsonPath("$[2].abrev").value("HV"));

        verify(categoryService, times(1)).findCategoryById((long)1);
    }


    @Test
    public void testGetCharacteristicValuesFromCategoryNotFound() throws Exception {
        when(categoryService.findCategoryById((long)999)).thenReturn(null);
    
        mockMvc.perform(get("/api/category/999"));
        // Arrange

        // Act & Assert
        mockMvc.perform(get("/api/category/999/characteristics"))
                .andExpect(status().isNotFound());
    
        verify(categoryService, times(1)).findCategoryById((long)999);

    }

    // Helper methods to create test objects
    private CategoryDTO createCategoryDTO(Long id, String name, String subGroupName, String groupName) {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setId(id);
        categoryDTO.setName(name);
        categoryDTO.setSubGroupName(subGroupName);
        categoryDTO.setGroupName(groupName);
        return categoryDTO;
    }

    private CharacteristicDTO createCharacteristicDTO(String abr, String name, String value) {
        CharacteristicDTO characteristicDTO = new CharacteristicDTO();
        characteristicDTO.setAbrev(abr);
        characteristicDTO.setName(name);
        characteristicDTO.setValue(value);
        return characteristicDTO;
    }

    private InstrumentDTO createInstrumentDTO(Long id, String ref) {
        InstrumentDTO instrumentDTO = new InstrumentDTO();
        instrumentDTO.setId(id);
        instrumentDTO.setReference(ref);
        return instrumentDTO;
    }

    @Test
    public void testUpdateCategory_ShouldReturnUpdatedCategory() throws Exception {
        Long categoryId = 1L;
        String subGroupName = "SubGroup1";
    
        Map<String, Object> body = new HashMap<>();
        body.put("Material", "Steel");
        body.put("Materialabrev", "ST");
    
        CategoryDTO updatedCategory = createCategoryDTO(categoryId, "UpdatedCategory", subGroupName, "Group1");
    
        when(categoryService.updateCategory(categoryId, subGroupName, body)).thenReturn(updatedCategory);
    
        mockMvc.perform(patch("/api/category/{id}/subgroup/{subGroupName}", categoryId, subGroupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("UpdatedCategory"));
    
        verify(categoryService, times(1)).updateCategory(categoryId, subGroupName, body);
    }
    
    @Test
    public void testGetCategoryFromGroup() throws Exception {
        List<CategoryDTO> categories = List.of(
            createCategoryDTO(1L, "Cat1", "Sub1", "Group1"),
            createCategoryDTO(2L, "Cat2", "Sub1", "Group1")
        );
    
        when(categoryService.findCategoriesOfGroup("Group1")).thenReturn(categories);
    
        mockMvc.perform(get("/api/category/group/Group1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Cat1"))
            .andExpect(jsonPath("$[1].name").value("Cat2"));
    
        verify(categoryService, times(1)).findCategoriesOfGroup("Group1");
    }
    
    @Test
    public void testGetCategoriesFromSubGroup() throws Exception {
        List<CategoryDTO> categories = List.of(
            createCategoryDTO(1L, "Cat1", "Sub1", "Group1"),
            createCategoryDTO(2L, "Cat2", "Sub1", "Group1")
        );
    
        when(categoryService.findCategoriesOfSubGroup("Sub1")).thenReturn(categories);
    
        mockMvc.perform(get("/api/category/subgroup/Sub1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Cat1"))
            .andExpect(jsonPath("$[1].name").value("Cat2"));
    
        verify(categoryService, times(1)).findCategoriesOfSubGroup("Sub1");
    }
    
    @Test
    public void testGetInstrumentsFromCategory() throws Exception {
        List<InstrumentDTO> instruments = List.of(
            createInstrumentDTO(1L, "REF123"),
            createInstrumentDTO(2L, "REF456")
        );
    
        when(instrumentService.findInstrumentsOfCatergory(1L)).thenReturn(instruments);
    
        mockMvc.perform(get("/api/category/instruments/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].reference").value("REF123"))
            .andExpect(jsonPath("$[1].reference").value("REF456"));
    
        verify(instrumentService, times(1)).findInstrumentsOfCatergory(1L);
    }

    @Test
    public void testDeleteCategory_ShouldReturnTrue() throws Exception {
        when(categoryService.deleteCategory(1L)).thenReturn(true);
    
        mockMvc.perform(delete("/api/category/delete/1"))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    
        verify(categoryService, times(1)).deleteCategory(1L);
    }
}