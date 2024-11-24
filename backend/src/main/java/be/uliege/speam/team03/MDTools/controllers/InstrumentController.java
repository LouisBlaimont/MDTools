package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.repositories.InstrumentRepository;

import org.springframework.web.bind.annotation.GetMapping;

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
}