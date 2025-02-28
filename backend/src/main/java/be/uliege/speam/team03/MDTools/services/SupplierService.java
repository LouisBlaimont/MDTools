package be.uliege.speam.team03.MDTools.services;

import java.util.*;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import be.uliege.speam.team03.MDTools.mapper.SupplierMapper;

@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    /**
     * Convert a list of Suppliers entities to a list of SupplierDTOs.
     * 
     * @param suppliers the list of Suppliers entities to convert
     * @return the converted list of SupplierDTOs
     */
    private List<SupplierDTO> convertToDTOList(List<Suppliers> suppliers) {
        List<SupplierDTO> supplierDTOs = new ArrayList<>();
        for (Suppliers supplier : suppliers) {
            supplierDTOs.add(supplierMapper.convertToDTO(supplier));
        }
        return supplierDTOs;
    }

    /**
     * Find suppliers by their name.
     * 
     * @param supplierName the name of the supplier to find
     * @return a list of SupplierDTOs matching the supplier name, or null if no suppliers are found
     */
    public List<SupplierDTO> findSuppliersByName(String supplierName) {
        Optional<Suppliers> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (!supplierMaybe.isPresent()) {
            return null;
        }
        Suppliers supplier = supplierMaybe.get();
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
        Suppliers savedSupplier = supplierRepository.save(supplierMapper.convertToEntity(supplier));
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
        Optional<Suppliers> supplierMaybe = supplierRepository.findById(supplierId);
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
        List<Suppliers> suppliers = new ArrayList<>();
        supplierRepository.findAll().forEach(suppliers::add);
        return suppliers.isEmpty() ? null : convertToDTOList(suppliers);
    }
}
