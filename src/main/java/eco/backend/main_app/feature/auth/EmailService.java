package eco.backend.main_app.feature.auth;

import eco.backend.main_app.utils.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    // Liest den Absender aus application.properties
    @Value("${app.email.sender}")
    private String senderAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sendet eine Verifizierungs E-Mail über den konfigurierten SMTP Server.
     */
    @Async
    public void sendVerificationEmail(String name, String toEmail, String tfaCode, String text) {
        try {
            logger.debug("Sende Verifizierungs-E-Mail an {} ...", toEmail);
            SimpleMailMessage message = getVerificationMailMessage(name, toEmail, tfaCode, text);

            mailSender.send(message);
            logger.debug("Verifizierungs-E-Mail an {} wurde erfolgreich gesendet.", toEmail);

        } catch (Exception e) {
            logger.error("Fehler beim Senden der Verifizierungs-E-Mail an {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendUserStatusEmail(String name, String toEmail, Boolean isEnabled) {
        try {
            logger.debug("Sende Status E-Mail an {} ...", toEmail);
            SimpleMailMessage message = getUserStatusMailMessage(name, toEmail, isEnabled ? "aktiviert" : "deaktiviert");

            mailSender.send(message);
            logger.debug("Status E-Mail an {} wurde erfolgreich gesendet.", toEmail);

        } catch (Exception e) {
            logger.error("Fehler beim Senden der Status E-Mail an {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendUserRemovedEmail(String name, String toEmail) {
        try {
            logger.debug("Sende E-Mail an {} ...", toEmail);
            SimpleMailMessage message = getUserRemovedMailMessage(name, toEmail);

            mailSender.send(message);
            logger.debug("E-Mail an {} wurde erfolgreich gesendet.", toEmail);

        } catch (Exception e) {
            logger.error("Fehler beim Senden der -Mail an {}: {}", toEmail, e.getMessage());
        }
    }

    private SimpleMailMessage getUserStatusMailMessage(String name, String toEmail, String status) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(toEmail);
        message.setSubject("Dein Profilstatus wurde durch den Admin geändert.");
        message.setText(AppConstants.TEXT_USER_STATUS_CHANGED_BY_ADMIN.formatted(name, status));

        return message;
    }

    private SimpleMailMessage getVerificationMailMessage(String name, String toEmail, String tfaCode, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(toEmail);
        message.setSubject("Dein Verifizierungscode");
        message.setText(text.formatted(name, tfaCode));

        return message;
    }

    private SimpleMailMessage getUserRemovedMailMessage(String name, String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(toEmail);
        message.setSubject("Dein Profil wurde durch den Admin gelöscht.");
        message.setText(AppConstants.TEXT_USER_REMOVED_BY_ADMIN.formatted(name));

        return message;
    }
}
