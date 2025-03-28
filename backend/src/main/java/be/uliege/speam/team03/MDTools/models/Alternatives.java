package be.uliege.speam.team03.MDTools.models;

import be.uliege.speam.team03.MDTools.compositeKeys.AlternativesKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alternatives")
@NoArgsConstructor
@Getter
@Setter
public class Alternatives {
    @EmbeddedId
    AlternativesKey id;

    @ManyToOne
    @MapsId("instrId1")
    @JoinColumn(name="instruments_id_1", referencedColumnName = "instrument_id")
    Instruments instr1;

    @ManyToOne
    @MapsId("instrId2")
    @JoinColumn(name="instruments_id_2", referencedColumnName = "instrument_id")
    Instruments instr2;


    public Alternatives(Instruments instr1, Instruments instr2){
        this.instr1 = instr1;
        this.instr2 = instr2;
        this.id = new AlternativesKey(instr1.getId(), instr2.getId());
    }
}
