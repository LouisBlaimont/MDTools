package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.*;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
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
      when(groupRepository.findByName("NonExistentGroup")).thenReturn(Optional.empty());

      List<SubGroupDTO> result = subGroupService.findAllSubGroups("NonExistentGroup");
      assertNull(result);
   }

   @Test
   void testFindSubGroup_WhenExists() {
      SubGroup subGroup = new SubGroup("TestSubGroup", new Group());
      subGroup.setSubGroupCharacteristics(new ArrayList<>());
      subGroup.setInstrCount(0);
      List<Category> categories = new ArrayList<>();
      subGroup.setCategories(categories);

      when(subGroupRepository.findByName("TestSubGroup")).thenReturn(Optional.of(subGroup));

      SubGroupDTO result = subGroupService.findSubGroup("TestSubGroup");

      assertNotNull(result);
      assertEquals("TestSubGroup", result.getName());

      result = subGroupService.findSubGroup(null);
      assertNull(result);
   }

   @Test
   void testFindSubGroup_WhenDoesNotExist() {
      when(subGroupRepository.findByName("NonExistentSubGroup")).thenReturn(Optional.empty());

      SubGroupDTO result = subGroupService.findSubGroup("NonExistentSubGroup");
      assertNull(result);

      result = subGroupService.findSubGroup(null);
      assertNull(result);
   }

   @Test
   void testAddSubGroup() {
      String groupName = "TestGroup";
      Group group = new Group();
      group.setName(groupName);

      when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
      when(subGroupRepository.findByName("NewSubGroup")).thenReturn(Optional.empty());
      when(subGroupRepository.save(any(SubGroup.class)))
            .thenAnswer(invocation -> {
               SubGroup savedSubGroup = invocation.getArgument(0); // Get the argument passed to save()
               savedSubGroup.setId(1L); // Set a unique ID (or use any logic)
               return savedSubGroup; // Return the modified object
            });

      Map<String, Object> body = new HashMap<>();
      body.put("name", "NewSubGroup");
      body.put("characteristics", List.of("Char1", "Char2"));

      GroupDTO result = subGroupService.addSubGroup(groupName, body);

      assertNotNull(result);
      assertEquals(groupName, result.getName());
   }

   @Test
   void testUpdateSubGroup() {
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
   void testDeleteSubGroup() {
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
