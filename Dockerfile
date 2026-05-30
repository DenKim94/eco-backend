# Stage 1: Build
# Image mit JDK 21 für den Build-Prozess
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle-Wrapper und Konfigurationsdateien kopieren (für Caching)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Wrapper ausführbar machen und Abhängigkeiten laden (ohne den Code zu kompilieren)
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Kopiere den Quellcode
COPY src/ src/

# Anwendung bauen (Tests werden übersprungen, um den Build zu beschleunigen)
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime
# Schlankes JRE 21 Image für die Ausführung
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Ordner für die SQLite Datenbank und Logs erstellen
RUN mkdir -p /app/database && mkdir -p /app/logs

# Das gebaute JAR aus der builder-Stage kopieren
COPY --from=builder /app/build/libs/*.jar app.jar

# Standardport (Fallback), falls in der .env nichts steht
ENV PORT_NUMBER=8080

EXPOSE ${PORT_NUMBER}

# Startbefehl
ENTRYPOINT ["java", "-jar", "app.jar"]
