package be.uliege.speam.team03.MDTools.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.uliege.speam.team03.MDTools.DTOs.LogDto;
import be.uliege.speam.team03.MDTools.services.LogService;
import lombok.AllArgsConstructor;

/**
 * This controller implements the API endpoints relative to the logs of the users. See the Wiki (>>2. Technical requirements>>API Specifications) for more information.
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/logs")
public class LogController {
   private final LogService logService;

   /**
    * Creates a new log entry in the system.
    *
    * @param logDto The Data Transfer Object containing the log information to be stored
    * @return ResponseEntity with the created LogDto and HTTP status 201 (Created)
    */
   @PostMapping
   public ResponseEntity<LogDto> createLog(@RequestBody LogDto logDto) {
      LogDto savedLog = logService.createLog(logDto);
      return ResponseEntity.status(HttpStatus.CREATED).body(savedLog);
   }

   /**
    * Retrieves a log entry by its ID.
    * 
    * @param logId The ID of the log entry to retrieve
    * @return ResponseEntity containing the LogDto if found
    * @throws ResourceNotFoundException if no log with the given ID exists
    */
   @GetMapping("{logId}")
   public ResponseEntity<LogDto> getLog(@PathVariable(required = true) Long logId) {
         LogDto log = logService.getLogById(logId);
         return ResponseEntity.ok(log);
   }

   /**
    * Retrieves all logs from the system.
    * 
    * @return ResponseEntity containing a List of LogDto objects representing all logs in the system with HTTP status 200 OK
    */
   @GetMapping("/list")
   public ResponseEntity<List<LogDto>> getAllLogs() {
      return ResponseEntity.ok(logService.getAllLogs());
   }
}
