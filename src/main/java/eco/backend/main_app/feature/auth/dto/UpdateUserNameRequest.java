package eco.backend.main_app.feature.auth.dto;

public record UpdateUserNameRequest(String email, String newUserName, String tfaCode) {
}
