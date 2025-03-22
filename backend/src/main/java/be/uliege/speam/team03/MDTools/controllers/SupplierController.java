package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SupplierService;

/**
 * This controller implements the API endpoints relative to the suppliers. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    private final SupplierService supplierService;
    private final InstrumentService instrumentService;

    public SupplierController(SupplierService supplierService, InstrumentService instrumentService) {
        this.supplierService = supplierService;
        this.instrumentService = instrumentService;
    }

    /**
     * Get all instruments of a specific supplier.
     * 
     * @param supplierId the ID of the supplier
     * @return a list of instruments for the specified supplier, or a 404 status if no instruments are found
     */
    @GetMapping("/{supplierId}/instruments")
    public ResponseEntity<?> getInstrumentsOfSupplier(@PathVariable Integer supplierId) {
        List<InstrumentDTO> products = instrumentService.findInstrumentsBySupplierId(supplierId);
        if (products == null || products.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found for the supplier " + supplierId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    /**
     * Get a supplier by its ID.
     * 
     * @param supplierId the ID of the supplier
     * @return the supplier with the specified ID, or a 404 status if no supplier is found
     */
    @GetMapping("/{supplierId}")
    public ResponseEntity<?> getSupplierById(@PathVariable Integer supplierId) {
        SupplierDTO supplier = supplierService.findSupplierById(supplierId);
        if (supplier == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No supplier found with id: " + supplierId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(supplier);
    }

    /**
     * Get all suppliers.
     * 
     * @return a list of all suppliers, or a 404 status if no suppliers are found
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllSuppliers() {
        List<SupplierDTO> suppliers = supplierService.findAllSuppliers();
        if (suppliers == null || suppliers.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No suppliers found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(suppliers);
    }

    /**
     * Add a new supplier.
     * 
     * @param newSupplier the supplier to add
     * @return the added supplier, or a 409 status if a supplier with the same ID already exists
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addSupplier(@RequestBody SupplierDTO newSupplier) {
        if (newSupplier.getName() == null || newSupplier.getName().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        // Check if a supplier with the same ID already exists so that we don't overwrite it
        if (newSupplier.getId() != null) {
            SupplierDTO existingSupplier = supplierService.findSupplierById(newSupplier.getId());
            if (existingSupplier != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Supplier with this id exists already.\n");
            }
        } else {
            // Assign a new ID to the supplier (the id of the last supplier + 1)
            Integer maxSupplierId = supplierService.findMaxSupplierId();
            newSupplier.setId(maxSupplierId + 1);
        }
        SupplierDTO savedSupplier = supplierService.saveSupplier(newSupplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSupplier);
    }

    /**
     * Update a supplier.
     * 
     * @param id the ID of the supplier to update
     * @param updatedSupplier the supplier data to update
     * @return the updated supplier, or a 404 status if no supplier is found with the specified ID
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateSupplier(@PathVariable Integer id, @RequestBody SupplierDTO updatedSupplier) {
        SupplierDTO existingSupplier = supplierService.findSupplierById(id);
        if (existingSupplier != null) {
            if (updatedSupplier.getName() != null) {
                existingSupplier.setName(updatedSupplier.getName());
            }
            if (updatedSupplier.isSoldByMD() != null) {
                existingSupplier.setSoldByMD(updatedSupplier.isSoldByMD());
            }
            if (updatedSupplier.isClosed() != null) {
                existingSupplier.setClosed(updatedSupplier.isClosed());
            }
            SupplierDTO savedSupplier = supplierService.saveSupplier(existingSupplier);
            return ResponseEntity.status(HttpStatus.OK).body(savedSupplier);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Supplier does not exist with id: " + id);
        }
    }
}
