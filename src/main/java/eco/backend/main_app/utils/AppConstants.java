package eco.backend.main_app.utils;

import java.time.format.DateTimeFormatter;

public class AppConstants {
    public static final String JSON_DATE_PATTERN = "dd.MM.yyyy";
    public static final String DB_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter JSON_DATE_FORMATTER = DateTimeFormatter.ofPattern(JSON_DATE_PATTERN);

    public static final String TEXT_VERIFY_EMAIL = """
            Hallo und willkommen!
            
            Dein Verifizierungscode lautet: %s
            
            Bitte gib diesen Code in der App ein, um deine E-Mail zu bestätigen.
            
            
            Viele Grüße.
            """;

    public static final String TEXT_RESET_PASSWORD = """
            Hallo!
            
            Dein Verifizierungscode lautet: %s
            
            Bitte gib diesen Code in der App ein, um dein Passwort zurücksetzen zu können.
            
            
            Viele Grüße.
            """;
}
