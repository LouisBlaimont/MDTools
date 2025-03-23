package be.uliege.speam.team03.MDTools.services;

import java.util.*;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import lombok.AllArgsConstructor;
import be.uliege.speam.team03.MDTools.mapper.SupplierMapper;

@AllArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    /**
     * Find a supplier by his name.
     * 
     * @param supplierName the name of the supplier to find
     * @return a SupplierDTO matching the supplier name, or null if no supplier is found
     */
    public SupplierDTO findSupplierByName(String supplierName) {
        Optional<Supplier> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Supplier supplier = supplierMaybe.get();
        return new SupplierDTO(supplier.getSupplierName(), supplier.getId(), supplier.getSoldByMd(), supplier.getClosed());
    }

    /**
     * Find suppliers by their name.
     * 
     * @param supplierName the name of the supplier to find
     * @return a list of SupplierDTOs matching the supplier name, or null if no suppliers are found
     */
    public List<SupplierDTO> findSuppliersByName(String supplierName) {
        Optional<Supplier> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Supplier supplier = supplierMaybe.get();
        List<SupplierDTO> supplierDTOs = new ArrayList<>();
        supplierDTOs.add(new SupplierDTO(supplier.getSupplierName(), supplier.getId(), supplier.getSoldByMd(), supplier.getClosed()));
        return supplierDTOs;
    }

    /**
     * Save a supplier.
     * 
     * @param supplier the SupplierDTO to save
     * @return the saved SupplierDTO
     * @throws IllegalArgumentException if the supplier name is null or empty
     */
    public SupplierDTO saveSupplier(SupplierDTO supplier) {
        if (supplier.getName() == null || supplier.getName().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        Supplier savedSupplier = supplierRepository.save(supplierMapper.convertToEntity(supplier));
        return supplierMapper.convertToDTO(savedSupplier);
    }

    /**
     * Delete a supplier by its ID.
     * 
     * @param supplierId the ID of the supplier to delete
     */
    public void deleteSupplier(Integer supplierId) {
        supplierRepository.deleteById(supplierId);
    }

    /**
     * Find a supplier by its ID.
     * 
     * @param supplierId the ID of the supplier to find
     * @return the SupplierDTO with the specified ID, or null if no supplier is found
     */
    public SupplierDTO findSupplierById(Integer supplierId) {
        Optional<Supplier> supplierMaybe = supplierRepository.findById(supplierId);
        return supplierMaybe.map(supplierMapper::convertToDTO).orElse(null);
    }

    /**
     * Find the maximum supplier ID, which is the one of the last element in the database.
     * 
     * @return the maximum supplier ID
     */
    public Integer findMaxSupplierId() {
        return supplierRepository.findMaxSupplierId();
    }

    /**
     * Find all suppliers.
     * 
     * @return a list of all SupplierDTOs, or null if no suppliers are found
     */
    public List<SupplierDTO> findAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        supplierRepository.findAll().forEach(suppliers::add);
        return suppliers.isEmpty() ? null : supplierMapper.convertToDTOList(suppliers);
    }
}
