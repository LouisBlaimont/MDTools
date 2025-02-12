package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Suppliers;

public interface SupplierRepository extends CrudRepository<Suppliers, Integer> {
    Optional<Suppliers> findByName(String supplierName);
}
