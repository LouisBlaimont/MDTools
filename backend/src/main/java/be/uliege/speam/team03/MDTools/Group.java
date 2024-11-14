package be.uliege.speam.team03.MDTools;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="\"group\"")

public class Group {
    @Id
    private Integer group_id;
    private String group_name;

    public Group(){}

    public Group(Integer id, String group_name){
        this.group_name = group_name;
        this.group_id = id;
    }

    public Integer getId(){
        return this.group_id;
    }
    public String getName(){
        return this.group_name;
    }   
}

