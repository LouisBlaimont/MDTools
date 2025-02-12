package be.uliege.speam.team03.MDTools.controllers;

import be.uliege.speam.team03.MDTools.DTOs.ImportRequestDTO;
import be.uliege.speam.team03.MDTools.services.ExcelImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
public class ExcelController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);
    private final ExcelImportService excelImportService;

    public ExcelController(ExcelImportService excelImportService) {
        this.excelImportService = excelImportService;
    }

    /**
     * Handles the "preflight" OPTIONS request for CORS.
     */
    @RequestMapping(value = "/excel", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        logger.info("✅ Preflight OPTIONS request received!");
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint to receive and process JSON data from Svelte.
     *
     * @param importRequest The JSON payload containing the import type and data.
     * @return HTTP response indicating the result of the import operation.
     */
    @PostMapping("/excel")
    public ResponseEntity<?> receiveJsonData(@RequestBody ImportRequestDTO importRequest) {
        if (importRequest == null || importRequest.getData().isEmpty()) {
            logger.warn("⚠ Received request with empty JSON payload!");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The received JSON is empty.");
        }

        logger.info("✅ JSON received with import type: {}, Group: {}, SubGroup: {}", 
                    importRequest.getImportType(), importRequest.getGroupName(), importRequest.getSubGroupName());

        try {
            excelImportService.processImport(importRequest);
            return ResponseEntity.ok("Data imported successfully!");
        } catch (Exception e) {
            logger.error("❌ Error while processing data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal error during import.");
        }
    }
}
