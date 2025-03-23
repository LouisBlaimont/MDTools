package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;

@Repository
public interface InstrumentRepository extends CrudRepository<Instruments, Integer>, InstrumentRepositoryCustom {
    
    Optional<Instruments> findByReference(String reference);
    Optional<Instruments> findByCategoryId(Integer id);
    Optional<List<Instruments>> findByCategory(Category category); 
    Optional<List<Instruments>> findBySupplierId(Integer id); 
    @NonNull
    Optional<Instruments> findById(@NonNull Integer id);

    /**
     * Find the maximum supplier ID, which is the one of the last element (supplier) in th database.
     * 
     * @return the maximum supplier ID, or 0 if no suppliers are found
     */
    @Query("SELECT COALESCE(MAX(i.id), 0) FROM Instruments i")
    Integer findMaxInstrumentId();
    @Override
    List<Instruments> searchByKeywords(List<String> keywords);
}
