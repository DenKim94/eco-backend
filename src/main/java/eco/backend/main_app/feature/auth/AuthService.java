package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.core.exception.GlobalExceptionHandler;
import eco.backend.main_app.feature.auth.dto.LoginRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.auth.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // Dependency Injection
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void register(String username, String rawPassword) {
        try {
            // 1. Prüfung: Gibt es den User schon?
            if (userRepository.existsByUsername(username)) {
                throw new GenericException(
                        "Provided username '" + username + "' already exists.",
                        HttpStatus.CONFLICT // Status 409
                );
            }

            // 2. HASHING
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // 3. Entity erstellen
            UserEntity newUser = new UserEntity(username, encodedPassword);

            // 4. Speichern
            userRepository.save(newUser);
        }
        catch( Exception e ){
            throw new GenericException(
                    "Failed to register user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR // Status 500
            );
        }
    }

    public UserEntity authenticateUser(LoginRequest request) {
        // AuthenticationManager prüft Username & Passwort gegen die DB
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        return (UserEntity) auth.getPrincipal();
    }
}
