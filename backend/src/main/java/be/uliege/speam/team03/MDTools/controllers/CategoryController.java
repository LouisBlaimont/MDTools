package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import lombok.AllArgsConstructor;

/**
 * This controller implements the API endpoints relative to the categories of
 * instruments. See the Wiki (>>2. Technical requirements>>API Specifications)
 * for more information.
 */
@RestController
@RequestMapping("/api/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final InstrumentService instrumentService;

    /**
     * Retrieves a list of all categories.
     *
     * @return a ResponseEntity containing a list of CategoryDTO objects and an HTTP
     *         status of OK
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> findAllCategories() {
        List<CategoryDTO> categories = categoryService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    /**
     * Retrieves a list of categories associated with a specific group name.
     *
     * @param groupName the name of the group whose categories are to be retrieved
     * @return a ResponseEntity containing a list of CategoryDTO objects and an HTTP
     *         status of OK
     * @throws ResourceNotFoundException If no group with the specified name is found
     */
    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<CategoryDTO>> getCategoryFromGroup(
            @PathVariable String groupName)
            throws ResourceNotFoundException {
        List<CategoryDTO> categories = categoryService.findCategoriesOfGroup(groupName);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    /**
     * Retrieves a paginated and sorted list of categories associated with a
     * specific subgroup.
     *
     * @param subGroupName the name of the subgroup for which categories are to be
     *                     retrieved
     * @param pageable     the pagination and sorting parameters
     * @return a ResponseEntity containing a Page of CategoryDTO objects and an HTTP
     *         status of OK
     * @throws ResourceNotFoundException If the given subgroup doesn't exist.
     */
    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesFromSubGroup(
            @PathVariable String subGroupName)
            throws ResourceNotFoundException {
        List<CategoryDTO> categories = categoryService.findCategoriesOfSubGroup(subGroupName);
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    /**
     * Adds a new category to a specific subgroup.
     *
     * @param body    A map containing the details of the category to be added.
     * @param id      The ID of the subgroup to which the category will be added.
     * @return A ResponseEntity containing the created CategoryDTO and a status of
     *         CREATED.
     * @throws ResourceNotFoundException If subGroupName doesn't exist
     * @throws BadRequestException If the body doesn't contain the key for all characteristics/abbreviations, 
     *                              or if he category created already exists.
     */
    @PostMapping("/subgroup/{subGroupName}")
    public ResponseEntity<CategoryDTO> addCategory(@RequestBody Map<String, Object> body, @PathVariable String subGroupName) throws ResourceNotFoundException, BadRequestException {
        CategoryDTO newCategory = categoryService.addCategoryToSubGroup(body, subGroupName);
        return ResponseEntity.status(HttpStatus.OK).body(newCategory);
    }

    /**
     * Handles POST requests to search for categories based on specific
     * characteristics.
     *
     * @param body A map containing the characteristics to filter categories by.
     *             The keys represent the characteristic names, and the values
     *             represent
     *             the corresponding values to filter on.
     * @return A ResponseEntity containing:
     *         - A list of CategoryDTO objects if matching categories are found
     *         (HTTP 200 OK).
     * @throws BadRequestException If the group/subgroup name are not provided in the body. 
     * @throws ResourceNotFoundException If the group/subgroup provided in the body don't exist.
     */
    @PostMapping("/search/by-characteristics")
    public ResponseEntity<List<CategoryDTO>> searchCategoriesByCharacteristics(@RequestBody Map<String, Object> body) throws ResourceNotFoundException{
        List<CategoryDTO> categories = categoryService.findCategoriesByCharacteristics(body);
        return ResponseEntity.status(HttpStatus.OK).body(categories);

    }

    /**
     * Retrieves a list of characteristics associated with a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return a ResponseEntity containing the list of CharacteristicDTO objects
     *         associated with the category and an HTTP status of OK
     * @throws ResourceNotFoundException If no category is found for the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<CharacteristicDTO>> getCategoryFromId(@PathVariable Long id) throws ResourceNotFoundException {
        List<CharacteristicDTO> charList = categoryService.findCategoryById(id);
        return ResponseEntity.status(HttpStatus.OK).body(charList);
    }

    /**
     * Retrieves a list of instruments associated with a specific category.
     *
     * @param id the ID of the category whose instruments are to be retrieved
     * @return a ResponseEntity containing a list of InstrumentDTO objects if instruments are found (HTTP 200
     *         OK)
     * @throws ResourceNotFoundException If the provided category doesn't exist.
     * @throws IllegialArgumentException If the category ID is null.
     */
    @GetMapping("/instruments/{id}")
    public ResponseEntity<List<InstrumentDTO>> getInstrumentsFromCategory(@PathVariable Long id) throws ResourceNotFoundException {
        List<InstrumentDTO> instrList = instrumentService.findInstrumentsOfCatergory(id);
        return ResponseEntity.status(HttpStatus.OK).body(instrList);
    }

    /**
     * Updates the characteristics of a category identified by its ID.
     * @param subGroupName the name of the subgroup of the category
     * @param id   the ID of the category to update
     * @param body a list of objects representing the new characteristics values and abbreviations for the category
     * @return a {@link ResponseEntity} containing the updated category an HTTP status of OK
     * @throws ResourceNotFoundException If the category or the subGroup provided don't exist.
     * @throws BadrequestException If the category doesn't belong to the provided subgroup, 
     *                              if the provided value/abbreviation aren't strings,
     *                              if a category with the same characteristics already exists.
     */
    @PatchMapping("/{id}/subgroup/{subGroupName}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @PathVariable String subGroupName,
            @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException{
        CategoryDTO updatedCategory = categoryService.updateCategory(id, subGroupName, body);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCategory);
    }



    /**
     * Delete a category given by its ID if this category doesn't contain instruments.
     * @param id The id of the category to delete
     * @return A boolean set to true when the category is deleted.
     * @throws ResourceNotFoundException If the provided category doesn't exist.
     * @throws BadRequestException If the provided category is not empty.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> deleteCategory(@PathVariable Long id) throws ResourceNotFoundException, BadRequestException{
        Boolean isRemoved = categoryService.deleteCategory(id);
        return ResponseEntity.ok(isRemoved);
    }
}
