package be.uliege.speam.team03.MDTools.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sound.midi.Instrument;

import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;

public class CategoryMapper {

    private final CategoryRepository categoryRepository;

    public CategoryMapper(CategoryRepository categoryRepository, PictureStorageService pictureStorageService) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Maps a Category entity to a CategoryDTO object.
     * 
     * @param category the category to convert
     * @return the converted category DTO
     */
    public CategoryDTO mapToCategoryDto(Category category, PictureStorageService pictureStorageService) {
        Long id = category.getId();
        String gName = category.getSubGroup().getGroup().getName();
        String subgName = category.getSubGroup().getName();

        String name;
        Optional<String> nameMaybe = categoryRepository.findCharacteristicVal(id, "Name");
        if (nameMaybe.isPresent()) {
            name = nameMaybe.get();
        } else {
            System.out.println("Name is not present");
            name = null;
        }

        String externalCode;
        Optional<String> externalCodeMaybe = categoryRepository.findCharacteristicVal(id, "Code Externe");
        if (externalCodeMaybe.isPresent()) {
            externalCode = externalCodeMaybe.get();
        } else {
            System.out.println("externalCode is not present");
            externalCode = null;
        }


        String function;
        Optional<String> functionMaybe = categoryRepository.findCharacteristicVal(id, "Function");
        if (functionMaybe.isPresent()) {
            function = functionMaybe.get();
        } else {
            function = null;
        }

        String design;
        Optional<String> designMaybe = categoryRepository.findCharacteristicVal(id, "Design");
        if (designMaybe.isPresent()) {
            design = designMaybe.get();
        } else {
            design = null;
        }

        String author;
        Optional<String> authorMaybe = categoryRepository.findCharacteristicVal(id, "Auteur");
        if (authorMaybe.isPresent()) {
            author = authorMaybe.get();
        } else {
            author = null;
        }

        String dimOrig;
        Optional<String> dimOrigMaybe = categoryRepository.findCharacteristicVal(id, "Dim orig");
        if (dimOrigMaybe.isPresent()) {
            dimOrig = dimOrigMaybe.get();
        } else {
            dimOrig = null;
        }
        String shape = category.getShape();

        String lenAbrv;
        Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicVal(id, "Length");
        if (lenAbrvMaybe.isPresent()) {
            lenAbrv = lenAbrvMaybe.get();
        } else {
            lenAbrv = null;
        }

        List<Long> pictureIds = pictureStorageService.getPicturesIdByReferenceIdAndPictureType((long) category.getId(),
                PictureType.CATEGORY);

        CategoryDTO categoryDTO = new CategoryDTO(id, gName, subgName, name, externalCode, function, design, shape, author, dimOrig, lenAbrv, pictureIds);

        return categoryDTO;
    }

    /**
     * Maps a Category entity to a CategoryDTO object.
     * 
     * @param category the category to convert
     * @return the converted category DTO
     */
    public CategoryDTO mapToCategoryDtoWithDefaultPictureFromInstruments(Category category,
            PictureStorageService pictureStorageService, InstrumentService instrumentService) {
        Long id = category.getId();
        String gName = category.getSubGroup().getGroup().getName();
        String subgName = category.getSubGroup().getName();

        String name;
        Optional<String> nameMaybe = categoryRepository.findCharacteristicVal(id, "Name");
        if (nameMaybe.isPresent()) {
            name = nameMaybe.get();
        } else {
            System.out.println("Name is not present");
            name = null;
        }

        String externalCode;
        Optional<String> externalCodeMaybe = categoryRepository.findCharacteristicVal(id, "Code Externe");
        if (externalCodeMaybe.isPresent()) {
            externalCode = externalCodeMaybe.get();
        } else {
            System.out.println("externalCode is not present");
            externalCode = null;
        }


        String function;
        Optional<String> functionMaybe = categoryRepository.findCharacteristicVal(id, "Function");
        if (functionMaybe.isPresent()) {
            function = functionMaybe.get();
        } else {
            function = null;
        }

        String design;
        Optional<String> designMaybe = categoryRepository.findCharacteristicVal(id, "Design");
        if (designMaybe.isPresent()) {
            design = designMaybe.get();
        } else {
            design = null;
        }

        String author;
        Optional<String> authorMaybe = categoryRepository.findCharacteristicVal(id, "Auteur");
        if (authorMaybe.isPresent()) {
            author = authorMaybe.get();
        } else {
            author = null;
        }

        String dimOrig;
        Optional<String> dimOrigMaybe = categoryRepository.findCharacteristicVal(id, "Dim orig");
        if (dimOrigMaybe.isPresent()) {
            dimOrig = dimOrigMaybe.get();
        } else {
            dimOrig = null;
        }

        String lenAbrv;
        Optional<String> lenAbrvMaybe = categoryRepository.findCharacteristicVal(id, "Length");
        if (lenAbrvMaybe.isPresent()) {
            lenAbrv = lenAbrvMaybe.get();
        } else {
            lenAbrv = null;
        }

        String shape = category.getShape();

        // Create a mutable ArrayList from the potentially immutable list
        List<Long> pictureIds = new ArrayList<>(pictureStorageService
                .getPicturesIdByReferenceIdAndPictureType((long) category.getId(), PictureType.CATEGORY));
        if (pictureIds.isEmpty()) {
            // Get a default picture from an instrument
            List<InstrumentDTO> instrList = instrumentService.findInstrumentsOfCatergory(id);
            if (!instrList.isEmpty()) {
                // Add all the pictures of all the instruments to the list
                for (InstrumentDTO instrumentDTO : instrList) {
                    List<Long> instrPictureIds = pictureStorageService
                            .getPicturesIdByReferenceIdAndPictureType(instrumentDTO.getId(), PictureType.INSTRUMENT);
                    pictureIds.addAll(instrPictureIds);
                }
            }
        }

        CategoryDTO categoryDTO = new CategoryDTO(id, gName, subgName, name, externalCode, function, design, shape, author, dimOrig, lenAbrv, pictureIds);

        return categoryDTO;
    }

    /**
     * Maps a CategoryDTO object to a Category entity.
     * 
     * @param categoryDTO
     * @return
     */
    public Category mapToCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setId(categoryDTO.getId());
        category.setShape(categoryDTO.getShape());
        // category.setPictureId(categoryDTO.getPictureId());
        return category;
    }
}
