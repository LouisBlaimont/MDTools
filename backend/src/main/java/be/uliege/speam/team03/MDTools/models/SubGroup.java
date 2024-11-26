package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sub_group")
public class SubGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_group_id")
    private Integer id;

    @Column(name = "sub_group_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @OneToMany(mappedBy = "subgroup")
    private List<SubGroupCharacteristic> subGroupcharacteristics;

    @Transient
    private Integer instrCount;

    @OneToMany(mappedBy = "subGroup")
    private List<Category> categories;


    public SubGroup() {}

    public SubGroup(String subGroupName, Group group) {
        this.name = subGroupName;
        this.group = group;
    }

    @PostLoad //happens each time data is fetched, replaced, .. in the DB so I must add cond so that it doesn't change the number when patch and get (post value is manually given)
    private void calculateInstrumentCount() {
        this.instrCount = 3;
    }


    public Integer getId() {
        return id;
    }
    public void setId(Integer subGroupId) {
        this.id = subGroupId;
    }

    public String getName(){
        return this.name;
    } 
    public void setName(String name){
        this.name = name;
    } 

    public Group getGroup() {
        return group;
    }
    public void setGroup(Group group) {
        this.group = group;
    }

    public List<Category> getCategories() {
        return categories;
    }
    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<SubGroupCharacteristic> getSubGroupCharacteristics(){
        return subGroupcharacteristics;
    }
    public void setCharacteristics(List<SubGroupCharacteristic> characteristics){
        this.subGroupcharacteristics = characteristics;   
    }

    public int getInstrCount(){
        return instrCount;
    }
    public void setInstrCount(int count){
        instrCount = count;
    }
    public void incrInstrCount(){
        instrCount+=1;
    }
    public void decrInstrCount(){
        instrCount -=1;
        if (instrCount < 0){
            instrCount=0;
        }
    }

}
