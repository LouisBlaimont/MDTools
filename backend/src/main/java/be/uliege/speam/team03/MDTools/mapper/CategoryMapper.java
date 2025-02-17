package be.uliege.speam.team03.MDTools.mapper;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import java.util.Optional;


import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;

public class CategoryMapper {

    private CategoryRepository categoryRepository;

    public CategoryMapper(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryDTO mapToCategoryDto(Category category){
        Integer id = category.getId();
        String gName = category.getSubGroup().getGroup().getName();
        String subgName = category.getSubGroup().getName();
        
        String name = new String();
        Optional<String> nameMaybe = categoryRepository.findCharacteristicVal((long) id, "Name");
        if (nameMaybe.isPresent()){ 
            name = nameMaybe.get();
        }
        else{
            name = null;
        }

        String function = new String();
        Optional<String> functionMaybe = categoryRepository.findCharacteristicVal((long) id, "Function");
        if (functionMaybe.isPresent()){
            function = functionMaybe.get();
        }
        else{
            name = null;
        }

        String shape = category.getShape();

        String lenAbrv = new String();
        Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicValAbrv((long) id, "Length");
        if (lenAbrvMaybe.isPresent()){
            lenAbrv = lenAbrvMaybe.get();
        }
        else{
            lenAbrv = null;
        }
        CategoryDTO categoryDTO = new CategoryDTO(id, gName, subgName, name, function, shape, lenAbrv, category.getPictureId());

        return categoryDTO;
    }
}
