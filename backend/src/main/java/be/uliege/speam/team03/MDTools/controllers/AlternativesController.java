package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.AlternativeService;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/alternatives")
@AllArgsConstructor
public class AlternativesController {
    private final AlternativeService alternativeService;

    @GetMapping("/user/instrument/{instrId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfInstrUser(@PathVariable Integer instrId) {
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesUser(instrId);
        if (alternatives == null || alternatives.isEmpty()){
            throw new ResourceNotFoundException("No alternatives found for the instrument :" + instrId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }

    @GetMapping("/admin/instrument/{instrId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfInstrAdmin(@PathVariable Integer instrId) {
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesAdmin(instrId);
        if (alternatives == null || alternatives.isEmpty()){
            throw new ResourceNotFoundException("No alternatives found for the instrument :" + instrId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }

    @GetMapping("/user/category/{categoryId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfCategoryUser(@PathVariable Integer categoryId) {
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesOfCategoryUser(categoryId);
        if (alternatives == null || alternatives.isEmpty()){
            throw new ResourceNotFoundException("No alternatives found for the category :" + categoryId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }

    @GetMapping("/admin/category/{categoryId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfCategoryAdmin(@PathVariable Integer categoryId) {
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesOfCategoryAdmin(categoryId);
        if (alternatives == null || alternatives.isEmpty()){
            throw new ResourceNotFoundException("No alternatives found for the category :" + categoryId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }
    @GetMapping("/all")
    public ResponseEntity<List<AlternativeReferenceDTO>> getAllAlternatives() {
        List<AlternativeReferenceDTO> alternatives = alternativeService.findAllAlternativesReferences();
        if (alternatives == null || alternatives.isEmpty()) {
            throw new ResourceNotFoundException("No alternatives found.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }

}
