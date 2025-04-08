package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CategoryRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import lombok.AllArgsConstructor;

/**
 * Service for managing characteristic-related logic.
 */
@Service
@AllArgsConstructor
public class CharacteristicService {
    private final CharacteristicRepository charRepository;
    private final CategoryCharacteristicRepository categoryCharacteristicRepository;
    private final SubGroupRepository subGroupRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Returns all existing characteristic names.
     *
     * @return list of names
     */
    public List<String> getAllCharacteristicNames() {
        return StreamSupport.stream(charRepository.findAll().spliterator(), false)
            .map(Characteristic::getName)
            .toList();
    }

    /**
     * Gets all possible value of the characteristic given by charName in the categories related the the subgroup given by subGroupName
     * @param charName 
     * @param subGroupName 
     * @return A list of values (string)
     */
    public List<String> getPossibleValuesOfChar(String charName, String subGroupName){
        Optional<Characteristic> charMaybe = charRepository.findByName(charName);
        if(charMaybe.isEmpty()){
            throw new ResourceNotFoundException("Characteristic with the name " + charName + " not found");
        }
        Characteristic characteristic = charMaybe.get();
        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(subGroupName);
        if(subGroupName.isEmpty()){
            throw new ResourceNotFoundException("Cannot find subgroup" + subGroupName);
        }
        SubGroup subGroup = subgroupMaybe.get();
        List<Category> categories = categoryRepository.findBySubGroup(subGroup, Sort.by("subGroupName", "id"));

        List<CategoryCharacteristic> categoryChars = categoryCharacteristicRepository.findByCharacteristicAndCategoryIn(characteristic, categories);

        return categoryChars.stream().map(CategoryCharacteristic::getVal).distinct().collect(Collectors.toList());
    }
}
