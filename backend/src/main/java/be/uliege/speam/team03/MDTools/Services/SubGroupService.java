package be.uliege.speam.team03.MDTools.Services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.*;
import be.uliege.speam.team03.MDTools.models.*;

import be.uliege.speam.team03.MDTools.repositories.*;


@Service
public class SubGroupService {
    private GroupRepository groupRepository;
    private SubGroupRepository subGroupRepository ;

    public SubGroupService(GroupRepository groupRepo, SubGroupRepository subGroupRepo){
        this.groupRepository = groupRepo;
        this.subGroupRepository = subGroupRepo;
    }

    public List<SubGroupDTO> findAllSubGroups(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();
        Optional<List<SubGroups>> subgroupsMaybe = subGroupRepository.findByGroup(group);
        if (subgroupsMaybe.isPresent() == false){
            return null;
        }
        List<SubGroups> subgroups = subgroupsMaybe.get();
        List<SubGroupDTO> subgroupsDTO = subgroups.stream().map(subgroup -> new SubGroupDTO(subgroup.getShape())).collect(Collectors.toList());
        return subgroupsDTO;
    }
}
