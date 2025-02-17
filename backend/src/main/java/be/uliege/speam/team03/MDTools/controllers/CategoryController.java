package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;





@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService service) {
        this.categoryService = service;
    }

    @GetMapping("/group/{groupName}")
    public ResponseEntity<?> getCategoryFromGroup(@PathVariable String groupName) {
        List<CategoryDTO> categories = categoryService.findCategoriesOfGroup(groupName);

        if (categories == null || categories.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No categories found for the group name: " + groupName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<?> getCategoriesFromSubGroup(@PathVariable String subGroupName){
        List<CategoryDTO> categories = categoryService.findCategoriesOfSubGroup(subGroupName);
        if (categories == null || categories.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No categories found for the subgroup name :" + subGroupName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
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
