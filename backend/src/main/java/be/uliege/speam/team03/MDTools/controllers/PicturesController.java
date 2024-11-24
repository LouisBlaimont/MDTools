package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.models.CategoryPictures;
import be.uliege.speam.team03.MDTools.models.InstrumentPictures;
import be.uliege.speam.team03.MDTools.repositories.CategoryPicturesRepository;
import be.uliege.speam.team03.MDTools.repositories.InstrumentPicturesRepository;
import be.uliege.speam.team03.MDTools.services.CategoryPictureService;
import be.uliege.speam.team03.MDTools.services.InstrumentPictureService;

@RestController
@RequestMapping("/pictures")
public class PicturesController {

    private final InstrumentPicturesRepository instrumentPicturesRepository;
    private final InstrumentPictureService instrumentPictureService;

    private final CategoryPicturesRepository categoryPicturesRepository;
    private final CategoryPictureService categoryPictureService;

    public PicturesController(
            InstrumentPicturesRepository instrumentPicturesRepository,
            InstrumentPictureService instrumentPictureService,
            CategoryPicturesRepository categoryPicturesRepository,
            CategoryPictureService categoryPictureService) {
        this.instrumentPicturesRepository = instrumentPicturesRepository;
        this.instrumentPictureService = instrumentPictureService;
        this.categoryPicturesRepository = categoryPicturesRepository;
        this.categoryPictureService = categoryPictureService;
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

    // Endpoint to fetch all category pictures
    @GetMapping("/category")
    public Iterable<CategoryPictures> findAllCategoryPictures() {
        return categoryPicturesRepository.findAll();
    }

    // Endpoint to process new category pictures
    @PostMapping("/category/process-new")
    public String processNewCategoryPictures() {
        try {
            categoryPictureService.processNewCategoryPictures();
            return "Category pictures processing complete!";
        } catch (Exception e) {
            return "Error occurred while processing category pictures: " + e.getMessage();
        }
    }
}
