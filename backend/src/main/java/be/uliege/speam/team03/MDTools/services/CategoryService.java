package be.uliege.speam.team03.MDTools.services;

import java.util.*;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

@Service
public class CategoryService {
    private GroupRepository groupRepository;
    private SubGroupRepository subGroupRepository;
    private CategoryRepository categoryRepository;
    private CharacteristicRepository characteristicRepository;
    private CategoryCharacteristicRepository categoryCharRepository;

    public CategoryService(GroupRepository groupRepo, SubGroupRepository subGroupRepo, CategoryRepository categoryRepo, CharacteristicRepository charRepo, CategoryCharacteristicRepository catCharRepo) {
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
        this.categoryRepository = categoryRepo;
        this.characteristicRepository = charRepo;
        this.categoryCharRepository = catCharRepo;
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
            Integer id = category.getId();
            String gName = group.getName();
            String subGroupName = category.getSubGroup().getName();
            
            String name = new String();
            Optional<String> nameMaybe = categoryRepository.findCharacteristicVal(id, "Name");
            if (nameMaybe.isPresent()){ 
                name = nameMaybe.get();
            }
            else{
                name = null;
            }

            String function = new String();
            Optional<String> functionMaybe = categoryRepository.findCharacteristicVal(id, "Function");
            if (functionMaybe.isPresent()){
                function = functionMaybe.get();
            }
            else{
                name = null;
            }

            String shape = category.getShape();

            String lenAbrv = new String();
            Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicValAbrv(id, "Length");
            if (lenAbrvMaybe.isPresent()){
                lenAbrv = lenAbrvMaybe.get();
            }
            else{
                lenAbrv = null;
            }
            CategoryDTO categoryDTO = new CategoryDTO(gName, subGroupName, name, function, shape, lenAbrv);
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
            Integer id = category.getId();
            String gName = subGroup.getGroup().getName();
            String subgName = subGroup.getName();
            
            String name = new String();
            Optional<String> nameMaybe = categoryRepository.findCharacteristicVal(id, "Name");
            if (nameMaybe.isPresent()){ 
                name = nameMaybe.get();
            }
            else{
                name = null;
            }

            String function = new String();
            Optional<String> functionMaybe = categoryRepository.findCharacteristicVal(id, "Function");
            if (functionMaybe.isPresent()){
                function = functionMaybe.get();
            }
            else{
                name = null;
            }

            String shape = category.getShape();

            String lenAbrv = new String();
            Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicValAbrv(id, "Length");
            if (lenAbrvMaybe.isPresent()){
                lenAbrv = lenAbrvMaybe.get();
            }
            else{
                lenAbrv = null;
            }
            CategoryDTO categoryDTO = new CategoryDTO(gName, subgName, name, function, shape, lenAbrv);
            categoriesDTO.add(categoryDTO);
        }
        return categoriesDTO;
    }

}
