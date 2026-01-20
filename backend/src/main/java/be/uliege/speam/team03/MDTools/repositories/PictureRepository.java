package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;

/**
 * Repository interface for managing Picture entities.
 * Extends JpaRepository to provide CRUD operations and additional query methods.
 * 
 * @see JpaRepository
 */
@Repository
public interface PictureRepository extends JpaRepository<Picture, Long> {
   /**
    * Retrieves a list of pictures based on the given reference ID and picture type.
    *
    * @param referenceId the ID of the reference to which the pictures belong
    * @param pictureType the type of the pictures to retrieve
    * @return a list of pictures that match the given reference ID and picture type
    */
   List<Picture> findByReferenceIdAndPictureType(Long referenceId, PictureType pictureType);

   @Query("""
      SELECT p FROM Picture p
      WHERE p.referenceId IN :ids
         AND p.pictureType = :type
   """)
   List<Picture> findByReferenceIdsAndPictureType(
      @Param("ids") List<Long> ids,
      @Param("type") PictureType type
   );

}
