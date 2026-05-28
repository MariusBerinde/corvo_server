BEGIN;

-- === Tabelle ===
CREATE TABLE IF NOT EXISTS ApprovedUsers (
                                             id SERIAL PRIMARY KEY,
                                             email VARCHAR(255) NOT NULL UNIQUE,
    data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS users (
                                     email VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    pwd VARCHAR(255) NOT NULL,
    role INTEGER NOT NULL DEFAULT 1 CHECK (role IN (0, 1))
    );

CREATE TABLE IF NOT EXISTS server (
                                      id SERIAL PRIMARY KEY,
                                      ip VARCHAR(20) NOT NULL UNIQUE,
    state BOOLEAN NOT NULL,
    name VARCHAR(20) NOT NULL,
    descr VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS service (
                                       id SERIAL PRIMARY KEY,
                                       ip VARCHAR(20) NOT NULL,
    name VARCHAR(20) NOT NULL,
    description TEXT,
    porta INTEGER NOT NULL,
    automatic_start BOOLEAN NOT NULL,
    state BOOLEAN NOT NULL,
    CONSTRAINT fk_ip FOREIGN KEY(ip) REFERENCES public.server(ip)
    );

CREATE TABLE IF NOT EXISTS rules (
                                     id SERIAL PRIMARY KEY,
                                     name VARCHAR(127),
    descr TEXT NOT NULL,
    status BOOLEAN NOT NULL DEFAULT false,
    ip VARCHAR(20) NOT NULL,
    service INTEGER,
    CONSTRAINT fk_ip FOREIGN KEY(ip) REFERENCES public.server(ip),
    CONSTRAINT fk_service FOREIGN KEY(service) REFERENCES public.service(id)
    );

CREATE TABLE IF NOT EXISTS lynis (
                                     id SERIAL PRIMARY KEY,
                                     auditor VARCHAR(255) NOT NULL,
    ip VARCHAR(20) NOT NULL,
    list_id_skipped_test TEXT,
    loaded BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_lynis_ip FOREIGN KEY(ip) REFERENCES public.server(ip)
    );

CREATE TABLE IF NOT EXISTS log (
                                   id SERIAL PRIMARY KEY,
                                   data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   user_email VARCHAR(255),
    ip VARCHAR(20),
    service INTEGER,
    descr TEXT NOT NULL,
    CONSTRAINT fk_ip FOREIGN KEY(ip) REFERENCES public.server(ip),
    CONSTRAINT fk_service FOREIGN KEY(service) REFERENCES public.service(id)
    );

-- Drop and recreate foreign key for user_email to support ON DELETE SET NULL
ALTER TABLE log
DROP CONSTRAINT IF EXISTS fk_email;

ALTER TABLE log
    ADD CONSTRAINT fk_email
        FOREIGN KEY(user_email) REFERENCES public.users(email)
            ON DELETE SET NULL;

-- === Funzione Trigger ===
CREATE OR REPLACE FUNCTION log_user_deletion() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO log (user_email, ip, service, descr)
VALUES (OLD.email, NULL, NULL, 'Utente eliminato');
RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- === Trigger ===
CREATE TRIGGER trg_before_user_delete
    BEFORE DELETE ON users
    FOR EACH ROW
    EXECUTE FUNCTION log_user_deletion();

COMMIT;
