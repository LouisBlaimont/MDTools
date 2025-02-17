package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RestController;
import java.util.List;

import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;


@RestController
public class InstrumentController {
    private final InstrumentRepository instrumentRepository; 
    public InstrumentController(InstrumentRepository instrumentRepository){
        this.instrumentRepository = instrumentRepository;
    }

    @GetMapping("/instruments")
    public Iterable<Instruments> findallInstruments(){
        return this.instrumentRepository.findAll();
    }

     // retrieve all instruments of a certain category 
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getInstrumentsOfCategory(@PathVariable Integer categoryId) {
        // calls the service corresponding with the correct function 
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsOfCatergory(categoryId);
        // checking if we found something 
        if (instruments == null || instruments.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found for the category " + categoryId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }
}