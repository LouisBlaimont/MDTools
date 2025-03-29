package be.uliege.speam.team03.MDTools.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.CategoryMapper;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

@Service
public class CategoryService {
    private GroupRepository groupRepository;
    private SubGroupRepository subGroupRepository;
    private CategoryRepository categoryRepository;
    private CategoryCharacteristicRepository categoryCharRepository;
    private CharacteristicRepository characteristicRepository;
    private CategoryMapper catMapper;
    private PictureStorageService pictureStorageService;
    private CharacteristicAbbreviationService charValAbbrevService;

    public CategoryService(GroupRepository groupRepo, SubGroupRepository subGroupRepo, CategoryRepository categoryRepo,
            CharacteristicRepository charRepo, CategoryCharacteristicRepository catCharRepo,
            PictureStorageService pictureStorageService, CharacteristicAbbreviationService charValAbbrevService) {
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
        this.categoryRepository = categoryRepo;
        this.categoryCharRepository = catCharRepo;
        this.charValAbbrevService = charValAbbrevService;
        this.catMapper = new CategoryMapper(categoryRepo);
        this.pictureStorageService = pictureStorageService;
    }

    /**
     * Gets all the categories in the database
     * 
     * @return List of categoryDTO
     */
    public List<CategoryDTO> findAll() {
        List<Category> categories = (List<Category>) categoryRepository.findAll();
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
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
     * Gets the category given by id
     *
     * @param id the id of the category
     * @return the category
     */
    public CategoryDTO findById(Integer id) {
        Optional<Category> categoryMaybe = categoryRepository.findById((long) id);
        if (categoryMaybe.isEmpty()){
            return null;
        }
        System.out.println("Category found");
        Category category = categoryMaybe.get();
        System.out.println("Category is " + category);
        return catMapper.mapToCategoryDto(category);
    }

    /**
     * Adds a new category
     *
     * @param body a map containing the characteristics of the category
     * @return the new category
     */
    @SuppressWarnings("unchecked")
    public CategoryDTO addCategoryToSubGroup(Map<String, Object> body, Integer subGroupId) {
        String subGroupName = (String) body.get("subGroupName");
        String name = (String) body.get("name");
        String function = (String) body.get("function");
        String shape = (String) body.get("shape");
        String lenAbrv = (String) body.get("lenAbrv");
        // String pictureId = (String) body.get("pictureId");

        Category category = new Category();
        Integer newCategoryId = category.getId();

        SubGroup subGroup;
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isPresent()){
            subGroup = subGroupMaybe.get();
            category.setSubGroup(subGroup);
            if((long) subGroupId != subGroup.getId()){
                throw new IllegalArgumentException("Subgroup name and id do not match.");
            }
        } else {
            subGroup = subGroupRepository.findById(subGroupId).get();
            category.setSubGroup(subGroup);
        }

        List<SubGroupCharacteristic> subGroupCharacteristics = subGroup.getSubGroupCharacteristics();
        List<String> characteristics = new ArrayList<>();
        for (SubGroupCharacteristic subGroupCharacteristic : subGroupCharacteristics){
            String characteristic = subGroupCharacteristic.getCharacteristic().getName();
            characteristics.add(characteristic);
        }

        List<Map<String, Object>> EntryChars;
        Object bodyChars = body.get("chars");
        if(bodyChars == null){
            EntryChars = null;
        }
        else if (bodyChars instanceof List<?>) {
            EntryChars = (List<Map<String, Object>>) bodyChars;
        } else {
            throw new IllegalArgumentException("Expected a List<Map<String, Object>> for 'subGroupList'");
        }

        if(EntryChars != null){
            Map<String, String> dictVal = new HashMap<>();
            Map<String, String> dictValAbrev = new HashMap<>();
            for (Map<String, Object> EntryChar : EntryChars) {
                String EntryCharName = (String) EntryChar.get("name");
                String EntryCharVal = (String) EntryChar.get("value");
                String EntryCharValAbrev = (String) EntryChar.get("abrev");
                dictVal.put(EntryCharName, EntryCharVal);
                dictValAbrev.put(EntryCharName, EntryCharValAbrev);
            }
    
            StringBuilder shapeBuilder = new StringBuilder();
    
            // List<CategoryCharacteristic> newCatChars = new ArrayList<>();
            // for (String charSubGroup : characteristics){
            //     if (dictVal.containsKey(charSubGroup) && dictValAbrev.containsKey(charSubGroup)){
            //         Optional<Characteristic> charMaybe =  characteristicRepository.findByName(charSubGroup);
            //         if (charMaybe.isPresent() == false){
            //             return null;
            //         }
            //         Characteristic newChar = charMaybe.get();
            //         Integer charId = newChar.getId(); 
            //         String newCharVal = dictVal.get(charSubGroup);
            //         String newCharAbrev = dictValAbrev.get(charSubGroup);
    
            //         if (newCharVal==null){
            //             continue;
            //         }
    
            //         CategoryCharacteristicKey key = new CategoryCharacteristicKey(newCategoryId, charId);
            //         CategoryCharacteristic catChar = new CategoryCharacteristic(category, newChar, newCharVal, newCharAbrev);
            //         catChar.setId(key);
            //         categoryCharRepository.save(catChar);
            //         newCatChars.add(catChar);
    
            //         if (charSubGroup.equals("Function") || charSubGroup.equals("Name")){
            //             continue;
            //         }
            //         if(charSubGroup.equals("Length")){
            //             lenAbrv = newCharAbrev;
            //         }
            //         shapeBuilder.append(newCharAbrev).append("/");
            //     }  
            // }
    
            // if (shapeBuilder.length()> 0){
            //     shapeBuilder.setLength(shapeBuilder.length()-1);
            // }
    
            shape = shapeBuilder.toString();
            category.setShape(shape);
    
            // category.setCategoryCharacteristic(newCatChars);
        }

        category.setName(name);
        category.setFunction(function);
        category.setShape(shape);
        category.setLenAbrv(lenAbrv);
        Category savedCategory = categoryRepository.save(category);
        return catMapper.mapToCategoryDto(savedCategory);
    }

