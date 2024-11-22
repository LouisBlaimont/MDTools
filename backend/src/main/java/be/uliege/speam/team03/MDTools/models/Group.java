package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name="\"group\"")

public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_id")
    private Integer id;
    
    @Column(name="group_name")
    private String name;

    @OneToMany(mappedBy = "group")
    private List<GroupCharacteristic> groupCharacteristics;

    @Transient
    private Integer instrCount;

    @PostLoad //happens each time data is fetched, replaced, .. in the DB so I must add cond so that it doesn't change the number when patch and get (post value is manually given)
    private void calculateInstrumentCount() {
        this.instrCount = 3;
    }

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
    public void setName(String name){
        this.name = name;
    }  
    public List<GroupCharacteristic> getGroupCharacteristics(){
        return groupCharacteristics;
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

