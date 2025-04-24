package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.mapper.CategoryMapper;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.SubGroupCharacteristic;
import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;

@Service
public class CategoryService {
    private GroupRepository groupRepository;
    private SubGroupRepository subGroupRepository;
    private CategoryRepository categoryRepository;
    private CategoryCharacteristicRepository categoryCharRepository;
    private CategoryMapper catMapper;
    private PictureStorageService pictureStorageService;
    private CharacteristicAbbreviationService charValAbbrevService;
    private InstrumentService instrumentService;

    public CategoryService(GroupRepository groupRepo, SubGroupRepository subGroupRepo, CategoryRepository categoryRepo,
            CharacteristicRepository charRepo, CategoryCharacteristicRepository catCharRepo,
            PictureStorageService pictureStorageService, CharacteristicAbbreviationService charValAbbrevService,
            InstrumentService instrumentService) {
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
        this.categoryRepository = categoryRepo;
        this.categoryCharRepository = catCharRepo;
        this.charValAbbrevService = charValAbbrevService;
        this.catMapper = new CategoryMapper(categoryRepo, pictureStorageService);
        this.pictureStorageService = pictureStorageService;
        this.instrumentService = instrumentService;
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
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category, pictureStorageService);
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
        if (groupMaybe.isEmpty()) {
            throw new ResourceNotFoundException("No group found with the name " + groupName);
        }
        Group group = groupMaybe.get();

