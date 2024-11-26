package be.uliege.speam.team03.MDTools.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import be.uliege.speam.team03.MDTools.DTOs.*;

@Service
public class SubGroupService {
    private GroupRepository groupRepository;
    private CharacteristicRepository charRepository;
    private SubGroupRepository subGroupRepository;
    private SubGroupCharacteristicRepository subGroupCharRepository;

    public SubGroupService(GroupRepository groupRepository, CharacteristicRepository charRepository, SubGroupRepository subGroupRepository,  SubGroupCharacteristicRepository subGroupCharRepository){
        this.groupRepository = groupRepository;
        this.charRepository = charRepository;
        this.subGroupRepository = subGroupRepository;
        this.subGroupCharRepository = subGroupCharRepository;
    }

    public List<SubGroupDTO> findAllSubGroups(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();
        List<SubGroup> subGroups = group.getSubGroups();
        List<SubGroupDTO> subGroupsDTO = new ArrayList<>();
        for (SubGroup subGroup : subGroups){
            SubGroupDTO subGroupDTO = new SubGroupDTO(subGroup.getName(), subGroup.getSubGroupCharacteristics().stream().map(detail -> detail.getCharacteristic().getName()).collect(Collectors.toList()), subGroup.getInstrCount());
            subGroupsDTO.add(subGroupDTO);
        }
        return subGroupsDTO;
        
    }
}
