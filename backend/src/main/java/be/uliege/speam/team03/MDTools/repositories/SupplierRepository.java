package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import be.uliege.speam.team03.MDTools.models.Suppliers;

import org.springframework.stereotype.Repository;


@Repository
public interface SupplierRepository extends CrudRepository<Suppliers, Integer> {
    Optional<Suppliers> findBySupplierName(String supplierName);
    @NonNull
    Optional<Suppliers> findById(@NonNull Integer supplierId);
    @NonNull
    List<Suppliers> findAll();

    /**
     * Find the maximum supplier ID, which is the one of the last element (supplier) in th database.
     * 
     * @return the maximum supplier ID, or 0 if no suppliers are found
     */
    @Query("SELECT COALESCE(MAX(s.id), 0) FROM Suppliers s")
    Integer findMaxSupplierId();
}