        List<SubGroup> subGroups = subGroupRepository.findByGroup(group);
        List<Category> categories = categoryRepository.findAllBySubGroupIn(subGroups, Sort.by("subGroupName", "id"));

        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category, pictureStorageService);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

    /**
     * Gets the sorted list of categories of the subgroup given by
     * subGroupName
     * 
     * @param subGroupName the name of the subgroup
     * @return List of CategoryDTO
     */
    public List<CategoryDTO> findCategoriesOfSubGroup(String subGroupName) {
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isEmpty()) {
            throw new ResourceNotFoundException("No subgroup found with the name " +  subGroupName);
        }

        SubGroup subGroup = subGroupMaybe.get();
        List<Category> categories = categoryRepository.findBySubGroup(subGroup, Sort.by("subGroupName", "id"));

        return categories.stream().map(category -> catMapper.mapToCategoryDto(category, pictureStorageService)).toList();
    }


    /**
     * Adds a new category
     *
     * @param body A map containing the characteristics of the category
     * @param subGroupName The name of the subgroup in which the category must be created
     * @return The new category
     */
    public CategoryDTO addCategoryToSubGroup(Map<String, Object> body, String subGroupName) {
        // Get subgroup
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if(subGroupMaybe.isEmpty()){
            throw new ResourceNotFoundException("The subgroup with name " + subGroupName + " was not found");
        }
        SubGroup subGroup = subGroupMaybe.get();
        Category category = new Category(subGroup);
        Category newCategory = categoryRepository.save(category);

        List<SubGroupCharacteristic> subGroupChar = subGroup.getSubGroupCharacteristics();
        List<CategoryCharacteristic> newCategoryCharacteristics = new ArrayList<>();
        List<CharacteristicValueAbbreviation> newAbreviations = new ArrayList<>();
        List<CharacteristicValueAbbreviation> updateAbreviations = new ArrayList<>();

        // Get characteristic values
        for (SubGroupCharacteristic sbg : subGroupChar){
            Characteristic characteristic = sbg.getCharacteristic();
            String charName = characteristic.getName();
            Object value = body.get(charName);

            // Missing value (not empty, just null) of a characteristic
            if(value == null || !(value instanceof String)){
                categoryRepository.delete(newCategory);
                throw new BadRequestException("Missing value key for characteristic " + charName);
            }
            String stringVal = (String) value;
            CategoryCharacteristic catChar = new CategoryCharacteristic(newCategory, characteristic, stringVal);
            catChar.setId(new CategoryCharacteristicKey(newCategory.getId(), characteristic.getId()));
            newCategoryCharacteristics.add(catChar);

            // Abbreviations management 
            if(!charName.equals("Length") && !charName.equals("Name") && !charName.equals("Function") && !stringVal.equals("")){
                Optional<String> existingAbrev = charValAbbrevService.getAbbreviation(stringVal);
                String abrev = (String) body.get(charName + "abrev");
                if (abrev == null){
                    categoryRepository.delete(newCategory);
                    throw new BadRequestException("Missing value key for the abreviation of " + charName);
                }

                if(existingAbrev.isEmpty()){
                    if (abrev.trim().isEmpty()){
                        continue;
                    }
                    newAbreviations.add(new CharacteristicValueAbbreviation(stringVal, abrev));
                }
                else{
                    if (abrev.trim().isEmpty()){
                        continue;
                    }
                    updateAbreviations.add(new CharacteristicValueAbbreviation(stringVal, abrev)); 
                }
            }
        }

        // Verify that same category is not existing
        List<Category> existingCats = categoryRepository.findBySubGroup(subGroup, Sort.by("subGroupName", "id"));
        for(Category existingCat : existingCats){
            List<CategoryCharacteristic> existingChars = existingCat.getCategoryCharacteristic();
            if (existingChars == null){
                continue;
            }

            Boolean same = true;
            for(CategoryCharacteristic newCatChar : newCategoryCharacteristics){
                Boolean match = existingChars.stream().anyMatch(ec -> ec.getCharacteristic().getId().equals(newCatChar.getCharacteristic().getId()) 
                && ec.getVal().equalsIgnoreCase(newCatChar.getVal()));
                if(!match){
                    same = false;
                    break;
                }
            }
            if(same && existingChars.size() == newCategoryCharacteristics.size()){
                categoryRepository.delete(newCategory);
                throw new BadRequestException("Category with same characteristics already exists");
            }
        }

        // No same category, save the characteristics values and the abreviations
        categoryCharRepository.saveAll(newCategoryCharacteristics);
        
        for(CharacteristicValueAbbreviation abrev : newAbreviations){
            charValAbbrevService.addAbbreviation(abrev.getValue(), abrev.getAbbreviation());
        }
        
        for(CharacteristicValueAbbreviation abrev : updateAbreviations){
            charValAbbrevService.updateAbbreviation(abrev.getValue(), abrev.getAbbreviation());
        }

        newCategory.setCategoryCharacteristic(newCategoryCharacteristics);
        newCategory.setPicturesId(null); // ?
        return catMapper.mapToCategoryDto(newCategory, pictureStorageService);
    }


    /**
     * Gets the categories given a set of characteristics given in the body.
     * The body contains every field corresponding to each characteristic of the
     * particular subgroup,
     * even the one where no conditions is requested. The function looks for the
     * non-empty field
     * and filter the categories by their input value.
     * 
     * @param body the map containing the characteristics
     * @return List of categoryDTO
     */
    public List<CategoryDTO> findCategoriesByCharacteristics(Map<String, Object> body) {
        String groupName = (String) body.get("groupName");
        String subGroupName = (String) body.get("subGroupName");

        if (groupName == null || subGroupName == null){
            throw new BadRequestException("Missing field for the group and subgroup");    
        }

        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (groupMaybe.isEmpty() || subGroupMaybe.isEmpty()) {
            throw new ResourceNotFoundException("No subgroup named " + subGroupName + "or no group named " + groupName + " found");
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

        List<Category> categories = categoryRepository.findBySubGroup(subGroup, Sort.by("subGroupName", "id"));


        // remove characteristics without value
        Map<String, String> filteredSearchBy = searchBy.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        
        Double minLength = body.containsKey("minLength") ? ((Number) body.get("minLength")).doubleValue() : 0.0;
        Double maxLength = body.containsKey("maxLength") ? ((Number) body.get("maxLength")).doubleValue() : Double.POSITIVE_INFINITY;

        if (filteredSearchBy.isEmpty() && minLength == 0.0 && maxLength == Double.POSITIVE_INFINITY) {
            List<CategoryDTO> categoriesDTO = new ArrayList<>();
            for (Category category : categories) {
                CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category, pictureStorageService);
                categoriesDTO.add(categoryDTO);
            }
            return categoriesDTO;
        }

        List<Long> categoryIds = categories.stream().map(Category::getId).toList();

        List<CategoryCharacteristic> categoryChars = categoryCharRepository.findByCategoryIds(categoryIds);

        Map<Category, Map<String, String>> categoryToChar = categoryChars.stream()
                .collect(Collectors.groupingBy(
                        CategoryCharacteristic::getCategory,
                        Collectors.toMap(cc -> cc.getCharacteristic().getName(),
                                CategoryCharacteristic::getVal, (existing, replacement) -> existing)));

        List<Category> filteredCategories = categoryToChar.entrySet().stream()
                .filter(entry -> {
                    Map<String, String> charMap = entry.getValue();
                    boolean matchesNormalChars = charMap.entrySet().containsAll(filteredSearchBy.entrySet());
                    boolean matchesLength  = true;
                    if(charMap.containsKey("Length")){
                        try{
                            double length = Double.parseDouble(charMap.get("Length"));
                            matchesLength = length >= minLength && length <= maxLength;
                        }catch(NumberFormatException e){
                            matchesLength = false;
                        }
                    }
                    return matchesNormalChars && matchesLength;
                })
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparing(Category::getSubGroup, Comparator.comparing(SubGroup::getName))
                        .thenComparing(Category::getId))
                .toList();

        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : filteredCategories) {
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category, pictureStorageService);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;

    }

    /**
     * Gets the characteristics (with their value) of the category given by catId
     * 
     * @param catId the id of the category
     * @return List of CharacteristicDTO
     */
    public List<CharacteristicDTO> findCategoryById(Long catId) {
        Optional<Category> categoryMaybe = categoryRepository.findById((long) catId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Not category with the ID" + catId + " found");
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
     * @param catId                  the id of the category
     * @param updatedCharacteristics the list of characteristics to update
     * @return List of CharacteristicDTO
     */
    public CategoryDTO updateCategory(Long catId, String subGroupName,
            Map<String, Object> body) {

        // Get subgroup
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if(subGroupMaybe.isEmpty()){
            throw new ResourceNotFoundException("The subgroup with name " + subGroupName + " was not found");
        }
        SubGroup subgroup = subGroupMaybe.get();
        // Get category
        Optional<Category> categoryMaybe = categoryRepository.findById(catId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Category not found.");
        }
        Category category = categoryMaybe.get();

        if(subgroup.getId() != category.getSubGroup().getId()){
            throw new BadRequestException("Mismatch between category and subgroup");
        }

        List<CategoryCharacteristic> existingCharacteristics = categoryCharRepository.findByCategoryId(catId);

        List<SubGroupCharacteristic> characteristicsOfSubGroup = subgroup.getSubGroupCharacteristics();

        List<CategoryCharacteristic> updatedCharacteristics = new ArrayList<>();
        List<CharacteristicValueAbbreviation> newAbbreviations = new ArrayList<>();
        List<CharacteristicValueAbbreviation> updatedAbbreviations = new ArrayList<>();
        
        List<CategoryCharacteristic> allCharacteristics = new ArrayList<>();

        for (SubGroupCharacteristic sbc : characteristicsOfSubGroup){
            Characteristic characteristic = sbc.getCharacteristic();
            String charName = characteristic.getName();

            // Check if a category characteristic existed for this value
            Optional<CategoryCharacteristic> found = existingCharacteristics.stream()
                .filter(cc -> cc.getCharacteristic().equals(characteristic))
                .findFirst();
    

            // Caracteristic value to be updated
            Object value = body.get(charName);
            if(value == null && found.isPresent()){
                allCharacteristics.add(found.get()); 
                continue;
            }
            else if(value == null && found.isEmpty()){
                continue;
            }
            if (!(value instanceof String)){
                throw new BadRequestException("Invalid value type for characteristic "+ charName);
            }
            String stringVal = (String) value;

            CategoryCharacteristic cc;
            // If yes, nothing to do
            if(found.isPresent()){
                cc = found.get();
            }
            // If no, create a new categoryCharacteristic;
            else{
                cc = new CategoryCharacteristic();
                cc.setCategory(category);
                cc.setCharacteristic(characteristic);
                CategoryCharacteristicKey key = new CategoryCharacteristicKey(catId, characteristic.getId());
                cc.setId(key);
            }

            // Update value 
            cc.setVal(stringVal);
            updatedCharacteristics.add(cc); // To save new characteristic
            allCharacteristics.add(cc); // To update correctly the category

            // Update abbrev, if abbrev didn't exist before create a new one
            Object abbrev = body.get(charName+"abrev");
            if(abbrev==null){
                continue;
            }
            if (!(abbrev instanceof String)){
                throw new BadRequestException("Invalid abbrev type for characteristic "+ charName);
            }
            String stringAbbrev = (String) abbrev;

            Optional<String> charAbrevMaybe = charValAbbrevService.getAbbreviation(stringVal);
            
            if (charAbrevMaybe.isPresent()){
                updatedAbbreviations.add(new CharacteristicValueAbbreviation(stringVal, stringAbbrev));
            }
            else{
                newAbbreviations.add(new CharacteristicValueAbbreviation(stringVal, stringAbbrev));
            }
        }

        // Verify that same category is not existing
        List<Category> existingCats = categoryRepository.findBySubGroup(subgroup, Sort.by("subGroupName", "id"));
        for(Category existingCat : existingCats){
            if(existingCat.getId().equals(category.getId())){
                continue;
            }
            List<CategoryCharacteristic> existingChars = existingCat.getCategoryCharacteristic();
            if (existingChars == null){
                continue;
            }

            Boolean same = true;
            for(CategoryCharacteristic allCatChar : allCharacteristics){
                Boolean match = existingChars.stream().anyMatch(ec -> ec.getCharacteristic().getId().equals(allCatChar.getCharacteristic().getId()) 
                && ec.getVal().equalsIgnoreCase(allCatChar.getVal()));
                if(!match){
                    same = false;
                    break;
                }
            }
            if(same && existingChars.size() == allCharacteristics.size()){
                throw new BadRequestException("Category with same characteristics already exists");
            }
        }

        categoryCharRepository.saveAll(updatedCharacteristics);

        for(CharacteristicValueAbbreviation abrev : newAbbreviations){
            charValAbbrevService.addAbbreviation(abrev.getValue(), abrev.getAbbreviation());
        }
        for(CharacteristicValueAbbreviation abrev : updatedAbbreviations){
            charValAbbrevService.updateAbbreviation(abrev.getValue(), abrev.getAbbreviation());
        }

        category.setCategoryCharacteristic(allCharacteristics);
        categoryRepository.save(category);


        return catMapper.mapToCategoryDto(category, pictureStorageService) ;
    }

    public CategoryDTO searchCategory(Long categoryId) {
        Optional<Category> cat = categoryRepository.findById((long) categoryId);
        return catMapper.mapToCategoryDto(cat.get(), pictureStorageService);
    }

    /**
     * Deletes a category identified by its ID
     * @param categoryId
     * @return A boolean set to true when the category is deleted.
     */
    public Boolean deleteCategory(Long categoryId){
        Optional<Category> categoryMaybe = categoryRepository.findById(categoryId);
        if (categoryMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        List<InstrumentDTO> instrumentsInCategory = instrumentService.findInstrumentsOfCatergory(categoryId);

        if(instrumentsInCategory.size() > 0){
            throw new BadRequestException("Instruments exist in the category, cannot delete it.");
        }
        categoryRepository.deleteById(categoryId);
        return true;
    }
}