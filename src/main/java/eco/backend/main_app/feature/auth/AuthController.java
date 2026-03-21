package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.core.security.JwtService;
import eco.backend.main_app.feature.auth.dto.*;
import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    // Dependency Injection via Constructor
    public AuthController(AuthService authService,
                          JwtService jwtService,
                          UserService userService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * POST /api/auth/register
     * Body: { "username": "Max", "password": "123" }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        try{
            // 1. Service aufrufen
            authService.register(request);

            // 2. Erfolgsmeldung zurückgeben
            return ResponseEntity
                    .status(HttpStatus.CREATED) // 201 Created
                    .body(Map.of(
                            "message", "Registrierung erfolgreich abgeschlossen.",
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
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // Authentifizierung durchführen und User laden
        UserEntity user = authService.authenticateUser(request);
        boolean isValid = userService.hasValidStatus(user);

        // JWT generieren
        String jwtToken = jwtService.getGeneratedToken(user);

        // Token zurückgeben
        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "expiresIn", jwtExpirationMs,
                "userName", user.getUsername(),
                "role", user.getRole(),
                "hasValidStatus", isValid
        ));
    }

    /**
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        UserEntity user = authService.updateTokenVersion(username);
        boolean isValid = userService.hasValidStatus(user);

        // JWT generieren
        String refreshedToken = jwtService.getGeneratedToken(user);

        // Refresh-Token zurückgeben
        return ResponseEntity.ok(Map.of(
                "token", refreshedToken,
                "expiresIn", jwtExpirationMs,
                "userName", user.getUsername(),
                "role", user.getRole(),
                "hasValidStatus", isValid
        ));
    }

    /**
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) { // User aus dem SecurityContext
        String username = authentication.getName();
        authService.updateTokenVersion(username);

        return ResponseEntity.ok(Map.of("message", "Account erfolgreich ausgeloggt."));
    }

    /**
     * DELETE /api/auth/delete-account
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteAccount(username);
        return ResponseEntity.ok(Map.of("message", "Profil von " + username + " wurde erfolgreich gelöscht."));
    }

    /**
     * POST /api/auth/verify-email
     * E-Mail des Users verifizieren
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody VerificationRequest dto) {

        authService.verifyEmailCode(userDetails.getUsername(), dto);
        return ResponseEntity.ok(Map.of("message", "Email wurde erfolgreich verifiziert."));
    }

    /**
     * POST /api/auth/resend-email
     * Einen neuen Bestätigungscode an den User senden
     */
    @PostMapping("/resend-email")
    public ResponseEntity<Map<String, String>> resendEmail(@AuthenticationPrincipal UserDetails userDetails) {

        authService.resendVerificationCode(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Email wurde erfolgreich gesendet."));
    }

    /**
     * POST /api/auth/user-password/request
     * Einen neuen Bestätigungscode an den User senden, um Passwort zu aktualisieren
     */
    @PostMapping("/user-password/request")
    public ResponseEntity<Map<String, String>> sendEmailForPasswordUpdate(@RequestBody PasswordUpdateRequest request) {

        authService.sendCodeForPasswordUpdate(request.email());
        return ResponseEntity.ok(Map.of("message", "Email wurde erfolgreich gesendet."));
    }

    /**
     * POST /api/auth/user-password/reset
     * TFA-Code prüfen und Passwort des Users aktualisieren
     */
    @PostMapping("/user-password/reset")
    public ResponseEntity<Map<String, String>> resetUserPassword(@RequestBody ResetPasswordRequest dto) {

        authService.resetUserPassword(dto);
        return ResponseEntity.ok(Map.of("message", "Passwort wurde erfolgreich aktualisiert."));
    }

    @GetMapping("/user/get-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        UserEntity user = userService.findUserByName(username);
        boolean isEnabled = user.getIsEnabled();
        boolean isValidatedEmail = user.getIsValidatedEmail();

        return ResponseEntity.ok(Map.of(
                "name", user.getUsername(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt(),
                "eMail", user.getEmail(),
                "isEnabled", isEnabled,
                "isValidatedEmail", isValidatedEmail
        ));
    }
}
