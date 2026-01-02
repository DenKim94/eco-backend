package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.tracking.dto.TrackingDto;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import eco.backend.main_app.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    /** Alle getrackten Daten des Users laden */
    public List<TrackingEntity> getAllEntries(String username) {
        UserEntity user = userService.findUserByName(username);
        return repository.findByUserIdOrderByTimestampDesc(user.getId());
    }

    /** Eintrag hinzufügen */
    @Transactional
    public TrackingEntity addEntry(String username, TrackingDto dto) {
        try {
            UserEntity user = userService.findUserByName(username);

            TrackingEntity entity = new TrackingEntity();
            entity.setUser(user);
            entity.setReadingValue(dto.value_kWh());

            LocalDateTime timestamp = getParsedDate(dto.date());
            entity.setTimestamp(timestamp);

            return repository.save(entity);

        } catch (Exception e) {
            throw new GenericException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private LocalDateTime getParsedDate(String date) {
        LocalDateTime timestamp;

        // Falls kein Datum übergeben wird, wird aktuelles Datum verwendet
        if (date != null && !date.isBlank()) {
            // Falls String vorhanden: Parsen + Tagesanfang
            LocalDate localDate = LocalDate.parse(date, AppConstants.JSON_DATE_FORMATTER);
            timestamp = localDate.atStartOfDay();

        } else {
            // Falls leer/null
            timestamp = LocalDateTime.now();
        }
        return timestamp;
    }

    /** Letzten neusten Eintrag ausgeben */
    public TrackingEntity getNewestEntry(String username) {
        UserEntity user = userService.findUserByName(username);

        return repository.findFirstByUserIdOrderByTimestampDesc(user.getId())
                .orElseThrow(() -> new GenericException("Could not find any entry.", HttpStatus.BAD_REQUEST));
    }

    /** Bestimmten Eintrag anhand der ID entfernen */
    @Transactional
    public void deleteEntryById(String username, Long readingId) {
        UserEntity user = userService.findUserByName(username);

        TrackingEntity foundEntry = getEntryById(readingId);

        if (!foundEntry.getUser().getId().equals(user.getId())) {
            throw new GenericException("You are not allowed to delete this entry.", HttpStatus.FORBIDDEN);
        }

        repository.delete(foundEntry);
    }

    /** Hilfsmethode: Eintrag anhand der ID finden */
    private TrackingEntity getEntryById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new GenericException("Could not find entry.", HttpStatus.BAD_REQUEST));
    }

    /** Bestimmten Eintrag anhand der ID aktualisieren */
    @Transactional
    public TrackingEntity updateEntryById(String username, Long id, TrackingDto updateDto) {
        try {
        // Den User laden, der die Anfrage stellt
        UserEntity currentUser = userService.findUserByName(username);

        // Den existierenden Eintrag aus der DB holen
        TrackingEntity existingEntry = getEntryById(id);

        // CHECK: Gehört der Eintrag zum korrekten User?
        // Wenn die USer-IDs nicht übereinstimmen, dürfen die Daten nicht geändert werden
        if (!existingEntry.getUser().getId().equals(currentUser.getId())) {
            throw new GenericException("You are not allowed to update this entry.", HttpStatus.FORBIDDEN);
        }

        // Wert und Datum aktualisieren (Mapping)
        existingEntry.setReadingValue(updateDto.value_kWh());
        existingEntry.setTimestamp(!updateDto.date().isBlank() ? getParsedDate(updateDto.date()) : existingEntry.getTimestamp());

        // Speichern
        return repository.save(existingEntry);

        } catch (Exception e) {
            throw new GenericException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
