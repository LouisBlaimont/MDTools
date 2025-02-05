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
@Table(name = "category_pictures")
public class CategoryPictures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer category_pictures_id;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "picture_path", nullable = false)
    private String picturePath;

    public CategoryPictures() {}

    public CategoryPictures(Category category, String picturePath) {
        this.category = category;
        this.picturePath = picturePath;
    }

    public Category getCategory() {
        return this.category;
    }

    public String getPicturePath() {
        return this.picturePath;
    }
}
