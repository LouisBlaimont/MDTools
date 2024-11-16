package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "characteristic")
public class Characteristic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer characteristic_id;
    private String characteristic_name;

    public Characteristic(){}

    public Characteristic(String name){
        this.characteristic_name = name;
    }

    public Integer getId(){
        return this.characteristic_id;
    }
    public String getName(){
        return this.characteristic_name;
    }
}
