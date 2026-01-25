package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.feature.tracking.dto.TrackingDto;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    private final TrackingService service;

    public TrackingController(TrackingService service) {
        this.service = service;
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<TrackingEntity>> getAll(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.getAllEntries(user.getUsername()));
    }

    @GetMapping("/get-newest")
    public ResponseEntity<?> getNewest(@AuthenticationPrincipal UserDetails user) {
        TrackingEntity result = service.getNewestEntry(user.getUsername());

        if (result == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Entry not found.");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<TrackingEntity> addEntry(@AuthenticationPrincipal UserDetails user, @RequestBody TrackingDto dto) {
        return ResponseEntity.ok(service.addEntry(user.getUsername(), dto));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Map<String, String>> deleteEntry(@AuthenticationPrincipal UserDetails user, @PathVariable("id") Long id) {
        service.deleteEntryById(user.getUsername(), id);
        return ResponseEntity.ok(Map.of("message", "Entry has been removed successfully."));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, String>> deleteAllEntries(@AuthenticationPrincipal UserDetails user){
        service.deleteAllEntries(user.getUsername());
        return ResponseEntity.ok(Map.of("message", "All tracked entries have been removed successfully."));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<TrackingEntity> updateEntry(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody TrackingDto dto) {
        return ResponseEntity.ok(service.updateEntryById(user.getUsername(), id, dto));
    }
}
