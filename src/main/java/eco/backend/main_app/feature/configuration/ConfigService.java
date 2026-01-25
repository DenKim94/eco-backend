package eco.backend.main_app.feature.configuration;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.configuration.dto.ConfigDto;
import eco.backend.main_app.feature.configuration.model.ConfigEntity;
import eco.backend.main_app.core.event.UserRegisteredEvent;
import eco.backend.main_app.feature.tracking.TrackingRepository;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import eco.backend.main_app.utils.ReuseHelper;
import org.springframework.context.event.EventListener;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class ConfigService {
    private final ConfigRepository configRepository;
    private final UserService userService;
    private final TrackingRepository trackingRepository;

    public ConfigService(ConfigRepository configRepository,
                         UserService userService,
                         TrackingRepository trackingRepository) {

        this.configRepository = configRepository;
        this.userService = userService;
        this.trackingRepository = trackingRepository;
    }

    /**
     * READ: Lädt die Konfiguration des eingeloggten Users.
     * Falls noch keine in der DB existiert, wird eine Fehlermeldung zurückgegeben.
     */
    public ConfigEntity getConfigByUsername(String username) {
        UserEntity user = userService.findUserByName(username);

        return configRepository.findByUserId(user.getId())
                .orElseThrow(() ->  new GenericException(
                        "Data inconsistency: No configuration of specific user found.",
                        HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    /** Hilfsmethode: Erzeugt ein Config-Objekt mit Standardwerten (definiert in der Entity-Klasse) */
    private ConfigEntity createDefaultConfig(UserEntity user) {
        ConfigEntity defaults = new ConfigEntity();
        defaults.setUser(user);
        return defaults;
    }

    @EventListener
    public void handleUserRegistration(UserRegisteredEvent event) {
        UserEntity user = event.getUser();

        // Default Config erstellen und speichern
        ConfigEntity defaultConfig = createDefaultConfig(user);
        configRepository.save(defaultConfig);

        System.out.println("Default-Config for User " + user.getUsername() + " has been created.");
    }

    /**
     * WRITE: Aktualisiert die Konfiguration des Users.
     */
    @Transactional
    public ConfigEntity updateConfig(String username, ConfigDto dto) {

        ConfigEntity config = getConfigByUsername(username);

        // Werte aus DTO übertragen (nur wenn ungleich null)
        if (dto.basePrice() != null) config.setBasePrice(dto.basePrice());
        if (dto.energyPrice() != null) config.setEnergyPrice(dto.energyPrice());
        if (dto.energyTax() != null) config.setEnergyTax(dto.energyTax());
        if (dto.vatRate() != null) config.setVatRate(dto.vatRate());
        if (dto.monthlyAdvance() != null) config.setMonthlyAdvance(dto.monthlyAdvance());
        if (dto.dueDate() != null) config.setDueDay(dto.dueDate());
        if (dto.sepaProcessingDays() != null) config.setSepaProcessingDays(dto.sepaProcessingDays());
        if (dto.additionalCredit() != null) config.setAdditionalCredit(dto.additionalCredit());
        if (dto.meterIdentifier() != null && !dto.meterIdentifier().isBlank()) config.setMeterIdentifier(dto.meterIdentifier());

        if (dto.referenceDate() != null && !dto.referenceDate().isBlank()){
            LocalDateTime timestamp = ReuseHelper.getParsedDateTimeNoFallback(dto.referenceDate());
            UserEntity user = userService.findUserByName(username);

            LocalDateTime start = timestamp.toLocalDate().atStartOfDay();
            LocalDateTime end = timestamp.toLocalDate().atTime(LocalTime.MAX);

            TrackingEntity foundEntry = trackingRepository.findFirstByUserIdAndTimestampBetween(user.getId(), start, end)
                    .orElseThrow(() -> new GenericException("No entry found for the given reference date.", HttpStatus.BAD_REQUEST));

            config.setReferenceDate(foundEntry.getTimestamp());
        }

        // Speichern der Änderungen
        return configRepository.save(config);
    }
}
