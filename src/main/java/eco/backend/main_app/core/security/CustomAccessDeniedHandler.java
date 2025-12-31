package eco.backend.main_app.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        // Status Code setzen (403 Forbidden)
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Fehlermeldung als JSON-Antwort bauen
        Map<String, Object> body = new HashMap<>();
        body.put("status", 403);
        body.put("error", "Access Denied");
        body.put("message", "You do not have permission to access this endpoint.");

        // JSON schreiben (mit Jackson ObjectMapper)
        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}

