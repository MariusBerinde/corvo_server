package marius.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import marius.server.data.Server;
import marius.server.repo.ServerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LocalAgentRegistration {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentRegistration  .class);
    private final AgentClientPython client;
    private final ServerRepo server;
    public LocalAgentRegistration(AgentClientPython clientService,ServerRepo server) {
        this.client = clientService;
        this.server = server;
    }
    @PostConstruct
    public void init() {
        log.info("Start ping to python agent...");
        new Thread(() -> {
            boolean risPing = false;
            while (!risPing) {
                risPing = client.pingLocalAgent();
                if (risPing) {
                    log.info("Agent ping ok");
                    boolean setUser = client.setActiveUser("java");
                    if (setUser) {
                        log.info("Set user ok");
                        JsonNode json = client.getStatusServer();
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

                                    if(server.findByIp(ip).isEmpty()){
                                        server.save(new Server(ip,true,"todo","todo"));
                                    }
                                    else{
                                        log.info("Server già aggiunto");
                                    }

                                }
                            }

                        }

                    }
                }
                else{
                    log.info("Agent ping failed");
                    try{
                        //Thread.sleep(50000);
                        Thread.sleep(100000);

                    }catch (InterruptedException e){
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }).start();
    }
}
