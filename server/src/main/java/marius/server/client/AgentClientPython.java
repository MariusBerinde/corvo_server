package marius.server.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import marius.server.data.AgentService;
import marius.server.data.Rules;
import marius.server.data.dto.*;
import marius.server.repo.ServiceRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AgentClientPython {
    @Value("${agent.local.port:5000}")
    private int defaultPort;

    @Value("${agent.local.host:localhost}")
    private String defaultHost;

    private RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(AgentClientPython.class);
    private static ServiceRepo serviceRepo;

    public AgentClientPython(RestTemplate restTemplate , ServiceRepo serviceRepo) {
        this.restTemplate = restTemplate;
        this.serviceRepo = serviceRepo;
    }

    /**
     * Method that make a http get request to the agent python
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return true if the status of the request is successful
     */
    public boolean pingAgent(String host, int port){
        log.info("pingAgent(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/ping", host, port);
        try {
            AgentPingResponse response = restTemplate.getForObject(url, AgentPingResponse.class);
            return response != null && "success".equals(response.getStatus()) && "Agent is running".equals(response.getMessage());
        }catch (RestClientException e){
            log.error("Pinging agent failed at {}:{}, error = {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * Method that make a http get request to the local agent python using default values
     * @return true if the status of the request is successful
     */
    public boolean pingLocalAgent(){
        return pingAgent(defaultHost, defaultPort);
    }

    /**
     * Sets the username of the user interacting with the server
     *
     * @param host the host address of the agent
     * @param port the port of the agent
     * @param username the username to set
     * @return true if username is set, false otherwise
     */
    public boolean setActiveUser(String host, int port, String username){

        log.info("setActiveUser (host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/set_user", host, port);
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
                log.info("✅ Utente '{}' registrato con successo su {}:{}: {}", username, host, port, response.getMessage());
            } else {
                log.error("❌ Registrazione fallita per '{}' su {}:{}: {}", username, host, port, response != null ? response.getMessage() : "nessuna risposta");
            }
            return success;
        } catch (RestClientException e){
            log.error("❌ setActiveUser fallito su {}:{}: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * Sets the username of the user interacting with the local server using default values
     *
     * @param username the username to set
     * @return true if username is set, false otherwise
     */
    public boolean setActiveUser(String username){
        return setActiveUser(defaultHost, defaultPort, username);
    }

    /**
     * Returns the info of the server data
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return a json node
     */
    public JsonNode getStatusServer(String host, int port){
        String url = String.format("http://%s:%d/get_status", host, port);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response;
        } catch (RestClientException e){
            log.error("❌ getStatusServer fallito su {}:{}: {}", host, port, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the info of the local server data using default values
     * @return a json node
     */
    public JsonNode getStatusServer(){
        return getStatusServer(defaultHost, defaultPort);
    }

    /**
     * Returns the logs of the server
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return a json node
     */
    public JsonNode getLogsServer(String host, int port){
        log.info("getLogsServer(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/get_logs", host, port);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response;
        } catch (RestClientException e){
            log.error("❌ getLogsServer fallito su {}:{}: {}", host, port, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the logs of the local server using default values/se
     * @return a json node
     */
    public JsonNode getLogsServer(){
        return getLogsServer(defaultHost, defaultPort);
    }

    /**
     * Returns the Lynis report content
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return a json node
     */
    public JsonNode getLynisReport(String host, int port){
        log.info("getLynisReport(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/get_report_content", host, port);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return response;
        } catch (RestClientException e){
            log.error("getLynisReport fallito su {}:{}: {}", host, port, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the Lynis report content from local server using default values
     * @return a json node
     */
    public JsonNode getLynisReport(){
        return getLynisReport(defaultHost, defaultPort);
    }

    /**
     * Adds Lynis rules to the agent
     * @param host the host address of the agent
     * @param port the port of the agent
     * @param rules list of rule IDs to add
     * @return true if rules are added successfully, false otherwise
     */
    public boolean addLynisRules(String host, int port, List<String> rules){
        log.info("addLynisRules(host: {}, port: {})", host, port);
        log.info("addLynisRules(host: {}, port: {}) rules = {}", host, port, rules);

        if(host == null || host.isEmpty()){
            host = defaultHost;
        }

        String url = String.format("http://%s:%d/add_rules", host, port);
        log.info("URL finale: {}", url);

        // Crea il JSON body
        Map<String, List<String>> requestBody = new HashMap<>();
        requestBody.put("rules", rules);

        // Debug del JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(requestBody);
            log.info("JSON che verrà inviato: {}", jsonString);
            log.info("Lunghezza JSON: {}", jsonString.length());
        } catch (Exception e) {
            log.error("Errore serializzazione JSON: {}", e.getMessage());
            return false;
        }

        // Imposta gli headers per simulare curl
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "curl/7.68.0");
        headers.set("Accept", "*/*");
        headers.set("Content-Length", String.valueOf(jsonString.length()));

        // Log degli headers
        log.info("Headers che verranno inviati:");
        headers.forEach((key, value) -> log.info("  {}: {}", key, value));

        // Crea l'HttpEntity
        HttpEntity<Map<String, List<String>>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Usa exchange per avere più controllo
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("Status code: {}", responseEntity.getStatusCode());
            log.info("Response headers: {}", responseEntity.getHeaders());
            log.info("Response body: {}", responseEntity.getBody());

            // Prova a parsare la risposta
            try {
                AgentResponseDTO response = mapper.readValue(responseEntity.getBody(), AgentResponseDTO.class);
                boolean success = response != null && "success".equalsIgnoreCase(response.getStatus());

                if (success) {
                    log.info("✅ Regole Lynis aggiunte con successo su {}:{}: {}", host, port, rules);
                } else {
                    log.error("❌ Aggiunta regole Lynis fallita su {}:{}: {}", host, port, response.getMessage());
                }
                return success;
            } catch (Exception e) {
                log.error("Errore parsing risposta: {}", e.getMessage());
                return false;
            }

        } catch (RestClientException e){
            log.error("❌ addLynisRules fallito su {}:{}: {}", host, port, e.getMessage());

            // Se è un HttpClientErrorException, ottieni più dettagli
            if (e instanceof HttpClientErrorException) {
                HttpClientErrorException httpEx = (HttpClientErrorException) e;
                log.error("Status code: {}", httpEx.getStatusCode());
                log.error("Response body: {}", httpEx.getResponseBodyAsString());
                log.error("Response headers: {}", httpEx.getResponseHeaders());
            }

            return false;
        }
    }

    /**
     * Adds Lynis rules to the local agent using default values
     * @param rules list of rule IDs to add
     * @return true if rules are added successfully, false otherwise
     */
    public boolean addLynisRules(List<String> rules){
        return addLynisRules(defaultHost, defaultPort, rules);
    }
    /**
     * Returns the Lynis report content as plain text
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return the report content as String, or null if error occurs
     */
    public String getLynisReportText(String host, int port){
        String url = String.format("http://%s:%d/get_lynis_report", host, port);
        try {
            // Configura RestTemplate per ricevere text/plain
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Report Lynis ottenuto con successo :{}", host, port);

                // Log opzionale del nome file se presente negli headers
                String filename = response.getHeaders().getFirst("X-Filename");
                if (filename != null) {
                    log.info("📄 Nome file report: {}", filename);
                }

                return response.getBody();
            } else {
                log.error("❌ Errore HTTP nell'ottenere report Lynis da {}:{}: status {}",
                        host, port, response.getStatusCode());
                return null;
            }
        } catch (RestClientException e){
            log.error("❌ getLynisReportText fallito su {}:{}: {}", host, port, e.getMessage());
            return null;
        }
    }

    /**
     * Returns the Lynis report content as plain text from local server using default values
     * @return the report content as String, or null if error occurs
     */
    public String getLynisReportText(){
        return getLynisReportText(defaultHost, defaultPort);
    }

    /**
     * Starts a Lynis scan on the agent
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return true if scan is started successfully, false otherwise
     */
    public boolean startLynisScan(String host, int port){
        log.info("startLynisScan(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/start_lynis_scan", host, port);
        try {
            AgentResponseDTO response = restTemplate.getForObject(url, AgentResponseDTO.class);
            boolean success = response != null && "success".equalsIgnoreCase(response.getStatus());

            if (success) {
                log.info("✅ Scansione Lynis avviata con successo su {}:{}: {}", host, port, response.getMessage());
            } else if (response != null && "error".equalsIgnoreCase(response.getStatus())) {
                // Gestisce i diversi tipi di errore
                if (response.getMessage().contains("già in corso")) {
                    log.warn("⚠️ Scansione Lynis già in corso su {}:{}: {}", host, port, response.getMessage());
                } else if (response.getMessage().contains("non riconosciuto")) {
                    log.error("❌ Utente non autorizzato per scansione Lynis su {}:{}: {}", host, port, response.getMessage());
                } else {
                    log.error("❌ Errore nell'avvio scansione Lynis su {}:{}: {}", host, port, response.getMessage());
                }
            } else {
                log.error("❌ Avvio scansione Lynis fallito su {}:{}: nessuna risposta valida", host, port);
            }

            return success;
        } catch (RestClientException e){
            log.error("❌ startLynisScan fallito su {}:{}: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * Starts a Lynis scan on the local agent using default values
     * @return true if scan is started successfully, false otherwise
     */
    public boolean startLynisScan(){
        return startLynisScan(defaultHost, defaultPort);
    }


    /**
     * Gets the status of various services and maps them to Services entities
     * @param host the host address of the agent
     * @param port the port of the agent
     * @return a list of Services entities representing each service's status
     */

    /*
    public List<AgentService> getServiceStatus(String host, int port) {
        log.info("getServiceStatus(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/get_service_status", host, port);
        try {
            ServiceStatusResponseDTO response = restTemplate.getForObject(url, ServiceStatusResponseDTO.class);
            if (response != null && "success".equalsIgnoreCase(response.getStatus())) {
                log.info("✅ Stato servizi ottenuto da {}:{}", host, port);

                List<AgentService> servicesList = new ArrayList<>();
                for (Map<String, Boolean> serviceEntry : response.getStatusServices()) {
                    for (Map.Entry<String, Boolean> entry : serviceEntry.entrySet()) {
                        String serviceName = entry.getKey().trim();
                        boolean state = entry.getValue();
                        if (host.equals( "127.0.0.1")){
                            log.info("getServiceStatus forzo l'ip a quello della wsl");
                            host = "172.22.59.12";
                        }

                        AgentService service = new AgentService(host, serviceName, "todo", port, false, state);


                        servicesList.add(service);
                    }
                }

                return servicesList;
            } else {
                log.error( "⚠️ Risposta non valida da getServiceStatus su {}:{}: {}", host, port, response != null ? response.getMessage() : "nessuna risposta");
            }
        } catch (RestClientException e){
            log.error("❌ getServiceStatus fallito su {}:{}: {}", host, port, e.getMessage());
        }

        return Collections.emptyList();
    }
    */
    public List<AgentService> getServiceStatus(String host, int port) {
        log.info("getServiceStatus(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/get_service_status", host, port);
        try {
            ServiceStatusResponseDTO response = restTemplate.getForObject(url, ServiceStatusResponseDTO.class);
            if (response != null && "success".equalsIgnoreCase(response.getStatus())) {
                log.info("✅ Stato servizi ottenuto da {}:{}", host, port);
                List<AgentService> servicesList = new ArrayList<>();

                // Gestione del cambio IP per WSL
                String actualHost = host;
                if ("127.0.0.1".equals(host)) {
                    log.info("getServiceStatus forzo l'ip a quello della wsl");
                    actualHost = "172.22.59.12";
                }

                for (ServiceStatusResponseDTO.ServiceInfo serviceInfo : response.getStatusServices()) {
                    String serviceName = serviceInfo.getName().trim();
                    boolean state = serviceInfo.isStatus();
                    boolean automaticStart = serviceInfo.isAutomaticStart();

                    log.info("getServiceStatus servizo creato con ip {} nome {} automaticStart{} stato {}", actualHost,serviceName, automaticStart, state);

                    AgentService service = new AgentService(host, serviceName, "todo", port, automaticStart, state);
                    servicesList.add(service);
                }
                return servicesList;
            } else {
                log.error("⚠️ Risposta non valida da getServiceStatus su {}:{}: {}", host, port, response != null ? response.getMessage() : "nessuna risposta");
            }
        } catch (RestClientException e) {
            log.error("❌ getServiceStatus fallito su {}:{}: {}", host, port, e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * get the rules and the status of the security rules on the system connected with ip and port
     * @param host the ip v4 address of the agent
     * @param port the port used by the server for comunicate with the server
     * @return a list of Rules for the ip
     */
    public List<Rules> getSystemRules(String host, int port) {
        log.info("getSystemRules(host: {}, port: {})", host, port);
        String url = String.format("http://%s:%d/get_rules", host, port);
        try {
            // Cambiato per usare una struttura dati che corrisponde alla risposta reale
            SystemRulesResponseDTO response = restTemplate.getForObject(url, SystemRulesResponseDTO.class);

            if (response != null && "success".equalsIgnoreCase(response.getStatus())) {
                log.info("✅ Stato servizi ottenuto da {}:{}", host, port);
                List<Rules> rulesList = new ArrayList<>();

                // Processa l'array di regole dal campo "message"
                if (response.getMessage() != null) {
                    for (RuleDTO rule : response.getMessage()) {
                        String finalHost = host;
                        if ("127.0.0.1".equals(host)) {
                            log.info("getSystemRules forzo l'ip a quello della wsl");
                            finalHost = "172.22.59.12";
                        }

                        Rules localRule = new Rules(
                                rule.getName(),           // name
                                rule.getDescription(),    // descr
                                rule.isStatus(),         // status
                                finalHost               // ip
                        );
                        rulesList.add(localRule);
                    }
                }
                return rulesList;
            } else {
                log.error("⚠️ Risposta non valida da getSystemRules su {}:{}: {}",
                        host, port, response != null ? response.toString() : "nessuna risposta");
            }
        } catch (RestClientException e) {
            log.error("❌ getSystemRules fallito su {}:{}: {}", host, port, e.getMessage());

        }
        return Collections.emptyList();
    }


    @Transactional
    public boolean deleteOldServices(String ip) {
        return serviceRepo.deleteByIp(ip)>0;
    }
}
