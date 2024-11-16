package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class GroupController {
    private final GroupRepository groupRepository; 
    public GroupController(GroupRepository groupRepository){
        this.groupRepository = groupRepository;
    }

    @GetMapping("/groups")
    public Iterable<Group> findallGroups(){
        return this.groupRepository.findAll();
    }
}

