package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.mock.web.MockMultipartFile;


import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupSummaryDTO;
import be.uliege.speam.team03.MDTools.services.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
        objectMapper = new ObjectMapper();
    }

    private String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    void testGetGroupDetailsByName_Success() throws Exception {
        GroupDTO group = new GroupDTO(1L, "Group1", 2, 2L, null);
        when(groupService.getGroupDetailsByName("Group1")).thenReturn(group);

        mockMvc.perform(get("/api/groups/Group1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Group1"))
                .andExpect(jsonPath("$.instrCount").value(2));

        verify(groupService, times(1)).getGroupDetailsByName("Group1");
    }

    @Test
    void testGetGroupDetailsByName_NotFound() throws Exception {
        when(groupService.getGroupDetailsByName("NonExistentGroup")).thenReturn(null);

        mockMvc.perform(get("/api/groups/NonExistentGroup")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(groupService, times(1)).getGroupDetailsByName("NonExistentGroup");
    }

    @Test
    void testAddGroup_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "NewGroup");
        requestBody.put("instrCount", 2);

        GroupDTO newGroup = new GroupDTO(1L, "NewGroup", 3, 2L, null);
        when(groupService.addGroup(anyMap())).thenReturn(newGroup);

        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NewGroup"))
                .andExpect(jsonPath("$.instrCount").value(3));

        verify(groupService, times(1)).addGroup(anyMap());
    }

    @Test
    void testAddGroup_Conflict() throws Exception {
        Map<String, Object> requestBody = Map.of("name", "ExistingGroup", "instrCount", 2);

        when(groupService.addGroup(anyMap())).thenReturn(null);

        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isBadRequest());

        verify(groupService, times(1)).addGroup(anyMap());
    }

    @Test
    void testDeleteGroup_Success() throws Exception {
        doNothing().when(groupService).deleteGroup("Group1");

        mockMvc.perform(delete("/api/groups/Group1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(groupService, times(1)).deleteGroup("Group1");
    }

    @Test
    void testUpdateGroup_Success() throws Exception {
        Map<String, Object> requestBody = Map.of("name", "UpdatedGroup", "instrCount", 2);

        GroupDTO updatedGroup = new GroupDTO(1L, "UpdatedGroup", 4, 4L, null);
        when(groupService.updateGroup(anyMap(), eq("Group1"))).thenReturn(updatedGroup);

        mockMvc.perform(patch("/api/groups/Group1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedGroup"))
                .andExpect(jsonPath("$.instrCount").value(4));

        verify(groupService, times(1)).updateGroup(anyMap(), eq("Group1"));
    }

    @Test
    void testGetSummaries_Success() throws Exception {
        List<GroupSummaryDTO> summaries = List.of(
                new GroupSummaryDTO("Group1", 5, 1L),
                new GroupSummaryDTO("Group2", 10, 2L)
        );

        when(groupService.getGroupsSummary()).thenReturn(summaries);

        mockMvc.perform(get("/api/groups/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Group1"))
                .andExpect(jsonPath("$[0].instrCount").value(5))
                .andExpect(jsonPath("$[1].name").value("Group2"))
                .andExpect(jsonPath("$[1].instrCount").value(10));

        verify(groupService, times(1)).getGroupsSummary();
    }
    @Test
    void testFindAllGroups_Success() throws Exception {
        List<GroupDTO> groups = List.of(
                new GroupDTO(1L, "Group1", 3, 1L, null),
                new GroupDTO(2L, "Group2", 5, 2L, null)
        );

        when(groupService.findAllGroups()).thenReturn(groups);

        mockMvc.perform(get("/api/groups/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Group1"))
                .andExpect(jsonPath("$[0].instrCount").value(3))
                .andExpect(jsonPath("$[1].name").value("Group2"))
                .andExpect(jsonPath("$[1].instrCount").value(5));

        verify(groupService, times(1)).findAllGroups();
    }
    @Test
    void testSetGroupPicture_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "dummy image content".getBytes());
    
        GroupDTO updatedGroup = new GroupDTO(
                1L,
                "Group1",
                2,
                99L,  
                null
        );
    
        when(groupService.setGroupPicture(eq("Group1"), any(MultipartFile.class)))
                .thenReturn(updatedGroup);
    
        mockMvc.perform(multipart("/api/groups/Group1/picture")
                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Group1"))
                .andExpect(jsonPath("$.pictureId").value(99));
    
        verify(groupService, times(1)).setGroupPicture(eq("Group1"), any(MultipartFile.class));
    }    
}
