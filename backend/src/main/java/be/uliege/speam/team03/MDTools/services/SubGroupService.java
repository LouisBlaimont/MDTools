package be.uliege.speam.team03.MDTools.services;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.SubGroupCharacteristicKey;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
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
     * Finds all sub-groups for a given group name.
     *
     * @param groupName the name of the group for which to find sub-groups
     * @return a list of SubGroupDTO objects representing the sub-groups of the specified group,
     *         or null if the group with the given name does not exist
     * @throws ResourceNotFoundException if the group with the given name does not exist
     */
    public List<SubGroupDTO> findAllSubGroups(String groupName) throws ResourceNotFoundException {
        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isPresent() == false) {
            throw new ResourceNotFoundException(groupName + " not found.");
        }
        Group group = groupMaybe.get();
        List<SubGroup> subGroups = group.getSubGroups();
        subGroups.forEach(subGroup -> subGroup.setInstrCount(nbInstrOfSubGroup(subGroup.getId())));
        List<SubGroupDTO> subGroupsDTO = new ArrayList<>();
        subGroupsDTO = subGroups.stream()
                    .map(SubGroupMapper::toDto)
                    .toList();
        return subGroupsDTO;
    }

    /**
     * Finds all sub-groups in the system.
     *
     * @return a list of SubGroupDTO objects representing all sub-groups in the system
     */
    public List<SubGroupDTO> findAllSubGroups() {
        List<SubGroup> subGroups = (List<SubGroup>) subGroupRepository.findAll();
        List<SubGroupDTO> subGroupsDTO = new ArrayList<>();
        subGroupsDTO = subGroups.stream()
                    .map(SubGroupMapper::toDto)
                    .toList();
        return subGroupsDTO;
    }

    /**
     * Adds a new sub-group to an existing group.
     *
     * @param groupName the name of the group to which the sub-group will be added
     * @param body a map containing the sub-group details:
     *             - "name": the name of the sub-group (String)
     *             - "characteristics": a list of characteristic names (List<String>)
     * @return a GroupDTO representing the updated group, or null if the group does not exist or the sub-group already exists
     * @throws BadRequestException if the group name is empty or is already taken
     * @throws ResourceNotFoundException if the group with the given name does not exist
     */
    @SuppressWarnings("unchecked")
    public GroupDTO addSubGroup(String groupName, Map<String, Object> body) throws BadRequestException, ResourceNotFoundException {
        if(ObjectUtils.isEmpty(groupName))
            throw new BadRequestException("Group name is required.");

        Optional<Group> groupMaybe = groupRepository.findByName(groupName);
        if (groupMaybe.isEmpty())
            throw new ResourceNotFoundException(groupName + " not found.");

        Group group = groupMaybe.get();

        String subGroupName = (String) body.get("name");
        List<String> characteristics = (List<String>) body.get("characteristics");

        if (characteristics == null) {
            characteristics = new ArrayList<>();
        } else {
            characteristics = characteristics.stream()
                .filter(c -> c != null && !c.trim().isEmpty())
                .toList();
        }

        // Always include these three essential characteristics
        Set<String> charSet = new LinkedHashSet<>(characteristics);
        charSet.add("Name");
        charSet.add("Function");
        charSet.add("Length");
        characteristics = new ArrayList<>(charSet);

        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(subGroupName);
        if (sameSubGroup.isPresent())
            throw new BadRequestException("Subgroup " + subGroupName + " already exists.");

        SubGroup newSubGroup = new SubGroup(subGroupName, group);
        newSubGroup = subGroupRepository.save(newSubGroup);

        List<SubGroupCharacteristic> subGroupChars = new ArrayList<>();
        int orderPositionCounter = 1;

        for (String charName : characteristics) {
            Optional<Characteristic> sameChar = charRepository.findByName(charName);
            Characteristic newChar;
            if (sameChar.isPresent()) {
                newChar = sameChar.get();
            } else {
                newChar = new Characteristic(charName);
                charRepository.save(newChar);
            }

            Long newSubGroupId = newSubGroup.getId();

            SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(newSubGroupId,
                    newChar.getId());

            Integer orderPosition = (!charName.equals("Name") && !charName.equals("Function") && !charName.equals("Length"))
                ? orderPositionCounter++
                : null;
            SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(newSubGroup, newChar, orderPosition);
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
     * Finds a subgroup by its name.
     *
     * @param name the name of the subgroup to find
     * @return the DTO representation of the found subgroup
     * @throws BadRequestException if the subgroup name is empty
     * @throws ResourceNotFoundException if no subgroup with the given name is found
     */
    public SubGroupDTO findSubGroup(String name) throws BadRequestException, ResourceNotFoundException {
        if(ObjectUtils.isEmpty(name))
            throw new BadRequestException("Subgroup name is required.");

        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(name);
        if (subgroupMaybe.isEmpty())
            throw new ResourceNotFoundException(name + " not found.");

        SubGroup subGroup = subgroupMaybe.get();
        subGroup.setInstrCount(nbInstrOfSubGroup(subGroup.getId()));
        SubGroupDTO subGroupDTO = SubGroupMapper.toDto(subGroup);
        return subGroupDTO;
    }

    /**
     * Finds a subgroup by its ID.
     * 
     * @param id
     * @return
     * @throws ResourceNotFoundException
     */
    public SubGroupDTO findSubGroupById(Long id) throws ResourceNotFoundException {
        Optional<SubGroup> subgroupMaybe = subGroupRepository.findById(id);
        if (subgroupMaybe.isEmpty())
            throw new ResourceNotFoundException("Subgroup not found.");

        SubGroup subGroup = subgroupMaybe.get();
        subGroup.setInstrCount(nbInstrOfSubGroup(subGroup.getId()));
        SubGroupDTO subGroupDTO = SubGroupMapper.toDto(subGroup);
        return subGroupDTO;
    }

    /**
     * Updates the name of an existing subgroup.
     *
     * @param subGroupName the current name of the subgroup to be updated.
     * @param body a map containing the new name for the subgroup.
     * @return the updated SubGroupDTO.
     * @throws BadRequestException if the subgroup name is empty, or if a subgroup with the new name already exists.
     * @throws ResourceNotFoundException if the subgroup with the given name is not found.
     */
    public SubGroupDTO updateSubGroup(String subGroupName, Map<String, Object> body) throws BadRequestException, ResourceNotFoundException {
        if(ObjectUtils.isEmpty(subGroupName))
            throw new BadRequestException("Subgroup name is required.");

        Optional<SubGroup> subgroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subgroupMaybe.isEmpty())
            throw new ResourceNotFoundException("Subgroup " + subGroupName + " not found.");

        SubGroup subgroup = subgroupMaybe.get();

        String name = (String) body.get("name");
        Optional<SubGroup> sameSubGroup = subGroupRepository.findByName(name);
        if (sameSubGroup.isPresent())
            throw new BadRequestException("Subgroup " + name + " already exists.");

        subgroup.setName(name);
        SubGroup savedSubGroup = subGroupRepository.save(subgroup);

        return SubGroupMapper.toDto(savedSubGroup);
    }


    /**
     * Deletes a sub-group by its name.
     *
     * @param subGroupName the name of the sub-group to be deleted
     * @return a message indicating the successful deletion of the sub-group
     * @throws ResourceNotFoundException if the sub-group with the given name is not found
     * @throws BadRequestException if the sub-group name is empty
     */
    public String deleteSubGroup(String subGroupName) throws ResourceNotFoundException, BadRequestException {
        if(ObjectUtils.isEmpty(subGroupName))
            throw new BadRequestException("Subgroup name is required.");

        Optional<SubGroup> subGroupMaybe = subGroupRepository.findByName(subGroupName);
        if (subGroupMaybe.isEmpty())
            throw new ResourceNotFoundException(subGroupName + " not found.");

        SubGroup subGroup = subGroupMaybe.get();

        Group group = subGroup.getGroup();
        List<SubGroup> subGroups = group.getSubGroups();
        subGroups.remove(subGroup);
        group.setSubGroups(subGroups);
        groupRepository.save(group);
        return "Successfully deleted group.";
    }


    /**
     * Sets the picture for a subgroup identified by its name.
     * If the subgroup already has a picture, the existing picture is deleted before storing the new one.
     *
     * @param subGroupName the name of the subgroup
     * @param picture the new picture to be set for the subgroup
     * @return a Data Transfer Object (DTO) representing the updated subgroup
     * @throws ResourceNotFoundException if the subgroup with the given name is not found
     */
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

    /**
     * Adds a new characteristic to an existing subgroup.
     *
     * If the characteristic already exists in the system, it will be reused.
     * A link between the subgroup and the characteristic is created and persisted.
     *
     * @param subGroupName the name of the subgroup to which the characteristic will be added
     * @param characteristicName the name of the characteristic to add
     * @return the updated SubGroupDTO with the new characteristic linked
     * @throws ResourceNotFoundException if the subgroup is not found
     * @throws BadRequestException if the subgroup name or characteristic name is missing or if the characteristic is already linked
     */
    public SubGroupDTO addCharacteristicToSubGroup(String subGroupName, String characteristicName)
            throws ResourceNotFoundException, BadRequestException {

        if (ObjectUtils.isEmpty(subGroupName)) {
            throw new BadRequestException("Subgroup name is required.");
        }

        if (ObjectUtils.isEmpty(characteristicName)) {
            throw new BadRequestException("Characteristic name is required.");
        }

        SubGroup subGroup = subGroupRepository.findByName(subGroupName)
                .orElseThrow(() -> new ResourceNotFoundException("Subgroup not found: " + subGroupName));

        Optional<Characteristic> existing = charRepository.findByName(characteristicName);
        Characteristic characteristic;

        if (existing.isPresent()) {
            characteristic = existing.get();
        } else {
            characteristic = new Characteristic(characteristicName);
            charRepository.save(characteristic);
        }

        SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(
            subGroup.getId(),
            characteristic.getId()
        );
        SubGroupCharacteristic subGroupChar = new SubGroupCharacteristic(subGroup, characteristic, null);
        subGroupChar.setId(key);

        subGroupCharRepository.save(subGroupChar);

        subGroup = subGroupRepository.findByName(subGroupName).get();
        return SubGroupMapper.toDto(subGroup);
    }

    /**
     * Changing the order of the characteristics of a subgroup
     * 
     * @param subGroupName subgroup in which we want to change order
     * @param newOrder new order for the characteristics
     * @return subgroup dto with new order
     * @throws ResourceNotFoundException
     * @throws BadRequestException
     */
    public SubGroupDTO updateCharacteristicOrder(String subGroupName, List<Map<String, Object>> newOrder)
        throws ResourceNotFoundException, BadRequestException {
    
        SubGroup subGroup = subGroupRepository.findByName(subGroupName)
            .orElseThrow(() -> new ResourceNotFoundException("SubGroup not found"));
    
        List<SubGroupCharacteristic> links = subGroupCharRepository.findBySubGroup(subGroup);
    
        for (Map<String, Object> entry : newOrder) {
            String name = (String) entry.get("name");
            Integer orderPosition = (Integer) entry.get("order_position");
    
            links.stream()
                .filter(link -> link.getCharacteristic().getName().equals(name))
                .findFirst()
                .ifPresent(link -> link.setOrderPosition(orderPosition));
        }
    
        subGroupCharRepository.saveAll(links);
        return SubGroupMapper.toDto(subGroup);
    }

     /**
     * Retrieves the number of instruments of a subgroup.
     * 
     * This method uses a specific function of the repository to fetch the number of instruments contained in the
     * categories related to the subgroup.
     * @param subGroupId the id of the subgroup whose instruments have to be counted.
     * @return an integer being the number of instruments.
     */
    public Integer nbInstrOfSubGroup(Long subGroupId){
        return subGroupRepository.nbInstrOfSubGroup(subGroupId);
    }

    /**
     * Removes a characteristic from a given subgroup.
     * This works whether the characteristic is ordered (in the form) or not.
     *
     * @param subGroupName the name of the subgroup
     * @param characteristicName the name of the characteristic to remove
     * @return the updated SubGroupDTO
     * @throws ResourceNotFoundException if the subgroup or characteristic is not found
     * @throws BadRequestException if the input is invalid
     */
    public SubGroupDTO removeCharacteristicFromSubGroup(String subGroupName, String characteristicName)
            throws ResourceNotFoundException, BadRequestException {

        if (ObjectUtils.isEmpty(subGroupName) || ObjectUtils.isEmpty(characteristicName)) {
            throw new BadRequestException("Subgroup name and characteristic name are required.");
        }

        SubGroup subGroup = subGroupRepository.findByName(subGroupName)
            .orElseThrow(() -> new ResourceNotFoundException("Subgroup not found: " + subGroupName));

        Characteristic characteristic = charRepository.findByName(characteristicName)
            .orElseThrow(() -> new ResourceNotFoundException("Characteristic not found: " + characteristicName));

        SubGroupCharacteristicKey key = new SubGroupCharacteristicKey(subGroup.getId(), characteristic.getId());

        SubGroupCharacteristic link = subGroupCharRepository.findById(key)
            .orElseThrow(() -> new ResourceNotFoundException("Characteristic not associated with this subgroup."));

        subGroupCharRepository.delete(link);

        // Reload and return updated DTO
        subGroup = subGroupRepository.findByName(subGroupName).get();
        return SubGroupMapper.toDto(subGroup);
    }
}
