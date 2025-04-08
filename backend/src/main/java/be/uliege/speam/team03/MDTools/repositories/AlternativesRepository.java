package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import be.uliege.speam.team03.MDTools.compositeKeys.AlternativesKey;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;

import org.springframework.data.jpa.repository.Query;
import be.uliege.speam.team03.MDTools.models.Instruments;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlternativesRepository extends CrudRepository<Alternatives, AlternativesKey> {
    List<Alternatives> findById_InstrId1(Integer instrId);
    List<Alternatives> findById_InstrId2(Integer instrId);
    Optional<Alternatives> findByInstr1AndInstr2(Instruments instr1, Instruments insr2);

    @Query("""
        SELECT new be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO(a.instr1.reference, a.instr2.reference)
        FROM Alternatives a
    """)
    List<AlternativeReferenceDTO> findAllAlternativesReferences();
    @SuppressWarnings("null")
    boolean existsById(AlternativesKey id);
}
