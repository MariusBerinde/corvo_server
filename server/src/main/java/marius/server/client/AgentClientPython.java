package marius.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marius.server.controller.ServerController;
import marius.server.data.dto.AgentPingResponse;
import marius.server.data.dto.AgentResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    /**
     * Sets the username of the user interacting with the server     *
     *
     * @param username
     * @return true if username is set , false otherwise
     */
    public boolean setActiveUser(String username){
        String url = String.format("http://localhost:%d/set_user", port);

        // Crea il JSON come stringa
        String jsonBody = String.format("{\"name\":\"%s\"}", username);

        // Imposta gli headers


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Crea l'HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        try {
            AgentResponseDTO response = restTemplate.postForObject(url, entity, AgentResponseDTO.class);
            boolean success = response != null &&
                    "success".equalsIgnoreCase(response.getStatus()) &&
                    response.getMessage() != null;
            if (success) {
                log.info("✅ Utente '{}' registrato con successo: {}", username, response.getMessage());
            } else {
                log.error("❌ Registrazione fallita per '{}': {}", username, response != null ? response.getMessage() : "nessuna risposta");
            }
            return success;
        } catch (RestClientException e){
            log.error("❌ setActiveUser fallito: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns the info of the local data of the server
     * @return a json node
     */
    public JsonNode getStatusServer(){
        String url = String.format("http://localhost:%d/get_status", port);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response;
        }  catch (RestClientException e){
        log.error("❌ getStatusServer fallito: {}", e.getMessage());
        return null;
    }

    }

    public JsonNode getLogsServer(){
        String url = String.format("http://localhost:%d/get_logs", port);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response;
        }  catch (RestClientException e){
            log.error("❌ getStatusServer fallito: {}", e.getMessage());
            return null;
    }

    }

    public JsonNode getLynisReport(){

        String url = String.format("http://localhost:%d/get_report_content", port);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response;
        }  catch (RestClientException e){
            log.error("getLynisReport fallito: {}", e.getMessage());
            return null;
        }
    }
}

