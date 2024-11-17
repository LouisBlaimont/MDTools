package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.Services.InstrumentPictureService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/instrument-pictures")
public class InstrumentPictureController {

    private final InstrumentPictureService instrumentPictureService;

    public InstrumentPictureController(InstrumentPictureService instrumentPictureService) {
        this.instrumentPictureService = instrumentPictureService;
    }

    // Endpoint pour déclencher le traitement des nouvelles photos
    @PostMapping("/process-new")
    public String processNewPictures() {
        try {
            instrumentPictureService.processNewInstrumentPictures();
            return "Processing complete!";
        } catch (Exception e) {
            return "Error occurred: " + e.getMessage();
        }
    }
}
