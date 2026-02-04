package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import be.uliege.speam.projections.CategoryFlatProjection;
import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.SubGroup;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

    /**
     * Retrieves the value of a specific characteristic for a given category.
     */
    @Query("""
        SELECT cc.val
        FROM CategoryCharacteristic cc
        JOIN cc.characteristic c
        WHERE cc.category.id = :categoryId
          AND c.name = :characteristicName
        """)
    Optional<String> findCharacteristicVal(
            @Param("categoryId") Long categoryId,
            @Param("characteristicName") String characteristicName
    );

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
    @NonNull
    @Override
    Optional<Category> findById(@NonNull Long category_id);

    @Query("""
        SELECT new be.uliege.speam.team03.MDTools.DTOs.CategoryDTO(
            c.id,
            g.name,
            sg.name,
            MAX(CASE WHEN ch.name = 'Code Externe' THEN cc.val END),
            MAX(CASE WHEN ch.name = 'Function' THEN cc.val END),
            MAX(CASE WHEN ch.name = 'Auteur' THEN cc.val END),
            MAX(CASE WHEN ch.name = 'Name' THEN cc.val END),
            MAX(CASE WHEN ch.name = 'Design' THEN cc.val END),
            c.shape,
            MAX(CASE WHEN ch.name = 'Dim orig' THEN cc.val END),
            MAX(CASE WHEN ch.name = 'Length' THEN cc.val END)
        )
        FROM Category c
        JOIN c.subGroup sg
        JOIN sg.group g
        LEFT JOIN c.categoryCharacteristic cc
        LEFT JOIN cc.characteristic ch
        WHERE (:groupId    IS NULL OR g.id = :groupId)
          AND (:subGroupId IS NULL OR sg.id = :subGroupId)
          AND (:ids        IS NULL OR c.id IN :ids)
        GROUP BY c.id, g.name, sg.name, c.shape
        ORDER BY sg.name, c.id
        """)
    List<CategoryDTO> findAllCategoriesFlat(
            @Param("groupId") Long groupId,
            @Param("subGroupId") Long subGroupId,
            @Param("ids") List<Long> ids
    );

    @Query(value = "SELECT COUNT(*) FROM category c WHERE c.sub_group_id = :subGroupId", nativeQuery = true)
    long countCategoriesBySubGroup(@Param("subGroupId") Long subGroupId);

    @Query(value = """
        SELECT
          c.category_id AS id,
          g.group_name AS groupName,
          sg.sub_group_name AS subGroupName,

          MAX(CASE WHEN ch.characteristic_name = 'Code Externe' THEN cc.characteristic_value END) AS externalCode,
          MAX(CASE WHEN ch.characteristic_name = 'Function' THEN cc.characteristic_value END)     AS function,
          MAX(CASE WHEN ch.characteristic_name = 'Auteur' THEN cc.characteristic_value END)      AS author,
          MAX(CASE WHEN ch.characteristic_name = 'Name' THEN cc.characteristic_value END)        AS name,
          MAX(CASE WHEN ch.characteristic_name = 'Design' THEN cc.characteristic_value END)      AS design,

          c.shape AS shape,

          MAX(CASE WHEN ch.characteristic_name = 'Dim orig' THEN cc.characteristic_value END)    AS dimOrig,
          MAX(CASE WHEN ch.characteristic_name = 'Length' THEN cc.characteristic_value END)      AS lenAbrv

        FROM category c
        JOIN sub_groups sg ON sg.sub_group_id = c.sub_group_id
        JOIN groups g ON g.group_id = sg.group_id
        LEFT JOIN category_characteristic cc ON cc.category_id = c.category_id
        LEFT JOIN characteristic ch ON ch.characteristic_id = cc.characteristic_id

        WHERE c.sub_group_id = :subGroupId
        GROUP BY c.category_id, g.group_name, sg.sub_group_name, c.shape

        ORDER BY
          CASE WHEN :sortColumn = 'sub_group_name' THEN sg.sub_group_name END ASC,
          CASE WHEN :sortColumn = 'external_code' THEN MAX(CASE WHEN ch.characteristic_name = 'Code Externe' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'function'      THEN MAX(CASE WHEN ch.characteristic_name = 'Function' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'author'        THEN MAX(CASE WHEN ch.characteristic_name = 'Auteur' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'name'          THEN MAX(CASE WHEN ch.characteristic_name = 'Name' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'design'        THEN MAX(CASE WHEN ch.characteristic_name = 'Design' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'dim_orig'      THEN MAX(CASE WHEN ch.characteristic_name = 'Dim orig' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'len_abrv'      THEN MAX(CASE WHEN ch.characteristic_name = 'Length' THEN cc.characteristic_value END) END ASC,
          CASE WHEN :sortColumn = 'shape'         THEN c.shape END ASC,
          c.category_id ASC

        LIMIT :limit
        OFFSET :offset
        """, nativeQuery = true)
    List<CategoryFlatProjection> findCategoriesFlatBySubGroupPagedAsc(
            @Param("subGroupId") Long subGroupId,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("sortColumn") String sortColumn
    );

    @Query(value = """
        SELECT
          c.category_id AS id,
          g.group_name AS groupName,
          sg.sub_group_name AS subGroupName,

          MAX(CASE WHEN ch.characteristic_name = 'Code Externe' THEN cc.characteristic_value END) AS externalCode,
          MAX(CASE WHEN ch.characteristic_name = 'Function' THEN cc.characteristic_value END)     AS function,
          MAX(CASE WHEN ch.characteristic_name = 'Auteur' THEN cc.characteristic_value END)      AS author,
          MAX(CASE WHEN ch.characteristic_name = 'Name' THEN cc.characteristic_value END)        AS name,
          MAX(CASE WHEN ch.characteristic_name = 'Design' THEN cc.characteristic_value END)      AS design,

          c.shape AS shape,

          MAX(CASE WHEN ch.characteristic_name = 'Dim orig' THEN cc.characteristic_value END)    AS dimOrig,
          MAX(CASE WHEN ch.characteristic_name = 'Length' THEN cc.characteristic_value END)      AS lenAbrv

        FROM category c
        JOIN sub_groups sg ON sg.sub_group_id = c.sub_group_id
        JOIN groups g ON g.group_id = sg.group_id
        LEFT JOIN category_characteristic cc ON cc.category_id = c.category_id
        LEFT JOIN characteristic ch ON ch.characteristic_id = cc.characteristic_id

        WHERE c.sub_group_id = :subGroupId
        GROUP BY c.category_id, g.group_name, sg.sub_group_name, c.shape

        ORDER BY
          CASE WHEN :sortColumn = 'sub_group_name' THEN sg.sub_group_name END DESC,
          CASE WHEN :sortColumn = 'external_code' THEN MAX(CASE WHEN ch.characteristic_name = 'Code Externe' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'function'      THEN MAX(CASE WHEN ch.characteristic_name = 'Function' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'author'        THEN MAX(CASE WHEN ch.characteristic_name = 'Auteur' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'name'          THEN MAX(CASE WHEN ch.characteristic_name = 'Name' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'design'        THEN MAX(CASE WHEN ch.characteristic_name = 'Design' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'dim_orig'      THEN MAX(CASE WHEN ch.characteristic_name = 'Dim orig' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'len_abrv'      THEN MAX(CASE WHEN ch.characteristic_name = 'Length' THEN cc.characteristic_value END) END DESC,
          CASE WHEN :sortColumn = 'shape'         THEN c.shape END DESC,
          c.category_id DESC

        LIMIT :limit
        OFFSET :offset
        """, nativeQuery = true)
    List<CategoryFlatProjection> findCategoriesFlatBySubGroupPagedDesc(
            @Param("subGroupId") Long subGroupId,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("sortColumn") String sortColumn
    );
}
