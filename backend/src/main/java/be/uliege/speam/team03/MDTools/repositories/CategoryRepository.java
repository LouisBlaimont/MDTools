package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            "WHERE cc.category.id = :categoryId AND c.name = :characteristicName")
    Optional<String> findCharacteristicVal(Long categoryId, String characteristicName);

    /**
     * Retrieves a list of categories associated with the specified subgroup.
     *
     * @param subGroup the subgroup for which the categories are to be retrieved
     * @return an Optional containing a list of categories if found, or an empty
     *         Optional if no categories are associated with the given subgroup
     */
    Optional<List<Category>> findBySubGroup(SubGroup subGroup);

    /**
     * Retrieves a paginated list of categories associated with the specified
     * subgroup.
     *
     * @param subGroup the subgroup for which categories are to be retrieved
     * @param pageable the pagination information, including page number and size
     * @return a page containing the categories that belong to the specified
     *         subgroup
     */
    Page<Category> findBySubGroup(SubGroup subGroup, Pageable pageable);

    /**
     * Retrieves a list of categories that belong to any of the specified
     * sub-groups.
     *
     * @param subGroups the list of sub-groups to search for categories.
     * @return an Optional containing a list of categories that match the given
     *         sub-groups,
     *         or an empty Optional if no categories are found.
     */
    Optional<List<Category>> findBySubGroupIn(List<SubGroup> subGroups);

    /**
     * Retrieves a paginated list of categories that belong to any of the specified
     * sub-groups.
     *
     * @param subGroups the list of sub-groups to filter the categories by
     * @param pageable  the pagination information
     * @return a page of categories that are associated with the given sub-groups
     */
    Page<Category> findAllBySubGroupIn(List<SubGroup> subGroups, Pageable pageable);


    /**
     * Retrieves an optional Category entity by its unique identifier.
     *
     * @param category_id the unique identifier of the category to retrieve.
     * @return an Optional containing the Category if found, or an empty Optional if
     *         not found.
     */
    Optional<Category> findById(Integer category_id);
}
