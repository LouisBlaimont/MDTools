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
    /**
     * Retrieves a list of alternatives based on an Id
     * @param instrId id of an instrument
     * @return list of alternatives of the instrument
     */
    List<Alternatives> findById_InstrId1(Long instrId);

    /**
     * Retrieves a list of alternatives based on an Id
     * @param instrId id of an instrument
     * @return list of alternatives of the instrument
     */
    List<Alternatives> findById_InstrId2(Long instrId);

    /**
     * Retrieves an alternatives based on 2 instruments
     * @param instr1 id of instrument 1
     * @param insr2 id of instrument 2
     * @return alternative object
     */
    Optional<Alternatives> findByInstr1AndInstr2(Instruments instr1, Instruments insr2);

    /**
     * Retrieves an alternatives based on 2 reference
     * @return reference of alternative
     */
    @Query("""
        SELECT new be.uliege.speam.team03.MDTools.DTOs.AlternativeReferenceDTO(a.instr1.reference, a.instr2.reference)
        FROM Alternatives a
    """)
    List<AlternativeReferenceDTO> findAllAlternativesReferences();

    /**
     * Return wheter an alternative exist based on an id
     */
    @SuppressWarnings("null")
    boolean existsById(AlternativesKey id);
}
