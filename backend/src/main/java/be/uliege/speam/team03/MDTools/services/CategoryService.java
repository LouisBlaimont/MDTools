package be.uliege.speam.team03.MDTools.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.CategoryMapper;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

@Service
public class CategoryService {
    private GroupRepository groupRepository;
    private SubGroupRepository subGroupRepository;
    private CategoryRepository categoryRepository;
    private CharacteristicRepository characteristicRepository;
    private CategoryCharacteristicRepository categoryCharRepository;
    private CategoryMapper catMapper;
    private PictureStorageService pictureStorageService;
    private CharacteristicAbbreviationService charValAbbrevService;

    public CategoryService(GroupRepository groupRepo, SubGroupRepository subGroupRepo, CategoryRepository categoryRepo,
            CharacteristicRepository charRepo, CategoryCharacteristicRepository catCharRepo,
            PictureStorageService pictureStorageService, CharacteristicAbbreviationService charValAbbrevService) {
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
        this.categoryRepository = categoryRepo;
        this.characteristicRepository = charRepo;
        this.categoryCharRepository = catCharRepo;
        this.charValAbbrevService = charValAbbrevService;
        this.catMapper = new CategoryMapper(categoryRepo);
        this.pictureStorageService = pictureStorageService;
    }

    /**
     * Gets the categories of the group given by groupName
     * 
     * @param groupName
     * @return List of categoryDTO
     */
    public List<CategoryDTO> findCategoriesOfGroup(String groupName) {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false) {
            return null;
        }
        Group group = groupMaybe.get();

        List<SubGroup> subGroups = subGroupRepository.findByGroup(group);
        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroupIn(subGroups);
        if (categoriesMaybe.isPresent() == false) {
            return null;
        }
        List<Category> categories = categoriesMaybe.get();
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

    /**
     * Gets the categories of the subgroup given by subGroupName
     * 
     * @param subGroupName
     * @return List of categoryDTO
     */
    public List<CategoryDTO> findCategoriesOfSubGroup(String subGroupName) {
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isPresent() == false) {
            return null;
        }
        SubGroup subGroup = subGroupMaybe.get();
        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroup(subGroup);
        if (categoriesMaybe.isPresent() == false) {
            return null;
        }

        List<Category> categories = categoriesMaybe.get();
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

    /**
     * Gets the categories given a set of characteristics given in the body.
     * The body contains every field corresponding to each characteristic of the
     * particular subgroup,
     * even the one where no conditions is requested. The function looks for the
     * non-empty field
     * and filter the categories by their input value.
     * 
     * @param body
     * @return List of categoryDTO
     */
    public List<CategoryDTO> findCategoriesByCharacteristics(Map<String, Object> body) {
        String groupName = (String) body.get("groupName");
        String subGroupName = (String) body.get("subGroupName");

        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (groupMaybe.isEmpty() || subGroupMaybe.isEmpty()) {
            return null;
        }

        SubGroup subGroup = subGroupMaybe.get();

        Map<String, String> searchBy = new HashMap<>();
        String function = (String) body.get("function");
        searchBy.put("Function", function);
        String name = (String) body.get("name");
        searchBy.put("Name", name);
        Object characteristics = body.get("characteristics");

        if (characteristics instanceof List<?>) {
            for (Object item : (List<?>) characteristics) {
                if (item instanceof Map<?, ?> characteristicMap) {
                    Object nameObj = characteristicMap.get("name");
                    Object valueObj = characteristicMap.get("value");

                    if (nameObj instanceof String charName && valueObj instanceof String charValue) {
                        searchBy.put(charName, charValue);
                    }
                }
            }
        }

        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroup(subGroup);
        if (categoriesMaybe.isPresent() == false) {
            return null;
        }
        List<Category> categories = categoriesMaybe.get();

        // remove characteristics without value
        Map<String, String> filteredSearchBy = searchBy.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredSearchBy.isEmpty()) {
            List<CategoryDTO> categoriesDTO = new ArrayList<>();
            for (Category category : categories) {
                CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
                categoriesDTO.add(categoryDTO);
            }
            return categoriesDTO;
        }

