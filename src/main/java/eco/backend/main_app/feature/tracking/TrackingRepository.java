package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrackingRepository extends JpaRepository<TrackingEntity, Long> {
    // Alle Ablesungen eines Users finden, sortiert nach Datum (neuester Eintrag zuerst)
    List<TrackingEntity> findByUserIdOrderByTimestampDesc(Long userId);

    // Alle Ablesungen eines Users finden, sortiert nach Datum (ältester Eintrag zuerst)
    List<TrackingEntity> findByUserIdOrderByTimestampAsc(Long userId);

    // Finde alle Einträge eines Users innerhalb eines Zeitraums (Start bis Ende),
    // sortiert vom neuesten zum ältesten Eintrag.
    List<TrackingEntity> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime start, LocalDateTime end);

    // Finde den neuesten Eintrag
    Optional<TrackingEntity> findFirstByUserIdOrderByTimestampDesc(Long userId);

    // Finde den direkten Vorgänger (Zeit <= neu, aber andere ID)
    Optional<TrackingEntity> findFirstByUserIdAndTimestampLessThanEqualAndIdNotOrderByTimestampDesc(
            Long userId, LocalDateTime timestamp, Long id);

    // Finde den direkten Nachfolger (Zeit >= neu, aber andere ID)
    Optional<TrackingEntity> findFirstByUserIdAndTimestampGreaterThanEqualAndIdNotOrderByTimestampAsc(
            Long userId, LocalDateTime timestamp, Long id);

    // Eintrag über Datum (Timestamp) finden
    Optional<TrackingEntity> findByUserIdAndTimestamp(Long userId, LocalDateTime timestamp);

    // Finde den (ersten) Eintrag eines Users an einem bestimmten Tag
    Optional<TrackingEntity> findFirstByUserIdAndTimestampBetween(
            Long userId, LocalDateTime start, LocalDateTime end
    );

    // Löscht alle Einträge einer bestimmten User-ID
    void deleteByUserId(Long userId);
}
