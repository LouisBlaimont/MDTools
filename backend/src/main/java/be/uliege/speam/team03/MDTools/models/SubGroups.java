package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "sub_group")
public class SubGroups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sub_group_id;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private String shape;

    public SubGroups() {}

    public SubGroups(Group group, String shape) {
        this.group = group;
        this.shape = shape;
    }

    public Group getGroup() {
        return this.group;
    }

    public String getShape() {
        return this.shape;
    }
}
