package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.tracking.dto.TrackingDto;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import eco.backend.main_app.utils.ReuseHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        return repository.findByUserIdOrderByTimestampAsc(user.getId());
    }

    /** Eintrag hinzufügen */
    @Transactional
    public TrackingEntity addEntry(String username, TrackingDto dto) {

        UserEntity user = userService.findUserByName(username);

        TrackingEntity entity = new TrackingEntity();
        entity.setUser(user);
        entity.setReadingValue(dto.value_kWh());

        LocalDateTime timestamp = ReuseHelper.getParsedDateTime(dto.date());
        entity.setTimestamp(timestamp);

        isValidTrackingValue(username, dto).ifPresent(errorMsg -> {
            throw new GenericException(errorMsg, HttpStatus.BAD_REQUEST);
        });

        return repository.save(entity);
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

        if (updateDto.date().isBlank() && updateDto.value_kWh() == null){
            throw new GenericException("Update failed: No data provided.", HttpStatus.BAD_REQUEST);
        }

        // Den User laden, der die Anfrage stellt
        UserEntity currentUser = userService.findUserByName(username);

        // Den existierenden Eintrag aus der DB holen
        TrackingEntity entryToUpdate = getEntryById(id);

        // CHECK: Gehört der Eintrag zum korrekten User?
        // Wenn die USer-IDs nicht übereinstimmen, dürfen die Daten nicht geändert werden
        if (!entryToUpdate.getUser().getId().equals(currentUser.getId())) {
            throw new GenericException("Update failed: Invalid entry ID.", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime updatedDate = updateDto.date().isBlank() ? entryToUpdate.getTimestamp() : ReuseHelper.getParsedDateTime(updateDto.date()) ;
        Double value_kWh = (updateDto.value_kWh() != null) ? updateDto.value_kWh() : entryToUpdate.getReadingValue();

        // CHECK: Validierung
        isValidUpdatedDto(entryToUpdate, value_kWh, updatedDate).ifPresent(errorMsg -> {
            throw new GenericException(errorMsg, HttpStatus.BAD_REQUEST);
        });

        // Wert und Datum aktualisieren (Mapping)
        if (updateDto.value_kWh() != null){
            entryToUpdate.setReadingValue(updateDto.value_kWh());
        }
        entryToUpdate.setTimestamp(updatedDate);

        // Speichern
        return repository.save(entryToUpdate);
    }

    /** Hilfsmethode: Validierung des getrackten Eintrags */
    public Optional<String> isValidTrackingValue(String username, TrackingDto dto) {

        // CHECK: Wert darf nicht null oder negativ sein
        if (dto.value_kWh() == null || dto.value_kWh() < 0) {
            return Optional.of("Validation error: Provided value is null or negative.");
        }

        TrackingEntity lastEntry = getNewestEntry(username);
        LocalDateTime currentTimestamp = ReuseHelper.getParsedDateTime(dto.date());

        // CHECK: Wenn es keinen letzten Eintrag gibt, ist der Wert gültig
        if (lastEntry == null || lastEntry.getReadingValue() == null) {
            return Optional.empty();
        }

        // CHECK: Aktueller Eintrag darf nicht ÄLTER sein als der letzte Eintrag
        if (currentTimestamp.toLocalDate().isBefore(lastEntry.getTimestamp().toLocalDate())) {
            return Optional.of("Validation error: Invalid date of provided value.");
        }

        // CHECK: Es darf noch kein Eintrag am selben Tag existieren (ignoriert Uhrzeit)
        boolean isSameDay = currentTimestamp.toLocalDate().isEqual(lastEntry.getTimestamp().toLocalDate());
        if (isSameDay) {
            return Optional.of("Validation error: Entry already exists for provided date.");
        }

        // CHECK: Wert muss größer sein als der vorherige
        return (dto.value_kWh() > lastEntry.getReadingValue()) ?
                Optional.empty() :
                Optional.of("Validation error: The value of the current entry must be greater than that of the previous entry.");
    }

    /**
     * Validiert ein Update: Prüft Wert und Datum gegen Vorgänger bzw. Nachfolger (Sandwich-Check).
     *
     * @param entityToUpdate Der Eintrag, der gerade bearbeitet wird (mit seiner ID und dem ALTEN Timestamp).
     * @param value_kWh Der neue Zählerstand (vom DTO).
     * @param newTimestamp Das neue Datum (vom DTO oder das alte, falls nicht geändert).
     * @return true, wenn das Update gültig ist.
     */
    public Optional<String> isValidUpdatedDto(TrackingEntity entityToUpdate, Double value_kWh, LocalDateTime newTimestamp) {

        boolean predecessorCheck = false;

        // CHECK: Wert darf nicht negativ sein
        if (value_kWh < 0) {
            return Optional.of("Update failed: Provided value is negative.");
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
            return Optional.empty();
        }

        // --- PRÜFUNG GEGEN VORGÄNGER ---
        if (predecessor != null) {
            predecessorCheck = value_kWh > predecessor.getReadingValue() && !newTimestamp.isBefore(predecessor.getTimestamp());
            if (successor == null) {
                return predecessorCheck ? Optional.empty() : Optional.of("Update failed: Conflict with previous entry.");
            }
        }

        // --- PRÜFUNG GEGEN NACHFOLGER ---
        boolean successorCheck = value_kWh < successor.getReadingValue() && !newTimestamp.isAfter(successor.getTimestamp());
        if (predecessor == null) {
            return successorCheck ? Optional.empty() : Optional.of("Update failed: Conflict with the following entry.");
        }

        return (predecessorCheck && successorCheck)? Optional.empty() : Optional.of("Update failed: Provided entry has invalid values.");
    }
}
