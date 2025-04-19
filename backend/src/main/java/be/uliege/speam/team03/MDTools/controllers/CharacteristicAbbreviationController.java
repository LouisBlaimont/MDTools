package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import be.uliege.speam.team03.MDTools.DTOs.CharacteristicAbbreviationDTO;
import be.uliege.speam.team03.MDTools.DTOs.CharacteristicDTO;
import be.uliege.speam.team03.MDTools.services.CharacteristicAbbreviationService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/category/abbreviation")
@AllArgsConstructor
public class CharacteristicAbbreviationController {
   private final CharacteristicAbbreviationService service;

   /**
    * Retrieves the abbreviation for a specified characteristic value.
    * 
    * @param charValue The value of the characteristic for which to find an abbreviation
    * @param charName The name of the characteristic
    * @return A ResponseEntity containing a CharacteristicDTO with the found abbreviation, 
    *         or an empty string if no abbreviation is found. Returns with HTTP status 200 (OK)
    */
   @GetMapping("/of/{charName}/{charValue}")
   public ResponseEntity<CharacteristicDTO> getAbbreviations(@PathVariable String charValue, @PathVariable String charName){
      Optional<String> abbrevMaybe =  service.getAbbreviation(charValue);
      if(abbrevMaybe.isPresent()){
         return ResponseEntity.status(HttpStatus.OK).body(new CharacteristicDTO(charName, charValue, abbrevMaybe.get()));
      }
      else {
         return ResponseEntity.status(HttpStatus.OK).body(new CharacteristicDTO(charName, charValue, ""));
      }
   }

   /**
    * Retrieves a paginated list of all characteristic abbreviations.
    * 
    * @param page The page number to retrieve (1-based indexing, defaulted to 1)
    * @param size The number of items per page (defaulted to 10)
    * @return A Page object containing characteristic abbreviation DTOs sorted in ascending order by value
    */
   @GetMapping("/all")
   public Page<CharacteristicAbbreviationDTO> getAllAbbreviations(@RequestParam(defaultValue = "1") int page,
         @RequestParam(defaultValue = "10") int size) {
      Pageable pageable = PageRequest.of(page - 1, size).withSort(Direction.ASC, "value");
      return service.getAllAbbreviations(pageable);
   }

   /**
    * Updates an existing characteristic abbreviation or creates a new one if it doesn't exist.
    * 
    * @param dto The CharacteristicAbbreviationDTO containing the abbreviation data to update
    * @return The updated CharacteristicAbbreviationDTO
    */
   @PostMapping()
   public CharacteristicAbbreviationDTO updateAbbreviation(@RequestBody CharacteristicAbbreviationDTO dto) {
      return service.updateAbbreviation(dto);
   }

   /**
    * Deletes the abbreviation associated with the specified characteristic value.
    *
    * @param characteristicValue the characteristic value whose abbreviation should be deleted
    */
   @DeleteMapping("/{characteristicValue}")
   public void deleteAbbreviation(@PathVariable String characteristicValue) {
      service.deleteAbbreviation(characteristicValue);
   }

   /**
    * Retrieves a list of abbreviations that do not have a corresponding characteristic in the system.
    * 
    * @return A List of Strings representing non-existing abbreviations
    */
   @GetMapping("/non-existing")
   public List<String> getNonExistingAbbreviations() {
      return service.getNonExistingAbbreviations();
   }

   /**
    * Adds a new characteristic abbreviation to the system.
    * 
    * @param dto The CharacteristicAbbreviationDTO containing the abbreviation details to be added
    * @see CharacteristicAbbreviationDTO
    */
   @PostMapping("/add")
   public void addAbbreviation(@RequestBody CharacteristicAbbreviationDTO dto) {
      service.addAbbreviation(dto);
   }
}
