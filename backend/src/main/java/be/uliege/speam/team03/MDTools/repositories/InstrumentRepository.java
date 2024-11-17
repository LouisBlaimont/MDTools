package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


import be.uliege.speam.team03.MDTools.models.Instruments;

public interface InstrumentRepository extends CrudRepository<Instruments, Integer> {
    
    Optional<Instruments> findByReference(String reference);
}
