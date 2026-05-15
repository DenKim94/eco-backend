package eco.backend.main_app.feature.calculation.dto;

import java.time.LocalDate;

public record CalculationResultsDto(
        String id,                      // Zähler-ID
        LocalDate periodStart,          // Startdatum der Abrechnungszeit
        LocalDate periodEnd,            // Enddatum der Abrechnungszeit
        long daysPeriod,                // Anzahl der Tage in der Abrechnungszeit
        double paidAmountPeriod,        // Summe der Einzahlungen über den Abrechnungszeitraum [€]
        double totalCostsPeriod,        // Gesamtkosten (brutto) anhand der verbrauchten Energiemenge [€]
        double sumUsedEnergy,           // Summe der bisher verbrauchte Energiemenge [kWh]
        double costDiffPeriod,          // Brutto Restbetrag [€]: Positiv = Guthaben, Negativ = Nachzahlung
        double usedEnergyPerDay,        // Durchschnittlicher Energieverbrauch pro Tag [kWh/Tag]
        String logMessage
) {}
