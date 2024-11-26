package be.uliege.speam.team03.MDTools.services;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;
import be.uliege.speam.team03.MDTools.DTOs.*;

@Service
public class GroupService {
    private GroupRepository groupRepository; 
    private CharacteristicRepository charRepository;
    private SubGroupRepository subGroupRepository;
    private SubGroupCharacteristicRepository subGroupCharRepository;

    public GroupService(GroupRepository groupRepository, CharacteristicRepository charRepository, SubGroupRepository subgroupRepository, SubGroupCharacteristicRepository subGroupCharRepository){
        this.groupRepository = groupRepository;
        this.charRepository = charRepository;
        this.subGroupRepository = subgroupRepository;
        this.subGroupCharRepository = subGroupCharRepository;
    }


    public List<GroupDTO> findAllGroups(){
        List<Group> groups = (List<Group>) groupRepository.findAll();
        List<GroupDTO> groupsDTO = groups.stream()
            .map(group -> new GroupDTO(
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
            ))
            .collect(Collectors.toList());
        return groupsDTO;
    }

    public GroupDTO getGroupDetailsByName(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();
        List<SubGroup> subGroups = group.getSubGroups();
        GroupDTO groupDTO = new GroupDTO(
                    group.getName(), 
                    subGroups.stream().
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
    
    @SuppressWarnings("unchecked")
    public GroupDTO addGroup(Map<String, Object> body){
        String groupName = (String) body.get("groupName");
        String subGroupName = (String) body.get("subGroupName");
        List<String> characteristics = (List<String>)body.get("characteristics");

        Optional<Group> sameGroup = groupRepository.findByName(groupName);

        if (sameGroup.isPresent()){
            return null;
        }

        Group newGroup = new Group(groupName);
        groupRepository.save(newGroup);

        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(subGroupName);
        if (sameSubGroup.isPresent()){
            return null;
        }
        SubGroup newSubGroup = new SubGroup(subGroupName, newGroup);
        subGroupRepository.save(newSubGroup);
        List<SubGroup> subGroupsList = new ArrayList<>();
        subGroupsList.add(newSubGroup);

        List<SubGroupCharacteristic> subGroupDetails = new ArrayList<>();

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
            SubGroupCharacteristic subGroupDetail = new SubGroupCharacteristic(newSubGroup, newChar, 1);
            subGroupDetail.setKey(key);
            subGroupDetails.add(subGroupDetail); 
        }

        subGroupCharRepository.saveAll(subGroupDetails);

        newGroup.setInstrCount(0);
        newGroup.setSubGroups(subGroupsList);

        newSubGroup.setInstrCount(0);
        newSubGroup.setCategories(null);
        newSubGroup.setCharacteristics(subGroupDetails);

        SubGroupDTO newSubGroupDTO = new SubGroupDTO(
                    newSubGroup.getName(), 
                    newSubGroup.getSubGroupCharacteristics().stream()
                        .map(detail-> detail.getCharacteristic().getName())
                        .collect(Collectors.toList()), 
                    newSubGroup.getInstrCount());
        List<SubGroupDTO> subGroupDTOList = new ArrayList<>();
        subGroupDTOList.add(newSubGroupDTO);
        GroupDTO newGroupDTO = new GroupDTO(newGroup.getName(), subGroupDTOList, newGroup.getInstrCount());
        return newGroupDTO;
    }
    
    public String deleteGroup(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null; 
        }
        Group group = groupMaybe.get();

        List<SubGroup> subGroups = group.getSubGroups();
        
        for (SubGroup subGroup : subGroups){
            List<SubGroupCharacteristic> subGroupDetails = subGroup.getSubGroupCharacteristics();

            List<Characteristic> exclusiveChars = new ArrayList<>();
            for (SubGroupCharacteristic subGroupDetail : subGroupDetails){
                Characteristic characteristic = subGroupDetail.getCharacteristic();
                long charAssociatedWithGroups = subGroupCharRepository.countByCharacteristic(characteristic);
                if (charAssociatedWithGroups == 1){
                    exclusiveChars.add(characteristic);
                }
            }
            subGroupCharRepository.deleteAll(subGroupDetails);
            charRepository.deleteAll(exclusiveChars);
            subGroupRepository.delete(subGroup);
        }

        groupRepository.delete(group);
        return "Successfully deleted group.";
    }

    
    public GroupDTO updateGroup(Map<String, Object> body, String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();

        String name = (String) body.get("name");
        Optional<Group> sameGroup = groupRepository.findByName(name);
        if (sameGroup.isPresent()){
            return null;
        }
        group.setName(name);
        groupRepository.save(group);

        List<SubGroup> subGroups = group.getSubGroups();
        GroupDTO groupDTO = new GroupDTO(
            group.getName(), 
            subGroups.stream().
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

    public List<GroupSummaryDTO> getGroupsSummary(){
        List<Group> groups = (List<Group>) groupRepository.findAll();
        List<GroupSummaryDTO> groupsSummaryDTO = groups.stream().map(group -> new GroupSummaryDTO(group.getName(), group.getInstrCount())).collect(Collectors.toList());
        return groupsSummaryDTO;
    }
}
