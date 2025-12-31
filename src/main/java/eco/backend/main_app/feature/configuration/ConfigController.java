package eco.backend.main_app.feature.configuration;

import eco.backend.main_app.feature.configuration.dto.ConfigDto;
import eco.backend.main_app.feature.configuration.model.ConfigEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * GET /api/config: Lädt die Konfiguration des eingeloggten Users.
     */
    @GetMapping
    public ResponseEntity<ConfigEntity> getConfig(@AuthenticationPrincipal UserDetails userDetails) {
        // Wir delegieren einfach an den Service
        ConfigEntity config = configService.getConfig(userDetails.getUsername());
        return ResponseEntity.ok(config);
    }

    /**
     * PUT /api/config: Aktualisiert die bestehende Konfiguration.
     */
    @PutMapping
    public ResponseEntity<ConfigEntity> updateConfig(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ConfigDto dto) {

        // Update durchführen
        ConfigEntity updatedConfig = configService.updateConfig(userDetails.getUsername(), dto);
        return ResponseEntity.ok(updatedConfig);
    }
}
