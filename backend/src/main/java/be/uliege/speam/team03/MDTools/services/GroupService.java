package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
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

/**
 * Service class for managing groups.
 * 
 * This service provides methods for retrieving, adding, updating, and deleting groups,
 * as well as managing group characteristics and pictures.
 * 
 * Dependencies:
 * - GroupRepository: Repository for accessing group data.
 * - CharacteristicRepository: Repository for accessing characteristic data.
 * - SubGroupRepository: Repository for accessing subgroup data.
 * - SubGroupCharacteristicRepository: Repository for accessing subgroup characteristic data.
 * - PictureStorageService: Service for managing picture storage.
 * 
 * Methods:
 * - findAllGroups: Retrieves all groups.
 * - getGroupDetailsByName: Retrieves the details of a group by its name.
 * - addGroup: Adds a new group.
 * - deleteGroup: Deletes a group by its name.
 * - updateGroup: Updates the details of a group.
 * - getGroupsSummary: Retrieves a summary of all groups.
 * - setGroupPicture: Sets the picture for a group.
 */
@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository; 
    private final CharacteristicRepository charRepository;
    private final SubGroupRepository subGroupRepository;
    private final SubGroupCharacteristicRepository subGroupCharRepository;

    private final PictureStorageService pictureStorageService;

    /**
     * Retrieves all groups.
     * 
     * @return a list of GroupDTO objects representing all groups.
     */
    public List<GroupDTO> findAllGroups(){
        List<Group> groups = (List<Group>) groupRepository.findAll();
        List<GroupDTO> groupsDTO = groups.stream()
            .map(GroupMapper::toDto)
            .toList();
        return groupsDTO;
    }

    /**
     * Retrieves the details of a group by its ID.
     * 
     * @param groupId
     * @return
     */
    public GroupDTO findGroupById(Integer groupId){
        Optional<Group> groupMaybe = groupRepository.findById(groupId);
        if (groupMaybe.isPresent() == false){
            return null;
        }
        Group group = groupMaybe.get();

        GroupDTO groupDTO = GroupMapper.toDto(group);
        return groupDTO;
    }

    /**
     * Retrieves the details of a group by its name.
     * 
     * @param groupName the name of the group.
     * @return a GroupDTO object representing the group details, or null if the group is not found.
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
     * Adds a new group.
     * 
     * @param body a map containing the group details.
     * @return a GroupDTO object representing the newly added group, or null if a group with the same name already exists.
     */
    @SuppressWarnings("unchecked")
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
            
            SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(newSubGroup.getId(), newChar.getId());
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

        GroupDTO newGroupDTO = GroupMapper.toDto(newGroup);
        return newGroupDTO;
    }
    
    /**
     * Deletes a group by its name.
     * 
     * @param groupName the name of the group to be deleted.
     * @return a string message indicating the result of the deletion.
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
     * Updates the details of a group.
     * 
     * @param body a map containing the updated group details.
     * @param groupName the name of the group to be updated.
     * @return a GroupDTO object representing the updated group.
     * @throws ResourceNotFoundException if the group is not found.
     * @throws BadRequestException if the name of the group is not provided or if a group with the same name already exists.
     */
    public GroupDTO updateGroup(Map<String, Object> body, String groupName) throws ResourceNotFoundException, BadRequestException {
        if(ObjectUtils.isEmpty(groupName)){
            throw new BadRequestException("Group name is required.");
        }
        
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
     * Retrieves a summary of all groups.
     *
     * This method fetches all groups from the repository and maps them to a list of
     * GroupSummaryDTO objects, which contain the group's name, instrument count, and picture ID.
     *
     * @return a list of GroupSummaryDTO objects representing the summary of all groups.
     */
    public List<GroupSummaryDTO> getGroupsSummary(){
        List<Group> groups = (List<Group>) groupRepository.findAll();
        List<GroupSummaryDTO> groupsSummaryDTO = groups.stream().map(group -> new GroupSummaryDTO(group.getName(), group.getInstrCount(), group.getPictureId())).toList();
        return groupsSummaryDTO;
    }

    
    /**
     * Sets the picture for a group identified by its name.
     * If the group already has a picture, the existing picture is deleted before storing the new one.
     *
     * @param groupName the name of the group whose picture is to be set
     * @param picture the new picture to be set for the group
     * @return a GroupDTO object representing the updated group
     * @throws ResourceNotFoundException if the group with the specified name is not found
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
