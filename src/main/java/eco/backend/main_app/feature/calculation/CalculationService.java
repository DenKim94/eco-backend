package eco.backend.main_app.feature.calculation;

import eco.backend.main_app.core.exception.GenericException;
import eco.backend.main_app.feature.auth.UserService;
import eco.backend.main_app.feature.auth.model.UserEntity;
import eco.backend.main_app.feature.calculation.dto.CalculationRequestDto;
import eco.backend.main_app.feature.calculation.dto.CalculationResultsDto;
import eco.backend.main_app.feature.calculation.model.CalculationEntity;
import eco.backend.main_app.feature.configuration.ConfigService;
import eco.backend.main_app.feature.configuration.model.ConfigEntity;
import eco.backend.main_app.feature.tracking.TrackingRepository;
import eco.backend.main_app.feature.tracking.model.TrackingEntity;
import eco.backend.main_app.utils.AppConstants;
import eco.backend.main_app.utils.ReuseHelper;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
public class CalculationService {
    private final TrackingRepository trackingRepository;
    private final UserService userService;
    private final ConfigService configService;
    private final CalculationRepository calculationRepository;

    public CalculationService(TrackingRepository trackingRepository,
                              UserService userService,
                              ConfigService configService,
                              CalculationRepository calculationRepository) {

        this.trackingRepository = trackingRepository;
        this.userService = userService;
        this.configService = configService;
        this.calculationRepository = calculationRepository;
    }

