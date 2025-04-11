package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.AlternativeService;
import lombok.AllArgsConstructor;



@RestController
@RequestMapping("/api/alternatives")
@AllArgsConstructor
public class AlternativesController {
    private final AlternativeService alternativeService;

    /**
     * Returns the alternatives of an instrument available for users.
     * @param instrId The id of the instrument.
     * @return A ResponseEntity (OK) with the alternatives as InstrumentDTO
     * @throws ResourceNotFoundException If the provided instrument doesn't exist.
     */
    @GetMapping("/user/instrument/{instrId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfInstrUser(@PathVariable Long instrId) throws ResourceNotFoundException{
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesUser(instrId);
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }

    /**
     * Returns the alternatives of an instrument available for admins.
     * @param instrId The id of the instrument.
     * @return A ResponseEntity (OK) with the alternatives as InstrumentDTO
     * @throws ResourceNotFoundException If the provided instrument doesn't exist.
     */
    @GetMapping("/admin/instrument/{instrId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfInstrAdmin(@PathVariable Long instrId) throws ResourceNotFoundException {
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesAdmin(instrId);
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }


    /**
     * Returns the alternatives of every instrument in a category available for users.
     * @param categoryId The id of the category.
     * @return A ResponseEntity (OK) with the alternatives as InstrumentDTO
     * @throws ResourceNotFoundException If the provided category doesn't exist.
     */
    @GetMapping("/user/category/{categoryId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfCategoryUser(@PathVariable Long categoryId) throws ResourceNotFoundException{
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesOfCategoryUser(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }


        /**
     * Returns the alternatives of every instrument in a category available for admins
     * @param categoryId The id of the category.
     * @return A ResponseEntity (OK) with the alternatives as InstrumentDTO
     * @throws ResourceNotFoundException If the provided category doesn't exist.
     */
    @GetMapping("/admin/category/{categoryId}")
    public ResponseEntity<List<InstrumentDTO>> getAlternativesOfCategoryAdmin(@PathVariable Long categoryId) throws ResourceNotFoundException {
        List<InstrumentDTO> alternatives = alternativeService.findAlternativesOfCategoryAdmin(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }

    /**
     * Retrieves all available alternative.
     * @return A ResponseEntity (OK) list of object containing the references of the instruments of the alternative.
     */
    @GetMapping("/all")
    public ResponseEntity<List<AlternativeReferenceDTO>> getAllAlternatives() {
        List<AlternativeReferenceDTO> alternatives = alternativeService.findAllAlternativesReferences();
        return ResponseEntity.status(HttpStatus.OK).body(alternatives);
    }


    /**
     * Removes an instrument as alternative from every instrument in a category.
     * Every alternatives concerning this instrument and an instrument of the category is deleted.
     * @param altId The instrument to be removed from the alternatives.
     * @param categoryId The id of the cateogry in which to look for alternative included the instrument identified by altId.
     * @return A boolean set to true if the alternatives were removed.
     * @throws ResourceNotFoundException If the category or the instrument provided don't exist.
     */
    @DeleteMapping("{altId}/category/{categoryId}")
    public ResponseEntity<Boolean> deleteAlternativeFromcategory(@PathVariable Long altId, @PathVariable Long categoryId) throws ResourceNotFoundException{
        Boolean deleted = alternativeService.removeAlternativeFromCategory(categoryId, altId);
        return ResponseEntity.status(HttpStatus.OK).body(deleted);
    }

    /**
     * Creates a new alternative between two instruments.
     * @param instr1 The id of the first instrument.
     * @param instr2 The id of the second instrument.
     * @return A ResponseEntity (OK) with a list of the updated alternatives available in the category of the first instrument. 
     * @throws ResourceNotFoundException If the provided instruments don't exist
     * @throws BadRequestException If the instruments have the same supplier, 
     *                              or if the instruments belong to different groups, 
     *                              or if the alternative already exists.
     */
    @PostMapping
    public ResponseEntity<List<InstrumentDTO>> addAlternative(@RequestParam Long instr1, @RequestParam Long instr2) throws ResourceNotFoundException, BadRequestException {
        List<InstrumentDTO> newAlternative = alternativeService.addAlternative(instr1, instr2);
        return ResponseEntity.status(HttpStatus.OK).body(newAlternative);
    }

    /**
     * Deletes an alternative between two instruments.
     * @param instrId The id of the instrument that is currently modified.
     * @param altId The id of the instrument that must be removed from the alternatives for the other instrument.
     * @return A ResponseEntity (OK) with a list of the updated alternatives available in the category of the instrument that is currently modified.
     * @throws ResourceNotFoundException If the instruments don't exist.
     * @throws BadRequestException If the alternative doesn't exist.
     */
    @DeleteMapping("{altId}/instrument/{instrId}")
    public ResponseEntity<List<InstrumentDTO>> deleteAlternative(@PathVariable Long instrId, @PathVariable Long altId) throws BadRequestException, ResourceNotFoundException{
        List<InstrumentDTO> newAltOfCat = alternativeService.removeAlternative(instrId, altId);
        return ResponseEntity.status(HttpStatus.OK).body(newAltOfCat);
    }
}
