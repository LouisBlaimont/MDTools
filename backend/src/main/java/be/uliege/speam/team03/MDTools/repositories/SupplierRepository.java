package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import be.uliege.speam.team03.MDTools.models.Suppliers;

import org.springframework.stereotype.Repository;


@Repository
public interface SupplierRepository extends CrudRepository<Suppliers, Integer> {
    Optional<Suppliers> findByName(String supplierName);
    Optional<Suppliers> findById(Integer supplierId);

    @SuppressWarnings("null")
    List<Suppliers> findAll();
}
