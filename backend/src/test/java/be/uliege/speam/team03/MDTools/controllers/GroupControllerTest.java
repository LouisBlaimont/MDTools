package be.uliege.speam.team03.MDTools.controllers;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    void testFindAllGroups_Success() throws Exception {
        List<GroupDTO> groups = List.of(
                new GroupDTO("Group1", null, 2),
                new GroupDTO("Group2", null, 3)
        );

        when(groupService.findAllGroups()).thenReturn(groups);

        mockMvc.perform(get("/api/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Group1"))
                .andExpect(jsonPath("$[1].name").value("Group2"))
                .andExpect(jsonPath("$[0].nbInstr").value(2))
                .andExpect(jsonPath("$[1].nbInstr").value(3));

        verify(groupService, times(1)).findAllGroups();
    }

    @Test
    void testFindAllGroups_InternalServerError() throws Exception {
        when(groupService.findAllGroups()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/groups")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error while retrieving groups."));

        verify(groupService, times(1)).findAllGroups();
    }

    @Test
    void testGetGroupDetailsByName_Success() throws Exception {
        GroupDTO group = new GroupDTO("Group1", null, 2);
        when(groupService.getGroupDetailsByName("Group1")).thenReturn(group);

        mockMvc.perform(get("/api/groups/Group1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Group1"))
                .andExpect(jsonPath("$.nbInstr").value(2));

        verify(groupService, times(1)).getGroupDetailsByName("Group1");
    }

    @Test
    void testGetGroupDetailsByName_NotFound() throws Exception {
        when(groupService.getGroupDetailsByName("NonExistentGroup")).thenReturn(null);

        mockMvc.perform(get("/api/groups/NonExistentGroup")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cannot find group name"));

        verify(groupService, times(1)).getGroupDetailsByName("NonExistentGroup");
    }

    @Test
    void testAddGroup_Success() throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("name", "NewGroup");
        requestBody.put("nbInstr", 2);

        GroupDTO newGroup = new GroupDTO("NewGroup", null, 2);
        when(groupService.addGroup(anyMap())).thenReturn(newGroup);

        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("NewGroup"))
                .andExpect(jsonPath("$.nbInstr").value(2));

        verify(groupService, times(1)).addGroup(anyMap());
    }

    @Test
    void testAddGroup_Conflict() throws Exception {
        Map<String, Object> requestBody = Map.of("name", "ExistingGroup", "nbInstr", 2);

        when(groupService.addGroup(anyMap())).thenReturn(null);

        mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Group/subgroup already exists."));

        verify(groupService, times(1)).addGroup(anyMap());
    }

    @Test
    void testDeleteGroup_Success() throws Exception {
        when(groupService.deleteGroup("Group1")).thenReturn("Group1 deleted successfully");

        mockMvc.perform(delete("/api/groups/Group1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Group1 deleted successfully"));

        verify(groupService, times(1)).deleteGroup("Group1");
    }

    @Test
    void testUpdateGroup_Success() throws Exception {
        Map<String, Object> requestBody = Map.of("name", "UpdatedGroup", "nbInstr", 2);

        GroupDTO updatedGroup = new GroupDTO("UpdatedGroup", null, 4);
        when(groupService.updateGroup(anyMap(), eq("Group1"))).thenReturn(updatedGroup);

        mockMvc.perform(patch("/api/groups/Group1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("UpdatedGroup"))
                .andExpect(jsonPath("$.nbInstr").value(4));

        verify(groupService, times(1)).updateGroup(anyMap(), eq("Group1"));
    }

    @Test
    void testGetSummaries_Success() throws Exception {
        List<GroupSummaryDTO> summaries = List.of(
                new GroupSummaryDTO("Group1", 5),
                new GroupSummaryDTO("Group2", 10)
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
}
