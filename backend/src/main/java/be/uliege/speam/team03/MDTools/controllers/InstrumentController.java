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
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.*;
import io.micrometer.common.lang.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import java.util.Map;

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
    public ResponseEntity<List<InstrumentDTO>> findallInstruments(){
        List<InstrumentDTO> instruments = instrumentService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Get an instrument by its ID.
     * 
     * @param id the ID of the instrument
     * @return the instrument with the specified ID, or a 404 status if no instrument is found
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentDTO> findInstrumentById(@PathVariable Long id) throws ResourceNotFoundException {
        InstrumentDTO instrument = instrumentService.findById(id);
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
    public ResponseEntity<InstrumentDTO> addInstrument(@NonNull @RequestBody InstrumentDTO newInstrument) throws BadRequestException {
        InstrumentDTO dto = instrumentService.addInstrument(newInstrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Update an instrument.
     * 
     * @param updatedInstrument the instrument to update
     * @return the updated instrument
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InstrumentDTO> updateInstrument(@PathVariable Long id, @RequestBody Map<String, Object> body) throws BadRequestException, ResourceNotFoundException{
        InstrumentDTO updatedInstrument = instrumentService.updateInstrument(body, id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedInstrument);
    }
    
    /**
     * Delete an instrument by its ID.
     * 
     * @param id the ID of the instrument
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrument(@PathVariable Long id) throws ResourceNotFoundException{
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

    /**
     * Retrieves all instruments belonging to a specific subgroup.
     *
     * @param subGroupName the name of the subgroup to find instruments for
     * @return a ResponseEntity containing a list of InstrumentDTO objects and HTTP status 200 (OK)
     */
    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<List<InstrumentDTO>> findInstrumentsBySubGroup(@PathVariable String subGroupName) {
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsBySubGroup(subGroupName);        
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Get all instruments by supplier name.
     *
     * @param supplierName the name of the supplier
     * @return a list of instruments associated with the specified supplier
     */
    @GetMapping("/supplier/{supplierName}")
    public ResponseEntity<List<InstrumentDTO>> findInstrumentsBySupplierName(@PathVariable String supplierName) {
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsBySupplierName(supplierName);
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Searches for instruments based on provided keywords.
     * 
     * This endpoint allows searching for instruments that match the given keywords.
     * The search is performed using the instrumentService.
     *
     * @param keywords a list of search terms to filter instruments
     * @return a ResponseEntity containing a list of matching InstrumentDTO objects
     *         - If keywords are null or empty, returns an empty list
     *         - Otherwise, returns all instruments matching the search criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<InstrumentDTO>> searchInstrument(
            @RequestParam(required = false) List<String> keywords) {
        
        if (keywords == null || keywords.stream().allMatch(k -> k == null || k.trim().isEmpty())) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<InstrumentDTO> instruments = instrumentService.searchInstrument(keywords);
        return ResponseEntity.ok(instruments);
    }


    /**
     * Retrieves a category based on its unique identifier.
     *
     * @param categoryId The unique identifier of the category to retrieve
     * @return ResponseEntity containing the CategoryDTO with HTTP status 200 (OK) if found
     * @throws ResourceNotFoundException If no category with the given ID exists
     */
    @GetMapping("/getCategory/{categoryId}")
    public ResponseEntity<CategoryDTO> searchCategory(@PathVariable Long categoryId) {
        CategoryDTO category = categoryService.searchCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }
}
