-- V1__Init_Schema.sql

-- 1. Tabelle: USERS (Haupt-Tabelle)
-- Speichert die zentralen Identitäten.
CREATE TABLE users (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    username     TEXT NOT NULL UNIQUE,
    password     TEXT NOT NULL,
    email        TEXT NOT NULL UNIQUE,
    created_at   TEXT NOT NULL
);

-- 2. Tabelle: METER_READINGS (Sub-Tabelle)
-- Speichert Zählerstände. Verweist auf User.
CREATE TABLE meter_readings (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    kwh_reading  REAL NOT NULL,
    timestamp    TEXT NOT NULL, -- ISO-8601 Format: 'YYYY-MM-DDTHH:MM:SS'
    user_id      INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
-- Index für schnelle Abfragen: "Zeige alle Readings von User X sortiert nach Zeit"
CREATE INDEX idx_readings_user ON meter_readings(user_id, timestamp);


-- 3. Tabelle: CONFIGS (Sub-Tabelle)
-- Preise & Tarifeinstellungen pro User.
CREATE TABLE configs (
    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
    base_price              REAL NOT NULL,      -- Grundpreis EUR/Monat (brutto)
    energy_price            REAL NOT NULL,      -- Verbrauchspreis EUR/kWh (brutto)
    energy_tax              REAL NOT NULL,      -- Stromsteuer in EUR/kWh
    vat_rate                REAL NOT NULL,      -- Umsatzsteuer (z.B. 0.19)
    monthly_advance         REAL NOT NULL,      -- Monatlicher Abschlag in EUR/Monat
    additional_credit       REAL NOT NULL DEFAULT 0.0, -- Guthaben in EUR
    due_day                 INTEGER NOT NULL,   -- Abrechnungstag im Monat (z.B. 5: Zum 5. des Monats)
    sepa_processing_days    INTEGER NOT NULL,   -- Anzahl der Tage für die Lastschriftankündigung (SEPA)
    meter_identifier        TEXT NOT NULL,      -- Zählernummer
    user_id                 INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX idx_configs_user_unique ON configs(user_id);


-- 4. Tabelle: CALCULATED_RESULTS (Sub-Tabelle)
-- Fertige Berechnungsergebnisse (Caching für Historie/Visualisierung).
CREATE TABLE calculated_results (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    period_start        TEXT    NOT NULL, -- Abrechnungszeitraum Start
    period_end          TEXT    NOT NULL, -- Abrechnungszeitraum Ende
    days_period         INTEGER NOT NULL, -- Anzahl der Tage in der Abrechnungszeit
    payments_period     REAL    NOT NULL, -- Summe der Einzahlungen über den Abrechnungszeitraum
    total_costs_period  REAL    NOT NULL, -- Gesamtkosten in EUR [Brutto]
    cost_diff_period    REAL    NOT NULL, -- Nachzahlung (>0) oder Rückerstattung (<0)
    sum_used_energy     REAL    NOT NULL, -- Verbrauch in kWh
    used_energy_per_day REAL    NOT NULL, -- Durchschnittlicher Energieverbrauch pro Tag [kWh/Tag]
    user_id             INTEGER NOT NULL, -- Gehört zu diesem User
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
CREATE INDEX idx_results_user_period ON calculated_results(user_id, period_end);
