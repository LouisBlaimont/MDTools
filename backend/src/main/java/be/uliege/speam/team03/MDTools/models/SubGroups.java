package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="sub_group")

public class SubGroups {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    private Integer sub_group_id;

    private Integer group_id;
    private String shape;

    public SubGroups(){}

    public SubGroups(Integer groupId, String shape){
        this.group_id = groupId;
        this.shape = shape;
    }

    public Integer getId(){
        return this.sub_group_id;
    }
    public Integer getGroupId(){
        return this.group_id;
    }
    public String getShape(){
        return this.shape;
    }
    
}
