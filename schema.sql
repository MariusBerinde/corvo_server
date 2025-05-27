-- Estensioni necessarie (per UUID, se usato)
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ENUM per user role
CREATE TYPE role_enum AS ENUM ('Supervisor', 'Worker');

-- Tabella USERS (user è parola riservata)
CREATE TABLE users (
  email VARCHAR(20) PRIMARY KEY,
  username VARCHAR(20) NOT NULL,
  pwd VARCHAR(20) NOT NULL,
  role role_enum DEFAULT 'Worker' NOT NULL
);

-- Tabella SERVICES
CREATE TABLE service (
  id SERIAL PRIMARY KEY,
  ip VARCHAR(20) NOT NULL,
  name VARCHAR(20) NOT NULL,
  description TEXT,
  porta INTEGER,
  automatic_start BOOLEAN NOT NULL,
  state BOOLEAN NOT NULL
);

-- Tabella SERVERS
CREATE TABLE server (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  ip VARCHAR(20) NOT NULL,
  state BOOLEAN NOT NULL,
  name VARCHAR(20),
  descr VARCHAR(20)
);

-- Tabella RULES
CREATE TABLE rule (
  id SERIAL PRIMARY KEY,
  descr TEXT NOT NULL,
  status BOOLEAN NOT NULL DEFAULT false,
  ip VARCHAR(20) NOT NULL,
  service INTEGER REFERENCES service(id)
);

-- Tabella LOG
CREATE TABLE log (
  id SERIAL PRIMARY KEY,
  data VARCHAR(20) NOT NULL,
  "user" VARCHAR(20) REFERENCES users(email),
  server VARCHAR(20) REFERENCES server(ip),
  service INTEGER REFERENCES service(id),
  descr VARCHAR(20) NOT NULL
);
