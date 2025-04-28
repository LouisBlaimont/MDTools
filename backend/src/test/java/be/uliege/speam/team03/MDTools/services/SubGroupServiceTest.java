package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

class SubGroupServiceTest {

   @Mock
   private PictureStorageService pictureStorageService;

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
   @Test
   void testFindAllSubGroups_NoGroupFilter() {
       SubGroup subGroup = new SubGroup("SubGroup1", new Group());
       when(subGroupRepository.findAll()).thenReturn(List.of(subGroup));
   
       List<SubGroupDTO> result = subGroupService.findAllSubGroups();
   
       assertNotNull(result);
       assertEquals(1, result.size());
       assertEquals("SubGroup1", result.get(0).getName());
   }
   @Test
   void testAddCharacteristicToSubGroup_EmptyName_ThrowsException() {
      // Arrange
      String subGroupName = "SomeSubGroup";
      String characteristicName = "";

      // Act & Assert
      BadRequestException ex = assertThrows(BadRequestException.class, () -> {
         subGroupService.addCharacteristicToSubGroup(subGroupName, characteristicName);
      });

      assertTrue(ex.getMessage().contains("Characteristic name is required"));
   }

   @Test
   void testSetSubGroupPicture_SuccessWithExistingPicture() throws Exception {
      SubGroup subGroup = new SubGroup("subgroup1", new Group());
      subGroup.setId(1L);
      subGroup.setPictureId(123L);

      MultipartFile mockFile = mock(MultipartFile.class);
      Picture pic = new Picture();
      pic.setId(456L);

      when(subGroupRepository.findByName("subgroup1")).thenReturn(Optional.of(subGroup));
      when(pictureStorageService.storePicture(eq(mockFile), eq(PictureType.SUBGROUP), eq(1L))).thenReturn(pic);
      when(subGroupRepository.save(any())).thenReturn(subGroup);

      SubGroupDTO result = subGroupService.setSubGroupPicture("subgroup1", mockFile);

      assertNotNull(result);
      verify(pictureStorageService).deletePicture(123L);
      verify(pictureStorageService).storePicture(mockFile, PictureType.SUBGROUP, 1L);
   }
   @Test
   void testUpdateCharacteristicOrder_Success() throws Exception {
      SubGroup subGroup = new SubGroup("sg", new Group());
      subGroup.setId(1L);

      Characteristic char1 = new Characteristic("C1");
      Characteristic char2 = new Characteristic("C2");

      SubGroupCharacteristic link1 = new SubGroupCharacteristic(subGroup, char1, 1);
      SubGroupCharacteristic link2 = new SubGroupCharacteristic(subGroup, char2, 2);
      link1.setId(new SubGroupCharacteristicKey(1L, 1L));
      link2.setId(new SubGroupCharacteristicKey(1L, 2L));

      when(subGroupRepository.findByName("sg")).thenReturn(Optional.of(subGroup));
      when(subGroupCharRepository.findBySubGroup(subGroup)).thenReturn(List.of(link1, link2));
      when(subGroupCharRepository.saveAll(any())).thenReturn(List.of(link1, link2));

      List<Map<String, Object>> newOrder = List.of(
         Map.of("name", "C1", "order_position", 5),
         Map.of("name", "C2", "order_position", 1)
      );

      SubGroupDTO result = subGroupService.updateCharacteristicOrder("sg", newOrder);

      assertNotNull(result);
      assertEquals("sg", result.getName());
      assertEquals(5, link1.getOrderPosition());
      assertEquals(1, link2.getOrderPosition());
   }
   @Test
   void testRemoveCharacteristicFromSubGroup_Success() throws Exception {
       SubGroup subGroup = new SubGroup("sg", new Group());
       subGroup.setId(1L);
       Characteristic charac = new Characteristic("C1");
       charac.setId(10L);
   
       SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(1L, 10L);
       SubGroupCharacteristic link = new SubGroupCharacteristic(subGroup, charac, null);
       link.setId(key);
   
       when(subGroupRepository.findByName("sg")).thenReturn(Optional.of(subGroup));
       when(charRepository.findByName("C1")).thenReturn(Optional.of(charac));
       when(subGroupCharRepository.findById(key)).thenReturn(Optional.of(link));
       when(subGroupRepository.findByName("sg")).thenReturn(Optional.of(subGroup));
   
       SubGroupDTO result = subGroupService.removeCharacteristicFromSubGroup("sg", "C1");
   
       assertNotNull(result);
       verify(subGroupCharRepository).delete(link);
   }
   @Test
   void testFindSubGroupById_Success() throws Exception {
       SubGroup subGroup = new SubGroup("TestSubGroup", new Group());
       subGroup.setId(1L);
       subGroup.setInstrCount(0);
       subGroup.setSubGroupCharacteristics(new ArrayList<>());
       subGroup.setCategories(new ArrayList<>());
   
       when(subGroupRepository.findById(1L)).thenReturn(Optional.of(subGroup));
   
       SubGroupDTO result = subGroupService.findSubGroupById(1L);
   
       assertNotNull(result);
       assertEquals("TestSubGroup", result.getName());
   }
   
