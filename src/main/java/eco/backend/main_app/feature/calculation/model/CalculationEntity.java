package eco.backend.main_app.feature.calculation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eco.backend.main_app.feature.auth.model.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculated_results")
public class CalculationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "total_costs_period", nullable = false)
    private Double totalCostsPeriod;

    @Column(name = "cost_diff_period", nullable = false)
    private Double costDiffPeriod;

    @Column(name = "sum_used_energy", nullable = false)
    private Double sumUsedEnergy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Der zugehörige user soll in der JSON-Antwort ignoriert werden
    private UserEntity user;

    // Leerer Konstruktor für JPA
    public CalculationEntity() {}

    // Convenience Konstruktor
    public CalculationEntity(LocalDateTime periodStart,
                             LocalDateTime periodEnd,
                             Double totalCostsPeriod,
                             Double costDiffPeriod,
                             Double sumUsedEnergy,
                             UserEntity user) {

        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalCostsPeriod = totalCostsPeriod;
        this.costDiffPeriod = costDiffPeriod;
        this.sumUsedEnergy = sumUsedEnergy;
        this.user = user;
    }

    public Long getId() { return id; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public Double getTotalCostsPeriod() { return totalCostsPeriod; }
    public Double getCostDiffPeriod() { return costDiffPeriod; }
    public Double getSumUsedEnergy() { return sumUsedEnergy; }
    public void setUser(UserEntity user) { this.user = user; }
    public UserEntity getUser() { return user; }
}
