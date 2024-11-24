package be.uliege.speam.team03.MDTools.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;


@ResponseStatus(HttpStatus.BAD_REQUEST) // 400
public class BadRequestException extends ResponseStatusException{
   private static final HttpStatus status = HttpStatus.BAD_REQUEST;

   public BadRequestException(String message) {
      super(status, message);
   }
}
