package eco.backend.main_app.core.event;

import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.context.ApplicationEvent;

public class UserRegisteredEvent extends ApplicationEvent {

    private final UserEntity user;

    public UserRegisteredEvent(Object source, UserEntity user) {
        super(source);
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }
}