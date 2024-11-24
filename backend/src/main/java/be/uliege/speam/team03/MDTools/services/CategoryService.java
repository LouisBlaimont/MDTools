package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;

@Service
public class CategoryService {
    private GroupRepository groupRepository;
    private SubGroupRepository subGroupRepository;
    private CategoryRepository categoryRepository;

    public CategoryService(GroupRepository groupRepo, SubGroupRepository subGroupRepo, CategoryRepository categoryRepo) {
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
        this.categoryRepository = categoryRepo;
    }

    public List<CategoryDTO> findAllCategory(String groupName) {
    // Fetch the group by name
    Group group = groupRepository.findByName(groupName)
            .orElseThrow(() -> new ResourceNotFoundException("Group not found with name: " + groupName));

    // Fetch all sub-groups associated with this group
    List<SubGroup> subGroups = subGroupRepository.findByGroup(group);

    // Fetch all categories associated with these sub-groups
    List<Category> categories = categoryRepository.findBySubGroupIn(subGroups);

    // Map categories to CategoryDTO
    return categories.stream()
            .map(cat -> new CategoryDTO(cat.getShape()))
            .collect(Collectors.toList());
    }

}
