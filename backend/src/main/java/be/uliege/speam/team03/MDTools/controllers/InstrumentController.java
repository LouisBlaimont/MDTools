package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.*;
import lombok.AllArgsConstructor;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.DTOs.SupplierDTO;
import be.uliege.speam.team03.MDTools.services.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private final CategoryService categoryService;


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
    @PostMapping
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
        if (newInstrument.getReference() == null || newInstrument.getReference().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reference is required to identify an instrument");
        }
        if(newInstrument.getSupplier() == null || newInstrument.getSupplier().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Supplier is required to identify an instrument");
        }
        if(newInstrument.getCategoryId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Category is required to identify an instrument");
        }
        if(newInstrument.getPrice() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Price is required to identify an instrument");
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
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateInstrument(@PathVariable Integer id, @RequestBody InstrumentDTO updatedInstrument) {
        InstrumentDTO existingInstrument = instrumentService.findById(id);
        // Check if the instrument exists
        if (existingInstrument == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instrument found with id: " + id);
        } 
        if (existingInstrument.getReference().equals(updatedInstrument.getReference()) && id != updatedInstrument.getId()) {
            Integer updatedInstrumentId = instrumentService.findByReference(updatedInstrument.getReference()).getId();
            if (updatedInstrumentId != id) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Instrument with this reference already exists for another instrument. Existing instrument id: " + updatedInstrumentId + " - Instrument being edited: " + id);
            }
        }
        if (updatedInstrument.getSupplier() != null) {
            existingInstrument.setSupplier(updatedInstrument.getSupplier());
        }
        if (updatedInstrument.getCategoryId() != null) {
            existingInstrument.setCategoryId(updatedInstrument.getCategoryId());
        }
        if (updatedInstrument.getReference() != null) {
            existingInstrument.setReference(updatedInstrument.getReference());
        }
        if (updatedInstrument.getSupplierDescription() != null) {
            existingInstrument.setSupplierDescription(updatedInstrument.getSupplierDescription());
        }
        if (updatedInstrument.getPrice() != null) {
            existingInstrument.setPrice(updatedInstrument.getPrice());
        }
        if (updatedInstrument.getObsolete()) {
            existingInstrument.setObsolete(updatedInstrument.getObsolete());
        }
        if (updatedInstrument.getPicturesId() != null) {
            existingInstrument.setPicturesId(updatedInstrument.getPicturesId());
        }
        InstrumentDTO savedInstrument = instrumentService.save(existingInstrument);
        return ResponseEntity.status(HttpStatus.OK).body(savedInstrument);
    }
    
    /**
     * Delete an instrument by its ID.
     * 
     * @param id the ID of the instrument
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrument(@PathVariable Integer id) {
        instrumentService.delete(id);
    }

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

    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<?> findInstrumentsBySubGroup(@PathVariable String subGroupName) {
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsBySubGroup(subGroupName);
        
        if (instruments == null || instruments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found for subgroup: " + subGroupName);
        }
        
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Get all instruments by supplier name.
     *
     * @param supplierName the name of the supplier
     * @return a list of instruments associated with the specified supplier
     */
    @GetMapping("/supplier/{supplierName}")
    public ResponseEntity<?> findInstrumentsBySupplierName(@PathVariable String supplierName) {
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsBySupplierName(supplierName);

        if (instruments == null || instruments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found for supplier: " + supplierName);
        }

        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    @GetMapping("/search")
    public ResponseEntity<List<InstrumentDTO>> searchInstrument(
            @RequestParam(required = false) List<String> keywords) {
        
        if (keywords == null || keywords.stream().allMatch(k -> k == null || k.trim().isEmpty())) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<InstrumentDTO> instruments = instrumentService.searchInstrument(keywords);
        return ResponseEntity.ok(instruments);
    }


    // getting the category from an instrument
    @GetMapping("/getCategory/{categoryId}")
    public ResponseEntity<CategoryDTO> searchCategory(@PathVariable Integer categoryId) {
        CategoryDTO category = categoryService.searchCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }
}

