package be.uliege.speam.team03.MDTools.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;


@ResponseStatus(HttpStatus.NOT_FOUND) // 400
public class ResourceNotFoundException extends ResponseStatusException{
   private static final HttpStatus status = HttpStatus.NOT_FOUND;

   public ResourceNotFoundException(String message) {
      super(status, message);
   }
}
