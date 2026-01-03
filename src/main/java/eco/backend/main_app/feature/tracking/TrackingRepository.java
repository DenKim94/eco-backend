package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrackingRepository extends JpaRepository<TrackingEntity, Long> {
    // Alle Ablesungen eines Users finden, sortiert nach Datum (neueste zuerst)
    List<TrackingEntity> findByUserIdOrderByTimestampDesc(Long userId);

    // Finde den neuesten Eintrag
    Optional<TrackingEntity> findFirstByUserIdOrderByTimestampDesc(Long userId);

    // Finde den direkten Vorgänger (Zeit <= neu, aber andere ID)
    Optional<TrackingEntity> findFirstByUserIdAndTimestampLessThanEqualAndIdNotOrderByTimestampDesc(
            Long userId, LocalDateTime timestamp, Long id);

    // Finde den direkten Nachfolger (Zeit >= neu, aber andere ID)
    Optional<TrackingEntity> findFirstByUserIdAndTimestampGreaterThanEqualAndIdNotOrderByTimestampAsc(
            Long userId, LocalDateTime timestamp, Long id);
}
