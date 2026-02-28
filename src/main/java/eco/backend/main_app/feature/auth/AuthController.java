package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.core.security.JwtService;
import eco.backend.main_app.feature.auth.dto.LoginRequest;
import eco.backend.main_app.feature.auth.dto.RegisterRequest;
import eco.backend.main_app.feature.auth.dto.ResetPasswordRequest;
import eco.backend.main_app.feature.auth.dto.VerificationRequest;
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
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // Authentifizierung durchführen und User laden
        UserEntity user = authService.authenticateUser(request);

        // JWT generieren
        String jwtToken = jwtService.getGeneratedToken(user);

        // Token zurückgeben
        return ResponseEntity.ok(Map.of(
                "token", jwtToken,
                "expiresIn", jwtExpirationMs,
                "user", user.getUsername(),
                "role", user.getRole()
        ));
    }

    /**
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();

        UserEntity user = authService.updateTokenVersion(username);

        // JWT generieren
        String refreshedToken = jwtService.getGeneratedToken(user);

        // Refresh-Token zurückgeben
        return ResponseEntity.ok(Map.of(
                "token", refreshedToken,
                "expiresIn", jwtExpirationMs,
                "user", user.getUsername(),
                "role", user.getRole()
        ));
    }

    /**
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication authentication) { // User aus dem SecurityContext
        String username = authentication.getName();
        authService.updateTokenVersion(username);

        return ResponseEntity.ok(Map.of("message", "User " + username + " logged out successfully"));
    }

    /**
     * DELETE /api/auth/delete-account
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount(Authentication authentication) {
        String username = authentication.getName();
        userService.deleteAccount(username);
        return ResponseEntity.ok(Map.of("message", "Account of " + username + " has been deleted successfully."));
    }

    /**
     * POST /api/auth/verify-email
     * E-Mail des Users verifizieren
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody VerificationRequest dto) {

        authService.verifyEmailCode(userDetails.getUsername(), dto);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    /**
     * POST /api/auth/resend-email
     * Einen neuen Bestätigungscode an den User senden
     */
    @PostMapping("/resend-email")
    public ResponseEntity<Map<String, String>> resendEmail(@AuthenticationPrincipal UserDetails userDetails) {

        authService.resendVerificationCode(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Verification code has been sent successfully."));
    }

    /**
     * POST /api/auth/user-password/request
     * Einen neuen Bestätigungscode an den User senden, um Passwort zu aktualisieren
     */
    @PostMapping("/user-password/request")
    public ResponseEntity<Map<String, String>> sendEmailForPasswordUpdate(@AuthenticationPrincipal UserDetails userDetails) {

        authService.sendCodeForPasswordUpdate(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("message", "Verification code has been sent successfully."));
    }

    /**
     * POST /api/auth/user-password/reset
     * TFA-Code prüfen und Passwort des Users aktualisieren
     */
    @PostMapping("/user-password/reset")
    public ResponseEntity<Map<String, String>> resetUserPassword(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody ResetPasswordRequest dto) {

        authService.resetUserPassword(userDetails.getUsername(), dto);
        return ResponseEntity.ok(Map.of("message", "User password has been updated successfully."));
    }

    @GetMapping("/user/get-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails){
        String username = userDetails.getUsername();
        UserEntity user = userService.findUserByName(username);
        boolean isValid = userService.hasValidStatus(user);

        return ResponseEntity.ok(Map.of(
                "name", user.getUsername(),
                "role", user.getRole(),
                "id", user.getId(),
                "createdAt", user.getCreatedAt(),
                "eMail", user.getEmail(),
                "hasValidStatus", isValid
        ));
    }
}
