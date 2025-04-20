package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.Supplier;

@Repository
public interface InstrumentRepository extends CrudRepository<Instruments, Integer>, InstrumentRepositoryCustom {
    
    /**
     * Retrieves an iinstrument based on its ID.
     * @param reference the reference of the instrument to search for.
     * @return Instrument that has the corresponding reference
     */
    Optional<Instruments> findByReference(String reference);
    
    // Optional<Instruments> findByCategoryId(Long id);
    /**
     * Retieves a list of instrument based on the category ID.
     * @param category  the category object
     * @return  all the instruments of the given category
     */
    Optional<List<Instruments>> findByCategory(Category category); 

    /**
     * Retieves a list of instruments associated to the specified supplier.
     * @param id the id of the supplier
     * @return  the list of all instruments sold by that supplier
     */
    Optional<List<Instruments>> findBySupplierId(Long id); 

    /**
     * Retieves a list of instruments associated to the specified supplier.
     * @param supplier the supplier object
     * @return  the list of all instruments sold by that supplier
     */
    List<Instruments> findAllBySupplier(Supplier supplier);

    /**
     * Retrives the instrument based on its id
     * @param id id of the instrument
     * @return the instrument that corresponds to the id
     */
    @NonNull
    Optional<Instruments> findById(@NonNull Long id);

    /**
     * Retrieves a list of instruments that match the given keywords
     * @param keywords a list of keywords given by the user
     * @return a list of instrument
     */
    @Override
    List<Instruments> searchByKeywords(List<String> keywords);
}
