package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.SupplierMapper;
import be.uliege.speam.team03.MDTools.models.Supplier;
import be.uliege.speam.team03.MDTools.repositories.SupplierPageRepository;
import be.uliege.speam.team03.MDTools.repositories.SupplierRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierPageRepository supplierPageRepository;
    private final SupplierMapper supplierMapper;

    /**
     * Find a supplier by their name. Throws an exception if no supplier is found.
     * 
     * @param supplierName the name of the supplier to find
     * @return a SupplierDTO matching the supplier name
     */
    public SupplierDTO findSupplierByName(String supplierName) {
        Optional<Supplier> supplierMaybe = supplierRepository.findBySupplierName(supplierName);
        if (!supplierMaybe.isPresent()) {
            throw new ResourceNotFoundException("Supplier with name " + supplierName + " does not exist");
        }
        Supplier supplier = supplierMaybe.get();
        return new SupplierDTO(supplier.getSupplierName(), supplier.getId(), supplier.getSoldByMd(), supplier.getClosed());
    }

    /**
     * Find suppliers by their name. Returns null if no suppliers are found.
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
     * Save a supplier. Throws an exception if the supplier name is null or empty.
     * 
     * @param supplier the SupplierDTO to save
     * @return the saved SupplierDTO
     * @throws IllegalArgumentException if the supplier name is null or empty
     */
    public SupplierDTO saveSupplier(SupplierDTO supplier) {
        if (supplier.getName() == null || supplier.getName().isEmpty()) {
            throw new BadRequestException("Supplier name cannot be null or empty");
        }
        Supplier savedSupplier = supplierRepository.save(supplierMapper.convertToEntity(supplier));
        return supplierMapper.convertToDTO(savedSupplier);
    }

    /**
     * Find a supplier by their ID. Throws an exception if no supplier is found.
     * 
     * @param supplierId the ID of the supplier to find
     * @return the SupplierDTO with the specified ID
     */
    public SupplierDTO findSupplierById(Long supplierId) throws ResourceNotFoundException {
        Optional<Supplier> supplierMaybe = supplierRepository.findById(supplierId);
        if (!supplierMaybe.isPresent()) {
            throw new ResourceNotFoundException("Supplier with ID " + supplierId + " does not exist");
        }
        return supplierMapper.convertToDTO(supplierMaybe.get());
    }

    /**
     * Find the maximum supplier ID in the database.
     * 
     * @return the maximum supplier ID
     */
    public Long findMaxSupplierId() {
        return supplierRepository.findMaxSupplierId();
    }

    /**
     * Find all suppliers. Returns null if no suppliers are found.
     * 
     * @return a list of all SupplierDTOs, or null if no suppliers are found
     */
    public List<SupplierDTO> findAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        supplierRepository.findAll().forEach(suppliers::add);
        if (suppliers.isEmpty()) {
            throw new ResourceNotFoundException("No suppliers found in the database");
        }
        return supplierMapper.convertToDTOList(suppliers);
    }

    /**
     * Find paginated suppliers based on the provided pagination information.
     * 
     * @param pageable the pagination information
     * @return a paginated list of SupplierDTOs
     */
    public Page<SupplierDTO> findPaginatedSuppliers(Pageable pageable) {
        return supplierPageRepository.findAll(pageable).map(supplierMapper::convertToDTO);
    }

    /**
     * Delete a supplier by their ID. Throws an exception if the supplier ID is invalid or does not exist.
     * 
     * @param supplierId the ID of the supplier to delete
     * @throws IllegalArgumentException if the supplier ID is null or empty
     */
    public void deleteSupplierById(Long supplierId) {
        if (supplierId == null || supplierId <= 0) {
            throw new IllegalArgumentException("Supplier ID cannot be null or empty");
        }
        Optional<Supplier> supplierMaybe = supplierRepository.findById(supplierId);
        if (!supplierMaybe.isPresent()) {
            throw new IllegalArgumentException("Supplier with ID " + supplierId + " does not exist");
        }
        Supplier supplier = supplierMaybe.get();
        supplierRepository.delete(supplier);
    }
}
