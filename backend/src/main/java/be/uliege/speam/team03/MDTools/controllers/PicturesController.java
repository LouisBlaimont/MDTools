package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.models.SubGroupPictures;
import be.uliege.speam.team03.MDTools.repositories.InstrumentPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.SubGroupPicturesRepository;
import be.uliege.speam.team03.MDTools.services.InstrumentPictureService;
import be.uliege.speam.team03.MDTools.services.SubGroupPictureService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pictures")
public class PicturesController {

    private final InstrumentPicturesRepository instrumentPicturesRepository;
    private final InstrumentPictureService instrumentPictureService;

    private final SubGroupPicturesRepository subGroupPicturesRepository;
    private final SubGroupPictureService subGroupPictureService;

    public PicturesController(
            InstrumentPicturesRepository instrumentPicturesRepository,
            InstrumentPictureService instrumentPictureService,
            SubGroupPicturesRepository subGroupPicturesRepository,
            SubGroupPictureService subGroupPictureService) {
        this.instrumentPicturesRepository = instrumentPicturesRepository;
        this.instrumentPictureService = instrumentPictureService;
        this.subGroupPicturesRepository = subGroupPicturesRepository;
        this.subGroupPictureService = subGroupPictureService;
    }

    // Endpoint to fetch all instrument pictures
    @GetMapping("/instruments")
    public Iterable<InstrumentPictures> findAllInstrumentPictures() {
        return instrumentPicturesRepository.findAll();
    }

    // Endpoint to process new instrument pictures
    @PostMapping("/instruments/process-new")
    public String processNewInstrumentPictures() {
        try {
            instrumentPictureService.processNewInstrumentPictures();
            return "Instrument pictures processing complete!";
        } catch (Exception e) {
            return "Error occurred while processing instrument pictures: " + e.getMessage();
        }
    }

    // Endpoint to fetch all sub-group pictures
    @GetMapping("/sub-groups")
    public Iterable<SubGroupPictures> findAllSubGroupPictures() {
        return subGroupPicturesRepository.findAll();
    }

    // Endpoint to process new sub-group pictures
    @PostMapping("/sub-groups/process-new")
    public String processNewSubGroupPictures() {
        try {
            subGroupPictureService.processNewSubGroupPictures();
            return "Sub-group pictures processing complete!";
        } catch (Exception e) {
            return "Error occurred while processing sub-group pictures: " + e.getMessage();
        }
    }
}
