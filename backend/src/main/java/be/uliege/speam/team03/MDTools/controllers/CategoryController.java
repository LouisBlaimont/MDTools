package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.CategoryService;
import be.uliege.speam.team03.MDTools.services.GroupService;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SubGroupService;
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
    private final GroupService groupService;
    private final SubGroupService subGroupService;

    /**
     * Retrieves a list of all categories.
     *
     * @return a ResponseEntity containing a list of CategoryDTO objects and an HTTP
     *         status of OK
     */
    @GetMapping("/all")
    public ResponseEntity<?> findAllCategories() {
        List<CategoryDTO> categories = categoryService.findAll();
        // Check if the list of categories is empty
        if (categories == null || categories.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No categories found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    /**
     * Retrieves a list of categories associated with a specific group name.
     *
     * @param groupName the name of the group whose categories are to be retrieved
     * @return a ResponseEntity containing a list of CategoryDTO objects and an HTTP
     *         status of OK
     * @throws ResourceNotFoundException if no categories are found for the
     *                                   specified group name
     */
    @GetMapping("/group/{groupName}")
    public ResponseEntity<Page<CategoryDTO>> getCategoryFromGroup(
            @PathVariable String groupName,
            @PageableDefault(size = 20, sort = {"subGroupName", "id"}, direction = Direction.ASC) Pageable pageable)
            throws ResourceNotFoundException {
        Page<CategoryDTO> categories = categoryService.findCategoriesOfGroup(groupName, pageable);
        if (categories == null || categories.isEmpty()) {
            throw new ResourceNotFoundException("No categories found for the group name: " + groupName);
        }
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
     * @throws ResourceNotFoundException if no categories are found for the given
     *                                   subgroup name
     */
    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<Page<CategoryDTO>> getCategoriesFromSubGroup(
            @PathVariable String subGroupName,
            @PageableDefault(size = 20, sort = {"subGroupName", "id"}, direction = Direction.ASC) Pageable pageable)
            throws ResourceNotFoundException {
        Page<CategoryDTO> categories = categoryService.findCategoriesOfSubGroup(subGroupName, pageable);
        if (categories == null || categories.isEmpty()) {
            throw new ResourceNotFoundException("No categories found for the subgroup name:" + subGroupName);
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    /**
     * Adds a new category to a specific subgroup within a group.
     *
     * @param body    A map containing the details of the category to be added.
     * @param id      The ID of the subgroup to which the category will be added.
     * @param groupId The ID of the group containing the subgroup.
     * @return A ResponseEntity containing the created CategoryDTO and a status of
     *         CREATED.
     * @throws ResourceNotFoundException If no group or subgroup is found for the
     *                                   provided IDs.
     */
    @PostMapping("/group/{groupId}/subgroup/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CategoryDTO> addCategory(@RequestBody Map<String, Object> body, @PathVariable Integer id,
            @PathVariable Integer groupId) {

        // Validate group and subgroup existence first
        GroupDTO group = groupService.findGroupById(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("No group found for the id :" + groupId);
        }

        SubGroupDTO subGroup = subGroupService.findSubGroupById(id);
        if (subGroup == null) {
            throw new ResourceNotFoundException("No subgroup found for the id :" + id);
        }

        // Call the service to add the category
        CategoryDTO newCategory = categoryService.addCategoryToSubGroup(body, id);

        // Set group and subgroup names
        newCategory.setSubGroupName(subGroup.getName());
        newCategory.setGroupName(group.getName());

        // Save the category
        CategoryDTO savedCategory = categoryService.save(newCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    /**
     * Handles the HTTP POST request to set a picture for a specific category.
     *
     * @param id   The ID of the category for which the picture is to be set.
     * @param file The picture file to be uploaded, provided as a multipart file.
     * @return A ResponseEntity containing the updated CategoryDTO and an HTTP
     *         status of OK.
     * @throws ResourceNotFoundException If the category with the specified ID is
     *                                   not found.
     */
    @PostMapping("/{id}/picture")
    public ResponseEntity<CategoryDTO> setGroupPicture(@PathVariable Long id, @RequestParam("file") MultipartFile file)
            throws ResourceNotFoundException {
        CategoryDTO categoryUpdated = null;
        categoryUpdated = categoryService.setCategoryPicture(id, file);

        return ResponseEntity.status(HttpStatus.OK).body(categoryUpdated);
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
     *         - A message indicating no categories were found (HTTP 404 NOT FOUND)
     *         if no matches exist.
     */
    @PostMapping("/search/by-characteristics")
    public ResponseEntity<List<CategoryDTO>> searchCategoriesByCharacteristics(@RequestBody Map<String, Object> body) {
        List<CategoryDTO> categories = categoryService.findCategoriesByCharacteristics(body);
        if (categories == null || categories.isEmpty()) {
            throw new ResourceNotFoundException("No categories found for the given characteristics");
        }
        return ResponseEntity.status(HttpStatus.OK).body(categories);

    }

    /**
     * Retrieves a list of characteristics associated with a category by its ID.
     *
     * @param id the ID of the category to retrieve
     * @return a ResponseEntity containing the list of CharacteristicDTO objects
     *         associated with the category and an HTTP status of OK
     * @throws ResourceNotFoundException if no category is found for the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<CharacteristicDTO>> getCategoryFromId(@PathVariable Integer id) {
        List<CharacteristicDTO> charList = categoryService.findCategoryById(id);
        if (charList == null) {
            throw new ResourceNotFoundException("No category found for the id :" + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(charList);
    }

    /**
     * Retrieves a list of instruments associated with a specific category.
     *
     * @param id the ID of the category whose instruments are to be retrieved
     * @return a ResponseEntity containing:
     *         - a list of InstrumentDTO objects if instruments are found (HTTP 200
     *         OK)
     *         - an error message if no instruments are found for the given category
     *         ID (HTTP 404 Not Found)
     */
    @GetMapping("/instruments/{id}")
    public ResponseEntity<List<InstrumentDTO>> getInstrumentsFromCategory(@PathVariable Integer id) {
        List<InstrumentDTO> instrList = instrumentService.findInstrumentsOfCatergory(id);
        if (instrList == null) {
            throw new ResourceNotFoundException("No instruments found for the category id :" + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrList);
    }

    /**
     * Updates the characteristics of a category identified by its ID.
     *
     * @param id   the ID of the category to update
     * @param body a list of {@link CharacteristicDTO} objects representing the new
     *             characteristics for the category
     * @return a {@link ResponseEntity} containing the updated list of
     *         {@link CharacteristicDTO} objects and an HTTP status of OK
     */
    @PatchMapping("/{id}")
    public ResponseEntity<List<CharacteristicDTO>> updateCategory(@PathVariable Integer id,
            @RequestBody List<CharacteristicDTO> body) {
        List<CharacteristicDTO> chars = categoryService.updateCategoryCharacteristics(id, body);
        return ResponseEntity.status(HttpStatus.OK).body(chars);
    }

    /**
     * Retrieves a map of characteristic names and their values for a given
     * category.
     * Used for exporting instruments with characteristic values to Excel.
     *
     * @param id The ID of the category.
     * @return A map where keys are characteristic names and values are the
     *         corresponding values (or empty strings).
     */
    @GetMapping("/{id}/characteristics")
    public ResponseEntity<Map<String, String>> getCharacteristicValuesFromCategory(@PathVariable Integer id) {
        Map<String, String> characteristicValues = categoryService.getCharacteristicValuesByCategoryId(id);

        if (characteristicValues == null || characteristicValues.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(characteristicValues);
    }
}
