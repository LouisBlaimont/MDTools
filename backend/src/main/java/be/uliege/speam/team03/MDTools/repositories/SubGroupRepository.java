package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
/**
 * Repository interface for managing SubGroup entities.
 * Extends the CrudRepository interface to provide CRUD operations.
 */
public interface SubGroupRepository extends CrudRepository<SubGroup, Integer>{
    /**
     * Finds and returns a list of SubGroup entities that belong to the specified Group.
     *
     * @param group the Group entity for which to find the associated SubGroup entities
     * @return a list of SubGroup entities associated with the specified Group
     */
    List<SubGroup> findByGroup(Group group);
    /**
     * Finds a SubGroup entity by its name.
     *
     * @param name the name of the SubGroup to find
     * @return an Optional containing the found SubGroup, or an empty Optional if no SubGroup with the given name exists
     */
    Optional<SubGroup> findByName(String name);

    /**
     * Retrieves the number of instruments in the subgroup given by subGroupId.
     * @param subGroupId
     * @return
     */
    @Query("SELECT COUNT(i) FROM Instruments i WHERE i.category.subGroup.id = :subGroupId")
    int nbInstrOfSubGroup(Long subGroupId);
}
