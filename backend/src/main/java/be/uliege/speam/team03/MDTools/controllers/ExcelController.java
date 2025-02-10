package be.uliege.speam.team03.MDTools.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
public class ExcelController {

    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);

    public ExcelController() {
        // Constructor if needed later
    }

    /**
     * Gère la requête "preflight" OPTIONS pour le CORS.
     */
    @RequestMapping(value = "/excel", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        logger.info("✅ Pré-requête OPTIONS reçue !");
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint pour recevoir les données JSON depuis Svelte.
     */
    @PostMapping("/excel")
    public ResponseEntity<?> receiveRawJson(@RequestBody String rawJson) {
        if (rawJson == null || rawJson.isEmpty()) {
            logger.warn("⚠ Requête reçue avec un JSON vide !");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le JSON reçu est vide.");
        }

        // Afficher le JSON brut dans les logs
        logger.info("✅ JSON brut reçu avec succès : {}", rawJson);

        // Retourner une réponse de confirmation
        return ResponseEntity.ok("Données JSON reçues avec succès !");
    }
}
