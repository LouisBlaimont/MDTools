package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="category_id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sub_group_id", nullable = false) 
    private SubGroup subGroup; 

    @OneToMany(mappedBy = "category")
    private List<CategoryCharacteristic> categoryCharacteristic;

    @Column(name="shape")
    private String shape;

    @Column(name="picture_id", nullable = true)
    private Long pictureId;

    @Transient
    private String function;
    @Transient
    private String lenAbrv;

    public Category(SubGroup subGroup, String shape) { 
        this.subGroup = subGroup;
        this.shape = shape;
    }
    public Category(SubGroup subGroup){
        this.subGroup = subGroup;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public void setLenAbrv(String lenAbrv) {
        this.lenAbrv = lenAbrv;
    }
    public String getFunction() {
        return this.function;
    }
    public String getLenAbrv() {
        return this.lenAbrv;
    }
}