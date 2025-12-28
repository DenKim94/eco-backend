package eco.backend.main_app.feature.auth.admin;

import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.admin.dto.ListUserRequest;
import eco.backend.main_app.feature.auth.model.UserEntity;
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

    // User entfernen
    @DeleteMapping("/users/{id}/remove")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        UserEntity user = userService.findUserById(id);
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "User " + user.getUsername() + " has been removed."));
    }

    // User sperren/entsperren
    @PatchMapping("/users/{id}/set-status")
    public ResponseEntity<?> setUserStatus(@PathVariable Long id, @RequestParam boolean isEnabled) {
        UserEntity user = userService.findUserById(id);
        if(!user.getIsEnabled() && !isEnabled){
            return ResponseEntity.ok(Map.of("message", "User " + user.getUsername() + " is already disabled."));
        }
        userService.setUserEnabled(id, isEnabled);
        String status = isEnabled ? "activated" : "disabled";
        return ResponseEntity.ok(Map.of("message", "User " + user.getUsername() + " has been " + status + "."));
    }

    // Alle registrierten User anzeigen
    @GetMapping("/get-users")
    public ResponseEntity<List<ListUserRequest>> listAllUsers() {
        List<ListUserRequest> users = userService.getAllUserData();
        return ResponseEntity.ok(users);
    }
}
