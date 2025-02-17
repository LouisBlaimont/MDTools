package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.SubGroup;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {
    @Query("SELECT cc.val FROM CategoryCharacteristic cc " +
           "JOIN cc.characteristic c " +
           "WHERE cc.category.id = :categoryId AND c.name = :characteristicName")
    Optional<String> findCharacteristicVal(Long categoryId, String characteristicName);

    @Query("SELECT cc.valAbrev FROM CategoryCharacteristic cc " +
           "JOIN cc.characteristic c " +
           "WHERE cc.category.id = :categoryId AND c.name = :characteristicName")
    Optional<String> findCharacteristicValAbrv(Long categoryId, String characteristicName);
    
    Optional<List<Category>> findBySubGroup(SubGroup subGroup);
    Optional<List<Category>> findBySubGroupIn(List<SubGroup> subGroups);
    Optional<Category> findById(Integer category_id);
}
