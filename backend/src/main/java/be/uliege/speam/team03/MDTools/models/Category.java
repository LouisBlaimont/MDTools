package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @ManyToOne
    @JoinColumn(name = "sub_group_id", nullable = false) // Updated to reference sub_group_id
    private SubGroup subGroup; // Updated to reference SubGroup instead of Group

    private String shape;

    public Category() {}

    public Category(SubGroup subGroup, String shape) { // Updated constructor
        this.subGroup = subGroup;
        this.shape = shape;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public SubGroup getSubGroup() {
        return subGroup;
    }

    public void setSubGroup(SubGroup subGroup) {
        this.subGroup = subGroup;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }
}
