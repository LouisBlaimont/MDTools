package be.uliege.speam.team03.MDTools.repositories;

import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstrumentPicturesRepository extends CrudRepository<InstrumentPictures, Integer> {
    // Les méthodes par défaut suffisent pour afficher tout
}
