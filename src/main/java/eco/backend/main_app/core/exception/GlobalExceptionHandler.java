package eco.backend.main_app.core.exception;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(GenericException ex) {
        Map<String, Object> errorResponse = new HashMap<>();

        // Nimmt den Status direkt aus der Exception
        errorResponse.put("status", ex.getStatus().value());
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, Object>> handleAuthError() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");

        response.put("message", "Wrong username or password.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}