package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "characteristic")
public class Characteristic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="characteristic_id")
    private Integer id;

    @Column(name="characteristic_name")
    private String name;

    @OneToMany(mappedBy = "characteristic")
    private List<GroupCharacteristic> groupCharacteristics; 

    public Characteristic(){}

    public Characteristic(String name){
        this.name = name;
    }

    public Integer getId(){
        return this.id;
    }
    public void setId(Integer id){
        this.id = id;
    }
    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
}
