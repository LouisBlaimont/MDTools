package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Group;

/**
 * Repository interface for managing {@link Group} entities.
 * Extends the {@link CrudRepository} interface to provide CRUD operations.
 * 
 * @see CrudRepository
 */
public interface GroupRepository extends CrudRepository<Group, Long>{

    /**
     * Retrieves an Optional containing a Group entity that matches the given name.
     *
     * @param name the name of the group to search for
     * @return an Optional containing the Group entity if found, or an empty Optional if not found
     */
    Optional<Group> findByName(String name);

    /**
     * Retrieves an Optional containing a Group entity that matches the given ID.
     * 
     * @param id
     * @return
     */
    Optional<Group> findById(Integer id);
}
