package eco.backend.main_app.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReuseHelper {
    /** Hilfsmethode: Datum aus String zu LocalDateTime parsen */
    static public LocalDateTime getParsedDateTime(String date) {
        LocalDateTime timestamp;

        // Falls kein Datum übergeben wird, wird aktuelles Datum verwendet
        if (date != null && !date.isBlank()) {
            // Falls String vorhanden: Parsen + Tagesanfang
            LocalDate localDate = LocalDate.parse(date, AppConstants.JSON_DATE_FORMATTER);
            timestamp = localDate.atTime(LocalTime.now());

        } else {
            // Falls leer/null
            timestamp = LocalDateTime.now();
        }
        return timestamp;
    }
}
