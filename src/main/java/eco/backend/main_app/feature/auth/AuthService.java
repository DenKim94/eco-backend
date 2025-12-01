package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.auth.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Dependency Injection
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(String username, String rawPassword) {
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
}
