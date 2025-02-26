package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.SubGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SubGroupControllerTest {

   @Mock
   private SubGroupService subGroupService;

   @InjectMocks
   private SubGroupController subGroupController;

   private MockMvc mockMvc;

   @BeforeEach
   void setUp() {
      MockitoAnnotations.openMocks(this);
      mockMvc = MockMvcBuilders.standaloneSetup(subGroupController).build();
   }

   @Test
   void testGetSubGroupsOfAGroup() {
      // Arrange
      String groupName = "group1";
      List<SubGroupDTO> expectedSubGroups = List.of(
            new SubGroupDTO(1L, "subgroup1", 4L, null, 4, null, 20L),
            new SubGroupDTO(2L, "subgroup2", 5L, null, 2, null, 30L));

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
            .toList();
      assertInstanceOf(SubGroupDTO.class, actualSubGroups.get(0), "List elements should be of type SubGroupDTO");

      assertEquals(expectedSubGroups.size(), actualSubGroups.size(), "Subgroup list sizes should match");
      assertEquals(expectedSubGroups, actualSubGroups, "Subgroup lists should match");

      assertEquals(expectedSubGroups.get(0).getName(), subGroupDTOList.get(0).getName());
      assertEquals(expectedSubGroups.get(0).getInstrCount(), subGroupDTOList.get(0).getInstrCount());
      assertEquals(expectedSubGroups.get(1).getName(), subGroupDTOList.get(1).getName());
      assertEquals(expectedSubGroups.get(1).getInstrCount(), subGroupDTOList.get(1).getInstrCount());

      verify(subGroupService, times(1)).findAllSubGroups(groupName);
   }

   @Test
   void testGetSubGroupsOfAGroup_NotFound() throws Exception {
      // Arrange
      String groupName = "group1";

      when(subGroupService.findAllSubGroups(groupName))
            .thenThrow(new ResourceNotFoundException(groupName + " not found."));

      // Act & Assert
      mockMvc.perform(get("/api/subgroups/group/" + groupName))
            .andExpect(status().isNotFound());
   }

   @Test
   void testAddSubGroup() throws Exception {
      // Arrange
      String groupName = "group1";
      Map<String, Object> requestBody = Map.of("name", "subgroup1");

      List<SubGroupDTO> subGroups = List.of(
            new SubGroupDTO(1L, "subgroup1", 3L, null, 2, null, 4L),
            new SubGroupDTO(2L, "subgroup2", 4L, null, 2, null, 5L));
      GroupDTO expectedGroup = new GroupDTO(1L, groupName, 2, 10L, subGroups);

      when(subGroupService.addSubGroup(eq(groupName), any())).thenReturn(expectedGroup);

      // Act & Assert
      mockMvc.perform(post("/api/subgroups/group/{groupName}", groupName)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(requestBody)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value(expectedGroup.getName()))
            .andExpect(jsonPath("$.subGroups", hasSize(expectedGroup.getSubGroups().size())));

      verify(subGroupService, times(1)).addSubGroup(eq(groupName), any());
   }

   @Test
   void testAddSubGroup_NotFound() throws Exception {
      // Arrange
      String groupName = "group1";
      Map<String, Object> body = new HashMap<>();
      body.put("name", "subgroup1");

      when(subGroupService.addSubGroup(groupName, body))
            .thenThrow(new ResourceNotFoundException(groupName + " not found."));

      // Act & Assert
      mockMvc.perform(MockMvcRequestBuilders.post("/api/subgroups/group/{groupName}", groupName)
            .contentType("application/json")
            .content("{\"name\":\"subgroup1\"}"))
            .andExpect(status().isNotFound());
   }

   @Test
   void testGetSubGroup() throws Exception {
      // Arrange
      String subgroupName = "subgroup1";
      SubGroupDTO subGroup = new SubGroupDTO(1L, subgroupName, 2L, null, 2, null, 2L);
      when(subGroupService.findSubGroup(subgroupName)).thenReturn(subGroup);

      // Act & Assert
      mockMvc.perform(MockMvcRequestBuilders.get("/api/subgroups/{subgroupName}", subgroupName))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.name").value(subGroup.getName()))
            .andExpect(jsonPath("$.instrCount").value(subGroup.getInstrCount()));

      verify(subGroupService, times(1)).findSubGroup(subgroupName);
   }

   @Test
   void testGetSubGroup_NotFound() throws Exception {
      // Arrange
      String subgroupName = "subgroup1";

      when(subGroupService.findSubGroup(subgroupName))
            .thenThrow(new ResourceNotFoundException("Cannot find subgroup name"));

      // Act & Assert
      mockMvc.perform(MockMvcRequestBuilders.get("/api/subgroups/{subgroupName}", subgroupName))
            .andExpect(status().isNotFound());

      verify(subGroupService, times(1)).findSubGroup(subgroupName);
   }

   @Test
   void testUpdateSubGroup() throws Exception {
      // Arrange
      String subgroupName = "subgroup1";
      Map<String, Object> body = new HashMap<>();
      body.put("name", "subgroup1_updated");
      SubGroupDTO subGroupUpdated = new SubGroupDTO(1L, subgroupName, 2L, null, 2, null, 2L);
      when(subGroupService.updateSubGroup(subgroupName, body)).thenReturn(subGroupUpdated);

      // Act
      ResponseEntity<?> response = subGroupController.updateSubGroup(subgroupName, body);

      // Assert
      assertEquals(HttpStatus.OK, response.getStatusCode());
      assertEquals(subGroupUpdated, response.getBody());
      verify(subGroupService, times(1)).updateSubGroup(subgroupName, body);
   }

   @Test
   void testUpdateSubGroup_NotFound() throws Exception {
      // Arrange
      String subgroupName = "subgroup1";
      Map<String, Object> body = new HashMap<>();
      body.put("name", "subgroup1_updated");

      when(subGroupService.updateSubGroup(subgroupName, body))
            .thenThrow(new ResourceNotFoundException("Cannot find subgroup or already existing subgroup name"));

      // Act & Assert
      ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
         subGroupController.updateSubGroup(subgroupName, body);
      });

      verify(subGroupService, times(1)).updateSubGroup(subgroupName, body);
   }

   @Test
   void testDeleteSubGroup() throws Exception {
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
   void testDeleteSubGroup_NotFound() throws Exception {
      // Arrange
      String subGroupName = "subgroup1";

      when(subGroupService.deleteSubGroup(subGroupName))
            .thenThrow(new ResourceNotFoundException("Cannot find subgroup."));

      // Act & Assert
      mockMvc.perform(delete("/subgroups/" + subGroupName).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
   }

}