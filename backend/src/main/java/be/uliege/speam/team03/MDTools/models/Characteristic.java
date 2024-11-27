package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
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
    private List<SubGroupCharacteristic> subGroupCharacteristics; 

    public Characteristic(String name){
        this.name = name;
    }
}
