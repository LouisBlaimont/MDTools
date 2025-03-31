package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;

public interface CategoryCharacteristicRepository
      extends CrudRepository<CategoryCharacteristic, CategoryCharacteristicKey> {
   @Query("SELECT cc FROM CategoryCharacteristic cc JOIN FETCH cc.category JOIN FETCH cc.characteristic WHERE cc.category.id IN :categoryIds")
   List<CategoryCharacteristic> findByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);

   List<CategoryCharacteristic> findByCategoryId(Integer categoryId);

   List<CategoryCharacteristic> findByCharacteristicId(Integer characteristicId);

   // Get all CategoryCharacteristics that does not have an abbreviation in the
   // category_characteristic_abbreviation table
   @Query("SELECT DISTINCT val FROM CategoryCharacteristic cc WHERE cc.val \n" + //
         "NOT IN (SELECT value FROM CharacteristicValueAbbreviation)\n")
   List<String> findAllWithoutAbbreviation();

   // Get all CategoryCharacteristics that does not have an abbreviation in the
   // category_characteristic_abbreviation table
   @Query("SELECT DISTINCT val FROM CategoryCharacteristic cc WHERE cc.val \n" + //
         "NOT IN (SELECT value FROM CharacteristicValueAbbreviation)\n")
   List<String> findAllWithoutAbbreviation(Pageable pageable);
}