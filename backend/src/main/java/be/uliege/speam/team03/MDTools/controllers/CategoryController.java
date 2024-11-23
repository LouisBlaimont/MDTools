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

    public CategoryController(CategoryService service){
        this.categoryService = service;
    }


    @GetMapping("/{groupName}")
    public ResponseEntity<?> getCategoryFromGroup(@PathVariable String groupName) {
        List<CategoryDTO> category = categoryService.findAllCategory(groupName);
        if (category == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(category);
        
    }

}