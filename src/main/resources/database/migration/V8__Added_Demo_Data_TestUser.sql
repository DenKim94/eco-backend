-- Demo-Daten
INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52707.0, '2024-09-10 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52839.8, '2024-10-12 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 52940.5, '2024-11-09 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53044.9, '2024-12-08 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53151.6, '2025-01-12 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53266.4, '2025-02-09 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53384.7, '2025-03-14 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53523.4, '2025-04-08 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53612.4, '2025-05-06 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53731.5, '2025-06-11 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 53887.6, '2025-07-22 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 54096.0, '2025-09-07 12:00:00' FROM users WHERE username = '${test_user_name}';

INSERT INTO meter_readings (user_id, kwh_reading, timestamp)
SELECT id, 54177.7, '2025-10-05 12:00:00' FROM users WHERE username = '${test_user_name}';

