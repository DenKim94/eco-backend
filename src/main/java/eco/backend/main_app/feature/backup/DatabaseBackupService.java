package eco.backend.main_app.feature.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DatabaseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseBackupService.class);

    // Jeden Tag um 03:00 Uhr nachts ausführen
    @Scheduled(cron = "0 0 3 * * ?")
    public void backupDatabase() {
        try {
            Path sourceFile = Path.of("database/eco_app.db");

            if (!Files.exists(sourceFile)) {
                logger.warn("Kein Backup erstellt: Die Datenbankdatei '{}' existiert nicht.", sourceFile.toAbsolutePath());
                return;
            }

            // Zeitstempel generieren (z.B. 2026-03-14_03-00)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
            Path backupFile = Path.of("database/backups/eco_app_backup_" + timestamp + ".db");

            // Backup-Ordner anlegen, falls er nicht existiert
            Files.createDirectories(backupFile.getParent());

            // Datei kopieren
            Files.copy(sourceFile, backupFile, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Backup erfolgreich erstellt: {} ", backupFile.getFileName());

        } catch (Exception e) {
            System.err.println("Fehler beim Datenbank-Backup: " + e.getMessage());
        }
    }
}
