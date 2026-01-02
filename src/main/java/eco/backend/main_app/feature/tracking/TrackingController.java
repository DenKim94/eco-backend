package eco.backend.main_app.feature.tracking;

import eco.backend.main_app.feature.tracking.dto.TrackingDto;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<TrackingEntity> getNewest(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(service.getNewestEntry(user.getUsername()));
    }

    @PostMapping("/add")
    public ResponseEntity<TrackingEntity> add(@AuthenticationPrincipal UserDetails user, @RequestBody TrackingDto dto) {
        return ResponseEntity.ok(service.addEntry(user.getUsername(), dto));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable("id") Long id) {
        service.deleteEntryById(user.getUsername(), id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<TrackingEntity> update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestBody TrackingDto dto) {
        return ResponseEntity.ok(service.updateEntryById(user.getUsername(), id, dto));
    }
}
