package be.uliege.speam.team03.MDTools;

import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.Group;
import be.uliege.speam.team03.MDTools.GroupRepository;

import org.springframework.web.bind.annotation.GetMapping;

@SuppressWarnings("unused")
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

