package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "sub_group_pictures")
public class SubGroupPictures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer photo_id;

    @ManyToOne
    @JoinColumn(name = "sub_group_id", nullable = false)
    private SubGroups subGroup;

    @Column(name = "picture_path", nullable = false)
    private String picturePath;

    public SubGroupPictures() {}

    public SubGroupPictures(SubGroups subGroup, String picturePath) {
        this.subGroup = subGroup;
        this.picturePath = picturePath;
    }

    public SubGroups getSubGroup() {
        return this.subGroup;
    }

    public String getPicturePath() {
        return this.picturePath;
    }
}
