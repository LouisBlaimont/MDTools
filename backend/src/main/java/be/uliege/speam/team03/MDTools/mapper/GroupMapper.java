package be.uliege.speam.team03.MDTools.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import be.uliege.speam.team03.MDTools.DTOs.GroupDTO;
import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.mapper.SubGroupMapper;
import be.uliege.speam.team03.MDTools.models.Group;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupMapper {


    public static GroupDTO toDto(@NonNull Group group) {
        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setInstrCount(group.getInstrCount());
        dto.setPictureId(group.getPictureId());
        dto.setSubGroups(
            group.getSubGroups().stream()
                .map(SubGroupMapper::toDto)
                .collect(Collectors.toList())
        );

        return dto;
    }

    public Group toEntity(GroupDTO dto) {
        Group entity = new Group();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setInstrCount(dto.getInstrCount());
        entity.setPictureId(dto.getPictureId());
        List<SubGroup> subGroups = dto.getSubGroups().stream()
            .map(subGroupDTO -> SubGroupMapper.toEntity(subGroupDTO))
            .collect(Collectors.toList());
        entity.setSubGroups(subGroups);

        return entity;
    }
}
