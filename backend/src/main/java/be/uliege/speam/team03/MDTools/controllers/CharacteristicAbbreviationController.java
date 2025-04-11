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

   @GetMapping("/all")
   public Page<CharacteristicAbbreviationDTO> getAllAbbreviations(@RequestParam(defaultValue = "1") int page,
         @RequestParam(defaultValue = "10") int size) {
      Pageable pageable = PageRequest.of(page - 1, size).withSort(Direction.ASC, "value");
      return service.getAllAbbreviations(pageable);
   }

   @PostMapping()
   public CharacteristicAbbreviationDTO updateAbbreviation(@RequestBody CharacteristicAbbreviationDTO dto) {
      return service.updateAbbreviation(dto);
   }

   @DeleteMapping("/{characteristicValue}")
   public void deleteAbbreviation(@PathVariable String characteristicValue) {
      service.deleteAbbreviation(characteristicValue);
   }

   @GetMapping("/non-existing")
   public List<String> getNonExistingAbbreviations() {
      return service.getNonExistingAbbreviations();
   }

   @PostMapping("/add")
   public void addAbbreviation(@RequestBody CharacteristicAbbreviationDTO dto) {
      service.addAbbreviation(dto);
   }
}
