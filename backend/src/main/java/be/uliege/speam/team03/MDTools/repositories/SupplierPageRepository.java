package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import be.uliege.speam.team03.MDTools.models.Supplier;

import org.springframework.stereotype.Repository;


@Repository
public interface SupplierPageRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findBySupplierName(String supplierName);
    @NonNull
    Optional<Supplier> findById(@NonNull Integer supplierId);
}
