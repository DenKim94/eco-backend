package eco.backend.main_app.feature.auth.model;
import jakarta.persistence.*;

// feature/auth/model/UserEntity.java
@Entity
@Table(name = "users") // Fester Tabellenname!
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password; // Hashed!

    // Getter, Setter, Constructor...
}
