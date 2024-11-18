package be.uliege.speam.team03.MDTools.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.compositeKeys.GroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.*;
import be.uliege.speam.team03.MDTools.repositories.*;

@Service
public class GroupService {
    private GroupRepository groupRepository; 
    private CharacteristicRepository charRepository;
    private GroupCharacteristicRepository groupCharRepository;

    public GroupService(GroupRepository groupRepository, CharacteristicRepository charRepository, GroupCharacteristicRepository groupCharRepository){
        this.groupRepository = groupRepository;
        this.charRepository = charRepository;
        this.groupCharRepository = groupCharRepository;
    }

    public Iterable<Group> findAllGroups(){
        return groupRepository.findAll();
    }

    public List<GroupCharacteristic> getGroupDetailsByName(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();
        List<GroupCharacteristic> groupDetails = group.getGroupCharacteristics();
        return groupDetails;
    }

    @SuppressWarnings("unchecked")
    public Group addGroup(Map<String, Object> body){
        String groupName = (String) body.get("groupName");
        List<String> characteristics = (List<String>)body.get("characteristics");

        Optional<Group> sameGroup = groupRepository.findByName(groupName);

        if (sameGroup.isPresent()){
            return null;
        }

        Group newGroup = new Group(groupName);
        groupRepository.save(newGroup);

        List<GroupCharacteristic> groupDetails = new ArrayList<>();

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
            
            GroupCharacteristicKey key = new GroupCharacteristicKey(newGroup.getId(), newChar.getId());
            GroupCharacteristic groupDetail = new GroupCharacteristic(newGroup, newChar, 1);
            groupDetail.setKey(key);
            groupDetails.add(groupDetail); 
        }

        groupCharRepository.saveAll(groupDetails);

        return newGroup;
    }

    public String deleteGroup(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null; 
        }
        Group group = groupMaybe.get();

        List<GroupCharacteristic> groupDetails = group.getGroupCharacteristics();

        List<Characteristic> exclusiveChars = new ArrayList<>();
        for (GroupCharacteristic groupDetail : groupDetails){
            Characteristic characteristic = groupDetail.getCharacteristic();
            long charAssociatedWithGroups = groupCharRepository.countByCharacteristic(characteristic);
            if (charAssociatedWithGroups == 1){
                exclusiveChars.add(characteristic);
            }
        }

        groupCharRepository.deleteAll(groupDetails);
        charRepository.deleteAll(exclusiveChars);
        groupRepository.delete(group);
        return "Successfully deleted group.";
    }

    public Group updateGroup(Map<String, Object> body, String groupName){
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

        return group;
    }
}
