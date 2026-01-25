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
import eco.backend.main_app.feature.tracking.TrackingService;
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
    private final TrackingService trackingService;
    private final CalculationRepository calculationRepository;

    public CalculationService(TrackingRepository trackingRepository,
                              UserService userService,
                              ConfigService configService,
                              TrackingService trackingService,
                              CalculationRepository calculationRepository) {

        this.trackingRepository = trackingRepository;
        this.userService = userService;
        this.configService = configService;
        this.trackingService = trackingService;
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

        // User-Konfiguration laden
        ConfigEntity configData = configService.getConfigByUsername(user.getUsername());

        // Lokale Parameter
        TrackingEntity currentEntry;
        TrackingEntity prevEntry;
        String logMessage = null;
        int DAYS_IN_YEAR = 365;
        int MONTHS_IN_YEAR = 12;

        // Referenzdatum aus Configs & Datumsangaben parsen
        LocalDateTime startDate = configData.getReferenceDate();
        LocalDateTime endDate = ReuseHelper.getParsedDateTimeNoFallback(requestDto.endDate());

        // Getrackte Daten laden (absteigend sortiert: neuester Eintrag zuerst)
        List<TrackingEntity> trackedData = (startDate == null || endDate == null) ?
                trackingRepository.findByUserIdOrderByTimestampDesc(user.getId()) :
                trackingRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(user.getId(), startDate, endDate);

        // Validierung der Mindestanzahl an Datenpunkten
        if (trackedData.size() < AppConstants.MIN_DATA_POINTS) {
            throw new GenericException("Not enough data points. At least " + AppConstants.MIN_DATA_POINTS + " are required.", HttpStatus.BAD_REQUEST);
        }

        // Die gewünschten Einträge anhand des Datums finden, sonst Fallback auf den neuesten und ältesten Eintrag
        prevEntry = (startDate == null) ? trackedData.getLast() : trackingService.findEntryByDate(trackedData, startDate.toLocalDate());
        currentEntry = (endDate == null) ? trackedData.getFirst() : trackingService.findEntryByDate(trackedData, endDate.toLocalDate());


        // Prüfung des Datums
        if (!validDates(prevEntry.getTimestamp(), currentEntry.getTimestamp())) {
            throw new GenericException("Invalid date values: The start date must be before the end date.", HttpStatus.BAD_REQUEST);
        }

        // Anzahl der Tage zwischen den beiden Einträgen
        long daysBetween = ChronoUnit.DAYS.between(prevEntry.getTimestamp().toLocalDate(), currentEntry.getTimestamp().toLocalDate());

        // ***** Berechnungslogik *****
        // Differenz der getrackten Werte bzw. verbrauchte Energiemenge [kWh]
        double diffTrackedValues = currentEntry.getReadingValue() - prevEntry.getReadingValue();

        SkippedMonthsResults skippedMonths = estimateSkippedMonths(prevEntry.getTimestamp().toLocalDate(), configData.getDueDay(), configData.getSepaProcessingDays());

        if (skippedMonths.message != null) { logMessage = skippedMonths.message; }

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
        double paidAmountPeriod = configData.getMonthlyAdvance() * (MONTHS_IN_YEAR - skippedMonths.value)/DAYS_IN_YEAR * daysBetween;

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
     * Hilfsfunktion berechnet die voraussichtliche Anzahl der übersprungenen Abrechnungsmonate
     * anhand der übergebenen Parameter
     *
     * @param startDate Startdatum des Abrechnungszeitraums
     * @param dueDay Abbuchungstag im Monat
     * @param sepaProcessingDays Anzahl der Tage für die Lastschriftankündigung (SEPA)
     *
     * @return SkippedMonthsResults: record (int value, String message)
     */
    private SkippedMonthsResults estimateSkippedMonths(LocalDate startDate, Integer dueDay, Integer sepaProcessingDays) {

        int skippedMonths = 0; // Zähler für übersprungene Monate
        String message = null;

        // Basis-Startpunkt finden
        LocalDate currentDue = startDate.withDayOfMonth(dueDay);

        // Wenn der Stichtag im Startmonat schon vorbei ist (z.B. Start 22.07., Due 05.),
        // muss es logisch eh erst im nächsten Monat weitergehen.
        // Das zählt technisch oft noch nicht als "ausgesetzt", sondern einfach als "Kalender-Logik".
        // Wenn du das aber als "Skipped" zählen willst, mach hier value++
        if (currentDue.isBefore(startDate)) {
            currentDue = currentDue.plusMonths(1);
        }

        // SEPA-Check (Business-Logik)
        // Wenn der erste theoretische Termin zu nah am Startdatum liegt -> Überspringen
        if (ChronoUnit.DAYS.between(startDate, currentDue) <= sepaProcessingDays) {
            skippedMonths++;
        }

        // Message ausgeben
        if (skippedMonths > 0) {
            message = " " + skippedMonths + " installment month(s) were skipped due to SEPA processing time.";
        }

        return new SkippedMonthsResults(skippedMonths, message);
    }

    private record SkippedMonthsResults(int value, String message) {}
}
