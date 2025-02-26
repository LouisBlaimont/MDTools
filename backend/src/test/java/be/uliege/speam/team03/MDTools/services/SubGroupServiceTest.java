package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.*;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SubGroupServiceTest {

   @Mock
   private GroupRepository groupRepository;

   @Mock
   private CharacteristicRepository charRepository;

   @Mock
   private SubGroupRepository subGroupRepository;

   @Mock
   private SubGroupCharacteristicRepository subGroupCharRepository;

   @InjectMocks
   private SubGroupService subGroupService;

   @BeforeEach
   void setUp() {
      MockitoAnnotations.openMocks(this);
   }

   @Test
   void testFindAllSubGroups_WhenGroupExists() {
      String groupName = "TestGroup";
      Group group = new Group();
      group.setName(groupName);

      SubGroup subGroup = new SubGroup("SubGroup1", group);
      subGroup.setSubGroupCharacteristics(new ArrayList<>());
      subGroup.setInstrCount(0);
      subGroup.setCategories(new ArrayList<>());
      group.setSubGroups(List.of(subGroup));

      when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));

      List<SubGroupDTO> result = subGroupService.findAllSubGroups(groupName);

      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("SubGroup1", result.get(0).getName());
   }

   @Test
   void testFindAllSubGroups_WhenGroupDoesNotExist() {
      // Simulate that the Group does not exist
      when(groupRepository.findByName("NonExistentGroup")).thenReturn(Optional.empty());

      // Assert that ResourceNotFoundException is thrown when the Group is not found
      assertThrows(ResourceNotFoundException.class, () -> {
         subGroupService.findAllSubGroups("NonExistentGroup");
      });
   }

   @Test
   void testFindSubGroup_WhenExists() throws Exception {
      SubGroup subGroup = new SubGroup("TestSubGroup", new Group());
      subGroup.setSubGroupCharacteristics(new ArrayList<>());
      subGroup.setInstrCount(0);
      List<Category> categories = new ArrayList<>();
      subGroup.setCategories(categories);

      when(subGroupRepository.findByName("TestSubGroup")).thenReturn(Optional.of(subGroup));

      SubGroupDTO result = subGroupService.findSubGroup("TestSubGroup");

      assertNotNull(result);
      assertEquals("TestSubGroup", result.getName());

      // Modify this part to assert a BadRequestException when null is passed
      assertThrows(BadRequestException.class, () -> {
         subGroupService.findSubGroup(null);
      });
   }

   @Test
   void testFindSubGroup_WhenDoesNotExist() throws Exception {
      // When the SubGroup doesn't exist
      when(subGroupRepository.findByName("NonExistentSubGroup")).thenReturn(Optional.empty());

      // Assert that ResourceNotFoundException is thrown when trying to find a
      // non-existent SubGroup
      assertThrows(ResourceNotFoundException.class, () -> {
         subGroupService.findSubGroup("NonExistentSubGroup");
      });

      // Assert that BadRequestException is thrown when null is passed
      assertThrows(BadRequestException.class, () -> {
         subGroupService.findSubGroup(null);
      });
   }

   @Test
   void testAddSubGroup() throws Exception {
      String groupName = "TestGroup";
      Group group = new Group();
      group.setName(groupName);

      // When the Group is not found
      when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
      // When the SubGroup does not exist
      when(subGroupRepository.findByName("NewSubGroup")).thenReturn(Optional.empty());

      // Mock saving the SubGroup and assigning it an ID
      when(subGroupRepository.save(any(SubGroup.class)))
            .thenAnswer(invocation -> {
               SubGroup savedSubGroup = invocation.getArgument(0); // Get the argument passed to save()
               savedSubGroup.setId(1L); // Set a unique ID (or use any logic)
               return savedSubGroup; // Return the modified object
            });

      Map<String, Object> body = new HashMap<>();
      body.put("name", "NewSubGroup");
      body.put("characteristics", List.of("Char1", "Char2"));

      // Test valid addition of SubGroup
      GroupDTO result = subGroupService.addSubGroup(groupName, body);

      assertNotNull(result);
      assertEquals(groupName, result.getName());

      // Now add a case where a ResourceNotFoundException is thrown
      when(groupRepository.findByName(groupName)).thenReturn(Optional.empty()); // Simulate group not found

      // Assert that ResourceNotFoundException is thrown when the Group is not found
      assertThrows(ResourceNotFoundException.class, () -> {
         subGroupService.addSubGroup(groupName, body);
      });

      // Simulate the SubGroup already exists
      when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
      when(subGroupRepository.findByName("NewSubGroup")).thenReturn(Optional.of(new SubGroup()));

      // Assert that BadRequestException is thrown when the SubGroup already
      // exists
      assertThrows(BadRequestException.class, () -> {
         subGroupService.addSubGroup(groupName, body);
      });
   }

   @Test
   void testUpdateSubGroup() throws Exception {
      String subGroupName = "OldSubGroup";
      SubGroup subGroup = new SubGroup(subGroupName, new Group());
      subGroup.setSubGroupCharacteristics(new ArrayList<>());
      subGroup.setInstrCount(0);
      when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
      when(subGroupRepository.findByName("UpdatedSubGroup")).thenReturn(Optional.empty());
      when(subGroupRepository.save(any(SubGroup.class)))
            .thenAnswer(invocation -> {
               SubGroup savedSubGroup = invocation.getArgument(0); // Get the argument passed to save()
               savedSubGroup.setId(1L); // Set a unique ID (or use any logic)
               return savedSubGroup; // Return the modified object
            });

      Map<String, Object> body = new HashMap<>();
      body.put("name", "UpdatedSubGroup");

      SubGroupDTO result = subGroupService.updateSubGroup(subGroupName, body);

      assertNotNull(result);
      assertEquals("UpdatedSubGroup", result.getName());

      // Verify that the repository's save method was called with the updated subgroup
      verify(subGroupRepository).save(subGroup);
      assertEquals("UpdatedSubGroup", subGroup.getName());
   }

   @Test
   void testDeleteSubGroup() throws Exception {
      String subGroupName = "TestSubGroup";
      Group group = new Group();
      SubGroup subGroup = new SubGroup(subGroupName, group);
      LinkedList<SubGroup> subGroups = new LinkedList<SubGroup>();
      group.setSubGroups(subGroups);

      when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.of(subGroup));
      when(groupRepository.findByName(anyString())).thenReturn(Optional.of(group));

      String result = subGroupService.deleteSubGroup(subGroupName);

      assertEquals("Successfully deleted group.", result);
   }
}
