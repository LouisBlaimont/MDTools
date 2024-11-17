package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.GroupCharacteristic;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private GroupRepository groupRepository; 

    public GroupController(GroupRepository groupRepository){
        this.groupRepository = groupRepository;
    }

    @GetMapping("")
    public Iterable<Group> findallGroups(){
        return this.groupRepository.findAll();
    }

    @GetMapping("/{groupName}")
    public List<GroupCharacteristic> getGroupDetailsByName(@PathVariable String groupName) {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        Group group = groupMaybe.get();
        List<GroupCharacteristic> groupDetails = group.getGroupCharacteristics();
        return groupDetails;
        
    }
     
}

