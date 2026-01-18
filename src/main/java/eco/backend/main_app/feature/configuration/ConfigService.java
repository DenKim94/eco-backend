package eco.backend.main_app.feature.configuration;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.configuration.dto.ConfigDto;
import eco.backend.main_app.feature.configuration.model.ConfigEntity;
import eco.backend.main_app.core.event.UserRegisteredEvent;
import org.springframework.context.event.EventListener;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
    private final ConfigRepository configRepository;
    private final UserService userService;

    public ConfigService(ConfigRepository configRepository, UserService userService) {
        this.configRepository = configRepository;
        this.userService = userService;
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
        if (dto.meterIdentifier() != null) config.setMeterIdentifier(dto.meterIdentifier());

        // Speichern der Änderungen
        return configRepository.save(config);
    }
}
