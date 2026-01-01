package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrackingRepository extends JpaRepository<TrackingEntity, Long> {
    // Alle Ablesungen eines Users finden, sortiert nach Datum (neueste zuerst)
    List<TrackingEntity> findByUserIdOrderByTimestampDesc(Long userId);
    Optional<TrackingEntity> findFirstByUserIdOrderByTimestampDesc(Long userId);
}
