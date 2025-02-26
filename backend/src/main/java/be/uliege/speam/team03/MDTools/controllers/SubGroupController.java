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
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.SubGroupService;

/**
 * This controller implements the API endpoints relative to the subgroups of instruments. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@RestController
@RequestMapping("/api/subgroups")
public class SubGroupController {
    private final SubGroupService subGroupService;

    public SubGroupController(SubGroupService service) {
        this.subGroupService = service;
    }

    @GetMapping("/group/{groupName}")
    public ResponseEntity<?> getSubGroupsOfAGroup(@PathVariable String groupName) {
        List<SubGroupDTO> subGroups = subGroupService.findAllSubGroups(groupName);
        if (subGroups == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroups);
    }

    @PostMapping("/group/{groupName}")
    public ResponseEntity<?> addSubGroup(@PathVariable String groupName, @RequestBody Map<String, Object> body) {
        GroupDTO groupWithNewSubGroup = subGroupService.addSubGroup(groupName, body);
        if (groupWithNewSubGroup == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot find group name or subgroup already exists");
        }
        return ResponseEntity.status(HttpStatus.OK).body(groupWithNewSubGroup);
    }

    @GetMapping("/{subgroupName}")
    public ResponseEntity<?> getSubGroup(@PathVariable String subgroupName) {
        SubGroupDTO subGroup = subGroupService.findSubGroup(subgroupName);
        if (subGroup == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find subgroup name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroup);
    }

    @PatchMapping("/{subgroupName}")
    public ResponseEntity<?> updateSubGroup(@PathVariable String subgroupName, @RequestBody Map<String, Object> body) {
        SubGroupDTO subGroupUpdated = subGroupService.updateSubGroup(subgroupName, body);
        if (subGroupUpdated == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot find subgroup or already existing subgroup name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroupUpdated);
    }

    @DeleteMapping("/{subGroupName}")
    public ResponseEntity<String> deleteSubGroup(@PathVariable String subGroupName) {
        String subGroupDeleted = subGroupService.deleteSubGroup(subGroupName);
        if (subGroupDeleted == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find subgroup.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroupDeleted);
    }

    @PostMapping("/{subGroupName}/picture")
    public ResponseEntity<?> setGroupPicture(@PathVariable String subGroupName, @RequestParam("file") MultipartFile file) {
        SubGroupDTO subGroupUpdated = null;
        try {
            subGroupUpdated = subGroupService.setSubGroupPicture(subGroupName, file);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group with name: " + subGroupName);
        }

        if (subGroupUpdated == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot find group with name: " + subGroupName);
        }

        return ResponseEntity.status(HttpStatus.OK).body(subGroupUpdated);
    }

}
