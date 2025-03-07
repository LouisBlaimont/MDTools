package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.PictureRepository;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import lombok.AllArgsConstructor;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import be.uliege.speam.team03.MDTools.services.SupplierService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

/**
 * This controller implements the API endpoints relative to the instruments. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@RestController
@RequestMapping("/api/instrument")
@AllArgsConstructor
public class InstrumentController {
    private final InstrumentService instrumentService;
    private final SupplierService supplierService;
    private final PictureRepository pictureRepository;
    private final PictureStorageService pictureStorageService;

    /**
     * Get all instruments.
     * 
     * @return a list of all instruments, or a 404 status if no instruments are found
     */
    @GetMapping("/all")
    public ResponseEntity<?> findallInstruments(){
        List<InstrumentDTO> instruments = instrumentService.findAll();
        // Check if the list of instruments is empty
        if (instruments == null || instruments.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrumentService.findAll());
    }

    /**
     * Get an instrument by its ID.
     * 
     * @param id the ID of the instrument
     * @return the instrument with the specified ID, or a 404 status if no instrument is found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findInstrumentById(@PathVariable Integer id){
        InstrumentDTO instrument = instrumentService.findById(id);
        // Check if the instrument exists
        if (instrument == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instrument found with id: " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrument);
    }

    /**
     * Add a new instrument.
     * 
     * @param newInstrument the instrument to add
     * @return the added instrument
     */
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addInstrument(@RequestBody InstrumentDTO newInstrument) {
        // Check if the instrument already exists
        if (newInstrument.getId() != null) {
            InstrumentDTO instrument = instrumentService.findById(newInstrument.getId());
            if (instrument != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID already exists");
            }
        }
        List<InstrumentDTO> existingInstruments = instrumentService.findAll();
        for (InstrumentDTO instrument : existingInstruments) {
            // Check if the reference already exists
            if (instrument.getReference().equals(newInstrument.getReference())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Instrument with this reference already exists.\n");
            }
        }
        // Check if the reference is not empty
        if (newInstrument.getReference().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reference is required to identify an instrument");
        }
        // Check if the supplier exists
        SupplierDTO supplier = supplierService.findSupplierByName(newInstrument.getSupplier());
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Supplier does not exist. Please add the supplier first.");
        }
        InstrumentDTO savedInstrument = instrumentService.save(newInstrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInstrument);
    }

    /**
     * Update an instrument.
     * 
     * @param updatedInstrument the instrument to update
     * @return the updated instrument
     */
    @PatchMapping("/edit/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateInstrument(@RequestBody InstrumentDTO updatedInstrument) {
        // Check if the instrument can be identified
        if (updatedInstrument.getId() == null| updatedInstrument.getReference().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID or reference is required to identify an instrument");
        }
        InstrumentDTO instrument = (updatedInstrument.getId() != null) ? instrumentService.findById(updatedInstrument.getId()) : instrumentService.findByReference(updatedInstrument.getReference());
        // Check if the instrument exists
        if (instrument == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instrument found with id: " + updatedInstrument.getId());
        } else if (instrument.getReference().equals(updatedInstrument.getReference())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Instrument with this reference already exists.\n");
        } else if (instrument.getId() != updatedInstrument.getId()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ID already exists for another instrument");
        }
        InstrumentDTO savedInstrument = instrumentService.save(updatedInstrument);
        return ResponseEntity.status(HttpStatus.OK).body(savedInstrument);
    }
    
    // @DeleteMapping("/delete/{id}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // public void deleteInstrument(@PathVariable Integer id) {
    //     instrumentService.delete(id);
    // }

    /**
     * Get picture IDs for a specific instrument.
     * 
     * @param instrumentId the ID of the instrument
     * @return a list of picture IDs for the specified instrument
     */
    @GetMapping("/pictures/{instrumentId}")
    public ResponseEntity<List<Long>> getInstrumentPictureIds(@PathVariable(required = true) Long instrumentId){
        List<Picture> pictures = this.pictureRepository.findByReferenceIdAndPictureType(instrumentId, PictureType.INSTRUMENT);
        List<Long> pictureIds = new java.util.ArrayList<Long>();
        for (Picture picture : pictures) {
            pictureIds.add(picture.getId());
        }
        return ResponseEntity.ok(pictureIds);
    }

    /**
     * Add a picture to a specific instrument.
     * 
     * @param instrumentId the ID of the instrument
     * @param file the picture file to add
     * @return a no-content response
     */
    @PostMapping("/pictures/{instrumentId}")
    public ResponseEntity<Void> addInstrumentPicture(@PathVariable(required = true) Long instrumentId,@RequestParam("file") MultipartFile file){
        // store picture
        pictureStorageService.storePicture(file, PictureType.INSTRUMENT, instrumentId);

        return ResponseEntity.noContent().build();
    }
        
}