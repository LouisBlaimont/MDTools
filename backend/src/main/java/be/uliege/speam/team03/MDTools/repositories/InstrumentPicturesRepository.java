package be.uliege.speam.team03.MDTools.repositories;

import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.models.Instruments;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentPicturesRepository extends CrudRepository<InstrumentPictures, Integer> {
    boolean existsByInstrumentAndPicturePath(Instruments instrument, String picturePath);
}