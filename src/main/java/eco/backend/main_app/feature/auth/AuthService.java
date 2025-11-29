package eco.backend.main_app.feature.auth;

import jakarta.transaction.Transactional;
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
        // 1. Validierung: Gibt es den User schon?
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken: " + username);
        }

        // 2. HASHING
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 3. Entity erstellen
        UserEntity newUser = new UserEntity(username, encodedPassword);

        // 4. Speichern
        userRepository.save(newUser);
    }
}
