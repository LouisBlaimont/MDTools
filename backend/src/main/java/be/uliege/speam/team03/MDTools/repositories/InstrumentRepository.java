package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;

@Repository
public interface InstrumentRepository extends CrudRepository<Instruments, Integer> {
    
    Optional<Instruments> findByReference(String reference);
    Optional<Instruments> findByCategoryId(Integer id);
    Optional<List<Instruments>> findByCategory(Category category); 
    Optional<List<Instruments>> findBySupplierId(Integer id); 
    @NonNull
    Optional<Instruments> findById(@NonNull Integer id);
    @Query("SELECT d FROM Instruments d WHERE d.supplierDescription LIKE %:keyword%")
    List<Instruments> searchByKeyword(@Param("keyword") String keyword);

}
