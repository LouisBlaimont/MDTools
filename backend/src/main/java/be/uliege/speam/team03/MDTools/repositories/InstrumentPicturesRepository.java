package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.models.Instruments;

@Repository
public interface InstrumentPicturesRepository extends CrudRepository<InstrumentPictures, Integer> {
    boolean existsByInstrumentAndPicturePath(Instruments instrument, String picturePath);
    
    // Custom query to find photos by instrument reference
    @Query("SELECT ip FROM InstrumentPictures ip JOIN ip.instrument i WHERE i.reference = :reference")
    List<InstrumentPictures> findByInstrumentReference(@Param("reference") String reference);
}