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
import java.time.LocalTime;
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

        UserEntity user = userService.findUserByName(username);

        TrackingEntity entity = new TrackingEntity();
        entity.setUser(user);
        entity.setReadingValue(dto.value_kWh());

        LocalDateTime timestamp = getParsedDate(dto.date());
        entity.setTimestamp(timestamp);

        if (!isValidTrackingValue(username, dto)) {
            throw new GenericException("Invalid entry properties.", HttpStatus.BAD_REQUEST);
        }
        return repository.save(entity);
    }

    /** Hilfsmethode: Datum aus String zu LocalDateTime parsen */
    private LocalDateTime getParsedDate(String date) {
        LocalDateTime timestamp;

        // Falls kein Datum übergeben wird, wird aktuelles Datum verwendet
        if (date != null && !date.isBlank()) {
            // Falls String vorhanden: Parsen + Tagesanfang
            LocalDate localDate = LocalDate.parse(date, AppConstants.JSON_DATE_FORMATTER);
            timestamp = localDate.atTime(LocalTime.now());

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
                .orElse(null);
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

        // Den User laden, der die Anfrage stellt
        UserEntity currentUser = userService.findUserByName(username);

        // Den existierenden Eintrag aus der DB holen
        TrackingEntity existingEntry = getEntryById(id);

        // CHECK: Gehört der Eintrag zum korrekten User?
        // Wenn die USer-IDs nicht übereinstimmen, dürfen die Daten nicht geändert werden
        if (!existingEntry.getUser().getId().equals(currentUser.getId())) {
            throw new GenericException("You are not allowed to update this entry.", HttpStatus.FORBIDDEN);
        }

        LocalDateTime updatedDate = updateDto.date().isBlank() ? existingEntry.getTimestamp() : getParsedDate(updateDto.date()) ;

        // Wert und Datum aktualisieren (Mapping)
        existingEntry.setReadingValue(updateDto.value_kWh());
        existingEntry.setTimestamp(updatedDate);

        // CHECK: Validierung
        if (!isValidUpdatedValue(existingEntry, updateDto.value_kWh(), updatedDate)){
            throw new GenericException("Update failed: Invalid entry properties.", HttpStatus.BAD_REQUEST);
        }

        // Speichern
        return repository.save(existingEntry);
    }

    /** Hilfsmethode: Validierung des getrackten Eintrags */
    public boolean isValidTrackingValue(String username, TrackingDto dto) {

        // CHECK: Wert darf nicht null oder negativ sein
        if (dto.value_kWh() == null || dto.value_kWh() < 0) {
            return false;
        }

        TrackingEntity lastEntry = getNewestEntry(username);
        LocalDateTime currentTimestamp = getParsedDate(dto.date());

        // CHECK: Wenn es keinen letzten Eintrag gibt, ist der Wert gültig
        if (lastEntry == null || lastEntry.getReadingValue() == null) {
            return true;
        }

        // CHECK: Aktueller Eintrag darf nicht ÄLTER sein als der letzte Eintrag
        if (currentTimestamp.isBefore(lastEntry.getTimestamp())) {
            return false;
        }

        // CHECK: Wert muss größer sein als der vorherige
        return (dto.value_kWh() >= lastEntry.getReadingValue());
    }

    /**
     * Validiert ein Update: Prüft gegen Vorgänger und Nachfolger (Sandwich-Check).
     *
     * @param entityToUpdate Der Eintrag, der gerade bearbeitet wird (mit seiner ID und dem ALTEN Timestamp).
     * @param newValue Der neue Zählerstand (vom DTO).
     * @param newTimestamp Das neue Datum (vom DTO oder das alte, falls nicht geändert).
     * @return true, wenn das Update gültig ist.
     */
    public boolean isValidUpdatedValue(TrackingEntity entityToUpdate, Double newValue, LocalDateTime newTimestamp) {

        boolean predecessorCheck = false;
        boolean successorCheck = false;

        // CHECK: Nicht null, nicht negativ
        if (newValue == null || newValue < 0) {
            return false;
        }

        Long userId = entityToUpdate.getUser().getId();
        Long currentId = entityToUpdate.getId();

        // Vorgänger finden (Der neueste Eintrag, der zeitlich VOR oder GLEICH dem neuen Datum ist, aber NICHT dieser Eintrag selbst)
        TrackingEntity predecessor = repository.findFirstByUserIdAndTimestampLessThanEqualAndIdNotOrderByTimestampDesc(
                userId, newTimestamp, currentId).orElse(null);

        // Nachfolger finden (Der älteste Eintrag, der zeitlich NACH oder GLEICH dem neuen Datum ist, aber NICHT dieser Eintrag selbst)
        TrackingEntity successor = repository.findFirstByUserIdAndTimestampGreaterThanEqualAndIdNotOrderByTimestampAsc(
                userId, newTimestamp, currentId).orElse(null);

        // Falls nur ein Eintrag in der Datenbank vorhanden ist, ist der Wert gültig
        if (predecessor == null && successor == null) {
            return true;
        }

        // --- PRÜFUNG GEGEN VORGÄNGER ---
        if (predecessor != null) {
            predecessorCheck = newValue >= predecessor.getReadingValue() && !newTimestamp.isBefore(predecessor.getTimestamp());
            if (successor == null) {
                return predecessorCheck;
            }
        }

        // --- PRÜFUNG GEGEN NACHFOLGER ---
        if (successor != null) {
            successorCheck = newValue <= successor.getReadingValue() && !newTimestamp.isAfter(successor.getTimestamp());
            if (predecessor == null) {
                return successorCheck;
            }
        }

        return predecessorCheck && successorCheck;
    }
}
