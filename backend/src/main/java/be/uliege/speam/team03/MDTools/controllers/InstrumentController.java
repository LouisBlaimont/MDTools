package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.*;
import lombok.AllArgsConstructor;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.DTOs.CategoryDTO;
import be.uliege.speam.team03.MDTools.exception.BadRequestException;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.services.*;
import io.micrometer.common.lang.NonNull;

import java.util.Collections;
import java.util.List;

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
    private final PictureRepository pictureRepository;
    private final PictureStorageService pictureStorageService;
    private final CategoryService categoryService;


    /**
     * Retrieves all instruments.
     * 
     * @return a ResponseEntity containing a list of all instruments with HTTP status 200 (OK)
     */
    @GetMapping("/all")
    public ResponseEntity<List<InstrumentDTO>> findallInstruments(){
        List<InstrumentDTO> instruments = instrumentService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Retrieves an instrument by its ID.
     * 
     * @param id the ID of the instrument
     * @return a ResponseEntity containing the instrument with the specified ID and HTTP status 200 (OK)
     * @throws ResourceNotFoundException if no instrument is found with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<InstrumentDTO> findInstrumentById(@PathVariable Long id) throws ResourceNotFoundException {
        InstrumentDTO instrument = instrumentService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(instrument);
    }

    /**
     * Creates a new instrument.
     * 
     * @param newInstrument the instrument to create
     * @return a ResponseEntity containing the created instrument with HTTP status 201 (Created)
     * @throws BadRequestException if the provided instrument data is invalid
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<InstrumentDTO> addInstrument(@NonNull @RequestBody InstrumentDTO newInstrument) throws BadRequestException {
        InstrumentDTO dto = instrumentService.addInstrument(newInstrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Updates an existing instrument.
     * 
     * @param id the ID of the instrument to update
     * @param body a map containing the fields to update
     * @return a ResponseEntity containing the updated instrument with HTTP status 200 (OK)
     * @throws BadRequestException if the update data is invalid
     * @throws ResourceNotFoundException if no instrument is found with the given ID
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InstrumentDTO> updateInstrument(@PathVariable Long id, @RequestBody Map<String, Object> body) throws BadRequestException, ResourceNotFoundException{
        InstrumentDTO updatedInstrument = instrumentService.updateInstrument(body, id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedInstrument);
    }
    
    /**
     * Deletes an instrument by its ID.
     * 
     * @param id the ID of the instrument to delete
     * @throws ResourceNotFoundException if no instrument is found with the given ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInstrument(@PathVariable Long id) throws ResourceNotFoundException{
        instrumentService.delete(id);
    }

    /**
     * Retrieves picture IDs associated with a specific instrument.
     * 
     * @param instrumentId the ID of the instrument
     * @return a ResponseEntity containing a list of picture IDs with HTTP status 200 (OK)
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
     * Adds a picture to a specific instrument.
     * 
     * @param instrumentId the ID of the instrument
     * @param file the picture file to add
     * @return a ResponseEntity with HTTP status 204 (No Content)
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
     * @param subGroupName the name of the subgroup
     * @return a ResponseEntity containing a list of instruments with HTTP status 200 (OK)
     */
    @GetMapping("/subgroup/{subGroupName}")
    public ResponseEntity<List<InstrumentDTO>> findInstrumentsBySubGroup(@PathVariable String subGroupName) {
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsBySubGroup(subGroupName);        
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Retrieves all instruments associated with a specific supplier.
     *
     * @param supplierName the name of the supplier
     * @return a ResponseEntity containing a list of instruments with HTTP status 200 (OK)
     */
    @GetMapping("/supplier/{supplierName}")
    public ResponseEntity<List<InstrumentDTO>> findInstrumentsBySupplierName(@PathVariable String supplierName) {
        List<InstrumentDTO> instruments = instrumentService.findInstrumentsBySupplierName(supplierName);
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }

    /**
     * Searches for instruments based on provided keywords.
     * 
     * @param keywords a list of search terms
     * @return a ResponseEntity containing a list of matching instruments with HTTP status 200 (OK)
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
     * Retrieves a category by its ID.
     *
     * @param categoryId the ID of the category
     * @return a ResponseEntity containing the category with HTTP status 200 (OK)
     */
    @GetMapping("/getCategory/{categoryId}")
    public ResponseEntity<CategoryDTO> searchCategory(@PathVariable Long categoryId) {
        CategoryDTO category = categoryService.searchCategory(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(category);
    }
}
