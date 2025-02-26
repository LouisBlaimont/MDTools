package be.uliege.speam.team03.MDTools.mapper;

import java.util.List;

import be.uliege.speam.team03.MDTools.DTOs.SubGroupDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.SubGroup;
import be.uliege.speam.team03.MDTools.models.SubGroupCharacteristic;
import lombok.NonNull;

public class SubGroupMapper {

   private SubGroupMapper() {
   }

   static public SubGroupDTO toDto(@NonNull SubGroup subGroup) {
      SubGroupDTO dto = new SubGroupDTO();

      dto.setId(subGroup.getId());
      dto.setName(subGroup.getName());
      dto.setGroupId(subGroup.getGroup().getId());
      if (subGroup.getSubGroupCharacteristics() != null)
         dto.setSubGroupCharacteristics(
               subGroup.getSubGroupCharacteristics().stream()
                     .map(SubGroupCharacteristic::getCharacteristic)
                     .map(characteristic -> characteristic.getName()).toList());
      dto.setInstrCount(subGroup.getInstrCount());
      if (subGroup.getCategories() != null)
         dto.setCategoriesId(
               subGroup.getCategories().stream()
                     .map(category -> (long) category.getId()).toList());
      dto.setPictureId(subGroup.getPictureId());

      return dto;

   }

   /**
    * Converts a SubGroupDTO to a SubGroup entity.
    * 
    * @param dto the SubGroupDTO to convert
    * @return the SubGroup entity : characteristics are not set and only the ids of
    *         categories are set
    */
   static public SubGroup toEntity(@NonNull SubGroupDTO dto) {
      SubGroup entity = new SubGroup();

      entity.setId(dto.getId());
      entity.setName(dto.getName());
      entity.setInstrCount(dto.getInstrCount());
      entity.setPictureId(dto.getPictureId());

      List<Category> categories = dto.getCategoriesId().stream()
            .map(categoryId -> {
               Category category = new Category();
               category.setId(Long.valueOf(categoryId).intValue());
               return category;
            }).toList();
      entity.setCategories(categories);

      return entity;
   }
}
