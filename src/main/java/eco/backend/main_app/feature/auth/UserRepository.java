package eco.backend.main_app.feature.auth;

import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    // SQL: SELECT * FROM users WHERE role != 'ADMIN'
    List<UserEntity> findAllByRoleNot(String role);
}
