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

    @Column(name = "days_period", nullable = false)
    private long daysPeriod;

    @Column(name = "payments_period", nullable = false)
    private Double paidAmountPeriod;

    @Column(name = "total_costs_period", nullable = false)
    private Double totalCostsPeriod;

    @Column(name = "cost_diff_period", nullable = false)
    private Double costDiffPeriod;

    @Column(name = "sum_used_energy", nullable = false)
    private Double sumUsedEnergy;

    @Column(name = "used_energy_per_day", nullable = false)
    private Double usedEnergyPerDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Der zugehörige user soll in der JSON-Antwort ignoriert werden
    private UserEntity user;

    // Leerer Konstruktor für JPA
    public CalculationEntity() {}

    // Convenience Konstruktor
    public CalculationEntity(LocalDateTime periodStart,
                             LocalDateTime periodEnd,
                             long daysPeriod,
                             Double paidAmountPeriod,
                             Double totalCostsPeriod,
                             Double costDiffPeriod,
                             Double sumUsedEnergy,
                             Double usedEnergyPerDay,
                             UserEntity user) {

        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.daysPeriod = daysPeriod;
        this.paidAmountPeriod = paidAmountPeriod;
        this.totalCostsPeriod = totalCostsPeriod;
        this.costDiffPeriod = costDiffPeriod;
        this.sumUsedEnergy = sumUsedEnergy;
        this.usedEnergyPerDay = usedEnergyPerDay;
        this.user = user;
    }

    public Long getId() { return id; }
    public LocalDateTime getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDateTime periodStart) { this.periodStart = periodStart; }
    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }
    public Double getTotalCostsPeriod() { return totalCostsPeriod; }
    public void setTotalCostsPeriod(Double totalCosts){ this.totalCostsPeriod = totalCosts; }
    public Double getCostDiffPeriod() { return costDiffPeriod; }
    public void setCostDiffPeriod(Double costDiffPeriod) { this.costDiffPeriod = costDiffPeriod; }
    public Double getSumUsedEnergy() { return sumUsedEnergy; }
    public void setSumUsedEnergy(Double sumUsedEnergy) { this.sumUsedEnergy = sumUsedEnergy; }
    public long getDaysPeriod(){ return daysPeriod ; }
    public void setDaysPeriod(long days){ this.daysPeriod = days ; }
    public Double getPaidAmountPeriod() { return paidAmountPeriod; }
    public void setPaidAmountPeriod(Double paidAmountPeriod){ this.paidAmountPeriod = paidAmountPeriod; }
    public Double getUsedEnergyPerDay() { return usedEnergyPerDay; }
    public void setUsedEnergyPerDay(Double usedEnergyPerDay){ this.usedEnergyPerDay = usedEnergyPerDay; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}
