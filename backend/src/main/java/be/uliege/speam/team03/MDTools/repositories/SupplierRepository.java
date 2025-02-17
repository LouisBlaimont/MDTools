package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Suppliers;

@Repository
public interface SupplierRepository extends CrudRepository<Suppliers, Integer> {
    Optional<Suppliers> findByName(String supplierName);
    Optional<Suppliers> findById(Integer supplierId);
}
