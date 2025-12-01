package eco.backend.main_app.core.exception;
import org.springframework.http.HttpStatus;

public class GenericException extends RuntimeException {
    private final HttpStatus status;

    public GenericException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
