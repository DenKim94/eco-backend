package eco.backend.main_app.feature.auth;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.admin.dto.UpdatePasswordRequest;
import eco.backend.main_app.feature.auth.dto.LoginRequest;
import eco.backend.main_app.feature.auth.dto.RegisterRequest;
import eco.backend.main_app.feature.auth.dto.ResetPasswordRequest;
import eco.backend.main_app.feature.auth.dto.VerificationRequest;
import eco.backend.main_app.utils.AppConstants;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.core.event.UserRegisteredEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.security.SecureRandom;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.token.max-version}")
    private int maxTokenVersion;

    // Dependency Injection
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       ApplicationEventPublisher eventPublisher,
                       EmailService emailService,
                       UserService userService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Transactional
    public void register(RegisterRequest dto) {
        logger.debug("Register new user ...");

        // 1. Prüfen, ob User bereits existiert
        if (userRepository.existsByUsername(dto.username())) {
            throw new GenericException(
                    "Provided username '" + dto.username() + "' already exists.",
                    HttpStatus.CONFLICT // Status 409
            );
        }

        try {
            String tfaCode = generateRandomCode();

            // 2. HASHING
            String encodedPassword = passwordEncoder.encode(dto.password());

            // 3. Entity erstellen
            UserEntity registeredUser = new UserEntity(dto.username(), encodedPassword, dto.email(), tfaCode);

            // 4. Speichern
            userRepository.save(registeredUser);

            // 5. Event auslösen
            eventPublisher.publishEvent(new UserRegisteredEvent(this, registeredUser));

            // 6. E-Mail senden
            emailService.sendVerificationEmail(dto.email(), tfaCode, AppConstants.TEXT_VERIFY_EMAIL);

            logger.debug("New user has been registered.");
        }
        catch( Exception e ){
            logger.error("Failed to register user: {}", e.getMessage());

            throw new GenericException(
                    "Failed to register user.",
                    HttpStatus.INTERNAL_SERVER_ERROR // Status 500
            );
        }
    }

    @Transactional
    public UserEntity authenticateUser(LoginRequest request) {

        logger.debug("Authenticate user ...");

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

        logger.debug("User authenticated successfully.");

        return user;
    }

    @Transactional
    public UserEntity updateTokenVersion(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));

        if(!userService.hasValidStatus(user)){
            throw new GenericException("Invalid account status.", HttpStatus.FORBIDDEN);
        }

        // Version hochzählen: Token wird ungültig
        user.updateTokenVersion();
        return userRepository.save(user);
    }


    /**
     * Fordert einen neuen Bestätigungscode an.
     * Setzt neuen Code in DB und sendet E-Mail erneut.
     */
    @Transactional
    public void resendVerificationCode(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));

        if (user.getIsValidatedEmail() ) {
            throw new GenericException("E-Mail already validated.", HttpStatus.CONFLICT);
        }

        if(!user.getIsEnabled()){ throw new GenericException("Account is disabled.", HttpStatus.FORBIDDEN); }

        // Neuen Code generieren und speichern
        String newCode = generateRandomCode();
        user.setTfaCode(newCode);
        userRepository.save(user);

        // Mail senden
        emailService.sendVerificationEmail(user.getEmail(), newCode, AppConstants.TEXT_VERIFY_EMAIL);
    }

    /**
     * Prüft den vom User eingegebenen Code gegen den in der DB gespeicherten.
     */
    @Transactional
    public void verifyEmailCode(String username, VerificationRequest dto) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));

        if(!user.getIsEnabled()){ throw new GenericException("Account is disabled.", HttpStatus.FORBIDDEN); }

        // Code Vergleich
        if (isInvalidTfaCode(user, dto.code())) {
            throw new GenericException("Invalid code provided.", HttpStatus.BAD_REQUEST);
        }

        // E-Mail als bestätigt markieren
        user.setIsValidatedEmail(true);

        // Code aus Sicherheitsgründen nach Bestätigung neu generieren
        user.setTfaCode(generateRandomCode());

        userRepository.save(user);
    }

    /**
     * Hilfsmethode: Generiert einen numerischen String der Länge 'maxStringLength'.
     * Z.B. maxStringLength=6: "482910"
     */
    private String generateRandomCode() {
        int maxStringLength = 10; // Anzahl maximaler Zeichen

        StringBuilder code = new StringBuilder();
        for (int i = 0; i < maxStringLength; i++) {
            code.append(secureRandom.nextInt(10)); // Ziffern 0-9
        }
        return code.toString();
    }

    public void resetUserPassword(String username, ResetPasswordRequest dto){
        logger.debug("User {} updates password ...", username);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));

        if (!userService.isAdmin(user.getId()) && (!user.getIsValidatedEmail() || !user.getIsEnabled())) {
            throw new GenericException("Invalid account status.", HttpStatus.FORBIDDEN);
        }

        // Code Vergleich
        if (isInvalidTfaCode(user, dto.tfaCode())) {
            throw new GenericException("Invalid code provided.", HttpStatus.BAD_REQUEST);
        }

        // Code aus Sicherheitsgründen nach Bestätigung neu generieren
        user.setTfaCode(generateRandomCode());

        // Neues Password setzen und speichern
        String encodedPassword = passwordEncoder.encode(dto.newPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        logger.debug("User password has been updated.");
    }

    private boolean isInvalidTfaCode(UserEntity user, String tfaCode){
        boolean isInvalid = false;

        if (user.getTfaCode() == null || user.getTfaCode().isBlank() || !user.getTfaCode().equals(tfaCode)) {
            logger.error("Invalid code provided.: {}", tfaCode);
            isInvalid = true;
        }

        return isInvalid;
    }

    /** Methode, um TFA-Code zum Passwort-Update via Mail an den User zu senden */
    @Transactional
    public void sendCodeForPasswordUpdate(String username) {
        logger.debug("Sending new code to reset password ...");

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));

        if(!userService.isAdmin(user.getId()) && (!user.getIsEnabled() || !user.getIsValidatedEmail())){ throw new GenericException("Invalid account status.", HttpStatus.FORBIDDEN); }

        // Neuen Code generieren und speichern
        String tfaCode = generateRandomCode();
        user.setTfaCode(tfaCode);
        userRepository.save(user);

        // Mail senden
        emailService.sendVerificationEmail(user.getEmail(), tfaCode, AppConstants.TEXT_RESET_PASSWORD);
    }

    @Transactional
    public void updateAdminPassword(String username, UpdatePasswordRequest dto){
        logger.debug("Update Admin password ...");

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new GenericException("User not found.", HttpStatus.NOT_FOUND));

        if(!userService.isAdmin(user.getId())){ throw new GenericException("Action not allowed.", HttpStatus.FORBIDDEN);}

        // Neues Password setzen und speichern
        String encodedPassword = passwordEncoder.encode(dto.newPassword());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        logger.debug("Admin password has been updated.");
    }

}
