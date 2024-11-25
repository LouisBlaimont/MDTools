package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
    private List<SubGroupCharacteristic> subgroupCharacteristics; 

    public Characteristic(){}

    public Characteristic(String name){
        this.name = name;
    }

    public Integer getId(){
        return this.id;
    }
    public String getName(){
        return this.name;
    }
}
