package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicValueAbbreviationRepository;

@ExtendWith(MockitoExtension.class)
class CharacteristicAbbreviationServiceTest {

    @Mock
    private CharacteristicValueAbbreviationRepository repository;

    private CharacteristicAbbreviationService service;

    @BeforeEach
    void setUp() {
        service = new CharacteristicAbbreviationService(repository);
    }

    @Test
    void getAbbreviation_WhenAbbreviationExists_ShouldReturnAbbreviation() {
        // Arrange
        String characteristicValue = "Temperature";
        String expectedAbbreviation = "TEMP";
        CharacteristicValueAbbreviation abbreviation = new CharacteristicValueAbbreviation(characteristicValue, expectedAbbreviation);
        when(repository.findByValue(characteristicValue)).thenReturn(Optional.of(abbreviation));

        // Act
        Optional<String> result = service.getAbbreviation(characteristicValue);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedAbbreviation, result.get());
        verify(repository).findByValue(characteristicValue);
    }

    @Test
    void getAbbreviation_WhenAbbreviationDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        String characteristicValue = "NonExistentValue";
        when(repository.findByValue(characteristicValue)).thenReturn(Optional.empty());

        // Act
        Optional<String> result = service.getAbbreviation(characteristicValue);

        // Assert
        assertFalse(result.isPresent());
        verify(repository).findByValue(characteristicValue);
    }

    @Test
    void addAbbreviation_ShouldSaveAbbreviation() {
        // Arrange
        String characteristicValue = "Pressure";
        String abbreviation = "PRES";

        // Act
        service.addAbbreviation(characteristicValue, abbreviation);

        // Assert
        verify(repository).save(any(CharacteristicValueAbbreviation.class));
    }

    @Test
    void updateAbbreviation_WhenAbbreviationExists_ShouldUpdateAbbreviation() {
        // Arrange
        String characteristicValue = "Temperature";
        String newAbbreviation = "TMP";
        CharacteristicValueAbbreviation existingAbbreviation = new CharacteristicValueAbbreviation(characteristicValue, "TEMP");
        when(repository.findByValue(characteristicValue)).thenReturn(Optional.of(existingAbbreviation));

        // Act
        service.updateAbbreviation(characteristicValue, newAbbreviation);

        // Assert
        assertEquals(newAbbreviation, existingAbbreviation.getAbbreviation());
        verify(repository).findByValue(characteristicValue);
        verify(repository).save(existingAbbreviation);
    }

    @Test
    void updateAbbreviation_WhenAbbreviationDoesNotExist_ShouldDoNothing() {
        // Arrange
        String characteristicValue = "NonExistentValue";
        String newAbbreviation = "NEW";
        when(repository.findByValue(characteristicValue)).thenReturn(Optional.empty());

        // Act
        service.updateAbbreviation(characteristicValue, newAbbreviation);

        // Assert
        verify(repository).findByValue(characteristicValue);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteAbbreviation_ShouldCallRepositoryDelete() {
        // Arrange
        String characteristicValue = "Temperature";

        // Act
        service.deleteAbbreviation(characteristicValue);

        // Assert
        verify(repository).deleteByValue(characteristicValue);
    }
}