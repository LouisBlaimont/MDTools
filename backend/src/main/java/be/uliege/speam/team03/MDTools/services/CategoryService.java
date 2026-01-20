package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.PictureType;
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
        this.pictureStorageService = pictureStorageService;
        this.instrumentService = instrumentService;
    }

    /**
     * Gets all the categories in the database
     * 
     * @return List of categoryDTO
     */
    public List<CategoryDTO> findAll() {
        return categoryRepository.findAllCategoriesFlat(null, null, null);
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
        List<CategoryDTO> list = categoryRepository.findAllCategoriesFlat(group.getId(), null, null);
        batchEnrichCategoryPictures(list);
        return list;
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

        List<CategoryDTO> list = categoryRepository.findAllCategoriesFlat(null, subGroup.getId(), null);
        batchEnrichCategoryPictures(list);;
        return list;
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
        CategoryDTO dto = searchCategory(category.getId());
        return dto;
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

        if (groupName == null || subGroupName == null) {
            throw new BadRequestException("Missing field for the group and subgroup");
        }
 
        SubGroup subGroup = subGroupRepository.findByName(subGroupName)
                .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found: " + subGroupName));

        String function = (String) body.get("function");
        String name = (String) body.get("name");

        Double minLength = body.containsKey("minLength") ? ((Number) body.get("minLength")).doubleValue() : null;
        Double maxLength = body.containsKey("maxLength") ? ((Number) body.get("maxLength")).doubleValue() : null;

        List<String> otherKeys = new ArrayList<>();
        List<String> otherValues = new ArrayList<>();

        Object characteristics = body.get("characteristics");
        if (characteristics instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> map) {
                    Object n = map.get("name");
                    Object v = map.get("value");
                    if (n instanceof String && v instanceof String && !((String) v).isBlank()) {
                        otherKeys.add((String) n);
                        otherValues.add((String) v);
                    }
                }
            }
        }

        int filterCount = 0;
        if (function != null && !function.isBlank()) filterCount++;
        if (name != null && !name.isBlank()) filterCount++;
        if (minLength != null && maxLength != null) filterCount++;
        filterCount += otherKeys.size();

        // AUCUN filtre → renvoie tout le subgroup (optimisé)
        if (filterCount == 0) {
            List<CategoryDTO> list = categoryRepository.findAllCategoriesFlat(null, subGroup.getId(), null);
            batchEnrichCategoryPictures(list);;
            return list;
        }

        // Empêcher IN () vide dans SQL
        boolean useOther = !otherKeys.isEmpty();
        if (!useOther) {
            otherKeys = List.of("__NO_MATCH__");
            otherValues = List.of("__NO_MATCH__");
        }

        // On récupère uniquement les IDs filtrés par SQL
        List<Long> categoryIds = categoryCharRepository.searchCategoriesSQL(
                subGroup.getId(),
                (function != null && !function.isBlank()) ? function : null,
                (name != null && !name.isBlank()) ? name : null,
                minLength,
                maxLength,
                otherKeys,
                otherValues,
                useOther,
                filterCount
        );

        if (categoryIds.isEmpty()) {
            return List.of();
        }

        // Requête pivotée → renvoie directement des CategoryDTO
        List<CategoryDTO> list = categoryRepository.findAllCategoriesFlat(null, null, categoryIds);
        batchEnrichCategoryPictures(list);;
        return list;
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

        CategoryDTO dto = searchCategory(category.getId());
        return dto;
    }

    public CategoryDTO searchCategory(Long categoryId) {
        CategoryDTO dto = categoryRepository.findAllCategoriesFlat(null, null, List.of(categoryId))
            .stream()
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        enrichCategoryWithPictures(dto);
        return dto;
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

    /**
     * Enrich the DTO with pictures, but CATEGORY pictures must already be filled by batch.
     * This fallback only loads INSTRUMENT pictures if no CATEGORY pictures exist.
     */
    private void enrichCategoryWithPictures(CategoryDTO dto) {

        // if batch already filled CATEGORY pictures → OK
        List<Long> pics = dto.getPicturesId();
        if (pics != null && !pics.isEmpty()) {
            return;
        }

        // fallback: instrument pictures
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsOfCatergory(dto.getId());
        List<Long> fallback = new ArrayList<>();

        for (InstrumentDTO inst : instruments) {
            // still per instrument (étape 2 optimisera ça)
            fallback.addAll(
                pictureStorageService.getPicturesIdByReferenceIdAndPictureType(
                    inst.getId(), PictureType.INSTRUMENT
                )
            );
        }

        dto.setPicturesId(fallback);
    }

    private void batchEnrichCategoryPictures(List<CategoryDTO> dtos) {
        if (dtos.isEmpty()) return;

        List<Long> categoryIds = dtos.stream().map(CategoryDTO::getId).toList();

        // 1. Category pictures
        Map<Long, List<Long>> catPics = pictureStorageService.getCategoryPicturesBatch(categoryIds);

        // assign direct if exists
        dtos.forEach(dto -> dto.setPicturesId(catPics.get(dto.getId())));

        // 2. Fallback only for those missing
        List<Long> missingIds = dtos.stream()
            .filter(dto -> dto.getPicturesId() == null || dto.getPicturesId().isEmpty())
            .map(CategoryDTO::getId)
            .toList();

        if (missingIds.isEmpty()) return;

        // 3. instruments batch
        Map<Long, List<Long>> catToInst = instrumentService.findInstrumentsBatch(missingIds);

        List<Long> instIds = catToInst.values().stream().flatMap(List::stream).toList();
        if (instIds.isEmpty()) return;

        // 4. pictures for instruments batch
        Map<Long, List<Long>> instPics = pictureStorageService.getInstrumentPicturesBatch(instIds);

        // 5. assign fallback
        for (CategoryDTO dto : dtos) {
            if (dto.getPicturesId() != null && !dto.getPicturesId().isEmpty()) continue;

            List<Long> inst = catToInst.get(dto.getId());
            if (inst == null) continue;

            List<Long> fallback = new ArrayList<>();
            for (Long instId : inst) {
                List<Long> pics = instPics.get(instId);
                if (pics != null) fallback.addAll(pics);
            }
            dto.setPicturesId(fallback);
        }
    }


}