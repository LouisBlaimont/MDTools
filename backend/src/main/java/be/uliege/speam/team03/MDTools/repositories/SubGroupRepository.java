package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;

public interface SubGroupRepository extends CrudRepository<SubGroup, Integer>{
    List<SubGroup> findByGroup(Group group);
    Optional<SubGroup> findByName(String name);
}

