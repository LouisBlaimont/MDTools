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

    /**
     * Retrieves all groups from the database.
     * 
     * @return A ResponseEntity containing:
     *         - HTTP 200 OK with a list of GroupDTO objects if groups are founds
     */
    @GetMapping("/all")
    public ResponseEntity<List<GroupDTO>> findallGroups() {
            List<GroupDTO> groups = groupService.findAllGroups();
            return ResponseEntity.status(HttpStatus.OK).body(groups);
    }

    /**
     * Retrieves the details of a specific group by its name.
     * 
     * @param groupName The name of the group to retrieve
     * @return ResponseEntity containing the GroupDTO with details of the requested group
     * @throws ResourceNotFoundException If the group with the specified name doesn't exist
     */
    @GetMapping("/{groupName}")
    public ResponseEntity<GroupDTO> getGroupDetailsByName(@PathVariable String groupName) throws ResourceNotFoundException {
        GroupDTO groupDetails = groupService.getGroupDetailsByName(groupName);
        if (groupDetails == null) {
            throw new ResourceNotFoundException("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupDetails);

    }


    /**
     * Creates a new group or subgroup based on the provided data.
     * 
     * @param body A map containing the group data to be added
     * @return ResponseEntity containing the created GroupDTO with HTTP status 201 (Created)
     * @throws BadRequestException if the group or subgroup already exists
     */
    @PostMapping
    public ResponseEntity<GroupDTO> addGroup(@RequestBody Map<String, Object> body) {
        GroupDTO newGroup = groupService.addGroup(body);
        if (newGroup == null) {
            throw new BadRequestException("Group/subgroup already exists.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(newGroup);
    }

    /**
     * Deletes a group by its name.
     * 
     * @param groupName the name of the group to delete
     * @return a ResponseEntity with the name of the deleted group in the body and HTTP status 200 (OK)
     * @throws BadRequestException if the group cannot be found
     */
    @DeleteMapping("/{groupName}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupName) throws ResourceNotFoundException {
        groupService.deleteGroup(groupName);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Updates a group based on the provided data.
     * 
     * @param groupName The name of the group to update
     * @param body A map containing the fields to update and their new values
     * @return ResponseEntity containing the updated GroupDTO with HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the group with the specified name is not found
     * @throws BadRequestException if the request contains invalid data
     */
    @PatchMapping("/{groupName}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable String groupName, @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException{
        GroupDTO groupUpdated = groupService.updateGroup(body, groupName);
        return ResponseEntity.status(HttpStatus.OK).body(groupUpdated);

    }

    /**
     * Retrieves a summary of all groups.
     * 
     * @return ResponseEntity containing a list of GroupSummaryDTO objects representing summaries of all groups
     */
    @GetMapping("/summary")
    public ResponseEntity<List<GroupSummaryDTO>> getSummaries() {
        List<GroupSummaryDTO> groups = groupService.getGroupsSummary();
        return ResponseEntity.ok(groups);
    }

    /**
     * Sets the profile picture for a specific group.
     * 
     * @param groupName the name of the group to update
     * @param file the image file to set as the group's profile picture
     * @return ResponseEntity containing the updated GroupDTO with HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the group with the given name is not found
     */
    @PostMapping("/{groupName}/picture")
    public ResponseEntity<GroupDTO> setGroupPicture(@PathVariable String groupName, @RequestParam("file") MultipartFile file) throws ResourceNotFoundException {
        GroupDTO groupUpdated = groupService.setGroupPicture(groupName, file);
        
        return ResponseEntity.status(HttpStatus.OK).body(groupUpdated);
    }
}
