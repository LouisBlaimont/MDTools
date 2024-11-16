package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="\"group\"")

public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer group_id;
    
    private String group_name;

    public Group(){}

    public Group(String group_name){
        this.group_name = group_name;
    }

    public Integer getId(){
        return this.group_id;
    }
    public String getName(){
        return this.group_name;
    }   
}

