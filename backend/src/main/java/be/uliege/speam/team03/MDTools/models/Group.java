package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name="groups")

public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="group_id")
    private Long id;
    
    @Column(name="group_name")
    private String name;

    @Transient
    private Integer instrCount = 0;

    @OneToMany(mappedBy = "group")
    private List<SubGroup> subGroups;

    @Column(name="picture_id", nullable = true)
    private Long pictureId;

    public Group(String groupName){
        this.name = groupName;
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
