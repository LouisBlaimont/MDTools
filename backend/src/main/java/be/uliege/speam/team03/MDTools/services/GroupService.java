package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.GroupSummaryDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.GroupMapper;
import be.uliege.speam.team03.MDTools.models.Characteristic;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.SubGroupCharacteristic;
import be.uliege.speam.team03.MDTools.repositories.CharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.GroupRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupCharacteristicRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository; 
    private final CharacteristicRepository charRepository;
    private final SubGroupRepository subGroupRepository;
    private final SubGroupCharacteristicRepository subGroupCharRepository;

    private final PictureStorageService pictureStorageService;

    /**
     * Gets all groups of instruments
     * @return List of GroupDTO
     */
    public List<GroupDTO> findAllGroups(){
        List<Group> groups = (List<Group>) groupRepository.findAll();
        List<GroupDTO> groupsDTO = groups.stream()
            .map(GroupMapper::toDto)
            .collect(Collectors.toList());
        return groupsDTO;
    }

    /**
     * Gets the details of the group given by groupname
     * @param groupName
     * @return GroupDTO
     */
    public GroupDTO getGroupDetailsByName(String groupName){
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();

        GroupDTO groupDTO = GroupMapper.toDto(group);
        return groupDTO;
    }
    
    /**
     * Adds a group whose details are given the body parameters
     * @param body
     * @return GroupDTO
     */
    public GroupDTO addGroup(Map<String, Object> body){
        String groupName = (String)body.get("groupName");
        String subGroupName = (String)body.get("subGroupName");
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
            
            SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(newSubGroup.getId().intValue(), newChar.getId());
            SubGroupCharacteristic subGroupDetail = new SubGroupCharacteristic(newSubGroup, newChar, 1);
            subGroupDetail.setId(key);
            subGroupDetails.add(subGroupDetail); 
        }

        subGroupCharRepository.saveAll(subGroupDetails);

        newGroup.setInstrCount(0);
        newGroup.setSubGroups(subGroupsList);

        newSubGroup.setInstrCount(0);
        newSubGroup.setCategories(null);
        newSubGroup.setSubGroupCharacteristics(subGroupDetails);

        // SubGroupDTO newSubGroupDTO = SubGroupMapper.toDto(newSubGroup);
        // List<SubGroupDTO> subGroupDTOList = new ArrayList<>();
        // subGroupDTOList.add(newSubGroupDTO);
        GroupDTO newGroupDTO = GroupMapper.toDto(newGroup);
        return newGroupDTO;
    }
    
    /**
     * Deletes group given by groupName
     * @param groupName
     * @return
     */
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

    
    /**
     * Update group given by groupName with the characteristics given in the body
     * @param body
     * @param groupName
     * @return GroupDTO
     * @throws ResourceNotFoundException
     * @throws BadRequestException
     */
    public GroupDTO updateGroup(Map<String, Object> body, String groupName) throws ResourceNotFoundException, BadRequestException {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false){
            throw new ResourceNotFoundException("Cannot find group with name: " + groupName);
        }
        Group group = groupMaybe.get();

        String name = (String) body.get("name");
        if (name == null){
            throw new BadRequestException("Name is required.");
        }
        Optional<Group> sameGroup = groupRepository.findByName(name);
        if (sameGroup.isPresent()){
            throw new BadRequestException("Group name already exists.");
        }
        group.setName(name);
        Group savedGroup = groupRepository.save(group);

        GroupDTO groupDTO = GroupMapper.toDto(savedGroup);

        return groupDTO;
    }

    /**
     * Gets the group and makes a summary containing their name, picture, and number of instruments.
     * @return
     */
    public List<GroupSummaryDTO> getGroupsSummary(){
        List<Group> groups = (List<Group>) groupRepository.findAll();
        List<GroupSummaryDTO> groupsSummaryDTO = groups.stream().map(group -> new GroupSummaryDTO(group.getName(), group.getInstrCount(), group.getPictureId())).collect(Collectors.toList());
        return groupsSummaryDTO;
    }

    /**
     * 
     * @param groupName
     * @param picture
     * @return
     * @throws ResourceNotFoundException
     */
    public GroupDTO setGroupPicture(String groupName, MultipartFile picture) throws ResourceNotFoundException {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isEmpty()){
            throw new ResourceNotFoundException("Group not found.");
        }
        Group group = groupMaybe.get();

        if(group.getPictureId() != null){
            pictureStorageService.deletePicture(group.getPictureId());
        }

        Picture metadata = pictureStorageService.storePicture(picture, PictureType.GROUP, group.getId());

        group.setPictureId(metadata.getId());
        Group savedGroup = groupRepository.save(group);
        return GroupMapper.toDto(savedGroup);
    }
}
