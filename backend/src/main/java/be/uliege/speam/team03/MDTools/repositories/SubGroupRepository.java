package be.uliege.speam.team03.MDTools.repositories;

import be.uliege.speam.team03.MDTools.models.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubGroupRepository extends CrudRepository<SubGroups, Integer> {
    Optional<List<SubGroups>> findByGroup(Group group);
}
