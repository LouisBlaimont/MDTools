package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.repositories.InstrumentPicturesRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pictures")
public class InstrumentPicturesController {

    private final InstrumentPicturesRepository instrumentPicturesRepository;

    public InstrumentPicturesController(InstrumentPicturesRepository instrumentPicturesRepository) {
        this.instrumentPicturesRepository = instrumentPicturesRepository;
    }

    // Endpoint pour afficher toutes les images
    @GetMapping
    public Iterable<InstrumentPictures> findAllInstrumentPictures() {
        return instrumentPicturesRepository.findAll();
    }
}
