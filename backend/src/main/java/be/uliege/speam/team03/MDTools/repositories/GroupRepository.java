package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Group;

public interface GroupRepository extends CrudRepository<Group, Integer>{
    Optional<Group> findByName(String name);
}

