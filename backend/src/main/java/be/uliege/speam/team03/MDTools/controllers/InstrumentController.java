package be.uliege.speam.team03.MDTools.controllers;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.InstrumentDTO;
import be.uliege.speam.team03.MDTools.services.InstrumentService;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    // Endpoint to retrieve all instruments
    @GetMapping
    public ResponseEntity<List<InstrumentDTO>> getAllInstruments() {
        List<InstrumentDTO> instruments = instrumentService.getAllInstruments();
        return ResponseEntity.ok(instruments);
    }

    // Endpoint to retrieve photos by reference
    @GetMapping("/reference/{reference}/photos")
    public ResponseEntity<List<String>> getPhotosByInstrumentReferences(@PathVariable String reference) {
        List<String> photoPaths = instrumentService.getPhotosByReference(reference);

        if (photoPaths.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(photoPaths);
    }

    @GetMapping("/{reference}")
    public ResponseEntity<?> getPhotosByInstrumentReference(@PathVariable String reference) {
        List<String> photoPaths = instrumentService.getPhotosByReference(reference);

        if (photoPaths == null || photoPaths.isEmpty()) {
            return ResponseEntity.notFound().build(); // No photos found
        }

        try {
            for (String photoPath : photoPaths) {
                // Ensure the path is resolved correctly relative to /app/pictures
                Path filePath = Paths.get("/app", photoPath).normalize();
                Resource resource = new UrlResource(filePath.toUri());

                if (resource.exists() && resource.isReadable()) {
                    // Return the first valid photo found
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_PNG) // Set the correct content type
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                            .body(resource);
                } else {
                    // Log if a file is missing or unreadable
                    System.err.println("Photo not found or unreadable: " + filePath);
                }
            }
            // If no photos are found after checking all paths
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to load photos: " + e.getMessage());
        }
    }
}
