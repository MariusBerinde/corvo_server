# 🐦‍⬛ Corvo — Backend

> Componente server centrale del sistema [Corvo](https://github.com/), sviluppato come progetto di tesi magistrale in collaborazione con **Sinelec S.p.A.**

---

## 📋 Descrizione

`corvo_back` è il core applicativo del sistema Corvo, sviluppato in **Java** con il framework **Spring Boot**. Agisce da orchestratore centrale: raccoglie le informazioni inviate dagli agent distribuiti nella rete, le persiste su **PostgreSQL** e le espone tramite API REST al frontend (`corvo_front`).

Gestisce inoltre l'autenticazione degli operatori, il controllo degli accessi basato su ruoli (RBAC) e la comunicazione asincrona e fault-tolerant con i nodi remoti.

> **Nota:** Questo repository contiene anche il file `docker-compose.yml` per l'avvio dell'intero sistema (frontend + backend + database). Gli altri componenti si trovano nei repository [`corvo_agent`](#) e [`corvo_front`](#).

---

## ✨ Funzionalità principali

- **Registrazione e monitoraggio agent** — gli agent si registrano autonomamente all'avvio; il backend mantiene una connessione periodica (ogni 5 minuti) e gestisce automaticamente disconnessioni e riconnessioni
- **Raccolta dati** — aggregazione di stato servizi, regole di auditing e configurazioni Lynis da tutti gli agent attivi
- **Avvio remoto scansioni Lynis** — invio comandi agli agent e recupero dei report risultanti
- **Autenticazione utenti** — login con verifica password tramite hashing **Argon2id** (memory-hard, salt univoco per utente)
- **RBAC** — due ruoli distinti (`Supervisor` e `Worker`) con permessi differenziati verificati server-side su ogni richiesta
- **Audit trail** — logging persistente su database di tutte le operazioni significative (creazione/modifica utenti, scansioni, configurazione regole)
- **Pre-approvazione registrazione** — whitelist di email autorizzate alla registrazione, gestita dai Supervisor

---

## 🏗️ Struttura del progetto

```
src/
├── main/java/
│   ├── (root)            # Classi di supporto e configurazione (es. CorsConfig)
│   ├── client/           # Comunicazione con gli agent Python
│   │   ├── AgentClientPython.java       # Client REST verso gli agent
│   │   └── LocalAgentRegistration.java  # Gestione asincrona e polling agent
│   ├── controller/       # Controller REST (MVC)
│   │   ├── ServerController.java        # Gestione server, servizi, regole, Lynis
│   │   ├── AuthController.java          # Autenticazione e gestione utenti
│   │   └── AgentController.java         # Forwarding richieste verso agent
│   ├── data/             # DataClass (entità JPA) e DTO
│   │   └── dto/          # Classi DTO per comunicazione con agent
│   └── repo/             # Repository Spring Data JPA (accesso PostgreSQL)
└── resources/
    └── application.properties           # Configurazione Spring Boot
docker-compose.yml                       # Orchestrazione dell'intero sistema
.env.example                             # Template variabili d'ambiente
```

---

## 🗄️ Schema del database

Il database PostgreSQL è composto dalle seguenti tabelle:

### Autenticazione e autorizzazione

| Tabella | Descrizione |
|---|---|
| `approved_users` | Whitelist di email pre-approvate alla registrazione |
| `users` | Utenti registrati: `email` (PK), `username` (UNIQUE), `pwd` (hash Argon2id), `role` (0=Supervisor, 1=Worker) |

### Monitoraggio

| Tabella | Descrizione |
|---|---|
| `server` | Agent registrati: `id`, `ip` (UNIQUE), `state` (online/offline), `name`, `descr` |
| `service` | Servizi di sistema monitorati per ciascun agent (`ip` → FK `server.ip`) |
| `rules` | Regole di auditing configurate per ciascun agent |
| `lynis` | Configurazioni ed esiti delle scansioni Lynis per ciascun agent |

### Audit

| Tabella | Descrizione |
|---|---|
| `log` | Traccia delle operazioni: `user_email`, `ip`, `service`, `descr`, `data` (timestamp automatico) |

---

## 🔌 API REST

Tutte le route validano la presenza dei campi obbligatori e restituiscono i codici HTTP standard (`200`, `400`, `401`, `403`, `404`). Le operazioni privilegiate verificano il ruolo dell'utente interrogando il database.

### `ServerController` — Gestione server, servizi, regole e Lynis

#### Server e Agent
| Metodo | Route | Descrizione |
|---|---|---|
| `GET` | `/getServerByIp` | Recupera informazioni di un agent tramite IP |
| `GET` | `/getAllServers` | Recupera tutti gli agent registrati |
| `POST` | `/addServer` | Aggiunge un server (endpoint di test) |
| `POST` | `/updateDetailServer` | Modifica `name` e `descr` di un agent |
| `POST` | `/addAgent` | Registra un nuovo agent (invocato dall'agent stesso all'avvio) |

#### Servizi
| Metodo | Route | Descrizione |
|---|---|---|
| `GET` | `/getServiceByIp` | Recupera i servizi di un agent specifico |
| `GET` | `/getAllServices` | Recupera tutti i servizi attivi |
| `POST` | `/addService` | Aggiunge un servizio (endpoint di test) |
| `POST` | `/getStatusServices` | Verifica lo stato operativo di un servizio |

#### Logging
| Metodo | Route | Descrizione |
|---|---|---|
| `GET` | `/getUserLogs` | Recupera i log dell'utente autenticato |
| `GET` | `/getAllLogs` | Recupera tutti i log di sistema (**solo Supervisor**) |
| `POST` | `/addLog` | Inserisce un singolo log |
| `POST` | `/addAllLogs` | Inserimento batch di log |

#### Lynis
| Metodo | Route | Descrizione |
|---|---|---|
| `GET` | `/getRulesByIp` | Recupera le regole di sicurezza di un agent |
| `POST` | `/addRule` | Aggiunge una regola (endpoint di test) |
| `POST` | `/getLynisByIp` | Recupera le regole escluse dalle scansioni |
| `POST` | `/addLynisConfig` | Aggiorna la configurazione Lynis di un agent |
| `GET` | `/getLynisReportByIp` | Recupera il report dell'ultima scansione |
| `GET` | `/startLynisScan` | Avvia una scansione Lynis su un agent remoto |

---

### `AuthController` — Autenticazione e gestione utenti

#### Pre-registrazione (**solo Supervisor**)
| Metodo | Route | Descrizione |
|---|---|---|
| `POST` | `/enableUserRegistration` | Aggiunge un'email alla whitelist di registrazione |
| `GET` | `/getApprovedUsers` | Recupera le email approvate |
| `POST` | `/isEmailApproved` | Verifica se un'email è nella whitelist |
| `POST` | `/deleteEnabledUser` | Revoca l'approvazione alla registrazione |

#### Gestione utenti
| Metodo | Route | Descrizione |
|---|---|---|
| `POST` | `/addUser` | Registra un nuovo utente (hash password con Argon2id) |
| `GET` | `/getAllUsers` | Recupera tutti gli utenti (senza campo password) |
| `POST` | `/updateRoleUser` | Modifica il ruolo di un utente (**solo Supervisor**) |
| `POST` | `/deleteUser` | Elimina un utente dal sistema |

#### Autenticazione
| Metodo | Route | Descrizione |
|---|---|---|
| `POST` | `/authUser` | Autentica un utente (email + password in chiaro, verificata via Argon2id) |
| `POST` | `/updatePassword` | Modifica la password dell'utente |

---

### `AgentController` — Forwarding verso agent

Utilizzato per test e come canale di backup per comunicare direttamente con gli agent.

| Route | Descrizione |
|---|---|
| `pingAgent` | Verifica raggiungibilità di un agent |
| `setUser` | Imposta l'utente attivo sull'agent |
| `getStatusServer` | Recupera lo stato dei servizi dall'agent |
| `getLogs` | Recupera i log locali dall'agent |
| `getLynisReport` | Recupera il report Lynis dall'agent |

---

## 👤 Utente amministratore predefinito

All'avvio, il sistema crea automaticamente un utente di tipo **Supervisor** con le seguenti credenziali:

| Campo | Valore |
|---|---|
| Username | `Admin` |
| Email | `admin@gmail.com` |
| Password | `Admin@123_!` |

> ⚠️ **Importante:** Cambiare la password dell'utente `Admin` al primo accesso, prima di mettere il sistema in produzione.

---

## ⚙️ Configurazione

Il backend si configura tramite il file `src/main/resources/application.properties`.

```properties
# Dialetto PostgreSQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

# Gestione tipi timestamp ed enum
spring.jpa.properties.hibernate.type.preferred_instant_jdbc_type=TIMESTAMP

# Porta di ascolto (default 8083, sovrascrivibile tramite variabile d'ambiente)
server.port=${SERVER_PORT:8083}

# Connessione al database PostgreSQL
spring.datasource.url=jdbc:postgresql://<HOST>:<PORT>/<DATABASE>
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>
```

---

## 🐳 Avvio con Docker Compose

Questo repository contiene il `docker-compose.yml` per avviare l'intero sistema (frontend + backend + database).

### Prerequisiti

- Docker e Docker Compose installati

### 1. Configura le variabili d'ambiente

```bash
cp .env.example .env
# Modifica .env con i parametri del tuo ambiente
```

Variabili principali:

```env
# Database
POSTGRES_DB=corvo
POSTGRES_USER=corvo_user
POSTGRES_PASSWORD=<password>

# pgAdmin
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=<password>

# Backend
SERVER_PORT=8083
```

### 2. Avvia i container

```bash
docker compose up -d
```

I servizi saranno disponibili su:

| Servizio | URL |
|---|---|
| Frontend | `http://localhost:4200` |
| Backend API | `http://localhost:8083` |
| pgAdmin | `http://localhost:8081` |

### 3. Ferma i container

```bash
docker compose down
```

Per fermare e rimuovere anche i volumi (⚠️ cancella i dati del database):

```bash
docker compose down -v
```

### Architettura dei container

| Servizio | Immagine base | Note |
|---|---|---|
| `postgres` | `postgres:15-alpine` | Dati persistiti su volume Docker |
| `pgadmin` | `dpage/pgadmin4` | Avvio subordinato all'healthcheck di postgres |
| `corvo-server` | `openjdk:24-jdk-slim` | Eseguito con utente non-root |
| `corvo-front` | `node:18-alpine` → `nginx:alpine` | Multi-stage build |

Tutti i container comunicano sulla rete interna `app_network` (driver `bridge`). Sono configurati `healthcheck` e politica di riavvio `on-failure` per tutti i servizi applicativi.

---

## 🚀 Avvio locale (senza Docker)

### Prerequisiti

- Java 17+
- Maven 3.8+
- PostgreSQL in esecuzione e raggiungibile

### Avvio

```bash
# 1. Clona il repository
git clone https://github.com/<tuo-utente>/corvo_back.git
cd corvo_back

# 2. Configura il database in application.properties

# 3. Compila e avvia
mvn spring-boot:run
```

Il backend si avvia sulla porta `8083` (modificabile tramite `SERVER_PORT`).

---

## 🔒 Sicurezza

### Protezione delle password — Argon2id

Le password non vengono mai memorizzate in chiaro. Il sistema utilizza **Argon2id** con i seguenti parametri:

| Parametro | Valore |
|---|---|
| Memory (`m`) | 47104 KB (46 MiB) |
| Iterations (`t`) | 8 |
| Parallelism (`p`) | 4 thread |
| Salt | 16 byte casuali per utente |

### Controllo degli accessi — RBAC

| Operazione | Worker | Supervisor |
|---|:---:|:---:|
| Pre-approvazione utente | ✗ | ✓ |
| Registrazione | ✓ | ✓ |
| Cambio password | ✓ | ✓ |
| Cancellazione proprio account | ✓ | ✓ |
| Modifica ruoli utenti | ✗ | ✓ |
| Monitoraggio sistemi | ✓ | ✓ |
| Uso di Lynis | ✓ | ✓ |
| Visualizzazione propri log | ✓ | ✓ |
| Visualizzazione log di altri utenti | ✗ | ✓ |

> La comunicazione avviene in **HTTP non cifrato**: il sistema è progettato per reti aziendali isolate. **Non esporre su reti pubbliche.**

---

## 🔄 Comunicazione con gli agent

Quando un agent si registra tramite `/addAgent`, il backend:

1. Valida i parametri ricevuti (IP, porta, nome, descrizione)
2. Esegue un ping di verifica verso l'agent
3. In caso di successo, inserisce l'agent nel database e avvia un thread dedicato (`LocalAgentRegistration`)
4. Il thread esegue un **polling ogni 5 minuti** raccogliendo: stato dei servizi, regole di auditing attive, configurazione Lynis

In caso di disconnessione, il backend ritenta fino a **10 volte** con backoff statico. Al decimo fallimento, l'agent viene marcato come `INACTIVE`. Il pool di thread gestisce fino a **10 agent concorrenti**.

---

## 📄 Contesto del progetto

`corvo_back` fa parte del sistema **Corvo**, sviluppato come progetto di tesi magistrale presso **Sinelec S.p.A.**

---

## 📜 Licenza

Tutti i diritti riservati. Progetto sviluppato nell'ambito di un tirocinio magistrale presso Sinelec S.p.A.
