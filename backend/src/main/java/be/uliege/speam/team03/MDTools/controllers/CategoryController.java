package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.services.CategoryService;



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
    
}
