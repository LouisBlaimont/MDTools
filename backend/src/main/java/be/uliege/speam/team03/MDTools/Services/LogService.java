package be.uliege.speam.team03.MDTools.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import be.uliege.speam.team03.MDTools.DTOs.LogDto;
import be.uliege.speam.team03.MDTools.mapper.LogMapper;
import be.uliege.speam.team03.MDTools.models.Log;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.LogRepository;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LogService {
   private LogRepository logRepository;
   private UserRepository userRepository;

   public LogDto createLog(LogDto logDto) {
      User user = userRepository.findById(logDto.getUserId())
            .orElseThrow(() -> new RuntimeException("User does not exist. Received ID: " + logDto.getUserId()));
      Log log = LogMapper.mapToLog(logDto, user);
      Log savedLog = logRepository.save(log);
      return LogMapper.mapToLogDto(savedLog);
   }

   public LogDto getLogById(Long logId) {
      Log log = logRepository.findById(logId)
            .orElseThrow(() -> new RuntimeException("Log does not exist. Received ID: " + logId));
      return LogMapper.mapToLogDto(log);
   }

   public List<LogDto> getAllLogs() {
      List<Log> logs = logRepository.findAll();
      return logs.stream().map(LogMapper::mapToLogDto).collect(Collectors.toList());
   }
}
