package eco.backend.main_app.feature.calculation;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.calculation.dto.CalculationRequestDto;
import eco.backend.main_app.feature.calculation.dto.CalculationResultsDto;
import eco.backend.main_app.feature.calculation.model.CalculationEntity;
import eco.backend.main_app.feature.configuration.ConfigService;
import eco.backend.main_app.feature.configuration.model.ConfigEntity;
import eco.backend.main_app.feature.tracking.TrackingRepository;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import eco.backend.main_app.utils.AppConstants;
import eco.backend.main_app.utils.ReuseHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CalculationService {
    private final TrackingRepository trackingRepository;
    private final UserService userService;
    private final ConfigService configService;
    private final CalculationRepository calculationRepository; // NEU

    public CalculationService(TrackingRepository trackingRepository,
                              UserService userService,
                              ConfigService configService,
                              CalculationRepository calculationRepository) {

        this.trackingRepository = trackingRepository;
        this.userService = userService;
        this.configService = configService;
        this.calculationRepository = calculationRepository;
    }

    /**
     * Berechnet die Kosten anhand der getrackten Daten.
     */
    public CalculationResultsDto runCalculation(String username, CalculationRequestDto dto) {
        UserEntity user = userService.findUserByName(username);
        LocalDateTime endDate = ReuseHelper.getParsedDateTime(dto.endDate());

        // User-Konfiguration laden
        ConfigEntity configData = configService.getConfigByUsername(user.getUsername());

        // Getrackte Daten laden (absteigend sortiert: neuester Eintrag zuerst)
        List<TrackingEntity> trackedData = trackingRepository.findByUserIdOrderByTimestampDesc(user.getId());

        // Validierung der Mindestanzahl an Datenpunkten
        if (trackedData.size() < AppConstants.MIN_DATA_POINTS) throw new GenericException("Not enough data points. At least " + AppConstants.MIN_DATA_POINTS + " are required.", HttpStatus.BAD_REQUEST);

        // Finde einen Eintrag anhand des Enddatums
        TrackingEntity currentEntry = trackedData.stream()
                .filter(e -> e.getTimestamp().toLocalDate().isEqual(endDate.toLocalDate()))
                .findFirst()
                .orElseThrow(() -> new GenericException("No entry found for the given end date.", HttpStatus.BAD_REQUEST));

        // Finde den ältesten Eintrag als Referenz
        TrackingEntity oldestEntry = trackedData.getLast();

        // TODO: Berechnungslogik
        Double absDiffTrackedValues = Math.abs(currentEntry.getReadingValue() - oldestEntry.getReadingValue());

        return null; // PLACEHOLDER; TODO: CalculationResultsDto ausgeben
    }

    /**
     * Ruft nur die gespeicherte Historie ab (ohne Neuberechnung).
     */
    public List<CalculationEntity> getHistory(String username) {
        UserEntity user = userService.findUserByName(username);
        return calculationRepository.findByUserIdOrderByPeriodEndDesc(user.getId());
    }
}
