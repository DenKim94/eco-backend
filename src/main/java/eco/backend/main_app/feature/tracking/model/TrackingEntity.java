package eco.backend.main_app.feature.tracking.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.utils.AppConstants;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meter_readings")
public class TrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kwh_reading", nullable = false)
    private Double readingValue; // Umbenannt für Java-Konvention, mappt auf kwh_reading

    @Column(nullable = false)
    @JsonFormat(pattern = AppConstants.JSON_DATE_PATTERN)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Der zugehörige user soll in der JSON-Antwort ignoriert werden
    private UserEntity user;

    // Getter, Setter, Leerer Konstruktor
    public TrackingEntity() {}

    public void setUser(UserEntity user) { this.user = user; }
    public UserEntity getUser() { return user; }
    public void setReadingValue(Double value_kWh){ this.readingValue = value_kWh; }
    public Double getReadingValue(){ return this.readingValue; }
    public LocalDateTime getTimestamp(){ return this.timestamp; }
    public void setTimestamp(LocalDateTime date_value){ this.timestamp = date_value; }
    public Long getId(){ return this.id; }
}
