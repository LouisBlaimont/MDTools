package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
@RequestMapping("/api/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<CategoryDTO>> getCategoryFromGroup(@PathVariable String groupName) throws ResourceNotFoundException {
        List<CategoryDTO> categories = categoryService.findCategoriesOfGroup(groupName);

        if (categories == null || categories.isEmpty()) {
            throw new ResourceNotFoundException("No categories found for the group name: " + groupName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesFromSubGroup(@PathVariable String subGroupName) throws ResourceNotFoundException {
        List<CategoryDTO> categories = categoryService.findCategoriesOfSubGroup(subGroupName);
        if (categories == null || categories.isEmpty()){
            throw new ResourceNotFoundException("No categories found for the subgroup name :" + subGroupName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @PostMapping("/{id}/picture")
    public ResponseEntity<CategoryDTO> setGroupPicture(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws ResourceNotFoundException {
        CategoryDTO categoryUpdated = null;
        categoryUpdated = categoryService.setCategoryPicture(id, file);
        
        return ResponseEntity.status(HttpStatus.OK).body(categoryUpdated);
    }

    @PostMapping("/search/by-characteristics")
    public ResponseEntity<?> searchCategoriesByCharacteristics(@RequestBody Map<String, Object> body){
        List<CategoryDTO> categories = categoryService.findCategoriesByCharacteristics(body);
        if (categories == null || categories.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No categories found for the given characteristics");
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
           
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryFromId(@PathVariable Integer id) {
        List<CharacteristicDTO> charList = categoryService.findCategoryById(id);
        if (charList == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No category found for the id :" +id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(charList);
    }
    
    
    
}
