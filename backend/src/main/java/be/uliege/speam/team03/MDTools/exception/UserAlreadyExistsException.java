package be.uliege.speam.team03.MDTools.exception;

public class UserAlreadyExistsException extends RuntimeException {
   public UserAlreadyExistsException(String message) {
      super(message);
   }

   public UserAlreadyExistsException(String message, Throwable cause) {
      super(message, cause);
   }

   public UserAlreadyExistsException(Throwable cause) {
      super(cause);
   }

   public UserAlreadyExistsException() {
      super();
   }
   
}
