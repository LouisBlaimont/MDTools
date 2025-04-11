package be.uliege.speam.team03.MDTools.mapper;

import java.util.Optional;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;

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
        Long id = category.getId();
        String gName = category.getSubGroup().getGroup().getName();
        String subgName = category.getSubGroup().getName();
        
        String name;
        Optional<String> nameMaybe = categoryRepository.findCharacteristicVal(id, "Name");
        if (nameMaybe.isPresent()){ 
            name = nameMaybe.get();
        } else {
            System.out.println("Name is not present");
            name = null;
        }

        String function;
        Optional<String> functionMaybe = categoryRepository.findCharacteristicVal(id, "Function");
        if (functionMaybe.isPresent()){
            function = functionMaybe.get();
        }
        else{
            function = null;
        }

        String shape = category.getShape();

        String lenAbrv;
        Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicVal(id, "Length");
        if (lenAbrvMaybe.isPresent()){
            lenAbrv = lenAbrvMaybe.get();
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
        category.setShape(categoryDTO.getShape());
        category.setPictureId(categoryDTO.getPictureId());
        return category;
    }
}
