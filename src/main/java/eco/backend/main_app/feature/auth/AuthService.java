package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.dto.LoginRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.auth.UserRepository;
import eco.backend.main_app.core.event.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher; // Neu

    @Value("${app.security.token.max-version}")
    private int maxTokenVersion;

    // Dependency Injection
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       ApplicationEventPublisher eventPublisher) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void register(String username, String rawPassword, String email) {

        // 1. Prüfung: Gibt es den User schon?
        if (userRepository.existsByUsername(username)) {
            throw new GenericException(
                    "Provided username '" + username + "' already exists.",
                    HttpStatus.CONFLICT // Status 409
            );
        }

        try {
            // 2. HASHING
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // 3. Entity erstellen
            UserEntity registeredUser = new UserEntity(username, encodedPassword, email);

            // 4. Speichern
            userRepository.save(registeredUser);

            // 5. Event auslösen
            eventPublisher.publishEvent(new UserRegisteredEvent(this, registeredUser));
        }
        catch( Exception e ){
            throw new GenericException(
                    "Failed to register user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR // Status 500
            );
        }
    }

    @Transactional
    public UserEntity authenticateUser(LoginRequest request) {
        // AuthenticationManager prüft Username & Passwort gegen die DB
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserEntity user = (UserEntity) auth.getPrincipal();

        int nextVersion = user.getTokenVersion() + 1;

        if (nextVersion > maxTokenVersion) {
            // Version zurücksetzen: Alte Tokens werden ungültig!
            user.resetTokenVersion();
        } else {
            // Version hochzählen: Alte Tokens werden ungültig!
            user.updateTokenVersion();
        }

        userRepository.save(user);

        return user;
    }

    @Transactional
    public void logout(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found", HttpStatus.NOT_FOUND));

        // Version hochzählen: Token wird ungültig
        user.updateTokenVersion();
        userRepository.save(user);
    }
}
