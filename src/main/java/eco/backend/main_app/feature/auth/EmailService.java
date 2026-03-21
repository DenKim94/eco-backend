package eco.backend.main_app.feature.auth;

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
     * Sendet eine E-Mail über den konfigurierten SMTP Server.
     */
    @Async
    public void sendVerificationEmail(String name, String toEmail, String tfaCode, String text) {
        try {
            logger.debug("Sende E-Mail an {} ...", toEmail);
            SimpleMailMessage message = getSimpleMailMessage(name, toEmail, tfaCode, text);

            mailSender.send(message);
            logger.debug("E-Mail an {} wurde erfolgreich versendet.", toEmail);

        } catch (Exception e) {
            logger.error("Fehler beim Versenden der E-Mail an {}: {}", toEmail, e.getMessage());
        }
    }

    private SimpleMailMessage getSimpleMailMessage(String name, String toEmail, String tfaCode, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(toEmail);
        message.setSubject("Dein Verifizierungscode");
        message.setText(text.formatted(name, tfaCode));

        return message;
    }
}
