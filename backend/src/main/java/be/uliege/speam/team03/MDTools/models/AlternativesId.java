package be.uliege.speam.team03.MDTools.models;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;

public class AlternativesId implements Serializable {
    @Column(name="instruments_id_1")
    private Integer instrumentsId1;

    @Column(name="instruments_id_2")
    private Integer instrumentsId2;

    public AlternativesId() {}

    public AlternativesId(Integer id1, Integer id2) {
        
        this.instrumentsId1 = id1;
        this.instrumentsId2 = id2;
    }

    public Integer getInstrumentsId1() {
        return instrumentsId1;
    }

    public Integer getInstrumentsId2() {
        return instrumentsId2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlternativesId that = (AlternativesId) o;
        return Objects.equals(instrumentsId1, that.instrumentsId1) &&
               Objects.equals(instrumentsId2, that.instrumentsId2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentsId1, instrumentsId2);
    }
}
