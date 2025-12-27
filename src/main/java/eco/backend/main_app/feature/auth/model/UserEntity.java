package eco.backend.main_app.feature.auth.model;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;

import java.util.Collection;
import java.util.List;

// feature/auth/model/UserEntity.java
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    // Speichert das BCRYPT-gehashte Passwort
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", nullable = false)
    private String createdAt;

    // Default-Wert 'USER'
    @Column(nullable = false)
    private String role = "USER";

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true; // Default: Aktiv

    @Column(name = "token_version", nullable = false)
    private Integer tokenVersion = 0;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Dynamisch je nach Feld 'role': "ROLE_USER" oder "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    // Standard-Getter für Spring Security
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    // Der Default-Konstruktor für JPA [Muss parameterlos sein]
    public UserEntity() {}

    // Konstruktor zum Anlegen neuer User
    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = "USER";
    }

    @PrePersist
    protected void onCreate() {
        // Setzt das Datum automatisch kurz vor dem SQL-INSERT
        if (this.createdAt == null) {
            // Speichert als ISO-String: z.B. "2025-11-29T15:45:00"
            this.createdAt = LocalDateTime.now().toString();
        }
        if (this.tokenVersion == null || this.tokenVersion < 0) {
            this.tokenVersion = 0;
        }
    }
    public void setEnabled(boolean enabled) { this.isEnabled = enabled; }
    public String getCreatedAt() { return createdAt; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getTokenVersion() { return tokenVersion; }
    public void updateTokenVersion() { this.tokenVersion += 1; }
    public void resetTokenVersion(){ this.tokenVersion = 0; }
}
