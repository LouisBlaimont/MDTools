package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.config.TestSecurityConfig;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.CharacteristicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class CharacteristicControllerTest {

    @Mock
    private CharacteristicService characteristicService;

    @InjectMocks
    private CharacteristicController characteristicController;

    private List<String> mockCharacteristicNames;
    private List<String> mockCharacteristicValues;

    @BeforeEach
    void setUp() {
        mockCharacteristicNames = Arrays.asList("Color", "Material", "Size");
        mockCharacteristicValues = Arrays.asList("Red", "Blue", "Green");
    }

    @Test
    @WithMockUser(roles = { "USER" })
    void getAllCharacteristics_ReturnsAllCharacteristicNames() {
        // Arrange
        when(characteristicService.getAllCharacteristicNames()).thenReturn(mockCharacteristicNames);

        // Act
        ResponseEntity<List<String>> response = characteristicController.getAllCharacteristics();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCharacteristicNames, response.getBody());
        verify(characteristicService, times(1)).getAllCharacteristicNames();
    }

    @Test
    @WithMockUser(roles = { "USER" })
    void getAllCharacteristics_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        when(characteristicService.getAllCharacteristicNames()).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<String>> response = characteristicController.getAllCharacteristics();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(characteristicService, times(1)).getAllCharacteristicNames();
    }

    @Test
    @WithMockUser(roles = { "USER" })
    void getPossibleValuesOfChar_WithValidInputs_ReturnsPossibleValues() throws ResourceNotFoundException {
        // Arrange
        String charName = "Color";
        String subGroupName = "TestSubGroup";
        
        when(characteristicService.getPossibleValuesOfChar(charName, subGroupName))
            .thenReturn(mockCharacteristicValues);

        // Act
        ResponseEntity<List<String>> response = characteristicController.getPossibleValuesOfChar(charName, subGroupName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockCharacteristicValues, response.getBody());
        verify(characteristicService, times(1)).getPossibleValuesOfChar(charName, subGroupName);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    void getPossibleValuesOfChar_WithNonExistentChar_ThrowsResourceNotFoundException() throws ResourceNotFoundException {
        // Arrange
        String charName = "NonExistentChar";
        String subGroupName = "TestSubGroup";
        
        when(characteristicService.getPossibleValuesOfChar(charName, subGroupName))
            .thenThrow(new ResourceNotFoundException("Characteristic not found: " + charName));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            characteristicController.getPossibleValuesOfChar(charName, subGroupName);
        });
        
        assertTrue(exception.getMessage().contains("Characteristic not found"));
        verify(characteristicService, times(1)).getPossibleValuesOfChar(charName, subGroupName);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    void getPossibleValuesOfChar_WithNonExistentSubGroup_ThrowsResourceNotFoundException() throws ResourceNotFoundException {
        // Arrange
        String charName = "Color";
        String subGroupName = "NonExistentSubGroup";
        
        when(characteristicService.getPossibleValuesOfChar(charName, subGroupName))
            .thenThrow(new ResourceNotFoundException("SubGroup not found: " + subGroupName));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            characteristicController.getPossibleValuesOfChar(charName, subGroupName);
        });
        
        assertTrue(exception.getMessage().contains("SubGroup not found"));
        verify(characteristicService, times(1)).getPossibleValuesOfChar(charName, subGroupName);
    }

    @Test
    @WithMockUser(roles = { "USER" })
    void getPossibleValuesOfChar_ReturnsEmptyList_WhenNoValuesExist() throws ResourceNotFoundException {
        // Arrange
        String charName = "EmptyChar";
        String subGroupName = "TestSubGroup";
        
        when(characteristicService.getPossibleValuesOfChar(charName, subGroupName))
            .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<String>> response = characteristicController.getPossibleValuesOfChar(charName, subGroupName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(characteristicService, times(1)).getPossibleValuesOfChar(charName, subGroupName);
    }
}