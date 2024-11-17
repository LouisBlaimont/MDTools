package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="\"group\"")

public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="group_id")
    private Integer id;
    
    @Column(name="group_name")
    private String name;

    @OneToMany(mappedBy = "group")
    private List<GroupCharacteristic> groupCharacteristics;

    public Group(){}

    public Group(String groupName){
        this.name = groupName;
    }

    public Integer getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }   
    public List<GroupCharacteristic> getGroupCharacteristics(){
        return groupCharacteristics;
    }
}

