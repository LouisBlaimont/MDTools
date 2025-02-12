package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;
import java.util.List;

import be.uliege.speam.team03.MDTools.models.Alternatives;
 
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlternativesRepository extends CrudRepository<Alternatives, Integer> {
    List<Alternatives> findByInstrumentId(Integer instrumentId);
}
