package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Characteristic;

public interface CharacteristicRepository extends CrudRepository<Characteristic, Integer>{
    Optional<Characteristic> findByName(String name);
}

