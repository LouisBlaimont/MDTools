package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.GroupMapper;
import be.uliege.speam.team03.MDTools.mapper.SubGroupMapper;
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
public class SubGroupService {
    private GroupRepository groupRepository;
    private CharacteristicRepository charRepository;
    private SubGroupRepository subGroupRepository;
    private SubGroupCharacteristicRepository subGroupCharRepository;

    private PictureStorageService pictureStorageService;

    /**
     * Gets the subgroups of the group given by groupName
     * @param groupName
     * @return List of SubGroupDTO
     */
    public List<SubGroupDTO> findAllSubGroups(String groupName) {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false) {
            return null;
        }
        Group group = groupMaybe.get();
        List<SubGroup> subGroups = group.getSubGroups();
        List<SubGroupDTO> subGroupsDTO = new ArrayList<>();
        subGroupsDTO = subGroups.stream()
                    .map(SubGroupMapper::toDto)
                    .collect(Collectors.toList());
        return subGroupsDTO;
    }

    /**
     * Adds a subgroup with the characteristics given in the body to the group given by groupName
     * @param groupName
     * @param body
     * @return Updated GroupDTO
     */
    public GroupDTO addSubGroup(String groupName, Map<String, Object> body) {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false) {
            return null;
        }
        Group group = groupMaybe.get();

        String subGroupName = (String) body.get("name");
        List<String> characteristics = (List<String>) body.get("characteristics");

        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(subGroupName);
        if (sameSubGroup.isPresent()) {
            return null;
        }

        SubGroup newSubGroup = new SubGroup(subGroupName, group);
        newSubGroup = subGroupRepository.save(newSubGroup);

        List<SubGroupCharacteristic> subGroupChars = new ArrayList<>();

        for (String charName : characteristics) {
            Optional<Characteristic> sameChar = charRepository.findByName(charName);
            Characteristic newChar;
            if (sameChar.isPresent()) {
                newChar = sameChar.get();
            } else {
                newChar = new Characteristic(charName);
                charRepository.save(newChar);
            }

            SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(newSubGroup.getId().intValue(),
                    newChar.getId());
            SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(newSubGroup, newChar, 1);
            subGroupChar.setId(key);
            subGroupChars.add(subGroupChar);
        }

        subGroupCharRepository.saveAll(subGroupChars);

        newSubGroup.setInstrCount(0);
        newSubGroup.setCategories(null);
        newSubGroup.setSubGroupCharacteristics(subGroupChars);

        List<SubGroup> subGroups = group.getSubGroups();
        if (subGroups == null) {
            subGroups = new ArrayList<>();
        }
        subGroups.add(newSubGroup);
        group.setSubGroups(subGroups);

        GroupDTO groupDTO = GroupMapper.toDto(group);

        return groupDTO;
    }

    /**
     * Gets the subgroup given by name
     * @param name
     * @return SubGroupDTO
     */
    public SubGroupDTO findSubGroup(String name) {
        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(name);
        if (subgroupMaybe.isPresent() == false) {
            return null;
        }
        SubGroup subGroup = subgroupMaybe.get();
        SubGroupDTO subGroupDTO = SubGroupMapper.toDto(subGroup);
        return subGroupDTO;
    }

    /**
     * Update the subgroup given by subGroupName with new characteristics found in the body
     * @param subGroupName
     * @param body
     * @return Updated SubGroupDTO
     */
    public SubGroupDTO updateSubGroup(String subGroupName, Map<String, Object> body) {
        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subgroupMaybe.isPresent() == false) {
            return null;
        }
        SubGroup subgroup = subgroupMaybe.get();

        String name = (String) body.get("name");
        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(name);
        if (sameSubGroup.isPresent()) {
            return null;
        }
        subgroup.setName(name);
        SubGroup savedSubGroup = subGroupRepository.save(subgroup);

        return SubGroupMapper.toDto(savedSubGroup);
    }

    /**
     * Deletes the subgroup given by subGroupName
     * @param subGroupName
     * @return
     */
    public String deleteSubGroup(String subGroupName) {
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isPresent() == false) {
            return null;
        }
        SubGroup subGroup = subGroupMaybe.get();

        Group group = subGroup.getGroup();
        List<SubGroup> subGroups = group.getSubGroups();
        subGroups.remove(subGroup);
        group.setSubGroups(subGroups);
        groupRepository.save(group);

        // ! Recursive deletion should be done on the db configuration side
        // List<SubGroupCharacteristic> subGroupChars =
        // subGroup.getSubGroupCharacteristics();
        // List<Characteristic> exclusiveChars = new ArrayList<>();

        // for (SubGroupCharacteristic subGroupChar : subGroupChars){
        // Characteristic characteristic = subGroupChar.getCharacteristic();
        // long charAssociatedWithGroups =
        // subGroupCharRepository.countByCharacteristic(characteristic);
        // if (charAssociatedWithGroups == 1){
        // exclusiveChars.add(characteristic);
        // }
        // }
        // subGroupCharRepository.deleteAll(subGroupChars);
        // charRepository.deleteAll(exclusiveChars);
        // subGroupRepository.delete(subGroup);

        return "Successfully deleted group.";
    }

    public SubGroupDTO setSubGroupPicture(String subGroupName, MultipartFile picture) throws ResourceNotFoundException {
        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isEmpty()) {
            throw new ResourceNotFoundException("Group not found.");
        }
        SubGroup subGroup = subGroupMaybe.get();

        if(subGroup.getPictureId() != null) {
            pictureStorageService.deletePicture(subGroup.getPictureId());
        }

        Picture metadata = pictureStorageService.storePicture(picture, PictureType.SUBGROUP, subGroup.getId());

        subGroup.setPictureId(metadata.getId());
        SubGroup savedSubGroup = subGroupRepository.save(subGroup);
        return SubGroupMapper.toDto(savedSubGroup);
    }

}
