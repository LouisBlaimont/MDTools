package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.Services.SubGroupService;
import be.uliege.speam.team03.MDTools.DTOs.*;

@RestController
@RequestMapping("/api/subgroups")
public class SubGroupController {

    private final SubGroupService subgroupService;

    public SubGroupController(SubGroupService service){
        this.subgroupService = service;
    }


    @GetMapping("/{groupName}")
    public ResponseEntity<?> getSubgroupsFromGroup(@PathVariable String groupName) {
        List<SubGroupDTO> subGroups = subgroupService.findAllSubGroups(groupName);
        if (subGroups == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot find group name");
        }
        return ResponseEntity.status(HttpStatus.OK).body(subGroups);
        
    }

}