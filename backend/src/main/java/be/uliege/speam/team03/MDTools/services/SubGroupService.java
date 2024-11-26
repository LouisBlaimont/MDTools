package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.SubGroupCharacteristic;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;

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

    @SuppressWarnings("unchecked")
    public GroupDTO addSubGroup(String groupName, Map<String, Object> body){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();

        String subGroupName = (String) body.get("name");
        List<String> characteristics = (List<String>) body.get("characteristics");

        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(subGroupName);
        if (sameSubGroup.isPresent()){
            return null;
        }

        SubGroup newSubGroup = new SubGroup(subGroupName, group);
        subGroupRepository.save(newSubGroup);

        List<SubGroupCharacteristic> subGroupChars = new ArrayList<>();

        for (String charName : characteristics){
            Optional<Characteristic> sameChar = charRepository.findByName(charName);
            Characteristic newChar;
            if (sameChar.isPresent()){
                newChar = sameChar.get();
            }
            else{
                newChar = new Characteristic(charName);
                charRepository.save(newChar);
            }
            
            SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(newSubGroup.getId(), newChar.getId());
            SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(newSubGroup, newChar, 1);
            subGroupChar.setKey(key);
            subGroupChars.add(subGroupChar); 
        }

        subGroupCharRepository.saveAll(subGroupChars);

        newSubGroup.setInstrCount(0);
        newSubGroup.setCategories(null);
        newSubGroup.setCharacteristics(subGroupChars);

        List<SubGroup> subGroups = group.getSubGroups();
        subGroups.add(newSubGroup);
        group.setSubGroups(subGroups);

        GroupDTO groupDTO = new GroupDTO(
            group.getName(), 
            group.getSubGroups().stream().
            map(subgroup -> new SubGroupDTO
                (subgroup.getName(), 
                subgroup.getSubGroupCharacteristics().stream()
                    .map(detail-> detail.getCharacteristic().getName())
                    .collect(Collectors.toList()),
                subgroup.getInstrCount()
                ))
                .collect(Collectors.toList()),
            group.getInstrCount()
            );

        return groupDTO;
    }

    public SubGroupDTO findSubGroup(String name){
        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(name);
        if (subgroupMaybe.isPresent() == false){
            return null;
        }
        SubGroup subGroup = subgroupMaybe.get();
        SubGroupDTO subGroupDTO = new SubGroupDTO(
            subGroup.getName(),
            subGroup.getSubGroupCharacteristics().stream()
                .map(subgroupChar -> subgroupChar.getCharacteristic().getName())
                .collect(Collectors.toList()), 
            subGroup.getInstrCount());
        return subGroupDTO;
    }

    public SubGroupDTO updateSubGroup(String subGroupName, Map<String, Object> body){
        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subgroupMaybe.isPresent() == false){
            return null;
        }
        SubGroup subgroup = subgroupMaybe.get();

        String name = (String) body.get("name");
        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(name);
        if (sameSubGroup.isPresent()){
            return null;
        }
        subgroup.setName(name);
        subGroupRepository.save(subgroup);

        SubGroupDTO subGroupDTO = new SubGroupDTO(
            subgroup.getName(), 
            subgroup.getSubGroupCharacteristics().stream()
                .map(subGroupChar -> subGroupChar.getCharacteristic().getName())
                .collect(Collectors.toList()), 
            subgroup.getInstrCount());
        return subGroupDTO;
    }

    public String deleteSubGroup(String subGroupName){
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isPresent() == false){
            return null;
        }
        SubGroup subGroup = subGroupMaybe.get();

        Group group = subGroup.getGroup();
        List<SubGroup> subGroups = group.getSubGroups();
        subGroups.remove(subGroup);
        group.setSubGroups(subGroups);
        groupRepository.save(group);

        List<SubGroupCharacteristic> subGroupChars = subGroup.getSubGroupCharacteristics();
        List<Characteristic> exclusiveChars = new ArrayList<>();

        for (SubGroupCharacteristic subGroupChar : subGroupChars){
            Characteristic characteristic = subGroupChar.getCharacteristic();
            long charAssociatedWithGroups = subGroupCharRepository.countByCharacteristic(characteristic);
            if (charAssociatedWithGroups == 1){
                exclusiveChars.add(characteristic);
            }
        }
        subGroupCharRepository.deleteAll(subGroupChars);
        charRepository.deleteAll(exclusiveChars);
        subGroupRepository.delete(subGroup);

        return "Successfully deleted group.";
    }

}
