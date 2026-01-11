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

        // Lokale Variablen
        TrackingEntity currentEntry;
        TrackingEntity prevEntry;
        String logMessage;
        int DAYS_IN_YEAR = 365;
        int MONTHS_IN_YEAR = 12;

        // Datumsangaben parsen
        LocalDateTime startDate = ReuseHelper.getParsedDateTimeNoFallback(requestDto.startDate());
        LocalDateTime endDate = ReuseHelper.getParsedDateTimeNoFallback(requestDto.endDate());

        // User-Konfiguration laden
        ConfigEntity configData = configService.getConfigByUsername(user.getUsername());

        // Getrackte Daten laden (absteigend sortiert: neuester Eintrag zuerst)
        List<TrackingEntity> trackedData = trackingRepository.findByUserIdOrderByTimestampDesc(user.getId());

        // Validierung der Mindestanzahl an Datenpunkten
        if (trackedData.size() < AppConstants.MIN_DATA_POINTS) {
            throw new GenericException("Not enough data points. At least " + AppConstants.MIN_DATA_POINTS + " are required.", HttpStatus.BAD_REQUEST);
        }

        if (useDefaultCalculation(startDate, endDate)) {
            logMessage = "Fallback: Calculation has been executed for the newest and oldest entry.";
            // Finde den neuesten Eintrag
            currentEntry = trackedData.getFirst();
            // Finde den ältesten Eintrag (Referenzwert)
            prevEntry = trackedData.getLast();

        }else{
            if (!validDates(startDate, endDate)) {
                throw new GenericException("Invalid date range: start-date must be before end-date.", HttpStatus.BAD_REQUEST);
            }

            // Finde die gewünschten Einträge anhand des Datums
            prevEntry = findEntryByDate(trackedData, startDate.toLocalDate());
            currentEntry = findEntryByDate(trackedData, endDate.toLocalDate());

            logMessage = "Calculation has been executed for the given date range.";
        }

        // Anzahl der Tage zwischen den beiden Einträgen
        long daysBetween = ChronoUnit.DAYS.between(prevEntry.getTimestamp().toLocalDate(), currentEntry.getTimestamp().toLocalDate());

        // ***** Berechnungslogik *****
        double absDiffTrackedValues = Math.abs(currentEntry.getReadingValue() - prevEntry.getReadingValue());

        if (absDiffTrackedValues == 0) {
            throw new GenericException("No change in tracked values.", HttpStatus.BAD_REQUEST);
        }

        // Normierter Verbrauch pro Tag [kWh/Tag]
        double normConsumptionPerDay = absDiffTrackedValues / daysBetween;
        
        // Netto Verbrauchspreis berechnen [€/kWh]
        double netConsumptionPrice = configData.getEnergyPrice()/(1 + configData.getVatRate()) - configData.getEnergyTax();
        
        // Netto Verbrauchskosten berechnen [€]
        double netConsumptionCostPeriod = absDiffTrackedValues * netConsumptionPrice;
        
        // Netto Grundpreis berechnen [€]
        double netBasePrice = configData.getBasePrice()/(1 + configData.getVatRate());
        
        // Netto Grundkosten berechnen [€]
        double netBaseCostPeriod = netBasePrice * MONTHS_IN_YEAR/DAYS_IN_YEAR * daysBetween;

        // Netto Gesamtkosten der Stromsteuer berechnen [€]
        double netEnergyTaxCostPeriod = configData.getEnergyTax() * absDiffTrackedValues;

        // Netto Gesamtkosten anhand der verbrauchten Energiemenge berechnen [€]
        double netTotalCostPeriod = (netConsumptionCostPeriod)*(1 + AppConstants.BUFFER_FACTOR) + netBaseCostPeriod + netEnergyTaxCostPeriod;

        // Brutto Gesamtkosten berechnen [€]
        double bruttoTotalCostPeriod = netTotalCostPeriod * (1 + configData.getVatRate());

        // Gezahlten Abschläge im betrachteten Abrechnungszeitraum
        double paidAmountPeriod = configData.getMonthlyAdvance() * MONTHS_IN_YEAR/DAYS_IN_YEAR * daysBetween;

        // Brutto Restbetrag berechnen [€]
        double costDiffPeriod = paidAmountPeriod - bruttoTotalCostPeriod + configData.getAdditionalCredit();

        // Ergebnisse übergeben
        return new CalculationResultsDto(
                configData.getMeterIdentifier(),
                startDate.toLocalDate(),
                endDate.toLocalDate(),
                daysBetween,
                paidAmountPeriod,
                bruttoTotalCostPeriod,
                netTotalCostPeriod,
                absDiffTrackedValues,
                costDiffPeriod,
                normConsumptionPerDay,
                logMessage
        );
    }

    /**
     * Hilfsfunktion prüft, ob das Startdatum vor dem Enddatum liegt.
     */
    private boolean validDates(LocalDateTime startDate, LocalDateTime endDate){
        return startDate.isBefore(endDate);
    }

    /**
     * Hilfsfunktion prüft, ob das Startdatum oder das Enddatum null ist.
     */
    private boolean useDefaultCalculation(LocalDateTime startDate, LocalDateTime endDate){
        return (startDate == null || endDate == null);
    }

    /**
     * Hilfsfunktion, um einen Eintrag anhand eines Datums in TrackingEntity zu finden.
     */
    private TrackingEntity findEntryByDate(List<TrackingEntity> trackedData, LocalDate date){

        return trackedData.stream()
                .filter(e -> e.getTimestamp().toLocalDate().isEqual(date))
                .findFirst()
                .orElseThrow(() -> new GenericException("No entry found for the given date.", HttpStatus.BAD_REQUEST));
    }

    // TODO [11.01.2026]: Berechneten Ergebnisse aus CalculationResultsDto in das calculationRepository schreiben und speichern

    /**
     * Ruft nur die gespeicherte Historie ab (ohne Neuberechnung).
     */
    public List<CalculationEntity> getHistory(String username) {
        UserEntity user = userService.findUserByName(username);
        return calculationRepository.findByUserIdOrderByPeriodEndDesc(user.getId());
    }

}