        List<Integer> categoryIds = categories.stream().map(Category::getId).toList();

        List<CategoryCharacteristic> categoryChars = categoryCharRepository.findByCategoryIds(categoryIds);

        Map<Category, Map<String, String>> categoryToChar = categoryChars.stream()
                .collect(Collectors.groupingBy(
                        CategoryCharacteristic::getCategory,
                        Collectors.toMap(cc -> cc.getCharacteristic().getName(),
                                CategoryCharacteristic::getVal, (existing, replacement) -> existing)));

        List<Category> filteredCategories = categoryToChar.entrySet().stream()
                .filter(entry -> entry.getValue().entrySet().containsAll(filteredSearchBy.entrySet()))
                .map(Map.Entry::getKey).toList();

        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : filteredCategories) {
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;

    }

    /**
     * Gets the characteristics (with their value) of the category given by catId
     * 
     * @param catId
     * @return List of CharacteristicDTO
     */
    public List<CharacteristicDTO> findCategoryById(Integer catId) {
        Optional<Category> categoryMaybe = categoryRepository.findById((long) catId);
        if (categoryMaybe.isEmpty()) {
            return null;
        }

        List<CharacteristicDTO> characteristics = categoryCharRepository.findByCategoryId(catId)
                .stream()
                .map(cc -> {
                    String name = cc.getCharacteristic().getName();
                    String val = cc.getVal();
                    String abbreviation = charValAbbrevService.getAbbreviation(val).orElse(val);
                    return new CharacteristicDTO(name, val, abbreviation);
                })
                .toList();

        return characteristics;
    }

    /**
     * Updates the characteristics of a category given by catId
     * 
     * @param catId
     * @param updatedCharacteristics
     * @return
     */
    public List<CharacteristicDTO> updateCategoryCharacteristics(Integer catId,
            List<CharacteristicDTO> updatedCharacteristics) {
        // Find the category
        Optional<Category> categoryMaybe = categoryRepository.findById((long) catId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Category not found. Not updating characteristics.");
        }

        // Get existing characteristics related to the category
        List<CategoryCharacteristic> existingCharacteristics = categoryCharRepository.findByCategoryId(catId);

        // Update the existing characteristics with the new values
        for (CharacteristicDTO updatedCharacteristic : updatedCharacteristics) {
            for (CategoryCharacteristic existingCharacteristic : existingCharacteristics) {
                if (existingCharacteristic.getCharacteristic().getName().equals(updatedCharacteristic.getName())) {
                    existingCharacteristic.setVal(updatedCharacteristic.getValue());
                    categoryCharRepository.save(existingCharacteristic); // Save the updated characteristic
                    break;
                }
            }
        }

        // Convert the updated characteristics back to DTOs and return
        List<CharacteristicDTO> updatedCharacteristicDTOs = existingCharacteristics
                .stream()
                .map(cc -> {
                    String name = cc.getCharacteristic().getName();
                    String val = cc.getVal();
                    String abbreviation = charValAbbrevService.getAbbreviation(val).orElse(val);
                    return new CharacteristicDTO(name, val, abbreviation);
                })
                .toList();

        return updatedCharacteristicDTOs;
    }

    /**
     * Set the picture of the category
     * 
     * @param categoryId
     * @param picture
     * @return
     * @throws ResourceNotFoundException
     */
    public CategoryDTO setCategoryPicture(Long categoryId, MultipartFile picture) throws ResourceNotFoundException {
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Group not found.");
        }
        Category category = categoryMaybe.get();

        if (category.getPictureId() != null) {
            pictureStorageService.deletePicture(category.getPictureId());
        }

        Picture metadata = pictureStorageService.storePicture(picture, PictureType.GROUP, categoryId);

        category.setPictureId(metadata.getId());
        Category savedCategory = categoryRepository.save(category);
        return catMapper.mapToCategoryDto(savedCategory);
    }

}
