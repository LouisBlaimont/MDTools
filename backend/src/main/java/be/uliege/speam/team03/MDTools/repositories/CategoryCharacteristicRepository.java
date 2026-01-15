package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.uliege.speam.team03.MDTools.compositeKeys.CategoryCharacteristicKey;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryCharacteristic;
import be.uliege.speam.team03.MDTools.models.Characteristic;

public interface CategoryCharacteristicRepository
      extends JpaRepository<CategoryCharacteristic, CategoryCharacteristicKey> {
      
      /**
       * Retrieves characteristics of a group based on the category ids
       * @param categoryIds
       * @return
       */
      @Query("SELECT cc FROM CategoryCharacteristic cc JOIN FETCH cc.category JOIN FETCH cc.characteristic WHERE cc.category.id IN :categoryIds ORDER BY cc.category.subGroup.name ASC, cc.category.id ASC")
      List<CategoryCharacteristic> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);

      /**
       * Retrieves a category characteristic object based on the category id
       * @param categoryId id of the category
       * @return characteristics 
       */
      List<CategoryCharacteristic> findByCategoryId(Long categoryId);

      List<CategoryCharacteristic> findByCharacteristicId(Long characteristicId);

      List<CategoryCharacteristic> findByCharacteristicAndCategoryIn(Characteristic characteristic, List<Category> categories);

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

      /**
       * Search categories based on various criteria using native SQL query.
       *
       * @param subGroupId  the ID of the sub-group to filter categories
       * @param function    the function to search for (nullable)
       * @param name        the name to search for (nullable)
       * @param minLength   the minimum length for length range search (nullable)
       * @param maxLength   the maximum length for length range search (nullable)
       * @param otherKeys   list of other characteristic keys to consider
       * @param otherValues list of other characteristic values to consider
       * @param useOther    flag indicating whether to use other keys/values in search
       * @param minMatches  minimum number of matching criteria required
       * @return list of category IDs matching the search criteria
       */
      @Query(value = """
      SELECT c.category_id
      FROM category c
      LEFT JOIN category_characteristic cc ON cc.category_id = c.category_id
      LEFT JOIN characteristic k ON k.characteristic_id = cc.characteristic_id
      WHERE c.sub_group_id = :subGroupId
      GROUP BY c.category_id
      HAVING
            /* Function */
            SUM(CASE WHEN (:function IS NOT NULL AND k.characteristic_name = 'Function'
                        AND cc.characteristic_value ILIKE CONCAT('%', :function, '%')) THEN 1 ELSE 0 END)
      +
            /* Name */
            SUM(CASE WHEN (:name IS NOT NULL AND k.characteristic_name = 'Name'
                        AND cc.characteristic_value ILIKE CONCAT('%', :name, '%')) THEN 1 ELSE 0 END)
      +
            /* Length */
            SUM(CASE WHEN (:minLength IS NOT NULL AND :maxLength IS NOT NULL
                        AND k.characteristic_name = 'Length'
                        AND cc.characteristic_value ~ '^[0-9]+(\\.[0-9]+)?$'
                        AND (cc.characteristic_value)::float BETWEEN :minLength AND :maxLength)
                  THEN 1 ELSE 0 END)
      +
            /* Other keys / values */
            SUM(CASE WHEN (:useOther = TRUE
                        AND k.characteristic_name IN (:otherKeys)
                        AND cc.characteristic_value IN (:otherValues)) THEN 1 ELSE 0 END)
      >= :minMatches
      """, nativeQuery = true)
      List<Long> searchCategoriesSQL(
      @Param("subGroupId") Long subGroupId,
      @Param("function") String function,
      @Param("name") String name,
      @Param("minLength") Double minLength,
      @Param("maxLength") Double maxLength,
      @Param("otherKeys") List<String> otherKeys,
      @Param("otherValues") List<String> otherValues,
      @Param("useOther") Boolean useOther,
      @Param("minMatches") Integer minMatches
      );


      @Query(value = """
      SELECT cc.category_id,
            ch.characteristic_name,
            cc.characteristic_value
      FROM category_characteristic cc
      JOIN characteristic ch ON ch.characteristic_id = cc.characteristic_id
      JOIN category cat ON cat.category_id = cc.category_id
      WHERE cat.sub_group_id = :subGroupId
      """, nativeQuery = true)
      List<Object[]> loadAllBySubGroup(@Param("subGroupId") Long subGroupId);


}