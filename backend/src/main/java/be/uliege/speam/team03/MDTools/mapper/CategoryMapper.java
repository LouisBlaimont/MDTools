package be.uliege.speam.team03.MDTools.mapper;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.services.CharacteristicAbbreviationService;

import java.util.Optional;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;

public class CategoryMapper {

    private CategoryRepository categoryRepository;

    public CategoryMapper(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO mapToCategoryDto(Category category) {
        Integer id = category.getId();
        String gName = category.getSubGroup().getGroup().getName();
        String subgName = category.getSubGroup().getName();

        String name = categoryRepository.findCharacteristicVal((long) id, "Name").orElse(null);

        String function = categoryRepository.findCharacteristicVal((long) id, "Function").orElse(null);

        String shape = category.getShape();

        String lenAbrv = categoryRepository.findCharacteristicVal((long) id, "Length").orElse(null);

        CategoryDTO categoryDTO = new CategoryDTO(id, gName, subgName, name, function, shape, lenAbrv,
                category.getPictureId());

        return categoryDTO;
    }
}
