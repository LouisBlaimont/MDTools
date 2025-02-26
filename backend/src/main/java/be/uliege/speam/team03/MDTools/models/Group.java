package be.uliege.speam.team03.MDTools.models;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Entity
@Table(name="\"group\"")

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

    @PostLoad //happens each time data is fetched, replaced, .. in the DB so I must add cond so that it doesn't change the number when patch and get (post value is manually given)
    void calculateInstrumentCount() {
        this.instrCount = 3;
    }

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
