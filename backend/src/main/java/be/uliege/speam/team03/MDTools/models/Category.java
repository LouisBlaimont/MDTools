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
    @JoinColumn(name = "group_id")
    private Group group;

    private String shape;

    public Category() {}

    public Category(Group group, String shape) {
        this.group = group;
        this.shape = shape;
    }

    public Integer getId(){
        return categoryId;
    }
    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group){
        this.group = group;
    }

    public String getShape() {
        return this.shape;
    }
    public void setShape(String shape){
        this.shape = shape;
    }
}
