package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.AlternativeService;
import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;



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


    @DeleteMapping("{altId}/category/{categoryId}")
    public ResponseEntity<?> deleteAlternativeFromcategory(@PathVariable Integer altId, @PathVariable Integer categoryId){
        Boolean deleted = alternativeService.removeAlternativeFromCategory(categoryId, altId);
        if (deleted != true){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete alternative");
        }
        return ResponseEntity.status(HttpStatus.OK).body(deleted);
    }

    @PostMapping
    public ResponseEntity<?> addAlternative(@RequestParam Integer instr1, @RequestParam Integer instr2) {
        try {
            List<InstrumentDTO> newAlternative = alternativeService.addAlternative(instr1, instr2);
            return ResponseEntity.status(HttpStatus.OK).body(newAlternative);
        } catch(BadRequestException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @DeleteMapping("{altId}/instrument/{instrId}")
    public ResponseEntity<?> deleteAlternative(@PathVariable Integer instrId, @PathVariable Integer altId){
        List<InstrumentDTO> newAltOfCat = alternativeService.removeAlternative(instrId, altId);
        if (newAltOfCat == null || newAltOfCat.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot delete alternative");
        }
        return ResponseEntity.status(HttpStatus.OK).body(newAltOfCat);
    }
}
