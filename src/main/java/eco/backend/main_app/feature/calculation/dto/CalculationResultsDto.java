package eco.backend.main_app.feature.calculation.dto;

import java.time.LocalDate;

public record CalculationResultsDto(
        String meterId,                 // Zähler-ID
        LocalDate startDate,            // Startdatum der Abrechnungszeit
        LocalDate endDate,              // Enddatum der Abrechnungszeit
        long daysBetween,               // Anzahl der Tage in der Abrechnungszeit
        double paidAmountPeriod,        // Summe der Einzahlungen über den Abrechnungszeitraum[€]
        double bruttoTotalCostPeriod,   // Gesamtkosten (brutto) anhand der verbrauchten Energiemenge [€]
        double totalConsumptionKwh,     // Summe der bisher verbrauchte Energiemenge [kWh]
        double costDiffPeriod,          // Brutto Restbetrag [€]: Positiv = Nachzahlung, Negativ = Guthaben
        double usedEnergyPerDay,        // Durchschnittlicher Energieverbrauch pro Tag [kWh/Tag]
        String logMessage
) {}
