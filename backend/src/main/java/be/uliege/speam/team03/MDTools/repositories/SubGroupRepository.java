package be.uliege.speam.team03.MDTools.repositories;

import be.uliege.speam.team03.MDTools.models.SubGroups;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubGroupRepository extends CrudRepository<SubGroups, Integer> {
    // Pas besoin de redéfinir findById, il est déjà inclus dans CrudRepository.
}
