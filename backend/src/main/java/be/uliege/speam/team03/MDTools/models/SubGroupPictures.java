package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sub_group_pictures")
public class SubGroupPictures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sub_group_pictures_id;

    @ManyToOne
    @JoinColumn(name = "sub_group_id", nullable = false)
    private SubGroup subgroup;

    @Column(name = "picture_path", nullable = false)
    private String picturePath;

    public SubGroupPictures() {}

    public SubGroupPictures(SubGroup subgroup, String picturePath) {
        this.subgroup = subgroup;
        this.picturePath = picturePath;
    }

    public SubGroup getSubGroup() {
        return this.subgroup;
    }

    public void setSubGroup(SubGroup subgroup) {
        this.subgroup = subgroup;
    }

    public String getPicturePath() {
        return this.picturePath;
    }
    public void setPath(String picture_path) {
        this.picturePath = picture_path;
    }
}
