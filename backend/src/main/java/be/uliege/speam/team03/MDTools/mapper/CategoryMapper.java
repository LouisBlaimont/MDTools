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

    /**
     * Maps a Category entity to a CategoryDTO object.
     * 
     * @param category
     * @return
     */
    public CategoryDTO mapToCategoryDto(Category category){
        Integer id = category.getId();
        String gName = category.getSubGroup().getGroup().getName();
        String subgName = category.getSubGroup().getName();
        
        String name;
        Optional<String> nameMaybe = categoryRepository.findCharacteristicVal((long) id, "Name");
        if (nameMaybe.isPresent()){ 
            name = nameMaybe.get();
        } else if (category.getName() != null){
            name = category.getName();
        }
        else{
            name = null;
        }

        String function;
        Optional<String> functionMaybe = categoryRepository.findCharacteristicVal((long) id, "Function");
        if (functionMaybe.isPresent()){
            function = functionMaybe.get();
        } else if (category.getFunction() != null){
            function = category.getFunction();
        }
        else{
            function = null;
        }

        String shape = category.getShape();

        String lenAbrv;
        Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicVal((long) id, "Length");
        if (lenAbrvMaybe.isPresent()){
            lenAbrv = lenAbrvMaybe.get();
        } else if (category.getLenAbrv() != null){
            lenAbrv = category.getLenAbrv();
        }
        else{
            lenAbrv = null;
        }
        CategoryDTO categoryDTO = new CategoryDTO(id, gName, subgName, name, function, shape, lenAbrv, category.getPictureId());

        return categoryDTO;
    }

    /**
     * Maps a CategoryDTO object to a Category entity.
     * 
     * @param categoryDTO
     * @return
     */
    public Category mapToCategory(CategoryDTO categoryDTO){
        Category category = new Category();
        category.setId(categoryDTO.getId());
        // category.setName(categoryDTO.getName());
        // category.setFunction(categoryDTO.getFunction());
        // category.setLenAbrv(categoryDTO.getLenAbrv());
        category.setShape(categoryDTO.getShape());
        category.setPictureId(categoryDTO.getPictureId());
        return category;
    }
}
