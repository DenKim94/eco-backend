package eco.backend.main_app.core.exception;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.nio.file.AccessDeniedException;
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

    // Fall 1: User ist deaktiviert
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledException() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.FORBIDDEN.value()); // 403 passt hier oft besser als 401
        response.put("error", "Account is disabled.");
        response.put("message", "User has been disabled by admin.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // Fall 2: Falsches Passwort / Falscher Username
    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleAuthError() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Wrong username or password.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.FORBIDDEN.value()); // 403
        response.put("error", "Access Denied");
        response.put("message", "You do not have required permission to perform this action.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
}