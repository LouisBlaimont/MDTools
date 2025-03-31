package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import be.uliege.speam.team03.MDTools.DTOs.CharacteristicAbbreviationDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.CharacteristicValueAbbreviation;
import be.uliege.speam.team03.MDTools.repositories.CategoryCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicValueAbbreviationRepository;

@ExtendWith(MockitoExtension.class)
class CharacteristicAbbreviationServiceTest {

    @Mock
    private CharacteristicValueAbbreviationRepository repository;

    @Mock
    private CategoryCharacteristicRepository categoryCharacteristicRepository;

    private CharacteristicAbbreviationService service;

    private CharacteristicValueAbbreviation abbreviation1;
    private CharacteristicValueAbbreviation abbreviation2;
    private List<CharacteristicValueAbbreviation> abbreviationList;

    @BeforeEach
    void setUp() {
        service = new CharacteristicAbbreviationService(repository, categoryCharacteristicRepository);

        abbreviation1 = new CharacteristicValueAbbreviation("Value1", "Abbr1");
        abbreviation2 = new CharacteristicValueAbbreviation("Value2", "Abbr2");
        abbreviationList = Arrays.asList(abbreviation1, abbreviation2);
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


        @Test
    void testGetAllAbbreviations() {
        when(repository.findAll()).thenReturn(abbreviationList);
        
        List<CharacteristicAbbreviationDTO> result = service.getAllAbbreviations();
        
        assertEquals(2, result.size());
        assertEquals("Value1", result.get(0).getValue());
        assertEquals("Abbr1", result.get(0).getAbbreviation());
        assertEquals("Value2", result.get(1).getValue());
        assertEquals("Abbr2", result.get(1).getAbbreviation());
        
        verify(repository, times(1)).findAll();
    }
    
    @Test
    void testGetAllAbbreviationsWithPagination() {
        Page<CharacteristicValueAbbreviation> page = new PageImpl<>(abbreviationList);
        PageRequest pageable = PageRequest.of(0, 10);
        
        when(repository.findAll(pageable)).thenReturn(page);
        
        Page<CharacteristicAbbreviationDTO> result = service.getAllAbbreviations(pageable);
        
        assertEquals(2, result.getContent().size());
        assertEquals("Value1", result.getContent().get(0).getValue());
        assertEquals("Abbr1", result.getContent().get(0).getAbbreviation());
        
        verify(repository, times(1)).findAll(pageable);
    }

    @Test
    void testAddAbbreviationWithStringParameters() {
        when(repository.save(any(CharacteristicValueAbbreviation.class))).thenReturn(abbreviation1);
        
        service.addAbbreviation("Value1", "Abbr1");
        
        verify(repository, times(1)).save(any(CharacteristicValueAbbreviation.class));
    }
    
    @Test
    void testAddAbbreviationWithDTO() {
        CharacteristicAbbreviationDTO dto = new CharacteristicAbbreviationDTO("Value1", "Abbr1");
        when(repository.save(any(CharacteristicValueAbbreviation.class))).thenReturn(abbreviation1);
        
        service.addAbbreviation(dto);
        
        verify(repository, times(1)).save(any(CharacteristicValueAbbreviation.class));
    }

    @Test
    void testUpdateAbbreviationWithDTO() {
        CharacteristicAbbreviationDTO dto = new CharacteristicAbbreviationDTO("Value1", "NewAbbr1");
        
        when(repository.findByValue("Value1")).thenReturn(Optional.of(abbreviation1));
        when(repository.save(any(CharacteristicValueAbbreviation.class))).thenReturn(
            new CharacteristicValueAbbreviation("Value1", "NewAbbr1")
        );
        
        CharacteristicAbbreviationDTO result = service.updateAbbreviation(dto);
        
        assertEquals("Value1", result.getValue());
        assertEquals("NewAbbr1", result.getAbbreviation());
        
        verify(repository, times(1)).findByValue("Value1");
        verify(repository, times(1)).save(any(CharacteristicValueAbbreviation.class));
    }

        @Test
    void testUpdateAbbreviationWithDTOWhenNotExists() {
        CharacteristicAbbreviationDTO dto = new CharacteristicAbbreviationDTO("NonExistingValue", "NewAbbr");
        
        when(repository.findByValue("NonExistingValue")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            service.updateAbbreviation(dto);
        });
        
        verify(repository, times(1)).findByValue("NonExistingValue");
        verify(repository, never()).save(any(CharacteristicValueAbbreviation.class));
    }
}