package marius.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import marius.server.controller.ServerController;
import marius.server.data.AgentService;
import marius.server.data.Lynis;
import marius.server.data.Rules;
import marius.server.data.Server;
import marius.server.repo.LynisRepo;
import marius.server.repo.RulesRepo;
import marius.server.repo.ServerRepo;
import marius.server.repo.ServiceRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
/**
 * Class used for manage the local agent
 */
public class LocalAgentRegistration {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentRegistration.class);
    private final AgentClientPython client;
    private final ServerRepo serverRepo;
    public static ServiceRepo serviceRepo;
    public static RulesRepo rulesRepo;
    private final ServerController serverController;
    private final LynisRepo lynisRepo;
    private final Map<String, ScheduledFuture<?>> pingSchedulers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService;
    private final Long MINUTE_IN_MILLIS = 60000L;
    private final Long TIME_TO_WAIT = MINUTE_IN_MILLIS / 2;

    public LocalAgentRegistration(AgentClientPython client, ServerRepo serverRepo,
                                  @Lazy ServerController serverController, LynisRepo lynisRepo,ServiceRepo serviceRepo,RulesRepo rulesRepo) {
        this.client = client;
        this.serverRepo = serverRepo;
        this.serverController = serverController;
        this.lynisRepo = lynisRepo;
        this.serviceRepo = serviceRepo;
        this.rulesRepo = rulesRepo;
        this.executorService = Executors.newScheduledThreadPool(10, r -> {
            Thread t = new Thread(r, "AgentManager-" + System.currentTimeMillis());
            t.setDaemon(true);
            return t;
        });
    }



    /**
     * Registra un nuovo agente e avvia il processo di inizializzazione
     * @param ip IP dell'agente
     * @param port Porta dell'agente
     * @return true se la registrazione è avviata con successo
     */
    public boolean registerAgent(String ip, int port) {
        log.info("Registrazione nuovo agente: {}:{}", ip, port);

        // Verifica se l'agente è già registrato
        if (pingSchedulers.containsKey(ip)) {
            log.warn("Agente {} già registrato", ip);
            return false;
        }

        // Avvia il processo di inizializzazione in un thread separato
        CompletableFuture.runAsync(() -> initAgent(ip, port), executorService)
                .exceptionally(throwable -> {
                    log.error("Errore durante l'inizializzazione dell'agente {}: ", ip, throwable);
                    return null;
                });

        return true;
    }

    /**
     * Inizializza la comunicazione con un agente specifico
     */
    private void initAgent(String ip, int port) {
        log.info("Inizializzazione agente {}:{}", ip, port);

        boolean connectionEstablished = false;
        int retryCount = 0;
        int maxRetries = 10;

        while (!connectionEstablished && retryCount < maxRetries && !Thread.currentThread().isInterrupted()) {
            try {
                boolean pingResult = client.pingAgent(ip, port);
                if (pingResult) {
                    log.info("Ping riuscito per agente {}", ip);

                    if (setupAgent(ip, port)) {
                        connectionEstablished = true;
                        startPeriodicPing(ip, port);
                        log.info("Agente {} inizializzato con successo", ip);
                    } else {
                        log.warn("Setup fallito per agente {}", ip);
                    }
                } else {
                    log.warn("Ping fallito per agente {}, tentativo {}/{}", ip, retryCount + 1, maxRetries);
                }

                if (!connectionEstablished) {
                    retryCount++;
                    Thread.sleep(TIME_TO_WAIT);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Inizializzazione agente {} interrotta", ip);
                return;
            } catch (Exception e) {
                log.error("Errore durante inizializzazione agente {}: ", ip, e);
                retryCount++;
                try {
                    Thread.sleep(TIME_TO_WAIT);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if (!connectionEstablished) {
            log.error("Impossibile stabilire connessione con agente {} dopo {} tentativi", ip, maxRetries);
            serverController.removeActiveNode(ip);
        }
    }

    /**
     * Method that sets a java user for the agent identified with IP and Port and gets the status of active rules
     * @param ip
     * @param port
     * @return
     */
    private boolean setupAgent(String ip, int port) {
        try {
            // Imposta l'utente attivo
            boolean setUser = client.setActiveUser(ip, port, "java");
            if (!setUser) {
                log.error("Impossibile impostare utente attivo per agente {}", ip);
                return false;
            }

            log.info("Utente impostato con successo per agente {}", ip);

            // Ottieni lo status del server
            JsonNode statusJson = client.getStatusServer(ip, port);
            if (statusJson != null && statusJson.hasNonNull("message")) {
                processServerStatus(statusJson, ip, port);
            }

            List<AgentService> services = client.getServiceStatus(ip,port);
            if (services != null && !services.isEmpty()) {
                this.serviceRepo.saveAll(services);
            }

            List<Rules> localRules = client.getSystemRules(ip, port);
            if (localRules != null && !localRules.isEmpty()) {
                Optional<List<Rules>> oldRules = rulesRepo.findByIp(ip);
                if (!oldRules.isPresent()) {
                    this.rulesRepo.saveAll(localRules);
                }else {
                    log.info("Setup agent try to update the new rules by deletign the old version and save the new ");
                    manageUpdateRules(ip, localRules);
                }
            }



            // Gestisci le regole Lynis
           // handleLynisRules(ip, port);


            return true;

        } catch (Exception e) {
            log.error("Errore durante setup agente {}: ", ip, e);
            return false;
        }
    }

    /**
     *  Updates the server rules by deleting old ones and re-proposing new ones from the agent.
     * @param  ip the ip address of the repo where ip are the rules
     * @param rulesList the updated version of ip
     */
    private void manageUpdateRules(String ip, List<Rules> rulesList) {
        log.info("Manage update rules for {} agente {}", ip, rulesList.size());
       this.rulesRepo.deleteByIp(ip);
       this.rulesRepo.saveAll(rulesList);
    }

    /**
     * Processa lo status del server dall'agente
     */
    private void processServerStatus(JsonNode statusJson, String ip, int port) {
        if (statusJson.get("message").isArray()) {
            JsonNode listMsg = statusJson.get("message");
            if (!listMsg.isEmpty()) {
                List<String> messages = new ArrayList<>();
                for (JsonNode data : listMsg) {
                    messages.add(data.asText());
                }

                String serverIp = messages.get(0).replaceAll("\\n", "").trim();
                log.info("Status server ricevuto per {}: {}", ip, serverIp);

                // Salva il server se non esiste
                if (serverRepo.findByIp(serverIp).isEmpty()) {
                    Server newServer = new Server(serverIp, true, "Agent-" + serverIp, "Auto-registered agent", port);
                    serverRepo.save(newServer);
                    log.info("Nuovo server salvato: {}", serverIp);
                }

            }
        }
    }

    /**
     * Gestisce le regole Lynis per l'agente
     */
    private void handleLynisRules(String ip, int port) {
        boolean isListLoaded = checkLoadingList(ip);
        if (!isListLoaded) {
            List<String> toLoadIds = getLoadingList(ip);
            boolean rulesSent = client.addLynisRules(ip, port, toLoadIds);
            if (rulesSent) {
                Optional<Lynis> config = lynisRepo.findByIp(ip);
                if (config.isPresent()) {
                    config.get().setLoaded(true);
                    lynisRepo.save(config.get());
                    log.info("Regole Lynis caricate per agente {}", ip);
                }
            }
        } else {
            log.info("Regole Lynis già caricate per agente {}", ip);
        }
    }

    /**
     * Avvia il ping periodico per un agente specifico
     */
    private void startPeriodicPing(String ip, int port) {
        log.info("Avvio ping periodico per agente {}", ip);

        ScheduledFuture<?> pingTask = executorService.scheduleWithFixedDelay(() -> {
            try {
                boolean pingResult = client.pingAgent(ip, port);

                if (pingResult) {
                    log.debug("Ping periodico riuscito per agente {}", ip);

                    // Trova il server e aggiungilo come attivo
                    Optional<Server> serverOpt = serverRepo.findByIp(ip);
                    if (serverOpt.isPresent()) {
                        serverController.addActiveNode(serverOpt.get());
                    }

                    List<AgentService> services = client.getServiceStatus(ip,port);
                    if (services != null && !services.isEmpty()) {
                        this.serviceRepo.saveAll(services);
                    }

                    List<Rules> localRules = client.getSystemRules(ip, port);
                    if (localRules != null && !localRules.isEmpty()) {
                        Optional<List<Rules>> oldRules = rulesRepo.findByIp(ip);
                        if (!oldRules.isPresent()) {
                            this.rulesRepo.saveAll(localRules);
                        }else {
                            log.info("Setup agent try to update the new rules by deletign the old version and save the new ");
                            manageUpdateRules(ip, localRules);
                        }
                    }
                    // Verifica e ricarica regole Lynis se necessario
                    handleLynisRules(ip, port);

                } else {
                    log.warn("Ping periodico fallito per agente {}", ip);
                    handlePingFailure(ip, port);
                }

            } catch (Exception e) {
                log.error("Errore durante ping periodico per agente {}: ", ip, e);
                handlePingFailure(ip, port);
            }
        }, 5, 5, TimeUnit.MINUTES);

        pingSchedulers.put(ip, pingTask);
    }

    /**
     * Gestisce il fallimento del ping per un agente specifico
     */
    private void handlePingFailure(String ip, int port) {
        log.warn("Gestione fallimento ping per agente {}", ip);

        // Rimuovi il nodo dalla lista attivi
        serverController.removeActiveNode(ip);

        // Ferma il ping periodico
        stopPeriodicPing(ip);

        // Avvia processo di ri-inizializzazione
        log.info("Riavvio processo di inizializzazione per agente {}", ip);
        registerAgent(ip, port);
    }

    /**
     * Ferma il ping periodico per un agente specifico
     */
    public void stopPeriodicPing(String ip) {
        ScheduledFuture<?> pingTask = pingSchedulers.remove(ip);
        if (pingTask != null && !pingTask.isCancelled()) {
            pingTask.cancel(true);
            log.info("Ping periodico fermato per agente {}", ip);
        }
    }

    /**
     * Rimuove completamente un agente
     */
    public void removeAgent(String ip) {
        log.info("Rimozione agente {}", ip);
        stopPeriodicPing(ip);
        serverController.removeActiveNode(ip);
    }

    /**
     * Verifica se la lista è già caricata
     */
    private boolean checkLoadingList(String ip) {
        log.debug("Verifica caricamento lista per agente {}", ip);
        Optional<Lynis> config = lynisRepo.findByIp(ip);
        if (config.isPresent()) {
            boolean isLoaded = config.get().getLoaded();
            log.debug("Lista caricata per agente {}: {}", ip, isLoaded);
            return isLoaded;
        }
        return false;
    }

    /**
     * Ottiene la lista degli ID da caricare
     */
    private List<String> getLoadingList(String ip) {
        log.debug("Recupero lista caricamento per agente {}", ip);
        Optional<Lynis> config = lynisRepo.findByIp(ip);
        List<String> list = new ArrayList<>();

        if (config.isPresent()) {
            String[] testIds = config.get().getListIdSkippedTest().split(",");
            for (String testId : testIds) {
                list.add(testId.trim());
            }
        }
        return list;
    }

    /**
     * Verifica se un agente è attivo
     */
    public boolean isAgentActive(String ip) {
        return pingSchedulers.containsKey(ip);
    }

    /**
     * Ottiene la lista degli agenti attivi
     */
    public Set<String> getActiveAgents() {
        return new HashSet<>(pingSchedulers.keySet());
    }

    /**
     * Shutdown del servizio
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutdown LocalAgentRegistration...");

        // Ferma tutti i ping periodici
        pingSchedulers.values().forEach(task -> task.cancel(true));
        pingSchedulers.clear();

        // Shutdown executor service
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
