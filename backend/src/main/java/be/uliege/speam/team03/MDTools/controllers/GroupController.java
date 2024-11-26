package be.uliege.speam.team03.MDTools.controllers;


import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupSummaryDTO;
import be.uliege.speam.team03.MDTools.services.GroupService;


@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService service){
        this.groupService = service;
    }

    @GetMapping("")
    public ResponseEntity<?> findallGroups(){
        try{
            List<GroupDTO> groups = groupService.findAllGroups();
            return ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while retrieving groups.");
        }
    }

    @GetMapping("/{groupName}")
    public ResponseEntity<?> getGroupDetailsByName(@PathVariable String groupName) {
        GroupDTO groupDetails = groupService.getGroupDetailsByName(groupName);
        if (groupDetails == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupDetails);
        
    }

    
    @PostMapping
    public ResponseEntity<?> addGroup(@RequestBody Map<String, Object> body){
        GroupDTO newGroup = groupService.addGroup(body);
        if (newGroup == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Group/subgroup already exists.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroup);
    }
    
    @DeleteMapping("/{groupName}")
    public ResponseEntity<String> deleteGroup(@PathVariable String groupName){
        String groupDeleted = groupService.deleteGroup(groupName);
        if (groupDeleted == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupDeleted);
    }

    
    @PatchMapping("/{groupName}")
    public ResponseEntity<?> updateGroup(@PathVariable String groupName, @RequestBody Map<String, Object> body){
        GroupDTO groupUpdated = groupService.updateGroup(body, groupName);
        if (groupUpdated == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group or Already existing group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupUpdated);

    } 
    @GetMapping("/summary")
    public ResponseEntity<?> getSummaries(){
        try{
            List<GroupSummaryDTO> groups = groupService.getGroupsSummary();
            return ResponseEntity.ok(groups);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while retrieving groups.");
        }
    }   
}
