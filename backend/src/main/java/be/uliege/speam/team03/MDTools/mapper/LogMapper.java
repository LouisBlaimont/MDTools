package be.uliege.speam.team03.MDTools.mapper;

import be.uliege.speam.team03.MDTools.DTOs.LogDto;
import be.uliege.speam.team03.MDTools.models.Log;
import be.uliege.speam.team03.MDTools.models.User;

public class LogMapper {
    public static LogDto mapToLogDto(Log log) {
        User user = log.getUser();
        return new LogDto(log.getLogId(), user.getUserId(), user.getUsername(), log.getAction(), log.getTimestamp());
    }

    public static Log mapToLog(LogDto logDto, User user) {
        return new Log(logDto.getLogId(), user, logDto.getAction(), logDto.getTimestamp());
    }
   
}
