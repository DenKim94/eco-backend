package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.core.security.JwtService;
import eco.backend.main_app.feature.auth.dto.LoginRequest;
import eco.backend.main_app.feature.auth.dto.RegisterRequest;
import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // Dependency Injection via Constructor
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    /**
     * POST /api/auth/register
     * Body: { "username": "Max", "password": "123" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try{
            // 1. Service aufrufen
            authService.register(request.username(), request.password(), request.email());

            // 2. Erfolgsmeldung zurückgeben
            return ResponseEntity
                    .status(HttpStatus.CREATED) // 201 Created
                    .body(Map.of(
                            "message", "User successfully registered.",
                            "username", request.username()
                    ));
        } catch (Exception e) {
            throw new GenericException(
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        }

    }

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Authentifizierung durchführen und User laden
        UserEntity user = authService.authenticateUser(request);

        // JWT generieren
        String jwtToken = jwtService.getGeneratedToken(user);

        // Token zurückgeben
        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "expiresIn", jwtExpirationMs
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) { // User aus dem SecurityContext
        String username = authentication.getName();
        authService.logout(username);
        return ResponseEntity.ok(Map.of("message", "User " + username + " logged out successfully"));
    }
}
