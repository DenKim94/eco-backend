package eco.backend.main_app.feature.configuration;

import eco.backend.main_app.feature.configuration.model.ConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigRepository extends JpaRepository<ConfigEntity, Long> {
    // Finde Config anhand der User-ID
    Optional<ConfigEntity> findByUserId(Long userId);
}
