package eco.backend.main_app.feature.auth.admin.dto;

public record ListUserRequest (Long id, String username, boolean isEnabled, String createdAt){ }
