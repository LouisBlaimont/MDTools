package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SupplierService;

/**
 * This controller implements the API endpoints relative to the suppliers. See
 * the Wiki (>>2. Technical requirements>>API Specifications) for more
 * information.
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
     * Retrieves all instruments associated with a specific supplier by their ID.
     * 
     * @param supplierId the ID of the supplier
     * @return a list of instruments for the specified supplier
     */
    @GetMapping("/{supplierId}/instruments")
    public ResponseEntity<List<InstrumentDTO>> getInstrumentsOfSupplier(@PathVariable Long supplierId) {
        List<InstrumentDTO> products = instrumentService.findInstrumentsBySupplierId(supplierId);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    /**
     * Retrieves a supplier by its ID.
     * 
     * @param supplierId the ID of the supplier
     * @return the supplier details for the specified ID
     */
    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long supplierId) {
        SupplierDTO supplier = supplierService.findSupplierById(supplierId);
        return ResponseEntity.status(HttpStatus.OK).body(supplier);
    }

    /**
     * Retrieves all suppliers.
     * 
     * @return a list of all suppliers
     */
    @GetMapping("/all")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        List<SupplierDTO> suppliers = supplierService.findAllSuppliers();
        return ResponseEntity.status(HttpStatus.OK).body(suppliers);
    }

    /**
     * Retrieves a paginated list of suppliers.
     * 
     * @param page the page number (default is 0)
     * @param size the number of suppliers per page (default is 10)
     * @return a paginated list of suppliers
     */
    @GetMapping
    public ResponseEntity<Page<SupplierDTO>> getPaginatedSuppliers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<SupplierDTO> suppliers = supplierService.findPaginatedSuppliers(PageRequest.of(page, size));
        return ResponseEntity.status(HttpStatus.OK).body(suppliers);
    }

    /**
     * Adds a new supplier to the system.
     * 
     * @param newSupplier the supplier to add
     * @return the added supplier
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SupplierDTO> addSupplier(@NonNull @RequestBody SupplierDTO newSupplier) {
        SupplierDTO savedSupplier = supplierService.saveSupplier(newSupplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSupplier);
    }

    /**
     * Updates an existing supplier's details.
     * 
     * @param id              the ID of the supplier to update
     * @param updatedSupplier the updated supplier data
     * @return the updated supplier details
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @RequestBody SupplierDTO updatedSupplier) {
        SupplierDTO existingSupplier = supplierService.findSupplierById(id);
        if (existingSupplier == null) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }

        if (updatedSupplier.getName() != null) {
            existingSupplier.setName(updatedSupplier.getName());
        }

        existingSupplier.setSoldByMd(updatedSupplier.isSoldByMd());
        existingSupplier.setClosed(updatedSupplier.isClosed());

        SupplierDTO savedSupplier = supplierService.saveSupplier(existingSupplier);
        return ResponseEntity.status(HttpStatus.OK).body(savedSupplier);
    }

    /**
     * Deletes a supplier by its ID.
     * 
     * @param id the ID of the supplier to delete
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplierById(id);
        ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Retrieves a supplier by its name.
     *
     * @param name the name of the supplier
     * @return the supplier details for the specified name
     */
    @GetMapping("name/{name}")
    public ResponseEntity<SupplierDTO> findSupplierByName(@PathVariable String name) {
        SupplierDTO supplier = supplierService.findSupplierByName(name);
        return ResponseEntity.ok(supplier);
    }
}
