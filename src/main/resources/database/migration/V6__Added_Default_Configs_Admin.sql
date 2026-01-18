INSERT INTO configs (
    user_id,
    base_price,             -- Grundpreis EUR/Monat (brutto)
    energy_price,           -- Verbrauchspreis EUR/kWh (brutto)
    energy_tax,             -- Stromsteuer in EUR/kWh
    vat_rate,               -- Umsatzsteuer (Relativ z.B. 0.19)
    monthly_advance,        -- Monatlicher Abschlag in EUR/Monat
    additional_credit,      -- Guthaben in EUR
    due_day,                -- Abrechnungstag im Monat  (z.B. 5: Zum 5. des Monats)
    sepa_processing_days,    -- Anzahl der Tage für die Lastschriftankündigung (SEPA)
    meter_identifier        -- Zählernummer
)

SELECT
    id,                 -- Wert wird aus der 'users' Tabelle entnommen und in die 'configs' Tabelle eingefügt
    11.90,              -- base_price
    0.3467,             -- energy_price
    0.0205,             -- energy_tax
    0.19,               -- vat_rate
    50.00,              -- monthly_advance
    0.00,               -- additional_credit
    5,                  -- due_day
    15,                 -- sepa_processing_days
    'EMPTY-METER-ID'    -- meter_identifier

FROM users
WHERE username = 'admin'
ON CONFLICT (user_id) DO NOTHING;