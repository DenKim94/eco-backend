package eco.backend.main_app.utils;

import java.time.format.DateTimeFormatter;

public class AppConstants {
    public static final String JSON_DATE_PATTERN = "dd.MM.yyyy";
    public static final String DB_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter JSON_DATE_FORMATTER = DateTimeFormatter.ofPattern(JSON_DATE_PATTERN);

    public static final String TEXT_VERIFY_ACTION = """
            Hallo %s!
            
            Dein Verifizierungscode lautet: %s
            
            Bitte gib diesen Code in der App ein, um die gewünschte Aktion auszuführen.
            
            ___
            
            Freundliche Grüße.
            """;

    public static final String TEXT_RESET_PASSWORD = """
            Hallo %s!
            
            Dein Verifizierungscode lautet: %s
            
            Bitte gib diesen Code in der App ein, um dein Passwort zurücksetzen zu können.
            
            ___
            
            Freundliche Grüße.
            """;
}
