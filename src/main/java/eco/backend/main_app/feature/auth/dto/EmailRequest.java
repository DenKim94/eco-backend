package eco.backend.main_app.feature.auth.dto;

public record EmailRequest(String toEmail, String genTfaCode) {
}
