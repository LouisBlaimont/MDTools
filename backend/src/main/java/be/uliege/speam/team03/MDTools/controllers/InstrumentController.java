package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SupplierService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {
    private final InstrumentService instrumentService; 
    public InstrumentController(InstrumentService instrumentService, SupplierService supplierService) {
        this.instrumentService = instrumentService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> findallInstruments(){
        List<InstrumentDTO> instruments = instrumentService.findAll();
        if (instruments == null || instruments.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrumentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findInstrumentById(@PathVariable Integer id){
        InstrumentDTO instrument = instrumentService.findById(id);
        if (instrument == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instrument found with id: " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrument);
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<InstrumentDTO> updateInstrument(@PathVariable Integer id, @RequestBody InstrumentDTO updatedInstrument) {
    //     InstrumentDTO existingInstrument = instrumentService.findById(id);
    //     if (existingInstrument != null) {
    //         existingInstrument.setReference(updatedInstrument.getReference());
    //         existingInstrument.setSupplierDescription(updatedInstrument.getSupplierDescription());
    //         existingInstrument.setPrice(updatedInstrument.getPrice());
    //         existingInstrument.setObsolete(updatedInstrument.isObsolete());
    //         // ...update other fields as necessary...
    //         InstrumentDTO savedInstrument = instrumentService.save(existingInstrument);
    //         return ResponseEntity.status(HttpStatus.OK).body(savedInstrument);
    //     } else {
    //         return addInstrument(existingInstrument);
    //     }
    // }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<InstrumentDTO> addInstrument(@RequestBody InstrumentDTO newInstrument) {
        InstrumentDTO savedInstrument = instrumentService.save(newInstrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInstrument);
    }
    
    // @DeleteMapping("/delete/{id}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // public void deleteInstrument(@PathVariable Integer id) {
    //     instrumentService.delete(id);
    // }
}