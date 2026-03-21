package eco.backend.main_app.feature.auth.admin;

import eco.backend.main_app.feature.auth.AuthService;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.admin.dto.ListUserRequest;
import eco.backend.main_app.feature.auth.admin.dto.UpdatePasswordRequest;
import eco.backend.main_app.feature.auth.model.UserEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
// WICHTIG: Sichert die ganze Klasse ab. Nur Admins kommen hier rein!
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AuthService authService;

    public AdminController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // User entfernen
    @DeleteMapping("/users/{id}/remove")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        UserEntity user = userService.findUserById(id);
        userService.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "Profil von " + user.getUsername() + " wurde erfolgreich entfernt."));
    }

    // User sperren/entsperren
    @PatchMapping("/users/{id}/set-status")
    public ResponseEntity<?> setUserStatus(@PathVariable Long id, @RequestParam boolean isEnabled) {
        UserEntity user = userService.findUserById(id);
        if(!user.getIsEnabled() && !isEnabled){
            return ResponseEntity.ok(Map.of("message", "Profil von " + user.getUsername() + " ist bereits deaktiviert."));
        }
        userService.setUserEnabled(id, isEnabled);
        String status = isEnabled ? "aktiviert" : "deaktiviert";
        return ResponseEntity.ok(Map.of("message", "Profil von " + user.getUsername() + " wurde " + status + "."));
    }

    // Alle registrierten User anzeigen
    @GetMapping("/get-users")
    public ResponseEntity<List<ListUserRequest>> listAllUsers() {
        List<ListUserRequest> users = userService.getAllUserData();
        return ResponseEntity.ok(users);
    }

    /**
     * POST /api/admin/update-password
     */
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, String>> updateAdminPassword(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestBody UpdatePasswordRequest dto) {

        authService.updateAdminPassword(userDetails.getUsername(), dto);
        return ResponseEntity.ok(Map.of("message", "Admin Passwort wurde erfolgreich geändert."));
    }
}
