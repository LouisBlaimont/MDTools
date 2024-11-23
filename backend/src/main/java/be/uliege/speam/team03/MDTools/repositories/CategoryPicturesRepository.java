package be.uliege.speam.team03.MDTools.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.Category;
import be.uliege.speam.team03.MDTools.models.CategoryPictures;

@Repository
public interface CategoryPicturesRepository extends CrudRepository<CategoryPictures, Integer> {
    boolean existsByCategoryAndPicturePath(Category category, String picturePath);
}