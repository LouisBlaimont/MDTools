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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupSummaryDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.GroupService;
import lombok.AllArgsConstructor;

/**
 * This controller implements the API endpoints relative to the groups of instruments. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    @GetMapping("/all")
    public ResponseEntity<?> findallGroups() {
            List<GroupDTO> groups = groupService.findAllGroups();
            if (groups == null || groups.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No groups found");
            }
            return ResponseEntity.status(HttpStatus.OK).body(groups);
    }

    @GetMapping("/{groupName}")
    public ResponseEntity<GroupDTO> getGroupDetailsByName(@PathVariable String groupName) throws ResourceNotFoundException {
        GroupDTO groupDetails = groupService.getGroupDetailsByName(groupName);
        if (groupDetails == null) {
            throw new ResourceNotFoundException("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupDetails);

    }

    @PostMapping
    public ResponseEntity<GroupDTO> addGroup(@RequestBody Map<String, Object> body) {
        GroupDTO newGroup = groupService.addGroup(body);
        if (newGroup == null) {
            throw new BadRequestException("Group/subgroup already exists.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroup);
    }

    @DeleteMapping("/{groupName}")
    public ResponseEntity<String> deleteGroup(@PathVariable String groupName) {
        String groupDeleted = groupService.deleteGroup(groupName);
        if (groupDeleted == null) {
            throw new BadRequestException("Cannot find group.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupDeleted);
    }

    @PatchMapping("/{groupName}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable String groupName, @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException{
        GroupDTO groupUpdated = groupService.updateGroup(body, groupName);
        return ResponseEntity.status(HttpStatus.OK).body(groupUpdated);

    }

    @GetMapping("/summary")
    public ResponseEntity<List<GroupSummaryDTO>> getSummaries() {
        List<GroupSummaryDTO> groups = groupService.getGroupsSummary();
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{groupName}/picture")
    public ResponseEntity<GroupDTO> setGroupPicture(@PathVariable String groupName, @RequestParam("file") MultipartFile file) throws ResourceNotFoundException {
        GroupDTO groupUpdated = groupService.setGroupPicture(groupName, file);
        
        return ResponseEntity.status(HttpStatus.OK).body(groupUpdated);
    }
}
