package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sub_groups")
public class SubGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_group_id")
    private Long id;

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

    @Column(name = "picture_id")
    private Long pictureId;


    public SubGroup(String subGroupName, Group group) {
        this.name = subGroupName;
        this.group = group;
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
