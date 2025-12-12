package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.security.JwtService;
import eco.backend.main_app.feature.auth.dto.LoginRequest;
import eco.backend.main_app.feature.auth.dto.RegisterRequest;
import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final AuthenticationManager authenticationManager; // Spring Security Login-Manager
    private final JwtService jwtService;
    private final UserService userService;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // Dependency Injection via Constructor
    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserService userService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * POST /api/auth/register
     * Body: { "username": "Max", "password": "123" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        // 1. Service aufrufen
        authService.register(request.username(), request.password());

        // 2. Erfolgsmeldung zurückgeben
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(Map.of(
                        "message", "User successfully registered.",
                        "username", request.username()
                ));
    }

    /**
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Authentifizierung durchführen
        // AuthenticationManager prüft Username & Passwort gegen die DB.

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        // UserDetails laden, um Token zu generieren
        UserEntity user = userService.getUserByUsername(request.username());

        // JWT generieren
        String jwtToken = jwtService.getGeneratedToken(user);

        // Token zurückgeben
        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "expiresIn", jwtExpirationMs
        ));
    }
}
