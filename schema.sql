CREATE TABLE ApprovedUsers(
	id SERIAL PRIMARY KEY,
	email VARCHAR(255) NOT NULL UNIQUE,  
	data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE users (
  email VARCHAR(255) PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  pwd VARCHAR(255) NOT NULL,
  role INTEGER NOT NULL DEFAULT 1 CHECK (role IN (0, 1))
);

-- Tabella SERVERS  
CREATE TABLE server (
  id SERIAL PRIMARY KEY,
  ip VARCHAR(20) NOT NULL UNIQUE,  -- IP deve essere unico se lo usi come riferimento
  state BOOLEAN NOT NULL,
  name VARCHAR(20) NOT NULL,
  descr VARCHAR(255)
);

-- Tabella SERVICES
CREATE TABLE service (
  id SERIAL PRIMARY KEY,
  ip VARCHAR(20) not NULL,
  name VARCHAR(20) NOT NULL,
  description TEXT,
  porta INTEGER,
  automatic_start BOOLEAN NOT NULL,
  state BOOLEAN NOT NULL,
  constraint fk_ip FOREIGN KEY(ip) REFERENCES public.server(ip)
);

-- Tabella RULES
CREATE TABLE rule (
  id SERIAL PRIMARY KEY,
  descr TEXT NOT NULL,
  status BOOLEAN NOT NULL DEFAULT false,
  ip VARCHAR(20) NOT NULL,
  server_id INTEGER REFERENCES server(id),  -- Foreign key corretta
  service_id INTEGER REFERENCES service(id)  -- Nome coerente
);

-- Tabella LOG
CREATE TABLE log (
  id SERIAL PRIMARY KEY,
  data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- TIMESTAMP invece di VARCHAR
  user_email VARCHAR(100) REFERENCES users(email),  -- Nome più chiaro
  server_id INTEGER REFERENCES server(id),  -- Foreign key corretta all'ID
  service_id INTEGER REFERENCES service(id),  -- Nome coerente
  descr TEXT NOT NULL  -- TEXT invece di VARCHAR(20) per più flessibilità
);
