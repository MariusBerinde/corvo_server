package marius.server.client;

import marius.server.controller.ServerController;
import marius.server.data.dto.AgentPingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AgentClientPython {
    @Value("${agent.local.port:5000}")
    private int port;

    private RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(AgentClientPython .class);

    public AgentClientPython(RestTemplate restTemplate){
        this.restTemplate = restTemplate;
    }

    /**
     * Method that make a http get request to the local agent python
     * @return true if the status of the request o
     */
    public boolean pingLocalAgent(){
        String url = String.format("http://localhost:%d/ping",port);
        try {
            AgentPingResponse response = restTemplate.getForObject(url, AgentPingResponse.class);
            return response != null && "success".equals(response.getStatus()) && "Agent is running".equals(response.getMessage());

        }catch (RestClientException e){
            log.error("Pinging local agent failed error ="+e.getMessage());
            return false;
        }
    }
}
