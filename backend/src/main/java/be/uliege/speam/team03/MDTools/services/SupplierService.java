package be.uliege.speam.team03.MDTools.services;

import java.util.*;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepo) {
        this.supplierRepository = supplierRepo;
    }

    private SupplierDTO convertToDTO(Suppliers supplier) {
        return new SupplierDTO(
            supplier.getSupplierName(),
            supplier.getSupplierId(),
            supplier.isSoldByMd(),
            supplier.isClosed()
        );
    }

    private Suppliers convertToEntity(SupplierDTO supplierDTO) {
        Suppliers supplier = new Suppliers();
        supplier.setSupplierName(supplierDTO.getName());
        supplier.setSupplierId(supplierDTO.getId());
        supplier.setSoldByMd(supplierDTO.isSoldByMD() != null ? supplierDTO.isSoldByMD() : true);
        supplier.setClosed(supplierDTO.isClosed());
        return supplier;
    }

    private List<SupplierDTO> convertToDTOList(List<Suppliers> suppliers) {
        List<SupplierDTO> supplierDTOs = new ArrayList<>();
        for (Suppliers supplier : suppliers) {
            supplierDTOs.add(convertToDTO(supplier));
        }
        return supplierDTOs;
    }

    public List<SupplierDTO> findSuppliersByName(String supplierName) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findByName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
        List<SupplierDTO> supplierDTOs = new ArrayList<>();
        supplierDTOs.add(new SupplierDTO(supplier.getSupplierName(), supplier.getSupplierId(), supplier.isSoldByMd(), supplier.isClosed()));
        return supplierDTOs;
    }

    public SupplierDTO saveSupplier(SupplierDTO supplier) {
        if (supplier.getName() == null || supplier.getName().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        Suppliers savedSupplier = supplierRepository.save(convertToEntity(supplier));
        return convertToDTO(savedSupplier);
    }

    public void deleteSupplier(Integer supplierId) {
        supplierRepository.deleteById(supplierId);
    }

    public SupplierDTO findSupplierById(Integer supplierId) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findById(supplierId);
        return supplierMaybe.map(this::convertToDTO).orElse(null);
    }

    public List<SupplierDTO> findAllSuppliers() {
        List<Suppliers> suppliers = new ArrayList<>();
        supplierRepository.findAll().forEach(suppliers::add);
        return suppliers.isEmpty() ? null : convertToDTOList(suppliers);
    }
}
