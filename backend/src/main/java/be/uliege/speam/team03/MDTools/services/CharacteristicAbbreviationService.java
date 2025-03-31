package be.uliege.speam.team03.MDTools.services;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.CharacteristicAbbreviationDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicValueAbbreviationRepository;
import io.micrometer.common.lang.NonNull;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

/**
 * Service class for managing characteristic abbreviations.
 * Provides methods to retrieve, add, update, and delete abbreviations
 * associated with characteristic values.
 * 
 * This service interacts with the
 * {@link CharacteristicValueAbbreviationRepository}
 * and {@link CategoryCharacteristicRepository} to perform CRUD operations
 * and retrieve specific data related to characteristic abbreviations.
 * 
 * Main functionalities include:
 * - Retrieving all characteristic abbreviations.
 * - Fetching a specific abbreviation by characteristic value.
 * - Adding new abbreviations.
 * - Updating existing abbreviations.
 * - Deleting abbreviations.
 * - Identifying non-existing abbreviations.
 * 
 * This service ensures that abbreviations are properly managed and provides
 * utility methods for interacting with the underlying data repositories.
 */
@AllArgsConstructor
@Service
public class CharacteristicAbbreviationService {
   private final CharacteristicValueAbbreviationRepository repository;
   private final CategoryCharacteristicRepository categoryCharacteristicRepository;

   /**
    * Retrieves all characteristic abbreviations from the repository and maps them
    * to a list of CharacteristicAbbreviationDTO objects.
    *
    * @return an Optional containing a list of CharacteristicAbbreviationDTO
    *         objects,
    *         or an empty Optional if no abbreviations are found.
    */
   public List<CharacteristicAbbreviationDTO> getAllAbbreviations() {
      return repository.findAll()
            .stream()
            .map(abbrev -> new CharacteristicAbbreviationDTO(abbrev.getValue(), abbrev.getAbbreviation()))
            .toList();
   }

   public Page<CharacteristicAbbreviationDTO> getAllAbbreviations(Pageable pageable) {
      return repository.findAll(pageable)
            .map(abbrev -> new CharacteristicAbbreviationDTO(abbrev.getValue(), abbrev.getAbbreviation()));
   }
   
   /**
    * Retrieves the abbreviation associated with a given characteristic value.
    *
    * @param characteristicValue the characteristic value for which the
    *                            abbreviation is to be retrieved
    * @return an {@link Optional} containing the abbreviation if found, or an empty
    *         {@link Optional} if no abbreviation exists for the given value
    */
   public Optional<String> getAbbreviation(String characteristicValue) {
      return repository.findByValue(characteristicValue).map(CharacteristicValueAbbreviation::getAbbreviation);
   }

   /**
    * Adds a new abbreviation for a given characteristic value.
    *
    * @param characteristicValue the characteristic value to associate with the
    *                            abbreviation
    * @param abbreviation        the abbreviation to be associated with the
    *                            characteristic value
    */
   public void addAbbreviation(String characteristicValue, String abbreviation) {
      repository.save(new CharacteristicValueAbbreviation(characteristicValue, abbreviation));
   }

   /**
    * Adds a new abbreviation for a characteristic value.
    *
    * @param dto the data transfer object containing the characteristic value
    *            and its corresponding abbreviation
    */
   public void addAbbreviation(CharacteristicAbbreviationDTO dto) {
      String characteristicValue = dto.getValue();
      String abbreviation = dto.getAbbreviation();
      repository.save(new CharacteristicValueAbbreviation(characteristicValue, abbreviation));
   }

   public void updateAbbreviation(String characteristicValue, String abbreviation) {
      repository.findByValue(characteristicValue).ifPresent(abbrev -> {
         abbrev.setAbbreviation(abbreviation);
         repository.save(abbrev);
      });
   }

   /**
    * Updates the abbreviation for a given characteristic value.
    *
    * @param dto The {@link CharacteristicAbbreviationDTO} containing the
    *            characteristic value
    *            and the new abbreviation to be updated.
    * @return A {@link CharacteristicAbbreviationDTO} containing the updated
    *         characteristic value
    *         and abbreviation.
    * @throws ResourceNotFoundException If no abbreviation is found for the given
    *                                   characteristic value.
    */
   public CharacteristicAbbreviationDTO updateAbbreviation(@NonNull CharacteristicAbbreviationDTO dto) {
      String characteristicValue = dto.getValue();
      String abbreviation = dto.getAbbreviation();
      Optional<CharacteristicValueAbbreviation> existingAbbrev = repository.findByValue(characteristicValue);
      CharacteristicValueAbbreviation response;
      if (existingAbbrev.isPresent()) {
         existingAbbrev.get().setAbbreviation(abbreviation);
         response = repository.save(existingAbbrev.get());
      } else {
         throw new ResourceNotFoundException("Abbreviation not found for value: " + characteristicValue);
      }

      return new CharacteristicAbbreviationDTO(response.getValue(), response.getAbbreviation());
   }

   /**
    * Deletes an abbreviation associated with the given characteristic value.
    *
    * @param characteristicValue the value of the characteristic whose abbreviation
    *                            is to be deleted
    * @throws IllegalArgumentException if the characteristicValue is null or
    *                                  invalid
    * @throws DataAccessException      if an error occurs during the deletion
    *                                  process
    */
   @Transactional
   public void deleteAbbreviation(String characteristicValue) {
      repository.deleteByValue(characteristicValue);
   }

   /**
    * Retrieves a list of characteristic abbreviations that do not exist in the
    * repository.
    *
    * @return a list of strings representing the non-existing abbreviations.
    */
   public List<String> getNonExistingAbbreviations() {
      List<String> nonExistingAbbreviations = categoryCharacteristicRepository.findAllWithoutAbbreviation();
      // Filter out the only numeric strings
      nonExistingAbbreviations.removeIf(abbreviation -> abbreviation.matches("\\d+"));
      return nonExistingAbbreviations;
   }
}
