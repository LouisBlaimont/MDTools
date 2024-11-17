package be.uliege.speam.team03.MDTools.models;

import jakarta.persistence.*;

@Entity
@Table(name = "instrument_pictures")
public class InstrumentPictures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer photo_id;

    @ManyToOne
    @JoinColumn(name = "instrument_id", nullable = false)
    private Instruments instrument;

    private String picture_path;

    public InstrumentPictures() {}

    public InstrumentPictures(Instruments instrument, String picturePath) {
        this.instrument = instrument;
        this.picture_path = picturePath;
    }

    public Instruments getInstrument() {
        return this.instrument;
    }

    public String getPicturePath() {
        return this.picture_path;
    }
}
