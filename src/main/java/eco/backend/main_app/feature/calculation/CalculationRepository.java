package eco.backend.main_app.feature.calculation;

import eco.backend.main_app.feature.calculation.model.CalculationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalculationRepository extends JpaRepository<CalculationEntity, Long> {
    // Historie abrufen (neueste zuerst)
    List<CalculationEntity> findByUserIdOrderByPeriodEndDesc(Long userId);

    // Optional; Suche nach exaktem Eintrag (User + End-Zeitpunkt)
    Optional<CalculationEntity> findByUserIdAndPeriodEnd(Long userId, LocalDateTime periodEnd);
}
