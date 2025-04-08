package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.CharacteristicService;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * This controller provides endpoints related to characteristics.
 */
@RestController
@RequestMapping("/api/characteristics")
@AllArgsConstructor
public class CharacteristicController {

    private final CharacteristicService characteristicService;

    /**
     * Retrieves the list of all existing characteristic names.
     *
     * @return a list of characteristic names (String only)
     */
    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllCharacteristics() {
        List<String> names = characteristicService.getAllCharacteristicNames();
        return ResponseEntity.status(HttpStatus.OK).body(names);
    }

    @GetMapping("/{charName}/values-in/{subGroupName}")
    public ResponseEntity<List<String>> getPossibleValuesOfChar(@PathVariable String charName, @PathVariable String subGroupName) throws ResourceNotFoundException{
        List<String> values = characteristicService.getPossibleValuesOfChar(charName, subGroupName);
        return ResponseEntity.status(HttpStatus.OK).body(values);
    }
}
