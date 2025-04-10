package be.uliege.speam.team03.MDTools.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Supplier;


@Repository
public interface SupplierPageRepository extends JpaRepository<Supplier, Integer> {
    Optional<Supplier> findBySupplierName(String supplierName);
    @NonNull
    Optional<Supplier> findById(@NonNull Long supplierId);
}
