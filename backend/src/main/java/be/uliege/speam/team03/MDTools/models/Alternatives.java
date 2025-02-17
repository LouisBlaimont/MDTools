package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "alternatives")
@IdClass(AlternativesId.class)
public class Alternatives {
    @Id
    @Column(name="instruments_id_1")
    private Integer instrumentsId1;

    @Id
    @Column(name="instruments_id_2")
    private Integer instrumentsId2;

    public Alternatives() {}

    public Alternatives(Integer id1, Integer id2) {
        this.instrumentsId1 = id1;
        this.instrumentsId2 = id2;
    }

    public Integer getId1() {
        return this.instrumentsId1;
    }

    public Integer getId2() {
        return this.instrumentsId2;
    }
}
