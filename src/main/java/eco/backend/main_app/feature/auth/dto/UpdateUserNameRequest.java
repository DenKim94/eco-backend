package eco.backend.main_app.feature.auth.dto;

public record UpdateUserNameRequest(String newUserName, String tfaCode) {
}
