package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sub_group")
public class SubGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_group_id")
    private Integer id;

    @Column(name = "sub_group_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @OneToMany(mappedBy = "subGroup")
    private List<SubGroupCharacteristic> subGroupCharacteristics;

    @Transient
    private Integer instrCount = 0;

    @OneToMany(mappedBy = "subGroup")
    private List<Category> categories;


    public SubGroup(String subGroupName, Group group) {
        this.name = subGroupName;
        this.group = group;
    }

    @PostLoad //happens each time data is fetched, replaced, .. in the DB so I must add cond so that it doesn't change the number when patch and get (post value is manually given)
    private void calculateInstrumentCount() {
        this.instrCount = 3;
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
