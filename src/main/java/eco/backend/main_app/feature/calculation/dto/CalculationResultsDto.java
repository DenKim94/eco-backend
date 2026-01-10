package eco.backend.main_app.feature.calculation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CalculationResultsDto(
        Long meterId,                   // Zählernummer
        LocalDate startDate,            // Startdatum der Abrechnungszeit
        LocalDate endDate,              // Enddatum der Abrechnungszeit
        int days,                       // Anzahl der Tage in der Abrechnungszeit
        BigDecimal monthlyPaymentSum,   // Summe der Einzahlungen über den Abrechnungszeitraum[€]
        BigDecimal totalCost,           // Gesamtkosten (brutto) anhand der verbrauchten Energiemenge [€]
        BigDecimal totalNetCost,        // Gesamtkosten (netto) anhand der verbrauchten Energiemenge [€]
        double totalConsumptionKwh,     // Summe der bisher verbrauchte Energiemenge [kWh]
        BigDecimal costDiff,            // Positiv = Nachzahlung, Negativ = Guthaben [€]
        double consumptionPerDay        // Durchschnittlicher Energieverbrauch pro Tag [kWh/Tag]
) {}