    /**
     * Berechnet die Kosten anhand der getrackten Daten für einen bestimmten Zeitraum.
     * Wird kein Start- oder Enddatum übergeben, wird der neueste und älteste Eintrag verwendet.
     *
     * @param username Name des (authentifizierten) Users
     * @param requestDto DTO mit Start- und Enddatum (optional)
     * @return DTO mit Berechnungsergebnissen
     */
    public CalculationResultsDto runCalculation(String username, CalculationRequestDto requestDto) {
        UserEntity user = userService.findUserByName(username);

        // Lokale Parameter
        TrackingEntity currentEntry;
        TrackingEntity prevEntry;
        String logMessage = null;
        int DAYS_IN_YEAR = 365;
        int MONTHS_IN_YEAR = 12;

        // Datumsangaben parsen
        LocalDateTime startDate = ReuseHelper.getParsedDateTimeNoFallback(requestDto.startDate()); // TODO: Das Datum muss aus Configs entnommen werden --> Referenzdatum
        LocalDateTime endDate = ReuseHelper.getParsedDateTimeNoFallback(requestDto.endDate());

        // User-Konfiguration laden
        ConfigEntity configData = configService.getConfigByUsername(user.getUsername());

        // Getrackte Daten laden (absteigend sortiert: neuester Eintrag zuerst)
        List<TrackingEntity> trackedData = trackingRepository.findByUserIdOrderByTimestampDesc(user.getId());

        // Validierung der Mindestanzahl an Datenpunkten
        if (trackedData.size() < AppConstants.MIN_DATA_POINTS) {
            throw new GenericException("Not enough data points. At least " + AppConstants.MIN_DATA_POINTS + " are required.", HttpStatus.BAD_REQUEST);
        }

        // Die gewünschten Einträge anhand des Datums finden, sonst Fallback auf den neuesten und ältesten Eintrag
        prevEntry = (startDate == null) ? trackedData.getLast() : findEntryByDate(trackedData, startDate.toLocalDate());
        currentEntry = (endDate == null) ? trackedData.getFirst() : findEntryByDate(trackedData, endDate.toLocalDate());

        // Prüfung des Datums
        if (!validDates(prevEntry.getTimestamp(), currentEntry.getTimestamp())) {
            throw new GenericException("Invalid date values: The start date must be before the end date.", HttpStatus.BAD_REQUEST);
        }

        // Anzahl der Tage zwischen den beiden Einträgen
        long daysBetween = ChronoUnit.DAYS.between(prevEntry.getTimestamp().toLocalDate(), currentEntry.getTimestamp().toLocalDate());

        // ***** Berechnungslogik *****
        double diffTrackedValues = currentEntry.getReadingValue() - prevEntry.getReadingValue();

        ExpectedAmountMonthsResult amountPeriod = getExpectedAmountPeriod(prevEntry.getTimestamp().toLocalDate(),
                                                                            currentEntry.getTimestamp().toLocalDate(),
                                                                            configData.getDueDay(),
                                                                            configData.getSepaProcessingDays());

        if (amountPeriod.message != null) { logMessage = amountPeriod.message; }

        // Normierter Verbrauch pro Tag [kWh/Tag]
        double normConsumptionPerDay = diffTrackedValues / daysBetween;
        
        // Netto Verbrauchspreis berechnen [€/kWh]
        double netConsumptionPrice = configData.getEnergyPrice()/(1 + configData.getVatRate()) - configData.getEnergyTax();
        
        // Netto Verbrauchskosten berechnen [€]
        double netConsumptionCostPeriod = diffTrackedValues * netConsumptionPrice;
        
        // Netto Grundpreis berechnen [€]
        double netBasePrice = configData.getBasePrice()/(1 + configData.getVatRate());
        
        // Netto Grundkosten berechnen [€]
        double netBaseCostPeriod = netBasePrice * MONTHS_IN_YEAR/DAYS_IN_YEAR * daysBetween;

        // Netto Gesamtkosten der Stromsteuer berechnen [€]
        double netEnergyTaxCostPeriod = configData.getEnergyTax() * diffTrackedValues;

        // Netto Gesamtkosten anhand der verbrauchten Energiemenge berechnen [€]
        double netTotalCostPeriod = (netConsumptionCostPeriod) + netBaseCostPeriod + netEnergyTaxCostPeriod;

        // Brutto Gesamtkosten berechnen [€]
        double bruttoTotalCostPeriod = netTotalCostPeriod * (1 + configData.getVatRate());

        // Gezahlten Abschläge im betrachteten Abrechnungszeitraum
        double paidAmountPeriod = configData.getMonthlyAdvance() * amountPeriod.expectedMonths/DAYS_IN_YEAR * daysBetween;

        // Brutto Restbetrag berechnen [€]
        double costDiffPeriod = (paidAmountPeriod - bruttoTotalCostPeriod) + configData.getAdditionalCredit();

        // Ergebnisse übergeben
        return new CalculationResultsDto(
                configData.getMeterIdentifier(),
                prevEntry.getTimestamp().toLocalDate(),
                currentEntry.getTimestamp().toLocalDate(),
                daysBetween,
                paidAmountPeriod,
                bruttoTotalCostPeriod,
                diffTrackedValues,
                costDiffPeriod,
                normConsumptionPerDay,
                logMessage
        );
    }

    /**
     * Hilfsfunktion prüft, ob das Startdatum vor dem Enddatum liegt.
     *
     * @param startDate Startdatum
     * @param endDate Enddatum
     */
    private boolean validDates(LocalDateTime startDate, LocalDateTime endDate){
        return startDate.isBefore(endDate);
    }

    /**
     * Hilfsfunktion, um einen Eintrag anhand eines Datums in TrackingEntity zu finden.
     *
     * @param trackedData Getrackten Daten als Liste
     * @param date Zieldatum
     */
    private TrackingEntity findEntryByDate(List<TrackingEntity> trackedData, LocalDate date){

        return trackedData.stream()
                .filter(e -> e.getTimestamp().toLocalDate().isEqual(date))
                .findFirst()
                .orElseThrow(() -> new GenericException("No entry found for the given date.", HttpStatus.BAD_REQUEST));
    }

