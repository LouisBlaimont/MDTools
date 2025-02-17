package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import be.uliege.speam.team03.MDTools.services.InstrumentService;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;
    private final InstrumentService instrumentService;

    public CategoryController(CategoryService service, InstrumentService instrumentService) {
        this.categoryService = service;
        this.instrumentService = instrumentService;
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

     // retrieve all instruments of a certain category 
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getInstrumentsOfCategory(@PathVariable Integer categoryId) {
        // calls the service corresponding with the correct function 
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsOfCatergory(categoryId);
        // checking if we found something 
        if (instruments == null || instruments.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found for the category " + categoryId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }
    
}
