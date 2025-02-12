package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.Category;

@Repository
public interface InstrumentRepository extends CrudRepository<Instruments, Integer> {
    
    Optional<Instruments> findByReference(String reference);
    Optional<Instruments> findByCategoryId(Integer id);
    Optional<List<Instruments>> findByCategory(Category category);  
}
