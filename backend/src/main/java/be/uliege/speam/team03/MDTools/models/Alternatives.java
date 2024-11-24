package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "alternatives")
public class Alternatives {
    @Id
    private Integer instruments_id_1;
    @Id
    private Integer instruments_id_2;

    public Alternatives(){}

    public Alternatives(Integer id1, Integer id2){
        this.instruments_id_1 = id1;
        this.instruments_id_2 = id2;
    }

    public Integer getId1(){
        return this.instruments_id_1;
    }
    public Integer getId2(){
        return this.instruments_id_2;
    }
    
}
