package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;


@Service
public class CategoryService {
    private GroupRepository groupRepository;
    private CategoryRepository categoryRepository ;

    public CategoryService(GroupRepository groupRepo, CategoryRepository categoryRepo){
        this.groupRepository = groupRepo;
        this.categoryRepository = categoryRepo;
    }

    public List<CategoryDTO> findAllCategory(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();
        Optional<List<Category>> categoryMaybe = categoryRepository.findByGroup(group);
        if (categoryMaybe.isPresent() == false){
            return null;
        }
        List<Category> category = categoryMaybe.get();
        List<CategoryDTO> categoryDTO = category.stream().map(cat -> new CategoryDTO(cat.getShape())).collect(Collectors.toList());
        return categoryDTO;
    }
}
