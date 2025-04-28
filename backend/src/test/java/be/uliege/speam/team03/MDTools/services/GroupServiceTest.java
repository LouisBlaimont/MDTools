package be.uliege.speam.team03.MDTools.services;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupSummaryDTO;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GroupServiceTest {

   @Mock
   private GroupRepository groupRepository;

   @Mock
   private CharacteristicRepository charRepository;

   @Mock
   private SubGroupRepository subGroupRepository;

   @Mock
   private SubGroupCharacteristicRepository subGroupCharRepository;

   @Mock
   private SubGroupService subGroupService;

   @InjectMocks
   private GroupService groupService;

   @Mock
   private PictureStorageService pictureStorageService;

   @BeforeEach
   void setUp() {
      MockitoAnnotations.openMocks(this);
   }

   @Test
   void testFindAllGroups() {
      // Arrange
      Group group = new Group("Group1");
      SubGroup subGroup = new SubGroup("SubGroup1", group);
      Characteristic characteristic = new Characteristic("Char1");
      SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(subGroup, characteristic, 1);
      subGroup.setSubGroupCharacteristics(Collections.singletonList(subGroupChar));
      group.setSubGroups(Collections.singletonList(subGroup));

      when(groupRepository.findAll()).thenReturn(Collections.singletonList(group));

      // Act
      List<GroupDTO> result = groupService.findAllGroups();

      // Assert
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals("Group1", result.get(0).getName());
      assertEquals(1, result.get(0).getSubGroups().size());
      assertEquals("SubGroup1", result.get(0).getSubGroups().get(0).getName());
      // assertEquals("Char1", result.get(0).getSubGroups().get(0).getCharacteristics().get(0));
   }

   @Test
   void testGetGroupDetailsByName_GroupFound() {
      // Arrange
      Group group = new Group("Group1");
      SubGroup subGroup = new SubGroup("SubGroup1", group);
      Characteristic characteristic = new Characteristic("Char1");
      SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(subGroup, characteristic, 1);
      subGroup.setSubGroupCharacteristics(Collections.singletonList(subGroupChar));
      group.setSubGroups(Collections.singletonList(subGroup));

      when(groupRepository.findByName("Group1")).thenReturn(Optional.of(group));

      // Act
      GroupDTO result = groupService.getGroupDetailsByName("Group1");

      // Assert
      assertNotNull(result);
      assertEquals("Group1", result.getName());
      assertEquals(1, result.getSubGroups().size());
      assertEquals("SubGroup1", result.getSubGroups().get(0).getName());
      // assertEquals("Char1", result.getSubGroups().get(0).getCharacteristics().get(0));
   }

   @Test
   void testGetGroupDetailsByName_GroupNotFound() {
      // Arrange
      when(groupRepository.findByName("NonExistentGroup")).thenReturn(Optional.empty());

      // Act
      GroupDTO result = groupService.getGroupDetailsByName("NonExistentGroup");

      // Assert
      assertNull(result);
   }

   @Test
   void testAddGroup_Success() {
      // Arrange
      Map<String, Object> body = new HashMap<>();
      body.put("groupName", "NewGroup");
      body.put("subGroupName", "NewSubGroup");
      body.put("characteristics", Collections.singletonList("NewChar"));

      when(groupRepository.findByName("NewGroup")).thenReturn(Optional.empty());
      when(subGroupRepository.findByName("NewSubGroup")).thenReturn(Optional.empty());
      when(charRepository.findByName("NewChar")).thenReturn(Optional.empty());
      when(groupRepository.save(any(Group.class)))
            .thenAnswer(invocation -> {
               Group group = invocation.getArgument(0); // Get the argument passed to save()
               group.setId(1L); // Set a unique ID (or use any logic)
               return group; // Return the modified object
            });
      when(subGroupRepository.save(any(SubGroup.class)))
            .thenAnswer(invocation -> {
               SubGroup subGroup = invocation.getArgument(0); // Get the argument passed to save()
               subGroup.setId(1L); // Set a unique ID (or use any logic)
               return subGroup; // Return the modified object
            });

      // Act
      GroupDTO result = groupService.addGroup(body);

      // Assert
      assertNotNull(result);
      assertEquals("NewGroup", result.getName());
      assertEquals(1, result.getSubGroups().size());
      assertEquals("NewSubGroup", result.getSubGroups().get(0).getName());
      // assertEquals("NewChar", result.getSubGroups().get(0).getCharacteristics().get(0));
   }

   @Test
   void testAddGroup_GroupAlreadyExists() {
      // Arrange
      Map<String, Object> body = new HashMap<>();
      body.put("groupName", "ExistingGroup");
      body.put("subGroupName", "NewSubGroup");
      body.put("characteristics", Collections.singletonList("NewChar"));

      when(groupRepository.findByName("ExistingGroup")).thenReturn(Optional.of(new Group("ExistingGroup")));

      // Act
      GroupDTO result = groupService.addGroup(body);

      // Assert
      assertNull(result);
   }

   @Test
   void testDeleteGroup_ThrowsBadRequest_WhenSubGroupsExist() {
      // Arrange
      Group group = new Group("Group1");
      SubGroup subGroup = new SubGroup("SubGroup1", group);
      group.setSubGroups(Collections.singletonList(subGroup));

      when(groupRepository.findByName("Group1")).thenReturn(Optional.of(group));

      // Act & Assert
      BadRequestException ex = assertThrows(BadRequestException.class, () -> {
         groupService.deleteGroup("Group1");
      });

      assertTrue(ex.getMessage().contains("Cannot delete group with existing subgroups."));
   }

   @Test
   void testDeleteGroup_Success_WhenNoSubGroups() {
      // Arrange
      Group group = new Group("Group1");
      group.setSubGroups(Collections.emptyList());

      when(groupRepository.findByName("Group1")).thenReturn(Optional.of(group));

      // Act
      groupService.deleteGroup("Group1");

      // Assert
      verify(groupRepository, times(1)).delete(group);
   }

   @Test
   void testDeleteGroup_GroupNotFound() {
      // Arrange
      when(groupRepository.findByName("NonExistentGroup")).thenReturn(Optional.empty());

      // Act
      assertThrows(ResourceNotFoundException.class, () -> {
          groupService.deleteGroup("NonExistentGroup");
      });

   }

   @Test
   void testUpdateGroup_Success() {
      // Arrange
      Group group = new Group("OldGroupName");
      // Add subgroup
      SubGroup subGroup = new SubGroup("SubGroup1", group);
      Characteristic characteristic = new Characteristic("Char1");
      SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(subGroup, characteristic, 1);
      subGroup.setSubGroupCharacteristics(Collections.singletonList(subGroupChar));
      group.setSubGroups(Collections.singletonList(subGroup));

      when(groupRepository.findByName("OldGroupName")).thenReturn(Optional.of(group));
      when(groupRepository.findByName("NewGroupName")).thenReturn(Optional.empty());
      when(groupRepository.save(any(Group.class)))
            .thenAnswer(invocation -> {
               Group updatedGroup = invocation.getArgument(0); // Get the argument passed to save()
               updatedGroup.setId(1L); // Set a unique ID (or use any logic)
               return updatedGroup; // Return the modified object
            });

      Map<String, Object> body = new HashMap<>();
      body.put("name", "NewGroupName");

      // Act
      GroupDTO result = groupService.updateGroup(body, "OldGroupName");

      // Assert
      assertNotNull(result);
      assertEquals("NewGroupName", result.getName());
      verify(groupRepository, times(1)).save(group);
   }

   @Test
   void testUpdateGroup_GroupNotFound() {
      // Arrange
      when(groupRepository.findByName("NonExistentGroup")).thenReturn(Optional.empty());

      Map<String, Object> body = new HashMap<>();
      body.put("name", "NewGroupName");

      assertThrows(ResourceNotFoundException.class, () -> groupService.updateGroup(body, "NonExistentGroup"));
   }

   @Test
   void testUpdateGroup_NewNameAlreadyExists() {
      // Arrange
      Group group = new Group("OldGroupName");
      when(groupRepository.findByName("OldGroupName")).thenReturn(Optional.of(group));
      when(groupRepository.findByName("ExistingGroupName")).thenReturn(Optional.of(new Group("ExistingGroupName")));

      Map<String, Object> body = new HashMap<>();
      body.put("name", "ExistingGroupName");

      assertThrows(BadRequestException.class, () -> groupService.updateGroup(body, "OldGroupName"));

   }

   @Test
   void testGetGroupsSummary() {
      // Arrange
      Group group1 = new Group("Group1");
      group1.setInstrCount(10);
      group1.setId(1L);
      Group group2 = new Group("Group2");
      group2.setInstrCount(20);
      group2.setId(2L);

      when(groupRepository.findAll()).thenReturn(Arrays.asList(group1, group2));
      when(groupRepository.nbInstrOfGroup(1L)).thenReturn(10);
      when(groupRepository.nbInstrOfGroup(2L)).thenReturn(20);

      // Act
      List<GroupSummaryDTO> result = groupService.getGroupsSummary();

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("Group1", result.get(0).getName());
      assertEquals(10, result.get(0).getInstrCount());
      assertEquals("Group2", result.get(1).getName());
      assertEquals(20, result.get(1).getInstrCount());
   }

   @Test
   void testSetGroupPicture_SuccessWithOldPicture() throws Exception {
      // Arrange
      Group group = new Group("Group1");
      group.setId(1L);
      group.setPictureId(42L); // old picture
      group.setSubGroups(new ArrayList<>()); // ⬅️ essentiel pour éviter le NPE

      MultipartFile file = mock(MultipartFile.class);
      Picture storedPicture = new Picture();
      storedPicture.setId(99L); // new picture

      when(groupRepository.findByName("Group1")).thenReturn(Optional.of(group));
      doNothing().when(pictureStorageService).deletePicture(42L);
      when(pictureStorageService.storePicture(eq(file), eq(PictureType.GROUP), eq(1L))).thenReturn(storedPicture);
      when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

      // Act
      GroupDTO result = groupService.setGroupPicture("Group1", file);

      // Assert
      assertNotNull(result);
      assertEquals(99L, result.getPictureId());
      verify(pictureStorageService).deletePicture(42L);
      verify(pictureStorageService).storePicture(file, PictureType.GROUP, 1L);
      verify(groupRepository).save(group);
   }


   @Test
   void testSetGroupPicture_GroupNotFound() {
      // Arrange
      MultipartFile file = mock(MultipartFile.class);
      when(groupRepository.findByName("UnknownGroup")).thenReturn(Optional.empty());

      // Act + Assert
      assertThrows(ResourceNotFoundException.class, () -> groupService.setGroupPicture("UnknownGroup", file));
      verify(pictureStorageService, never()).storePicture(
         any(MultipartFile.class),
         any(PictureType.class),
         any(Long.class)
      );
   }
   @Test
   void testFindGroupById_Success() {
      // Arrange
      Group group = new Group("Group1");
      group.setId(1L);
      group.setInstrCount(0); // initial, sera mis à jour

      SubGroup subGroup = new SubGroup("SubGroup1", group);
      subGroup.setId(10L);

      group.setSubGroups(List.of(subGroup));

      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(groupRepository.nbInstrOfGroup(1L)).thenReturn(5);
      when(subGroupService.nbInstrOfSubGroup(10L)).thenReturn(3);

      // Act
      GroupDTO result = groupService.findGroupById(1L);

      // Assert
      assertNotNull(result);
      assertEquals("Group1", result.getName());
      assertEquals(5, result.getInstrCount());
      assertEquals(1, result.getSubGroups().size());
      assertEquals("SubGroup1", result.getSubGroups().get(0).getName());
      assertEquals(3, result.getSubGroups().get(0).getInstrCount());
   }

   @Test
   void testFindGroupById_NotFound() {
       when(groupRepository.findById(99L)).thenReturn(Optional.empty());
   
       GroupDTO result = groupService.findGroupById(99L);
   
       assertNull(result);
   }
}