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

    //TODO move "body" logic to controller by using DTOs as RequestParameters instead of Map<String, Object>

    /**
     * Get all subgroups of a group.
     * 
     * @param groupName the name of the group
     * @return a list of all subgroups of the specified group, or a 404 status if no subgroups are found
     */
    @GetMapping("/all")
    public ResponseEntity<?> findAllSubGroups() {
        List<SubGroupDTO> subGroups = subGroupService.findAllSubGroups();
        // Check if the list of subgroups is empty
        if (subGroups == null || subGroups.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No subgroups found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroups);
    }

    @GetMapping("/group/{groupName}")
    public ResponseEntity<List<SubGroupDTO>> getSubGroupsOfAGroup(@PathVariable String groupName) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.findAllSubGroups(groupName));
    }

    @PostMapping("/group/{groupName}")
    public ResponseEntity<GroupDTO> addSubGroup(@PathVariable String groupName, @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.addSubGroup(groupName, body));
    }

    @GetMapping("/{subgroupName}")
    public ResponseEntity<SubGroupDTO> getSubGroup(@PathVariable String subgroupName) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.findSubGroup(subgroupName));
    }

    @PatchMapping("/{subgroupName}")
    public ResponseEntity<SubGroupDTO> updateSubGroup(@PathVariable String subgroupName, @RequestBody Map<String, Object> body) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.updateSubGroup(subgroupName, body));
    }

    @DeleteMapping("/{subGroupName}")
    public ResponseEntity<String> deleteSubGroup(@PathVariable String subGroupName) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.deleteSubGroup(subGroupName));
    }

    @PostMapping("/{subGroupName}/picture")
    public ResponseEntity<?> setGroupPicture(@PathVariable String subGroupName, @RequestParam("file") MultipartFile file) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK).body(subGroupService.setSubGroupPicture(subGroupName, file));
    }
    
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
    @PutMapping("/{subGroupName}/characteristics/order")
    public ResponseEntity<SubGroupDTO> updateCharacteristicOrder(
        @PathVariable String subGroupName,
        @RequestBody List<Map<String, Object>> newOrder
    ) throws ResourceNotFoundException, BadRequestException {
        return ResponseEntity.status(HttpStatus.OK)
            .body(subGroupService.updateCharacteristicOrder(subGroupName, newOrder));
    }
}
