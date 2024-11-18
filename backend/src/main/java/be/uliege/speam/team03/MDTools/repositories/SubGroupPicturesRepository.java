package be.uliege.speam.team03.MDTools.repositories;

import be.uliege.speam.team03.MDTools.models.SubGroupPictures;
import be.uliege.speam.team03.MDTools.models.SubGroups;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubGroupPicturesRepository extends CrudRepository<SubGroupPictures, Integer> {
    boolean existsBySubGroupAndPicturePath(SubGroups subGroup, String picturePath);
}