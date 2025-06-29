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
CREATE TABLE rules (
  id SERIAL PRIMARY KEY,
  descr TEXT NOT NULL,
  status BOOLEAN NOT NULL DEFAULT false,
  ip VARCHAR(20) NOT NULL,
	service integer,
  constraint fk_ip FOREIGN KEY(ip) REFERENCES public.server(ip),
  constraint fk_service FOREIGN KEY(service) REFERENCES public.service(id)
);


CREATE TABLE lynis (
  id SERIAL PRIMARY KEY,
  auditor VARCHAR(255) NOT NULL,
  ip VARCHAR(20) NOT NULL,
  list_id_skipped_test TEXT,
  loaded BOOLEAN not null default false ,
  CONSTRAINT fk_lynis_ip FOREIGN KEY(ip) REFERENCES public.server(ip)
);

-- Tabella LOG
CREATE TABLE log (
  id SERIAL PRIMARY KEY,
  data TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  user_email VARCHAR(255) REFERENCES users(email),  
  ip VARCHAR(20) NOT NULL,  
  service integer,
  descr TEXT NOT NULL  ,
  constraint fk_ip FOREIGN KEY(ip) REFERENCES public.server(ip),
  constraint fk_service FOREIGN KEY(service) REFERENCES public.service(id),
  constraint fk_email FOREIGN KEY(user_email) REFERENCES public.users(email)
);
