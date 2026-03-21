package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.admin.dto.ListUserRequest;
import eco.backend.main_app.feature.auth.model.UserEntity;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Diese Methode wird von Spring Security aufgerufen beim Login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account nicht gefunden."));
    }

    // Hilfsmethode für eigene Services
    public UserEntity findUserByName(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("Account nicht gefunden.", HttpStatus.NOT_FOUND));
    }

    public UserEntity findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new GenericException("Account nicht gefunden.", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void deleteUserById(Long userId) {

        if (isAdmin(userId)) {
            throw new GenericException("Admin-Account kann nicht entfernt werden.", HttpStatus.FORBIDDEN);
        }

        if (!userRepository.existsById(userId)) {
            throw new GenericException("Account nicht gefunden.", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public void setUserEnabled(Long userId, boolean enabled) {
        UserEntity user = findUserById(userId);

        if (isAdmin(userId) && !enabled) {
            throw new GenericException("Admin-Account kann nicht deaktiviert werden.", HttpStatus.FORBIDDEN);
        }

        user.setEnabled(enabled);
        userRepository.save(user);
    }

    public boolean isAdmin(Long userId) {
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
        if(isAdmin(user.getId())){ throw new GenericException("Admin-Account kann nicht entfernt werden.", HttpStatus.FORBIDDEN); }
        deleteUserById(user.getId());
    }

    public boolean hasValidStatus(UserEntity user){
        logger.debug("Prüfe Accountstatus... ");
        return ((user.getIsEnabled() && user.getIsValidatedEmail()) || isAdmin(user.getId()));
    }
}
