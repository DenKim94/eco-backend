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
    public void sendVerificationEmail(String toEmail, String tfaCode, String text) {
        try {
            logger.debug("Sending E-Mail to {} ...", toEmail);
            SimpleMailMessage message = getSimpleMailMessage(toEmail, tfaCode, text);

            mailSender.send(message);
            logger.debug("E-Mail to {} has been sent.", toEmail);

        } catch (Exception e) {
            logger.error("Failed to send E-Mail to {}: {}", toEmail, e.getMessage());
        }
    }

    private SimpleMailMessage getSimpleMailMessage(String toEmail, String tfaCode, String text) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderAddress);
        message.setTo(toEmail);
        message.setSubject("ECO-App: Dein Verifizierungscode");
        message.setText(text.formatted(tfaCode));

        return message;
    }
}