   @Test
   void testFindSubGroupById_NotFound() {
       when(subGroupRepository.findById(999L)).thenReturn(Optional.empty());
   
       assertThrows(ResourceNotFoundException.class, () -> {
           subGroupService.findSubGroupById(999L);
       });
   }
   @Test
   void testAddCharacteristicToSubGroup_Success_WithExistingCharacteristic() throws Exception {
       SubGroup subGroup = new SubGroup("SubGroup1", new Group());
       subGroup.setId(1L);
   
       Characteristic charac = new Characteristic("Char1");
       charac.setId(2L);
   
       when(subGroupRepository.findByName("SubGroup1")).thenReturn(Optional.of(subGroup));
       when(charRepository.findByName("Char1")).thenReturn(Optional.of(charac));
       when(subGroupRepository.findByName("SubGroup1")).thenReturn(Optional.of(subGroup)); // for final .get()
   
       SubGroupDTO result = subGroupService.addCharacteristicToSubGroup("SubGroup1", "Char1");
   
       assertNotNull(result);
       assertEquals("SubGroup1", result.getName());
   }
   
   @Test
   void testAddCharacteristicToSubGroup_Success_WithNewCharacteristic() throws Exception {
       SubGroup subGroup = new SubGroup("SubGroup1", new Group());
       subGroup.setId(1L);
   
       when(subGroupRepository.findByName("SubGroup1")).thenReturn(Optional.of(subGroup));
       when(charRepository.findByName("Char2")).thenReturn(Optional.empty());
       when(subGroupRepository.findByName("SubGroup1")).thenReturn(Optional.of(subGroup)); // final .get()
   
       SubGroupDTO result = subGroupService.addCharacteristicToSubGroup("SubGroup1", "Char2");
   
       assertNotNull(result);
       assertEquals("SubGroup1", result.getName());
   }
   
   @Test
   void testAddCharacteristicToSubGroup_EmptySubGroupName_ThrowsException() {
       BadRequestException ex = assertThrows(BadRequestException.class, () -> {
           subGroupService.addCharacteristicToSubGroup("", "Char1");
       });
       assertTrue(ex.getMessage().contains("Subgroup name is required."));
   }
   
   @Test
   void testAddCharacteristicToSubGroup_EmptyCharacteristicName_ThrowsException() {
       BadRequestException ex = assertThrows(BadRequestException.class, () -> {
           subGroupService.addCharacteristicToSubGroup("SubGroup1", "");
       });
       assertTrue(ex.getMessage().contains("Characteristic name is required"));
      }
   
   @Test
   void testAddCharacteristicToSubGroup_SubGroupNotFound() {
       when(subGroupRepository.findByName("Unknown")).thenReturn(Optional.empty());
   
       assertThrows(ResourceNotFoundException.class, () -> {
           subGroupService.addCharacteristicToSubGroup("Unknown", "Char1");
       });
   }
   @Test
   void testAddSubGroup_EmptyGroupName_ThrowsBadRequestException() {
      Map<String, Object> body = new HashMap<>();
      body.put("name", "SubGroupA");
      body.put("characteristics", List.of("Char1"));

      BadRequestException ex = assertThrows(BadRequestException.class, () -> {
         subGroupService.addSubGroup("", body);
      });

      assertTrue(ex.getMessage().contains("Group name is required."));

   }
   @Test
   void testAddSubGroup_WithExistingCharacteristic_ReusesIt() throws Exception {
      String groupName = "GroupA";
      String subGroupName = "SubGroupA";

      Group group = new Group();
      group.setName(groupName);

      Characteristic existingChar = new Characteristic("Char1");
      existingChar.setId(1L);

      when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
      when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());

