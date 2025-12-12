package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Diese Methode wird von Spring Security aufgerufen beim Login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    // Hilfsmethode für eigene Services
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));
    }
}
