package be.uliege.speam.team03.MDTools.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
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

    public CategoryService(GroupRepository groupRepo, SubGroupRepository subGroupRepo, CategoryRepository categoryRepo, CharacteristicRepository charRepo, CategoryCharacteristicRepository catCharRepo) {
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
        this.categoryRepository = categoryRepo;
        this.characteristicRepository = charRepo;
        this.categoryCharRepository = catCharRepo;
        this.catMapper = new CategoryMapper(categoryRepo);
    }

    public List<CategoryDTO> findCategoriesOfGroup(String groupName) {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();

        List<SubGroup> subGroups = subGroupRepository.findByGroup(group);
        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroupIn(subGroups);
        if (categoriesMaybe.isPresent() == false){
            return null;
        }
        List<Category> categories = categoriesMaybe.get();
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories){
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

    public List<CategoryDTO> findCategoriesOfSubGroup(String subGroupName){
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isPresent() == false){
            return null;
        }
        SubGroup subGroup = subGroupMaybe.get();
        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroup(subGroup);
        if (categoriesMaybe.isPresent() == false){
            return null;
        }

        List<Category> categories = categoriesMaybe.get();
        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : categories){
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

    //ici l'idée c'est que on a tjrs group, subgroup et les autres champs sont soit remplis soit des empty string si on cherche pas par cette char
    public List<CategoryDTO> findCategoriesByCharacteristics(Map<String, Object> body){
        String groupName = (String) body.get("groupName");
        String subGroupName = (String) body.get("subGroupName");
        
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (groupMaybe.isEmpty() || subGroupMaybe.isEmpty()){
            return null;
        }

        SubGroup subGroup = subGroupMaybe.get();
        
        Map<String, String> searchBy = new HashMap<>();
        String function = (String) body.get("function");
        searchBy.put("function", function);
        String name = (String) body.get("name");
        searchBy.put("name", name);
        Object characteristics = body.get("characteristics");

        if (characteristics instanceof List<?>){
            for (Object item : (List<?>) characteristics){
                if (item instanceof Map<?,?> characteristicMap){
                    Object nameObj = characteristicMap.get("name");
                    Object valueObj = characteristicMap.get("value");

                    if (nameObj instanceof String charName && valueObj instanceof String charValue){
                        searchBy.put(charName, charValue);
                    }
                }
            }
        }
        
        Optional<List<Category>> categoriesMaybe = categoryRepository.findBySubGroup(subGroup);
        if (categoriesMaybe.isPresent() == false){
            return null;
        }
        List<Category> categories = categoriesMaybe.get();

        //remove characteristics without value
        Map<String, String> filteredSearchBy = searchBy.entrySet().stream().filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (filteredSearchBy.isEmpty()){
            List<CategoryDTO> categoriesDTO = new ArrayList<>();
            for (Category category : categories){
                CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
                categoriesDTO.add(categoryDTO);
            }
            return categoriesDTO;
        }

        List<Integer> categoryIds = categories.stream().map(Category::getId).toList();

        List<CategoryCharacteristic> categoryChars = categoryCharRepository.findByCategoryIds(categoryIds);

        Map<Category, Map<String,String>> categoryToChar = categoryChars.stream()
        .collect(Collectors.groupingBy(
            CategoryCharacteristic::getCategory,
            Collectors.toMap(cc -> cc.getCharacteristic().getName(),
            CategoryCharacteristic::getVal, (existing, replacement) -> existing)
        ));

        List<Category> filteredCategories = categoryToChar.entrySet().stream()
        .filter(entry -> entry.getValue().entrySet().containsAll(filteredSearchBy.entrySet()))
        .map(Map.Entry::getKey).toList();

        List<CategoryDTO> categoriesDTO = new ArrayList<>();
        for (Category category : filteredCategories){
            CategoryDTO categoryDTO = catMapper.mapToCategoryDto(category);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;





        
    }

}
