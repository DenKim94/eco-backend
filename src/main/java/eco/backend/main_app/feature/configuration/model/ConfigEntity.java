package eco.backend.main_app.feature.configuration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eco.backend.main_app.feature.auth.model.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "configs")
public class ConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Defaultwerte setzen:
    @Column(name = "base_price", nullable = false)
    private Double basePrice = 11.90;       // Grundpreis EUR/Monat (brutto)

    @Column(name = "energy_price", nullable = false)
    private Double energyPrice = 0.3467;    // Verbrauchspreis EUR/kWh (brutto)

    @Column(name = "energy_tax", nullable = false)
    private Double energyTax = 0.0205;      // Stromsteuer in EUR/kWh

    @Column(name = "vat_rate", nullable = false)
    private Double vatRate = 0.19;          // Umsatzsteuer (Relativ z.B. 0.19)

    @Column(name = "monthly_advance", nullable = false)
    private Double monthlyAdvance = 50.0;   // Monatlicher Abschlag in EUR/Monat

    @Column(name = "additional_credit", nullable = false)
    private Double additionalCredit = 0.0;  // Guthaben in EUR

    @Column(name = "due_day", nullable = false)
    private Integer dueDay = 5;             // Abrechnungstag im Monat (z.B. 5: Zum 5. des Monats)

    @Column(name = "sepa_processing_days", nullable = false)
    private Integer sepaProcessingDays = 15; // Anzahl der Tage für die Lastschriftankündigung (SEPA)

    @Column(name = "meter_identifier", nullable = false)
    private String meterIdentifier = "EMPTY-METER-ID"; // Zählernummer

    // Verknüpfung zum User (LAZY: Userdaten werden erst dann geladen, wenn diese benötigt werden)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private UserEntity user;

    public ConfigEntity() {}

    public void setUser(UserEntity user) { this.user = user; }
    public UserEntity getUser() { return user; }

    public void setBasePrice(Double basePrice) { this.basePrice = basePrice; }
    public Double getBasePrice(){ return this.basePrice; }
    public void setEnergyPrice(Double energyPrice){ this.energyPrice = energyPrice; }
    public Double getEnergyPrice(){ return this.energyPrice; }
    public void setEnergyTax(Double energyTax){ this.energyTax = energyTax; }
    public Double getEnergyTax(){ return this.energyTax; }
    public void setVatRate(Double vatRate){ this.vatRate = vatRate; }
    public Double getVatRate(){ return this.vatRate; }
    public void setMonthlyAdvance(Double monthlyAdvance){ this.monthlyAdvance = monthlyAdvance; }
    public Double getMonthlyAdvance(){ return this.monthlyAdvance; }
    public void setAdditionalCredit(Double additionalCredit){ this.additionalCredit = additionalCredit; }
    public Double getAdditionalCredit(){ return this.additionalCredit; }
    public void setDueDay(Integer dueDay){ this.dueDay = dueDay; }
    public Integer getDueDay(){ return this.dueDay; }
    public void setSepaProcessingDays(Integer processingDays){ this.sepaProcessingDays = processingDays; }
    public Integer getSepaProcessingDays(){ return this.sepaProcessingDays; }
    public void setMeterIdentifier(String meterIdentifier){ this.meterIdentifier = meterIdentifier; }
    public String getMeterIdentifier(){ return this.meterIdentifier; }
}