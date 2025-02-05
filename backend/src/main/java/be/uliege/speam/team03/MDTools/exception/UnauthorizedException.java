package be.uliege.speam.team03.MDTools.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;


@ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
public class UnauthorizedException extends ResponseStatusException{
   private static final HttpStatus status = HttpStatus.UNAUTHORIZED;

   public UnauthorizedException(String message) {
      super(status, message);
   }
}