    /**
     * Speichert oder aktualisiert das Berechnungsergebnis in der Datenbank.
     *
     * @param username Der Name des (authentifizierten) Users
     * @param results Das DTO mit den Berechnungsergebnissen
     */
    @Transactional
    public void saveResultsInEntity(String username, CalculationResultsDto results) {
        UserEntity user = userService.findUserByName(username);

        // Konvertierung: LocalDate (DTO) -> LocalDateTime (Entity)
        // Die Uhrzeit wird per Default auf 00:00:00 gesetzt
        LocalDateTime startTimestamp = results.startDate().atStartOfDay();
        LocalDateTime endTimestamp = results.endDate().atStartOfDay();

        // Prüfen: Gibt es schon einen Eintrag für dieses Enddatum?
        // Dies verhindert Duplikate, wenn der User am selben Tag mehrfach rechnet.
        CalculationEntity entityToSave = calculationRepository
                .findByUserIdAndPeriodEnd(user.getId(), endTimestamp)
                .orElse(new CalculationEntity()); // Falls kein Eintrag gefunden wurde, wird eine neue leere Zeile in der Tabelle angelegt

        // Werte setzen: Bei neuem Objekt (ID ist null) müssen User und Enddatum gesetzt werden
        if (entityToSave.getId() == null) {
            entityToSave.setUser(user);
            entityToSave.setPeriodEnd(endTimestamp);
        }

        // Diese Werte werden IMMER aktualisiert
        entityToSave.setPeriodStart(startTimestamp);
        entityToSave.setTotalCostsPeriod(results.bruttoTotalCostPeriod());
        entityToSave.setCostDiffPeriod(results.costDiffPeriod());
        entityToSave.setDaysPeriod(results.daysBetween());
        entityToSave.setSumUsedEnergy(results.totalConsumptionKwh());
        entityToSave.setPaidAmountPeriod(results.paidAmountPeriod());
        entityToSave.setUsedEnergyPerDay(results.usedEnergyPerDay());

        // Speichern
        calculationRepository.save(entityToSave);
    }

    /**
     * Ruft nur die gespeicherte Historie ab (ohne Neuberechnung).
     *
     * @param username Der Name des (authentifizierten) Users
     */
    public List<CalculationEntity> getHistory(String username) {
        UserEntity user = userService.findUserByName(username);

        return calculationRepository.findByUserIdOrderByPeriodEndAsc(user.getId());
    }

    /**
     * Hilfsfunktion berechnet die voraussichtliche Anzahl der Abrechnungsmonate
     * anhand der übergebenen Parameter
     *
     * @param startDate Startdatum des Abrechnungszeitraums
     * @param endDate Enddatum des Abrechnungszeitraums
     * @param dueDay Abbuchungstag im Monat
     * @param sepaProcessingDays Anzahl der Tage für die Lastschriftankündigung (SEPA)
     *
     * @return ExpectedAmountMonthsResult: record (int expectedMonths, String message)
     */
    private ExpectedAmountMonthsResult getExpectedAmountPeriod(LocalDate startDate, LocalDate endDate,
                                                              Integer dueDay, Integer sepaProcessingDays) {
        int expectedMonths = 0;
        String message = null;

        // Finde den ERSTEN möglichen Zahltag nach Vertragsbeginn,
        // wenn Vertrag am 22.07. startet, ist der dueDay (z.B. 05.07.) vorbei,
        // d.h. der 05.08. ist theoretisch der erste Abrechnungsmonat.
        LocalDate firstDue = startDate.withDayOfMonth(dueDay);
        if (firstDue.isBefore(startDate)) {
            firstDue = firstDue.plusMonths(1);
            message = "Installment months were skipped.";
        }

        // Realitätsnahe Optimierung/Annahme:
        // Viele Versorger brauchen mind. 14 Tage Vorlauf für SEPA.
        // Wenn z.B. Start=22.07. und dueDay=05.08. (nur 14 Tage), wird das oft übersprungen!
        if (ChronoUnit.DAYS.between(startDate, firstDue) <= sepaProcessingDays) {
            firstDue = firstDue.plusMonths(1);
            message = "Installment months were skipped.";
        }

        while (!firstDue.isAfter(endDate)) {
            expectedMonths++;
            firstDue = firstDue.plusMonths(1);
        }

        if(message != null){ message = message + " Expected billing months: " + expectedMonths; }

        return new ExpectedAmountMonthsResult(expectedMonths, message);
    }

    private record ExpectedAmountMonthsResult(int expectedMonths, String message) {}
}
