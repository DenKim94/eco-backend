package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.admin.dto.ListUserRequest;
import eco.backend.main_app.feature.auth.model.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public UserEntity findUserByName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("Username not found.", HttpStatus.NOT_FOUND));
    }

    public UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new GenericException("User not found by ID.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void deleteUserById(Long userId) {

        if (isAdmin(userId)) {
            throw new GenericException("Admin can't be removed.", HttpStatus.FORBIDDEN);
        }

        if (!userRepository.existsById(userId)) {
            throw new GenericException("User not found by ID.", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        UserEntity user = findUserById(userId);

        if (isAdmin(userId) && !enabled) {
            throw new GenericException("Admin can't be disabled.", HttpStatus.FORBIDDEN);
        }

        user.setEnabled(enabled);
        userRepository.save(user);
    }

    private boolean isAdmin(Long userId) {
        UserEntity user = findUserById(userId);
        return "ADMIN".equals(user.getRole());
    }

    public List<ListUserRequest> getAllUserData() {
        return userRepository.findAllByRoleNot("ADMIN").stream()
                .map(user -> new ListUserRequest(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.isEnabled(),
                        user.getIsValidatedEmail(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    public void deleteAccount(String username) {
        UserEntity user = findUserByName(username);
        deleteUserById(user.getId());
    }
}
