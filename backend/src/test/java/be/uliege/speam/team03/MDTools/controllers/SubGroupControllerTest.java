package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.services.SubGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SubGroupControllerTest {

   @Mock
   private SubGroupService subGroupService;

   @InjectMocks
   private SubGroupController subGroupController;

   @BeforeEach
   void setUp() {
      MockitoAnnotations.openMocks(this);
   }

   @Test
   void testGetSubGroupsOfAGroup() {
      // Arrange
      String groupName = "group1";
      List<SubGroupDTO> expectedSubGroups = List.of(
            new SubGroupDTO("subgroup1", null, 4),
            new SubGroupDTO("subgroup2", null, 2));

      when(subGroupService.findAllSubGroups(groupName)).thenReturn(expectedSubGroups);

      // Act
      ResponseEntity<?> response = subGroupController.getSubGroupsOfAGroup(groupName);

      // Assert
      assertNotNull(response, "Response should not be null");
      assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be 200 OK");
      assertInstanceOf(List.class, response.getBody(), "Response body should be a list");

      Object responseBody = response.getBody();
      assertInstanceOf(List.class, responseBody, "Response body should be a list");
      List<?> actualSubGroups = (List<?>) responseBody;
      assertNotNull(actualSubGroups);
      List<SubGroupDTO> subGroupDTOList = actualSubGroups.stream()
            .map(element -> (SubGroupDTO) element)
            .collect(Collectors.toList());
      assertInstanceOf(SubGroupDTO.class, actualSubGroups.get(0), "List elements should be of type SubGroupDTO");
      
      
      assertEquals(expectedSubGroups.size(), actualSubGroups.size(), "Subgroup list sizes should match");
      assertEquals(expectedSubGroups, actualSubGroups, "Subgroup lists should match");


      assertEquals(expectedSubGroups.get(0).getName(), subGroupDTOList.get(0).getName());
      assertEquals(expectedSubGroups.get(0).getNbInstr(), subGroupDTOList.get(0).getNbInstr());
      assertEquals(expectedSubGroups.get(1).getName(), subGroupDTOList.get(1).getName());
      assertEquals(expectedSubGroups.get(1).getNbInstr(), subGroupDTOList.get(1).getNbInstr());


      verify(subGroupService, times(1)).findAllSubGroups(groupName);
   }

   @Test
   void testGetSubGroupsOfAGroup_NotFound() {
      // Arrange
      String groupName = "group1";
      when(subGroupService.findAllSubGroups(groupName)).thenReturn(null);

      // Act
      ResponseEntity<?> response = subGroupController.getSubGroupsOfAGroup(groupName);

      // Assert
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals("Cannot find group name", response.getBody());
      verify(subGroupService, times(1)).findAllSubGroups(groupName);
   }

   @Test
   void testAddSubGroup() {
      // Arrange
      String groupName = "group1";
      Map<String, Object> requestBody = Map.of("name", "subgroup1");

      List<SubGroupDTO> subGroups = List.of(
            new SubGroupDTO("subgroup1", null, 2),
            new SubGroupDTO("subgroup2", null, 4));
      GroupDTO expectedGroup = new GroupDTO(groupName, subGroups, 4);

      when(subGroupService.addSubGroup(groupName, requestBody)).thenReturn(expectedGroup);

      // Act
      ResponseEntity<?> response = subGroupController.addSubGroup(groupName, requestBody);

      // Assert
      assertNotNull(response);
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertInstanceOf(GroupDTO.class, response.getBody());

      GroupDTO responseBody = (GroupDTO) response.getBody();
      assertNotNull(responseBody);
      assertEquals(expectedGroup.getName(), responseBody.getName());
      assertEquals(expectedGroup.getSubGroups().size(), responseBody.getSubGroups().size());
      assertEquals(expectedGroup.getSubGroups(), responseBody.getSubGroups());

      verify(subGroupService, times(1)).addSubGroup(groupName, requestBody);
   }

   @Test
   void testAddSubGroup_NotFound() {
      // Arrange
      String groupName = "group1";
      Map<String, Object> body = new HashMap<>();
      body.put("name", "subgroup1");
      when(subGroupService.addSubGroup(groupName, body)).thenReturn(null);

      // Act
      ResponseEntity<?> response = subGroupController.addSubGroup(groupName, body);

      // Assert
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals("Cannot find group name or subgroup already exists", response.getBody());
      verify(subGroupService, times(1)).addSubGroup(groupName, body);
   }

   @Test
   void testGetSubGroup() {
      // Arrange
      String subgroupName = "subgroup1";
      SubGroupDTO subGroup = new SubGroupDTO(subgroupName, null, 2);
      when(subGroupService.findSubGroup(subgroupName)).thenReturn(subGroup);

      // Act
      ResponseEntity<?> response = subGroupController.getSubGroup(subgroupName);

      // Assert
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(subGroup, response.getBody());
      verify(subGroupService, times(1)).findSubGroup(subgroupName);
   }

   @Test
   void testGetSubGroup_NotFound() {
      // Arrange
      String subgroupName = "subgroup1";
      when(subGroupService.findSubGroup(subgroupName)).thenReturn(null);

      // Act
      ResponseEntity<?> response = subGroupController.getSubGroup(subgroupName);

      // Assert
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals("Cannot find subgroup name", response.getBody());
      verify(subGroupService, times(1)).findSubGroup(subgroupName);
   }

   @Test
   void testUpdateSubGroup() {
      // Arrange
      String subgroupName = "subgroup1";
      Map<String, Object> body = new HashMap<>();
      body.put("name", "subgroup1_updated");
      SubGroupDTO subGroupUpdated = new SubGroupDTO(subgroupName, null, 2);
      when(subGroupService.updateSubGroup(subgroupName, body)).thenReturn(subGroupUpdated);

      // Act
      ResponseEntity<?> response = subGroupController.updateSubGroup(subgroupName, body);

      // Assert
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(subGroupUpdated, response.getBody());
      verify(subGroupService, times(1)).updateSubGroup(subgroupName, body);
   }

   @Test
   void testUpdateSubGroup_NotFound() {
      // Arrange
      String subgroupName = "subgroup1";
      Map<String, Object> body = new HashMap<>();
      body.put("name", "subgroup1_updated");
      when(subGroupService.updateSubGroup(subgroupName, body)).thenReturn(null);

      // Act
      ResponseEntity<?> response = subGroupController.updateSubGroup(subgroupName, body);

      // Assert
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals("Cannot find subgroup or already existing subgroup name", response.getBody());
      verify(subGroupService, times(1)).updateSubGroup(subgroupName, body);
   }

   @Test
   void testDeleteSubGroup() {
      // Arrange
      String subGroupName = "subgroup1";
      when(subGroupService.deleteSubGroup(subGroupName)).thenReturn(subGroupName);

      // Act
      ResponseEntity<String> response = subGroupController.deleteSubGroup(subGroupName);

      // Assert
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(subGroupName, response.getBody());
      verify(subGroupService, times(1)).deleteSubGroup(subGroupName);
   }

   @Test
   void testDeleteSubGroup_NotFound() {
      // Arrange
      String subGroupName = "subgroup1";
      when(subGroupService.deleteSubGroup(subGroupName)).thenReturn(null);

      // Act
      ResponseEntity<String> response = subGroupController.deleteSubGroup(subGroupName);

      // Assert
      assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
      assertEquals("Cannot find subgroup.", response.getBody());
      verify(subGroupService, times(1)).deleteSubGroup(subGroupName);
   }
}