-- 1. Schema-Erweiterung: Neue Spalte 'role'
-- Default-Wert ist wichtig, damit existierende Zeilen nicht NULL sind
ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USER';

INSERT INTO users (username, password, email, role, tfa_code, created_at)
VALUES (
    '${admin_name}',
    '${admin_hash}',
    'no-email-required',
    'ADMIN',
    '${admin_tfa_code}',
    '2026-03-29T13:00:00'
);

INSERT INTO users (username, password, email, role, tfa_code, created_at)
VALUES (
    '${test_user_name}',
    '${test_user_hash}',
    '${test_user_email}',
    'USER',
    '${test_user_tfa_code}',
    '2026-03-29T13:30:00'
);