package be.uliege.speam.team03.MDTools.exception;

public class ServerErrorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ServerErrorException(String message) {
        super(message);
    }

    public ServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerErrorException(Throwable cause) {
        super(cause);
    }
   
}
