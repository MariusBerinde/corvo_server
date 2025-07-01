package marius.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import marius.server.controller.ServerController;
import marius.server.data.Server;
import marius.server.repo.ServerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class LocalAgentRegistration {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentRegistration  .class);
    private final AgentClientPython client;
    private final ServerRepo server;
    private final ServerController serverController;
    private ScheduledExecutorService pingScheduler;
    private volatile boolean agentInitialized = false;
    private volatile String currentServerIp;
    private Long MINUTE_IN_MILLIS = 60000L;
    private Long TIME_TO_WAIT =  MINUTE_IN_MILLIS/2;

    public LocalAgentRegistration(AgentClientPython clientService,ServerRepo server,ServerController serverController) {
        this.client = clientService;
        this.server = server;
        this.serverController = serverController;
        this.pingScheduler  = Executors.newSingleThreadScheduledExecutor(
                r->{

                   Thread t = new Thread(r,"PingScheduler Thread");
                    t.setDaemon(true);
                    return t;
                }

        );
    }

    /**
     * used for init the process
     */
    @PostConstruct
    public void init() {
        log.info("Start ping to python agent...");
        new Thread(()->{
            initAgent();
        },"agent-init").start();

    }

    private void initAgent() {
        boolean risPing = false;
        while (!risPing && !Thread.currentThread().isInterrupted()) {
            risPing = client.pingLocalAgent();
            if (risPing) {
                if (setupAgent()){
                    agentInitialized = true;
                    startPeriodicPing();
                    break;

                }else{
                    log.info("Agent ping failed");
                    try{
                        //Thread.sleep(50000);
                        Thread.sleep(TIME_TO_WAIT);
                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }else{

                log.info("Agent ping failed");
                try{
                    //Thread.sleep(50000);
                    Thread.sleep(TIME_TO_WAIT);

                }catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    return;
                }

            }

        }
    }



    /**
     *  Used for make the setup for the first meeting between the java server and the
     *  python agent
     * @return true when the first meeting is made
     */
    private boolean setupAgent() {
        boolean setUser = client.setActiveUser("java");

        if (setUser) {
            log.info("Set user ok");
            JsonNode json = client.getStatusServer();
            /**
             * manage status server
             */
            if (json != null) {
                if(json.hasNonNull("message")){
                    if(json.get("message").isArray()){
                        JsonNode listMsg = json.get("message");
                        if(listMsg.isEmpty()){
                            log.info("msg è un array vuoto");
                        }

                        List<String> msg = new ArrayList<>();
                        for (JsonNode data: json.get("message")){
                            msg.add(data.asText());
                        }
                        String ip = msg.get(0).replaceAll("\\n","").trim();
                        log.info("Get status server: {}", ip);

                        /**
                         * first time encontered server with ip
                         */
                        if(server.findByIp(ip).isEmpty()){
                            server.save(new Server(ip,true,"todo","todo"));
                        }
                        else{
                            log.info("Server già aggiunto");
                        }
                        serverController.addActiveNode(ip);
                        this.currentServerIp = ip;

                    }
                }

            }


            //TODO: add get Active services
        }
        return true;
    }

    private void startPeriodicPing() {
        log.info("Start ping to python agent every 5 seconds...");
        pingScheduler.scheduleWithFixedDelay(()->{
            try{
                boolean pingResult = client.pingLocalAgent();

                //log.debug("Ping periodico successful per server: {}", currentServerIp);
                if (pingResult) {
                    log.info("Ping periodico successful per server: {}", currentServerIp);
                    // Aggiorna stato server come attivo se necessario

                    serverController.addActiveNode(currentServerIp);
                } else {
                    log.warn("Ping periodico failed per server: {}", currentServerIp);
                    serverController.removeActiveNode(currentServerIp);
                    handlePingFailure();
                }
            } catch (Exception e) {
            log.error("Errore durante ping periodico: ", e);
            handlePingFailure();
        }

        },1,1, TimeUnit.SECONDS);
    }

    private void handlePingFailure() {
        // Segna il server come inattivo
        if (currentServerIp != null) {

            serverController.removeActiveNode(currentServerIp);
        }

        // Riavvia il processo di inizializzazione
        agentInitialized = false;
        log.info("Riavvio processo di inizializzazione agent...");

        new Thread(() -> {
            initAgent();
        }, "agent-reinitialization").start();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutdown LocalAgentRegistration...");
        if (pingScheduler != null && !pingScheduler.isShutdown()) {
            pingScheduler.shutdown();
            try {
                if (!pingScheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    pingScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                pingScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // Metodi di utilità per monitoraggio
    public boolean isAgentActive() {
        return agentInitialized;
    }

    public String getCurrentServerIp() {
        return currentServerIp;
    }

}
