package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.SubGroup;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {
    /**
     * Retrieves the value of a specific characteristic for a given category.
     *
     * @param categoryId         the ID of the category to search for.
     * @param characteristicName the name of the characteristic whose value is to be
     *                           retrieved.
     * @return an {@link Optional} containing the value of the characteristic if
     *         found, or an empty {@link Optional} if not found.
     */
    @Query("SELECT cc.val FROM CategoryCharacteristic cc " +
            "JOIN cc.characteristic c " +
            "WHERE cc.category.id = :categoryId AND c.name = :characteristicName ORDER BY cc.category.subGroup.name ASC, cc.category.id ASC")
    Optional<String> findCharacteristicVal(Long categoryId, String characteristicName);

    /**
     * Retrieves a list of categories associated with the specified
     * subgroup.
     *
     * @param subGroup the subgroup for which categories are to be retrieved
     * @param sort the sorting information
     * @return a list containing the categories that belong to the specified
     *         subgroup
     */
    List<Category> findBySubGroup(SubGroup subGroup, Sort sort);

    /**
     * Retrieves a list of categories that belong to any of the specified
     * sub-groups.
     *
     * @param subGroups the list of sub-groups to filter the categories by
     * @param sort the srting information
     * @return a list of categories that are associated with the given sub-groups
     */
    List<Category> findAllBySubGroupIn(List<SubGroup> subGroups, Sort sort);


    /**
     * Retrieves an optional Category entity by its unique identifier.
     *
     * @param category_id the unique identifier of the category to retrieve.
     * @return an Optional containing the Category if found, or an empty Optional if
     *         not found.
     */
    Optional<Category> findById(Integer category_id);
}
