package be.uliege.speam.team03.MDTools.services;

import java.util.List;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.LogDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.mapper.LogMapper;
import be.uliege.speam.team03.MDTools.models.Log;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.LogRepository;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;

/**
 * Service class for managing logs.
 * This class provides methods to create, retrieve, and list logs.
 * It uses LogRepository and UserRepository for database operations.
 */
@Service
@AllArgsConstructor
public class LogService {
   private LogRepository logRepository;
   private UserRepository userRepository;

   /**
    * Creates a new log entry.
    *
    * @param logDto the data transfer object containing log details
    * @return the created log entry as a data transfer object
    * @throws ResourceNotFoundException if the user associated with the log does not exist
    */
   public LogDto createLog(LogDto logDto) throws ResourceNotFoundException{
      User user = userRepository.findById(logDto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User does not exist. Received ID: " + logDto.getUserId()));
      Log log = LogMapper.mapToLog(logDto, user);
      Log savedLog = logRepository.save(log);
      return LogMapper.mapToLogDto(savedLog);
   }

   /**
    * Retrieves a log entry by its ID.
    *
    * @param logId the ID of the log entry to retrieve
    * @return a LogDto object representing the log entry
    * @throws ResourceNotFoundException if no log entry with the specified ID is found
    */
   public LogDto getLogById(Long logId) throws ResourceNotFoundException {
      Log log = logRepository.findById(logId)
            .orElseThrow(() -> new ResourceNotFoundException("Log does not exist. Received ID: " + logId));
      return LogMapper.mapToLogDto(log);
   }

   /**
    * Retrieves all logs from the repository and maps them to LogDto objects.
    *
    * @return a list of LogDto objects representing all logs.
    */
   public List<LogDto> getAllLogs() {
      List<Log> logs = logRepository.findAll();
      return logs.stream().map(LogMapper::mapToLogDto).toList();
   }

   //TODO listing logs by filters
   //TODO add pageable to list logs
}
