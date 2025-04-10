package be.uliege.speam.team03.MDTools.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO;
import be.uliege.speam.team03.MDTools.compositeKeys.AlternativesKey;
import be.uliege.speam.team03.MDTools.models.Alternatives;
import be.uliege.speam.team03.MDTools.models.Instruments;

@Repository
public interface AlternativesRepository extends CrudRepository<Alternatives, AlternativesKey> {
    List<Alternatives> findById_InstrId1(Long instrId);
    List<Alternatives> findById_InstrId2(Long instrId);
    Optional<Alternatives> findByInstr1AndInstr2(Instruments instr1, Instruments insr2);

    @Query("""
        SELECT new be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO(a.instr1.reference, a.instr2.reference)
        FROM Alternatives a
    """)
    List<AlternativeReferenceDTO> findAllAlternativesReferences();
}
