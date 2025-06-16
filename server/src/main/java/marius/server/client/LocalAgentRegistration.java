package marius.server.client;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalAgentRegistration {

    private static final Logger log = LoggerFactory.getLogger(LocalAgentRegistration  .class);
    private final AgentClientPython client;
    public LocalAgentRegistration(AgentClientPython clientService) {
        this.client = clientService;
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
              }
              else{
                  log.info("Agent ping failed");
                  try{
                      Thread.sleep(10000);

                  }catch (InterruptedException e){
                      Thread.currentThread().interrupt();
                      return;
                  }
              }
          }
        }).start();
    }
}
