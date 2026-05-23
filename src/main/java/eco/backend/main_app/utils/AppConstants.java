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
            Bei Fragen wende dich bitte an den Admin.
            
            ___
            
            Freundliche Grüße.
            """;

    public static final String TEXT_RESET_PASSWORD = """
            Hallo %s!
            
            Dein Verifizierungscode lautet: %s
            
            Bitte gib diesen Code in der App ein, um dein Passwort zurücksetzen zu können.
            Bei Fragen wende dich bitte an den Admin.
            
            ___
            
            Freundliche Grüße.
            """;

    public static final String TEXT_USER_REMOVED_BY_ADMIN = """
            Hallo %s!
            
            Dein Profil wurde durch den Admin entfernt.
            Deine hinterlegten Daten wurden ebenfalls vollständig gelöscht.
            
            Bei Fragen wende dich bitte an den Admin.
            
            ___
            
            Freundliche Grüße.
            """;

    public static final String TEXT_USER_STATUS_CHANGED_BY_ADMIN = """
            Hallo %s!
            
            Dein Profil wurde durch den Admin temporär %s.
            Daher ist die Nutzung der App für dich aktuell nicht möglich.
            
            Bei Fragen wende dich bitte an den Admin.
            
            ___
            
            Freundliche Grüße.
            """;
}
