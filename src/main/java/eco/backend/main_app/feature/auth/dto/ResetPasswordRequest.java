package eco.backend.main_app.feature.auth.dto;

public record ResetPasswordRequest( String newPassword, String tfaCode ) {
}
