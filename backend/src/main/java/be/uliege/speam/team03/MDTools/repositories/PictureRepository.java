package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;

@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
   List<Picture> findByReferenceIdAndPictureType(Long referenceId, PictureType pictureType);
}
