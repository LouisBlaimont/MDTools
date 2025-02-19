package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.AlternativesId;
 
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlternativesRepository extends CrudRepository<Alternatives, AlternativesId> {
    List<Alternatives> findByInstrumentsId1(Integer instrumentId);
}
