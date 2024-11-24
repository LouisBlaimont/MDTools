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

    @Column(name = "picture_path", nullable = false)
    private String picturePath;

    public InstrumentPictures() {}

    public InstrumentPictures(Instruments instrument, String picturePath) {
        this.instrument = instrument;
        this.picturePath = picturePath;
    }

    public Instruments getInstrument() {
        return this.instrument;
    }

    public void setInstrument(Instruments instrument) {
        this.instrument = instrument;
    }

    public String getPicturePath() {
        return this.picturePath;
    }
    public void setPath(String picture_path) {
        this.picturePath = picture_path;
    }
}
