package be.uliege.speam.team03.MDTools.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import be.uliege.speam.team03.MDTools.DTOs.LogDto;
import be.uliege.speam.team03.MDTools.exception.ResourceNotFoundException;
import be.uliege.speam.team03.MDTools.models.Log;
import be.uliege.speam.team03.MDTools.models.User;
import be.uliege.speam.team03.MDTools.repositories.LogRepository;
import be.uliege.speam.team03.MDTools.repositories.UserRepository;

public class LogServiceTest {

   @Mock
   private LogRepository logRepository;

   @Mock
   private UserRepository userRepository;

   @InjectMocks
   private LogService logService;

   @BeforeEach
   public void setUp() {
      MockitoAnnotations.openMocks(this);
   }

   @Test
   public void testCreateLog() {
      // Arrange
      Long userId = 1L;
      User user = new User();
      user.setUsername("Jonh Doe");
      LogDto logDto = new LogDto();
      logDto.setUserId(userId);
      logDto.setAction("Test log message");

      Timestamp timestamp = Timestamp.valueOf("2018-09-01 09:01:15");

      logDto.setTimestamp(timestamp.toString());

      Log savedLog = new Log(12L, user, "Test log message", timestamp);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(logRepository.save(any(Log.class))).thenReturn(savedLog);

      // Act
      LogDto result = logService.createLog(logDto);

      // Assert
      assertNotNull(result);
      assertNotNull(result.getLogId());
      assertEquals(savedLog.getAction(), result.getAction());
      assertEquals(savedLog.getUser().getUserId(), result.getUserId());

      verify(userRepository, times(1)).findById(userId);
      verify(logRepository, times(1)).save(any(Log.class));
   }

   @Test
   public void testCreateLog_UserNotFound() {
      // Arrange
      Long userId = 1L;
      LogDto logDto = new LogDto();
      logDto.setUserId(userId);

      when(userRepository.findById(userId)).thenReturn(Optional.empty());
      
      assertThrows(ResourceNotFoundException.class, () -> {
         logService.createLog(logDto);
      });

      verify(userRepository, times(1)).findById(userId);
      verify(logRepository, never()).save(any(Log.class));
   }

   @Test
   public void testGetLogById() {
      // Arrange
      Long logId = 1L;
      User user = new User();
      user.setUsername("John Doe");
      Timestamp timestamp = Timestamp.valueOf("2018-09-01 09:01:15");

      Log log = new Log(null, user, "test1", timestamp);

      when(logRepository.findById(logId)).thenReturn(Optional.of(log));

      // Act
      LogDto result = logService.getLogById(logId);

      // Assert
      assertNotNull(result);
      assertEquals(log.getLogId(), result.getLogId());
      assertEquals(log.getAction(), result.getAction());
      assertEquals(log.getUser().getUserId(), result.getUserId());
      assertEquals(log.getTimestamp().toString(), result.getTimestamp());

      verify(logRepository, times(1)).findById(logId);
   }

   @Test
   public void testGetLogById_LogNotFound() {
      // Arrange
      Long logId = 1L;

      when(logRepository.findById(logId)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> {
         logService.getLogById(logId);
      });

      verify(logRepository, times(1)).findById(logId);
   }

   @Test
   public void testGetAllLogs() {
      // Arrange
      User user = new User();
      user.setUsername("John Doe");
      Timestamp timestamp1 = Timestamp.valueOf("2018-09-01 09:01:15");
      Timestamp timestamp2 = Timestamp.valueOf("2019-10-02 10:02:25");
      Log log1 = new Log(null, user, "test1", timestamp1);
      Log log2 = new Log(null, user, "test2", timestamp2);
      List<Log> logs = Arrays.asList(log1, log2);

      when(logRepository.findAll()).thenReturn(logs);

      // Act
      List<LogDto> result = logService.getAllLogs();

      // Assert
      assertNotNull(result);
      assertEquals(2, result.size());

      LogDto logDto1 = result.get(0);
      assertEquals(log1.getLogId(), logDto1.getLogId());
      assertEquals(log1.getAction(), logDto1.getAction());
      assertEquals(log1.getUser().getUserId(), logDto1.getUserId());
      assertEquals(log1.getTimestamp().toString(), logDto1.getTimestamp());

      LogDto logDto2 = result.get(1);
      assertEquals(log2.getLogId(), logDto2.getLogId());
      assertEquals(log2.getAction(), logDto2.getAction());
      assertEquals(log2.getUser().getUserId(), logDto2.getUserId());
      assertEquals(log2.getTimestamp().toString(), logDto2.getTimestamp());
      verify(logRepository, times(1)).findAll();
   }
}