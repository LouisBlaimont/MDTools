package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.*;
import be.uliege.speam.team03.MDTools.services.SubGroupService;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/subgroups")
public class SubGroupController {
    private final SubGroupService subGroupService;
    public SubGroupController(SubGroupService service){
        this.subGroupService = service;
    }

    @GetMapping("/{groupName}")
    public ResponseEntity<?> getSubGroupsOfAGroup(@PathVariable String groupName){
        List<SubGroupDTO> subGroups = subGroupService.findAllSubGroups(groupName);
        if (subGroups == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroups);
    }
    
    
}
