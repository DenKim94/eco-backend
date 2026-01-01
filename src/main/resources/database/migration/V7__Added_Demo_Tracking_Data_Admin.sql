-- Demo-Daten (Reale Daten aus 2024-2025)
INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52515, '2024-07-21 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52712, '2024-09-12 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52821.8, '2024-10-12 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52931.2, '2024-11-09 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53041, '2024-12-08 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53165.4, '2025-01-10 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53280.2, '2025-02-09 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53402.4, '2025-03-09 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53508.8, '2025-04-06 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53608.4, '2025-05-06 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53723.2, '2025-06-08 12:00:00' FROM users WHERE username = 'admin';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53906.8, '2025-07-22 12:00:00' FROM users WHERE username = 'admin';
