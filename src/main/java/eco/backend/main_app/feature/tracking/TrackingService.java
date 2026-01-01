package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.tracking.dto.TrackingDto;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrackingService {

    private final TrackingRepository repository;
    private final UserService userService;

    public TrackingService(TrackingRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    // Alle getrackten Daten des Users laden
    public List<TrackingEntity> getAllTrackings(String username) {
        UserEntity user = userService.findUserByName(username);
        return repository.findByUserIdOrderByTimestampDesc(user.getId());
    }

    // Eintrag hinzufügen
    @Transactional
    public TrackingEntity addTracking(String username, TrackingDto dto) {
        try {
            UserEntity user = userService.findUserByName(username);

            TrackingEntity entity = new TrackingEntity();
            entity.setUser(user);
            entity.setReadingValue(dto.value_kWh());
            // Falls kein Datum übergeben wird, wird aktuelles Datum verwendet
            entity.setTimestamp(dto.date() != null ? dto.date() : LocalDateTime.now());

            return repository.save(entity);

        } catch (Exception e) {
            throw new GenericException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Letzten (neusten) Eintrag ausgeben
    public TrackingEntity getNewestEntry(String username) {
        UserEntity user = userService.findUserByName(username);

        return repository.findFirstByUserIdOrderByTimestampDesc(user.getId())
                .orElseThrow(() -> new GenericException("Could not find any entry.", HttpStatus.BAD_REQUEST));
    }

    // TODO: Letzten (neusten) Eintrag löschen

    // TODO: Letzten (neusten) Eintrag aktualisieren

    // TODO: Bestimmten Eintrag anhand der ID aktualisieren

    // Bestimmten Eintrag anhand der ID entfernen
    @Transactional
    public void deleteTrackingById(String username, Long readingId) {
        UserEntity user = userService.findUserByName(username);

        TrackingEntity reading = repository.findById(readingId)
                .orElseThrow(() -> new GenericException("Could not delete entry.", HttpStatus.BAD_REQUEST ));

        if (!reading.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Nicht berechtigt, diesen Eintrag zu löschen.");
        }

        repository.delete(reading);
    }
}
