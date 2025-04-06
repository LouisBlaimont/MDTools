package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Characteristic;

/**
 * Repository interface for managing {@link Characteristic} entities.
 * Extends the {@link CrudRepository} interface to provide CRUD operations.
 * 
 * @see CrudRepository
 */
public interface CharacteristicRepository extends CrudRepository<Characteristic, Integer>{
    /**
     * Retrieves an optional Characteristic entity based on its name.
     *
     * @param name the name of the characteristic to search for
     * @return an Optional containing the found Characteristic if it exists, or an empty Optional if not found
     */
    Optional<Characteristic> findByName(String name);
}
