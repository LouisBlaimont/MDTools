package be.uliege.speam.team03.MDTools.DTOs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Flat DTO representation for Category.
 * Used both for search pivoted queries (JPQL constructor)
 * and for JSON serialization to the frontend.
 */
@NoArgsConstructor
@AllArgsConstructor // full constructor including picturesId (used by serialization only)
@Getter
@Setter
public class CategoryDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String groupName;
    private String subGroupName;

    private String externalCode;   // formerly Code Externe
    private String function;
    private String author;
    private String name;
    private String design;
    private String shape;

    private String dimOrig;        // Dim orig
    private String lenAbrv;        // formerly Length abbreviation

    private List<Long> picturesId; // enriched after pivot query

    /**
     * JPQL constructor used by findAllCategoriesFlat queries.
     * picturesId is not part of the SQL constructor and must be enriched afterwards.
     */
    public CategoryDTO(
            Long id,
            String groupName,
            String subGroupName,
            String externalCode,
            String function,
            String author,
            String name,
            String design,
            String shape,
            String dimOrig,
            String lenAbrv
    ) {
        this.id = id;
        this.groupName = groupName;
        this.subGroupName = subGroupName;
        this.externalCode = externalCode;
        this.function = function;
        this.author = author;
        this.name = name;
        this.design = design;
        this.shape = shape;
        this.dimOrig = dimOrig;
        this.lenAbrv = lenAbrv;
    }
}
