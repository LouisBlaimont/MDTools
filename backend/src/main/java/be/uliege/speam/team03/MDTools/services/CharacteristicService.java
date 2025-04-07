package be.uliege.speam.team03.MDTools.services;

import java.util.List;

import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import lombok.AllArgsConstructor;

/**
 * Service for managing characteristic-related logic.
 */
@Service
@AllArgsConstructor
public class CharacteristicService {
    private final CharacteristicRepository charRepository;

    /**
     * Returns all existing characteristic names.
     *
     * @return list of names
     */
    public List<String> getAllCharacteristicNames() {
        return StreamSupport.stream(charRepository.findAll().spliterator(), false)
            .map(Characteristic::getName)
            .toList();
    }
}
