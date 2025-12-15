package eco.backend.main_app.feature.auth.admin;

import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.admin.dto.ListUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
// WICHTIG: Sichert die ganze Klasse ab. Nur Admins kommen hier rein!
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // User löschen
    @DeleteMapping("/users/{id}/remove")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "User has been deleted."));
    }

    // User sperren/entsperren
    @PatchMapping("/users/{id}/set-status")
    public ResponseEntity<?> setUserStatus(@PathVariable Long id, @RequestParam boolean isEnabled) {
        userService.setUserEnabled(id, isEnabled);
        String status = isEnabled ? "activated" : "blocked";
        return ResponseEntity.ok(Map.of("message", "User has been " + status));
    }

    @GetMapping("/get-users")
    public ResponseEntity<List<ListUserRequest>> listAllUsers() {
        List<ListUserRequest> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
