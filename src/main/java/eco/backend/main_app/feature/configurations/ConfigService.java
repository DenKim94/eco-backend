package eco.backend.main_app.feature.configurations;

import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.configurations.dto.ConfigDto;
import eco.backend.main_app.feature.configurations.model.ConfigEntity;
import jakarta.transaction.Transactional;
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
     * Falls noch keine in der DB existiert, wird eine temporäre
     * Default-Konfiguration zurückgegeben.
     */
    public ConfigEntity getConfig(String username) {
        UserEntity user = userService.findUserByName(username);

        // Versuche zu laden, sonst erzeuge Default-Objekt (ohne zu speichern)
        return configRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultConfig(user));
    }

    // Hilfsmethode: Erzeugt ein Config-Objekt mit Standardwerten (definiert in der Entity-Klasse)
    private ConfigEntity createDefaultConfig(UserEntity user) {
        ConfigEntity defaults = new ConfigEntity();
        defaults.setUser(user);
        return defaults;
    }

    /**
     * WRITE: Erstellt einen neuen Eintrag oder aktualisiert den bestehenden (Upsert).
     */
    @Transactional
    public ConfigEntity saveOrUpdateConfig(String username, ConfigDto dto) {
        UserEntity user = userService.findUserByName(username);

        // Bestehende Config suchen ODER neue leere Entity (mit Java-Defaults) anlegen
        ConfigEntity config = getConfig(username);

        // Werte aus DTO übertragen (nur wenn nicht null, um versehentliches Löschen zu vermeiden)
        if (dto.basePrice() != null) config.setBasePrice(dto.basePrice());
        if (dto.energyPrice() != null) config.setEnergyPrice(dto.energyPrice());
        if (dto.energyTax() != null) config.setEnergyTax(dto.energyTax());
        if (dto.vatRate() != null) config.setVatRate(dto.vatRate());
        if (dto.monthlyAdvance() != null) config.setMonthlyAdvance(dto.monthlyAdvance());
        if (dto.additionalCredit() != null) config.setAdditionalCredit(dto.additionalCredit());
        if (dto.meterIdentifier() != null) config.setMeterIdentifier(dto.meterIdentifier());

        // Speichern (Insert oder Update passiert automatisch durch JPA)
        return configRepository.save(config);
    }
}