    /**
     * Saves a new category
     *
     * @param newCategory
     * @return
     */
    public CategoryDTO save(CategoryDTO newCategory) {
        if(newCategory.getGroupName() == null || newCategory.getSubGroupName() == null){
            throw new IllegalArgumentException("Group or Subgroup not found. Not adding category.");
        }
        Category category = catMapper.mapToCategory(newCategory);
        category.setSubGroup(subGroupRepository.findByName(newCategory.getSubGroupName()).get());
        System.out.println("Category is before saving in Service" + category);
        Category savedCategory = categoryRepository.save(category);
        System.out.println("Category is after saving in Service" + savedCategory);
        return catMapper.mapToCategoryDto(savedCategory);
    }

    /**
     * Gets the categories of the subgroup given by subGroupName
     * @param subGroupName the name of the subgroup
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
     * The body contains every field corresponding to each characteristic of the particular subgroup,
     * even the one where no conditions is requested. The function looks for the non-empty field
     * and filter the categories by their input value.
     * @param body the map containing the characteristics
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
     * @param catId the id of the category
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
     * @param catId the id of the category
     * @param updatedCharacteristics the list of characteristics to update
     * @return List of CharacteristicDTO
     * @throws ResourceNotFoundException
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
     * @param categoryId the id of the category
     * @param picture the picture to set
     * @return the categoryDTO
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






// public CategoryDTO addCategoryToSubGroup(String subGroupName, Map<String, Object> body){
//     Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
//     if (subGroupMaybe.isPresent() == false){
//         return null;
//     }
//     SubGroup subGroup = subGroupMaybe.get();

//     Category newCat = new Category(subGroup);
//     Integer newCatId = newCat.getId();

//     String gName = subGroup.getGroup().getName();
//     String subgName = subGroup.getName();

//     List<SubGroupCharacteristic> subGroupCharacteristics = subGroup.getSubGroupCharacteristics();
//     List<String> characteristics = new ArrayList<>();
//     for (SubGroupCharacteristic subGroupCharacteristic : subGroupCharacteristics){
//         String characteristic = subGroupCharacteristic.getCharacteristic().getName();
//         characteristics.add(characteristic);
//     }

//     String function = (String) body.get("function");
//     if (function == null ){
//         return null;
//     }
//     String name = (String) body.get("name");
//     if (name == null){
//         return null;
//     }

//     String lenAbrv = new String();
//     lenAbrv = null;

//     List<Map<String, Object>> EntryChars = (List<Map<String, Object>>) body.get("chars");
//     Map<String, String> dictVal = new HashMap<>();
//     Map<String, String> dictValAbrev = new HashMap<>();
//     for (Map<String, Object> EntryChar : EntryChars) {
//         String EntryCharName = (String) EntryChar.get("name");
//         String EntryCharVal = (String) EntryChar.get("value");
//         String EntryCharValAbrev = (String) EntryChar.get("abrev");
//         dictVal.put(EntryCharName, EntryCharVal);
//         dictValAbrev.put(EntryCharName, EntryCharValAbrev);
//     }

    // StringBuilder shapeBuilder = new StringBuilder();
    // List<CategoryCharacteristic> newCatChars = new ArrayList<>();
    // for (String charSubGroup : characteristics){
    //     if (dictVal.containsKey(charSubGroup) && dictValAbrev.containsKey(charSubGroup)){
    //         Optional<Characteristic> charMaybe =  characteristicRepository.findByName(charSubGroup);
    //         if (charMaybe.isPresent() == false){
    //             return null;
    //         }
    //         Characteristic newChar = charMaybe.get();
    //         Integer charId = newChar.getId();
    //         String newCharVal = dictVal.get(charSubGroup);
    //         String newCharAbrev = dictValAbrev.get(charSubGroup);

    //         if (newCharVal==null){
    //             continue;
    //         }

    //         CategoryCharacteristicKey key = new CategoryCharacteristicKey(newCatId, charId);
    //         CategoryCharacteristic catChar = new CategoryCharacteristic(newCat, newChar, newCharVal, newCharAbrev);
    //         catChar.setId(key);
    //         categoryCharRepository.save(catChar);
    //         newCatChars.add(catChar);

    //         if (charSubGroup.equals("Function") || charSubGroup.equals("Name")){
    //             continue;
    //         }
    //         if(charSubGroup.equals("Length")){
    //             lenAbrv = newCharAbrev;
    //         }
    //         shapeBuilder.append(newCharAbrev).append("/");
    //     }
    // }
    // if (shapeBuilder.length()> 0){
    //     shapeBuilder.setLength(shapeBuilder.length()-1);
    // }
    // String shape = shapeBuilder.toString();
    // newCat.setShape(shape);
    // newCat.setCategoryCharacteristic(newCatChars);
//     categoryRepository.save(newCat);

//     CategoryDTO newCatDTO = new CategoryDTO(gName, subgName, name, function, shape, lenAbrv);
//     return newCatDTO;
// }

// }