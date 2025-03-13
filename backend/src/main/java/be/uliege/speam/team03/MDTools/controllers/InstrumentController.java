package be.uliege.speam.team03.MDTools.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import be.uliege.speam.team03.MDTools.models.Picture;
import be.uliege.speam.team03.MDTools.models.Instruments;
import be.uliege.speam.team03.MDTools.models.PictureType;
import be.uliege.speam.team03.MDTools.repositories.PictureRepository;
import be.uliege.speam.team03.MDTools.services.PictureStorageService;
import lombok.AllArgsConstructor;
import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.HttpStatus;

/**
 * This controller implements the API endpoints relative to the instruments. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@RestController
@RequestMapping("/api/instruments")
@AllArgsConstructor
public class InstrumentController {
    private final InstrumentService instrumentService;
    private final PictureRepository pictureRepository;
    private final PictureStorageService pictureStorageService;

    @GetMapping("/all")
    public ResponseEntity<?> findallInstruments(){
        List<InstrumentDTO> instruments = instrumentService.findAll();
        if (instruments == null || instruments.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instruments found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrumentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findInstrumentById(@PathVariable Integer id){
        InstrumentDTO instrument = instrumentService.findById(id);
        if (instrument == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No instrument found with id: " + id);
        }
        return ResponseEntity.status(HttpStatus.OK).body(instrument);
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<InstrumentDTO> updateInstrument(@PathVariable Integer id, @RequestBody InstrumentDTO updatedInstrument) {
    //     InstrumentDTO existingInstrument = instrumentService.findById(id);
    //     if (existingInstrument != null) {
    //         existingInstrument.setReference(updatedInstrument.getReference());
    //         existingInstrument.setSupplierDescription(updatedInstrument.getSupplierDescription());
    //         existingInstrument.setPrice(updatedInstrument.getPrice());
    //         existingInstrument.setObsolete(updatedInstrument.isObsolete());
    //         // ...update other fields as necessary...
    //         InstrumentDTO savedInstrument = instrumentService.save(existingInstrument);
    //         return ResponseEntity.status(HttpStatus.OK).body(savedInstrument);
    //     } else {
    //         return addInstrument(existingInstrument);
    //     }
    // }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<InstrumentDTO> addInstrument(@RequestBody InstrumentDTO newInstrument) {
        InstrumentDTO savedInstrument = instrumentService.save(newInstrument);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInstrument);
    }
    
    // @DeleteMapping("/delete/{id}")
    // @ResponseStatus(HttpStatus.NO_CONTENT)
    // public void deleteInstrument(@PathVariable Integer id) {
    //     instrumentService.delete(id);
    // }

    @GetMapping("/pictures/{instrumentId}")
    public ResponseEntity<List<Long>> getInstrumentPictureIds(@PathVariable(required = true) Long instrumentId){
        List<Picture> pictures = this.pictureRepository.findByReferenceIdAndPictureType(instrumentId, PictureType.INSTRUMENT);
        List<Long> pictureIds = new java.util.ArrayList<Long>();
        for (Picture picture : pictures) {
            pictureIds.add(picture.getId());
        }
        return ResponseEntity.ok(pictureIds);
    }

    @PostMapping("/pictures/{instrumentId}")
    public ResponseEntity<Void> addInstrumentPicture(@PathVariable(required = true) Long instrumentId,@RequestParam("file") MultipartFile file){
        // store picture
        pictureStorageService.storePicture(file, PictureType.INSTRUMENT, instrumentId);

        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<InstrumentDTO>> searchInstrument(@RequestParam String keyword) {
        List<InstrumentDTO> instruments = instrumentService.searchInstrument(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(instruments);
    }
}