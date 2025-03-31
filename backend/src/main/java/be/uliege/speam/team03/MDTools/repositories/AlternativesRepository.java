package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;

import be.uliege.speam.team03.MDTools.compositeKeys.AlternativesKey;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlternativesRepository extends CrudRepository<Alternatives, AlternativesKey> {
    List<Alternatives> findById_InstrId1(Integer instrId);
    List<Alternatives> findById_InstrId2(Integer instrId);

    @Query("""
        SELECT new be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO(a.instr1.reference, a.instr2.reference)
        FROM Alternatives a
    """)
    List<AlternativeReferenceDTO> findAllAlternativesReferences();
}
