package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SupplierService;
@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    private final SupplierService supplierService;
    private final InstrumentService instrumentService;

    public SupplierController(SupplierService supplierService, InstrumentService instrumentService) {
        this.supplierService = supplierService;
        this.instrumentService = instrumentService;
    }

    @GetMapping("/{supplierId}/instruments")
    public ResponseEntity<?> getInstrumentsOfSupplier(@PathVariable Integer supplierId) {
        List<InstrumentDTO> products = instrumentService.findInstrumentsBySupplierId(supplierId);
        if (products == null || products.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found for the supplier " + supplierId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<?> getSupplierById(@PathVariable Integer supplierId) {
        SupplierDTO supplier = supplierService.findSupplierById(supplierId);
        if (supplier == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No supplier found with id: " + supplierId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(supplier);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSuppliers() {
        List<SupplierDTO> suppliers = supplierService.findAllSuppliers();
        if (suppliers == null || suppliers.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No suppliers found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(suppliers);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<SupplierDTO> addSupplier(@RequestBody SupplierDTO newSupplier) {
        if (newSupplier.getName() == null || newSupplier.getName().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be null or empty");
        }
        SupplierDTO savedSupplier = supplierService.saveSupplier(newSupplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSupplier);
    }
}
