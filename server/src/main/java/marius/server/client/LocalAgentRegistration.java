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
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
/*
  Class used for manage the comunication with the user agent
 */
public class LocalAgentRegistration {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentRegistration.class);
    private final AgentClientPython client;
    private final ServerRepo serverRepo;
    private static ServiceRepo serviceRepo;
    private static RulesRepo rulesRepo;
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
     * Init the comunication between java server and the agent python
     * @param ip the IP v4 address of the agent
     * @param port the port
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
            Optional<Server> tmp_server = serverRepo.findByIp(ip);
            if (tmp_server.isPresent()) {
                tmp_server.get().setState(false);
                // set the status of the server down on database
                serverRepo.save(tmp_server.get());
                setsDownServices(ip);
                setsDownRule(ip);

            }
        }
    }

    private void setsDownRule(String ip) {
        List<Rules> dbRules = rulesRepo.findByIp(ip).get();
        for (Rules dbRule : dbRules) {
            dbRule.setStatus(false);
        }
        rulesRepo.saveAll(dbRules);
    }

    private void setsDownServices(String ip) {
        List<AgentService> servicesIp = serviceRepo.findAllByIp(ip);
       for (AgentService service : servicesIp) {
           service.setState(false);
       }
       serviceRepo.saveAll(servicesIp);
    }


    /**
     * Method that sets a java user for the agent identified with IP and Port and gets the status of active rules
     * @param ip the ip of the agent
     * @param port the port user by the agent
     * @return true if the java server make a handshake with the python agent
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
                List<AgentService> dbServices = serviceRepo.findAllByIp(ip);
                if (dbServices != null && !dbServices.isEmpty()) {
                    //updateServices(ip,dbServices,services);
                    updateServicesAlt(ip,services);
                }
                else {
                    this.serviceRepo.saveAll(services);
                }
            }
            else{
                return false;
            }

            List<Rules> localRules = client.getSystemRules(ip, port);
            if (localRules != null && !localRules.isEmpty()) {
                Optional<List<Rules>> oldRules = rulesRepo.findByIp(ip);
                if (!oldRules.isPresent()) {
                    this.rulesRepo.saveAll(localRules);
                }else {
                    log.info("Setup agent try to update the new rules by deletign the old version and save the new ");
                    manageUpdateRules(ip,oldRules.get(), localRules);
                }
            }
            else{
                return false;
            }

            // Gestisci le regole Lynis
            handleLynisRules(ip, port);


            return true;

        } catch (Exception e) {
            log.error("Errore durante setup agente {}: ", ip, e);
            return false;
        }
    }

   private void updateServices(String ip,List<AgentService> dbService,List<AgentService> incomingServices) {
       log.info("Manage update services for {} agente {}", ip,incomingServices.size());

       Map<String, AgentService> oldServicesMap = dbService.stream()
               .collect(Collectors.toMap(AgentService::getName, Function.identity()));

       List<AgentService> newRules = new ArrayList<>();

       for (AgentService updatedRule : incomingServices) {
           AgentService oldService = oldServicesMap.get(updatedRule.getName());
           if (oldService != null ) {
               oldService.setState(updatedRule.isState());
               oldService.setAutomaticStart(updatedRule.isAutomaticStart());
           } else {
               newRules.add(updatedRule); // 👈 è una regola nuova, va aggiunta
           }
       }

       this.serviceRepo.saveAll(dbService);
       this.serviceRepo.saveAll(newRules);

   }

   private void updateServicesAlt(String ip,List<AgentService> incomingServices) {
        log.info("MANAGE UPDATE ALT :services by deleting the old data in the database for {} agente {}", ip,incomingServices.size());
        boolean state = client.deleteOldServices(ip);
        if (!state) {
            log.info("MANAGE UPDATE ALT: old data not deleted");
        }
        else {
            log.info("MANAGE UPDATE ALT: old data deleted");
        }
        this.serviceRepo.saveAll(incomingServices);


   }


    /**
     *  Updates the server rules by deleting old ones and re-proposing new ones from the agent.
     * @param  ip the ip address of the repo where ip are the rules
     * @param rulesList the updated version of ip
     */
    private void manageUpdateRules(String ip, List<Rules> oldRules, List<Rules> rulesList) {
        log.info("Manage update rules for {} agente {}", ip, rulesList.size());

        Map<String, Rules> oldRulesMap = oldRules.stream()
                .collect(Collectors.toMap(Rules::getName, Function.identity()));

        List<Rules> newRules = new ArrayList<>();

        for (Rules updatedRule : rulesList) {
            Rules oldRule = oldRulesMap.get(updatedRule.getName());
            if (oldRule != null) {
                oldRule.setDescr(updatedRule.getDescr());
                oldRule.setStatus(updatedRule.isStatus());
            } else {
                newRules.add(updatedRule); // 👈 è una regola nuova, va aggiunta
            }
        }

        this.rulesRepo.saveAll(oldRules);     // aggiorna le esistenti
        this.rulesRepo.saveAll(newRules);     // inserisce le nuove
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
     * Loads a pending lisk of skippable test form database to agent
     * @param ip the IPV4 agent address
     * @param port the port used by the agent for comunicate with the server
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
                    client.addLynisRules(ip, port, toLoadIds);
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
                    log.info("Ping periodico riuscito per agente {}", ip);
                    setupAgent(ip, port);

                } else {
                    log.error("Ping periodico fallito per agente {}", ip);
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
     * Control if on the server is present a skippable list to load on the agente
     * @param ip the IPV4 address of the agent
     * @return true if a pending list is on server , false otherwise
     */
    private boolean checkLoadingList(String ip) {
        log.info("Verifica caricamento lista per agente {}", ip);
        Optional<Lynis> config = lynisRepo.findByIp(ip);
        if (config.isPresent()) {
            boolean isLoaded = config.get().getLoaded();
            log.info("Lista caricata per agente {}: {}", ip, isLoaded);
            return isLoaded;
        }
        return false;
    }

    /**
     * Loads the pending list of skippable test from database to the server
     * @param ip the IP v4 address of the agent
     * @return a List<String> with the acronysm of the skippable rules from the database
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
