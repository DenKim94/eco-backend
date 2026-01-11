package eco.backend.main_app.utils;

import java.time.format.DateTimeFormatter;

public class AppConstants {
    public static final String JSON_DATE_PATTERN = "dd.MM.yyyy";
    public static final String DB_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter JSON_DATE_FORMATTER = DateTimeFormatter.ofPattern(JSON_DATE_PATTERN);

    /** Mindestanzahl an Datenpunkten, die für die Berechnung notwendig sind */
    public static final int MIN_DATA_POINTS = 2;

    /** Puffer als Aufschlag-Faktor für die Kostenberechnung [relativer Wert zwischen 0 und 1]:
     * z.B. 0.025 = 2.5 % Puffer */
    public static final double BUFFER_FACTOR = 0.025; //
}
