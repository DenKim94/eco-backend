package eco.backend.main_app.feature.auth.admin.dto;

public record ListUserRequest (Long id, String userName,
                               String eMail, boolean isEnabledUser,
                               boolean isValidatedEmail, String createdAt){ }
