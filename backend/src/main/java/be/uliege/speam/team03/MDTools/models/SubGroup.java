package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "sub_groups")
public class SubGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subGroupId;

    private String subGroupName;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Transient
    private Integer instrCount;

    @PostLoad //happens each time data is fetched, replaced, .. in the DB so I must add cond so that it doesn't change the number when patch and get (post value is manually given)
    private void calculateInstrumentCount() {
        this.instrCount = 3;
    }

    @OneToMany(mappedBy = "subGroup")
    private List<Category> categories;

    @OneToMany(mappedBy = "subgroup")
    private List<SubGroupCharacteristic> subgroupCharacteristics;
    public SubGroup() {}

    public SubGroup(String subGroupName, Group group) {
        this.subGroupName = subGroupName;
        this.group = group;
    }

    public Integer getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(Integer subGroupId) {
        this.subGroupId = subGroupId;
    }

    public String getName(){
        return this.subGroupName;
    } 
    public void setName(String name){
        this.subGroupName = name;
    } 

    public String getSubGroupName() {
        return subGroupName;
    }

    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
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
        return subgroupCharacteristics;
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