      when(subGroupRepository.save(any(SubGroup.class))).thenAnswer(inv -> {
         SubGroup saved = inv.getArgument(0);
         saved.setId(1L);
         return saved;
      });

      when(charRepository.findByName("Char1")).thenReturn(Optional.of(existingChar));

      Map<String, Object> body = new HashMap<>();
      body.put("look", subGroupName);
      body.put("characteristics", List.of("Char1"));

      GroupDTO result = subGroupService.addSubGroup(groupName, body);

      assertNotNull(result);
      assertEquals(groupName, result.getName());
      verify(charRepository, atLeast(1)).save(any(Characteristic.class));
   }
   @Test
   void testAddSubGroup_WithNewCharacteristic_SavesIt() throws Exception {
       String groupName = "GroupB";
       String subGroupName = "SubGroupB";
   
       Group group = new Group();
       group.setName(groupName);
   
       when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
       when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());
   
       when(subGroupRepository.save(any(SubGroup.class))).thenAnswer(inv -> {
           SubGroup saved = inv.getArgument(0);
           saved.setId(2L);
           return saved;
       });
   
       when(charRepository.findByName("CharX")).thenReturn(Optional.empty());
   
       Map<String, Object> body = new HashMap<>();
       body.put("look", subGroupName);
       body.put("characteristics", List.of("CharX"));
   
       GroupDTO result = subGroupService.addSubGroup(groupName, body);
   
       assertNotNull(result);
       verify(charRepository, times(4)).save(any(Characteristic.class));
      }
   @Test
   void testAddSubGroup_GroupWithNullSubGroups_InitializesList() throws Exception {
       String groupName = "GroupC";
       String subGroupName = "SubGroupC";
   
       Group group = new Group();
       group.setName(groupName);
       group.setSubGroups(null); // simulate null list
   
       when(groupRepository.findByName(groupName)).thenReturn(Optional.of(group));
       when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());
   
       when(subGroupRepository.save(any(SubGroup.class))).thenAnswer(inv -> {
           SubGroup saved = inv.getArgument(0);
           saved.setId(3L);
           return saved;
       });
   
       when(charRepository.findByName("CharZ")).thenReturn(Optional.empty());
   
       Map<String, Object> body = new HashMap<>();
       body.put("look", subGroupName);
       body.put("characteristics", List.of("CharZ"));
   
       GroupDTO result = subGroupService.addSubGroup(groupName, body);
   
       assertNotNull(result);
       assertEquals(groupName, result.getName());
   }
   @Test
   void testUpdateSubGroup_EmptySubGroupName_ThrowsBadRequest() {
       Map<String, Object> body = Map.of("name", "NewName");
   
       BadRequestException ex = assertThrows(BadRequestException.class, () -> {
           subGroupService.updateSubGroup("", body);
       });
   
       assertTrue(ex.getMessage().contains("Subgroup name is required."));
   }
   @Test
   void testUpdateSubGroup_NotFound_ThrowsResourceNotFound() {
       String subGroupName = "NonExistent";
       Map<String, Object> body = Map.of("look", "NewName");
   
       when(subGroupRepository.findByName(subGroupName)).thenReturn(Optional.empty());
   
       ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
           subGroupService.updateSubGroup(subGroupName, body);
       });
   
       assertTrue(ex.getMessage().contains("Subgroup NonExistent not found."));

   }
   @Test
   void testUpdateSubGroup_NewNameAlreadyExists_ThrowsBadRequest() {
       String oldName = "OldSubGroup";
       String newName = "NewSubGroup";
   
       SubGroup oldSubGroup = new SubGroup(oldName, new Group());
       oldSubGroup.setSubGroupCharacteristics(new ArrayList<>());
       oldSubGroup.setInstrCount(0);
   
       when(subGroupRepository.findByName(oldName)).thenReturn(Optional.of(oldSubGroup));
       when(subGroupRepository.findByName(newName)).thenReturn(Optional.of(new SubGroup())); // simulate collision
   
       Map<String, Object> body = Map.of("name", newName);
   
       BadRequestException ex = assertThrows(BadRequestException.class, () -> {
           subGroupService.updateSubGroup(oldName, body);
       });
   
       assertTrue(ex.getMessage().contains("Subgroup NewSubGroup already exists."));
   }
}
