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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
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


    public UserEntity() {}

    // Konstruktor zum Anlegen neuer User
    public UserEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @PrePersist
    protected void onCreate() {
        // Setzt das Datum automatisch kurz vor dem SQL-INSERT
        if (this.createdAt == null) {
            // Speichert als ISO-String: z.B. "2025-11-29T15:45:00"
            this.createdAt = LocalDateTime.now().toString();
        }
    }

    public String getCreatedAt() { return createdAt; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}
