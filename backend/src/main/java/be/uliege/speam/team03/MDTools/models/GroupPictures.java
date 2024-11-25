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
@Table(name = "group_pictures")
public class GroupPictures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer group_pictures_id;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "picture_path", nullable = false)
    private String picturePath;

    public GroupPictures() {}

    public GroupPictures(Group group, String picturePath) {
        this.group = group;
        this.picturePath = picturePath;
    }

    public Group getGroup() {
        return this.group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getPicturePath() {
        return this.picturePath;
    }
    public void setPath(String picture_path) {
        this.picturePath = picture_path;
    }
}
