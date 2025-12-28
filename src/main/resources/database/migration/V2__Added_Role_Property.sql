-- 1. Schema-Erweiterung: Neue Spalte 'role'
-- Default-Wert ist wichtig, damit existierende Zeilen nicht NULL sind
ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'USER';

INSERT INTO users (username, password, email, role, created_at)
VALUES (
    'admin',
    '${admin_hash}',
    'no-email-required',
    'ADMIN',
    '2025-12-28T14:00:00'
);
