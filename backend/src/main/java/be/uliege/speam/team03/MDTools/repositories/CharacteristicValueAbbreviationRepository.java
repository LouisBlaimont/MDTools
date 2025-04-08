package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;


/**
 * Repository interface for managing CharacteristicValueAbbreviation entities.
 * Provides methods for retrieving and deleting characteristic value abbreviations
 * based on their values.
 *
 * <p>This interface extends JpaRepository, which provides basic CRUD operations
 * and allows for the definition of custom query methods.</p>
 *
 * <p>Available operations:</p>
 * <ul>
 *   <li>Find a CharacteristicValueAbbreviation by its value.</li>
 *   <li>Delete a CharacteristicValueAbbreviation by its value.</li>
 * </ul>
 *
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see be.uliege.speam.team03.MDTools.entities.CharacteristicValueAbbreviation
 */
public interface CharacteristicValueAbbreviationRepository extends JpaRepository<CharacteristicValueAbbreviation, Long> {

   /**
    * Retrieves an optional CharacteristicValueAbbreviation entity based on the provided characteristic value.
    *
    * @param characteristicValue the value of the characteristic to search for.
    * @return an Optional containing the CharacteristicValueAbbreviation if found, or an empty Optional if not found.
    */
   Optional<CharacteristicValueAbbreviation> findByValue(String characteristicValue);

   /**
    * Deletes a characteristic value abbreviation from the repository based on its value.
    *
    * @param characteristicValue the value of the characteristic to be deleted
    */
   void deleteByValue(String characteristicValue);
}
