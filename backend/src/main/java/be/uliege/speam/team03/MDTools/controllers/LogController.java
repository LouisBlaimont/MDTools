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

@AllArgsConstructor
@RestController
@RequestMapping(value = "/api/logs")
public class LogController {
   private final LogService logService;

   @PostMapping
   public ResponseEntity<LogDto> createLog(@RequestBody LogDto logDto) {
      LogDto savedLog = logService.createLog(logDto);
      return new ResponseEntity<>(savedLog, HttpStatus.CREATED);
   }

   @GetMapping("{logId}")
   public ResponseEntity<LogDto> getLog(@PathVariable(required = true) Long logId) {
         LogDto log = logService.getLogById(logId);
         return ResponseEntity.ok(log);
   }

   @GetMapping("/list")
   public ResponseEntity<List<LogDto>> getAllLogs() {
      return ResponseEntity.ok(logService.getAllLogs());
   }
}
