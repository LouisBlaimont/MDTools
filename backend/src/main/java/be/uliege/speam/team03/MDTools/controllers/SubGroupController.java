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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.SubGroupService;
import lombok.AllArgsConstructor;

/**
 * This controller implements the API endpoints relative to the subgroups of instruments. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@RestController
@RequestMapping("/api/subgroups")
@AllArgsConstructor
public class SubGroupController {
    private final SubGroupService subGroupService;

    /**
     * Get all subgroups of a group.
     * 
     * @param groupName the name of the group
     * @return a list of all subgroups of the specified group, or a 404 status if no subgroups are found
     */
    @GetMapping("/all")
    public ResponseEntity<List<SubGroupDTO>> findAllSubGroups() {
        List<SubGroupDTO> subGroups = subGroupService.findAllSubGroups();
        return ResponseEntity.status(HttpStatus.OK).body(subGroups);
    }

    /**
     * Retrieves all sub-groups that belong to a specific group.
     * 
     * @param groupName the name of the group to retrieve sub-groups for
     * @return ResponseEntity containing the list of SubGroupDTO objects representing the sub-groups
     * @throws ResourceNotFoundException if the specified group does not exist
     * @throws BadRequestException if the request parameters are invalid
     */
    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<SubGroupDTO>> getSubGroupsOfAGroup(@PathVariable String groupName) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.findAllSubGroups(groupName));
    }

    /**
     * Adds a subgroup to a specified group.
     *
     * @param groupName the name of the parent group to which a subgroup will be added
     * @param body a map containing the subgroup details
     * @return ResponseEntity containing the GroupDTO with the updated group information
     * @throws ResourceNotFoundException if the parent group is not found
     * @throws BadRequestException if the request body is invalid or contains insufficient information
     */
    @PostMapping("/group/{groupName}")
    public ResponseEntity<GroupDTO> addSubGroup(@PathVariable String groupName, @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.addSubGroup(groupName, body));
    }

    /**
     * Retrieves a specific sub-group by its name.
     * 
     * @param subgroupName the name of the sub-group to retrieve
     * @return a ResponseEntity containing the SubGroupDTO with HTTP status OK (200)
     * @throws ResourceNotFoundException if the sub-group with the given name doesn't exist
     * @throws BadRequestException if the request parameters are invalid
     */
    @GetMapping("/{subgroupName}")
    public ResponseEntity<SubGroupDTO> getSubGroup(@PathVariable String subgroupName) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.findSubGroup(subgroupName));
    }

    /**
     * Updates an existing subgroup with the given information.
     *
     * @param subgroupName the name of the subgroup to update
     * @param body a map containing the fields to update and their new values
     * @return a ResponseEntity containing the updated SubGroupDTO with HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the subgroup with the given name does not exist
     * @throws BadRequestException if the request contains invalid data
     */
    @PatchMapping("/{subgroupName}")
    public ResponseEntity<SubGroupDTO> updateSubGroup(@PathVariable String subgroupName, @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.updateSubGroup(subgroupName, body));
    }

    /**
     * Deletes a subgroup with the specified name.
     *
     * @param subGroupName The name of the subgroup to delete
     * @return A ResponseEntity containing a success message if deletion was successful
     * @throws ResourceNotFoundException If the subgroup with the given name does not exist
     * @throws BadRequestException If the deletion cannot be performed (e.g., the subgroup is in use)
     */
    @DeleteMapping("/{subGroupName}")
    public ResponseEntity<String> deleteSubGroup(@PathVariable String subGroupName) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.deleteSubGroup(subGroupName));
    }

    /**
     * Sets the profile picture for a specific subgroup.
     * 
     * @param subGroupName the name of the subgroup to update
     * @param file the image file to set as the subgroup's picture
     * @return ResponseEntity containing the updated SubGroupDTO with HTTP status 200 (OK)
     * @throws ResourceNotFoundException if the subgroup doesn't exist
     * @throws BadRequestException if the provided file is invalid (not an image, too large, etc.)
     */
    @PostMapping("/{subGroupName}/picture")
    public ResponseEntity<SubGroupDTO> setGroupPicture(@PathVariable String subGroupName, @RequestParam("file") MultipartFile file) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.setSubGroupPicture(subGroupName, file));
    }
    
    /**
     * Adds a characteristic to a specified SubGroup.
     * 
     * @param subGroupName the name of the SubGroup to which the characteristic will be added
     * @param body a Map containing the characteristic name with key "name"
     * @return ResponseEntity with the updated SubGroupDTO and HTTP status OK
     * @throws ResourceNotFoundException if the SubGroup with the given name is not found
     * @throws BadRequestException if the characteristic name is not provided or empty
     */
    @PostMapping("/{subGroupName}/characteristics")
    public ResponseEntity<SubGroupDTO> addCharacteristicToSubGroup(
        @PathVariable String subGroupName,
        @RequestBody Map<String, String> body
    ) throws ResourceNotFoundException, BadRequestException {
        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Characteristic name is required.");
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(subGroupService.addCharacteristicToSubGroup(subGroupName, name));
    }
    /**
     * Updates the order of characteristics in a specified subgroup.
     * 
     * @param subGroupName the name of the subgroup to update
     * @param newOrder a list of maps representing the new order of characteristics
     * @return a ResponseEntity containing the updated SubGroupDTO
     * @throws ResourceNotFoundException if the subgroup cannot be found
     * @throws BadRequestException if the request contains invalid data
     */
    @PutMapping("/{subGroupName}/characteristics/order")
    public ResponseEntity<SubGroupDTO> updateCharacteristicOrder(
        @PathVariable String subGroupName,
        @RequestBody List<Map<String, Object>> newOrder
    ) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK)
            .body(subGroupService.updateCharacteristicOrder(subGroupName, newOrder));
    }

    /**
     * Removes a characteristic from a sub-group.
     * 
     * @param subGroupName      the name of the sub-group from which to remove the characteristic
     * @param characteristicName the name of the characteristic to remove
     * @return a ResponseEntity containing the updated SubGroupDTO
     * @throws ResourceNotFoundException if the sub-group or characteristic is not found
     * @throws BadRequestException if the removal operation cannot be completed due to business constraints
     */
    @DeleteMapping("/{subGroupName}/characteristics/{characteristicName}")
    public ResponseEntity<SubGroupDTO> removeCharacteristicFromSubGroup(
        @PathVariable String subGroupName,
        @PathVariable String characteristicName
    ) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK)
            .body(subGroupService.removeCharacteristicFromSubGroup(subGroupName, characteristicName));
    }
}
