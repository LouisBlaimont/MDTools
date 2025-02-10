package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;

public interface CategoryCharacteristicRepository extends CrudRepository<CategoryCharacteristic, CategoryCharacteristicKey> {
    @Query("SELECT cc FROM CategoryCharacteristic cc JOIN FETCH cc.category JOIN FETCH cc.characteristic WHERE cc.category.id IN :categoryIds")
    List<CategoryCharacteristic> findByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);
}
