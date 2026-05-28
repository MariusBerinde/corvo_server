package marius.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import marius.server.client.AgentClientPython;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for communicating with a local Python agent service.
 *
 * <p>This controller acts as a bridge between the Java application and a Python-based
 * agent service running on localhost:5000. It provides endpoints for health checking,
 * user management, server status monitoring, and log retrieval from the Python agent.</p>
 *
 * <p>The controller is primarily used for development and testing purposes, allowing
 * direct interaction with the Python agent without going through the main application
 * interface.</p>
 *
 * <p><strong>Prerequisites:</strong></p>
 * <ul>
 * <li>Python agent service must be running on localhost:5000</li>
 * <li>User must be set using {@code /setUser} endpoint before accessing protected resources</li>
 * </ul>
 *
 * @author Marius Dumitru Berinde
 */
@RestController
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController .class);
    /**
     * Object used for launch the  REST request to the Python server
     */
    private  final AgentClientPython client;

    public AgentController(AgentClientPython client) {
        this.client = client;
    }

    /**
     * Performs a health check on the local Python agent service.
     * @return the following message :
     * <ul>
     *     <li> status OK and body "Up" if the agent is reachable </li>
     *     <li> status NOT FOUND otherwise</li>
     * </ul>
     */
    @GetMapping("/pingAgent")
    public ResponseEntity<String> pingAgent(){
        log.info("ping agent to client ");
        boolean status = client.pingLocalAgent();
        return status ? ResponseEntity.ok().body("Up"): ResponseEntity.notFound().build();
    }


    /**
     * Sets the active user context in the Python agent to "java".
     *
     * <p>This endpoint establishes a user session with the Python agent by setting
     * the active user to "java". This is a prerequisite for accessing other protected
     * agent endpoints that require user authentication.</p>
     *
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: user successfully set, body: "Up"</li>
     *         <li><strong>404 NOT FOUND</strong>: failed to set user or agent unreachable</li>
     *         </ul>
     *
     *  Must be called before accessing other protected agent endpoints
     *  Hardcoded to use "java" as the username for indicate to the python agent that is java server and not a specific user that try an operation and not a user
     * @see #getStatusServer() requires this endpoint to be called first
     */
    @GetMapping("/setUser")
    public ResponseEntity<String> setUser(){
        log.info("start setUser");
        boolean status = client.setActiveUser("java");
        log.info("Set user status: {}", status);
        return status ? ResponseEntity.ok().body("Up"): ResponseEntity.notFound().build();
    }

    /**
     * Route used for get the IP from the python agent
     * @return a json object with :
     * <ul>
     *     <li> status OK and body ip of the server if the server is reachable</li>
     *     <li> status NOT FOUND otherwise</li>
     * </ul>
     *  you must use the route {@code setUser} before otherwise the server will denie the request
     */
    @GetMapping("/getStatusServer")
    public ResponseEntity getStatusServer(){
        log.info("start getStatusServer");
        JsonNode json = client.getStatusServer();
        if(json == null){
            log.error("getStatusServer json is null");
        }else{
            log.info("getStatusServer json: {}", json);
        }

        String ip = null;
        if (json.hasNonNull("status")){
            log.info("Get status server: {}", json.get("status"));
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
                  ip = msg.get(0).replaceAll("\\n","").trim();
                  log.info("Get status server: {}", ip);

               }
           }
            return ResponseEntity.ok().body(ip);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves log entries from the Python agent service.
     *
     * <p>This endpoint fetches the current log entries from the Python agent,
     * providing insight into agent operations and any errors or events that
     * have occurred. The logs are returned in JSON format.</p>
     *
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: logs retrieved successfully, body contains JSON log data</li>
     *         <li><strong>404 NOT FOUND</strong>: agent unreachable or no logs available</li>
     *         </ul>
     *
     *  Log format and structure depend on the Python agent implementation
     *  Null responses from the agent are treated as NOT FOUND errors
     */
    @GetMapping("/getLogs")
    public ResponseEntity getLogs(){
        log.info("start getLogs");
        JsonNode data = client.getLogsServer();
        if(data == null){
            log.error("getLogs json is null");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(data);
    }

    /**
     * Retrieves the Lynis security audit report from the Python agent.
     *
     * <p>This endpoint fetches the Lynis security audit report that has been
     * generated by the Python agent. Lynis is a security auditing tool that
     * performs system hardening and vulnerability assessments.</p>
     *
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: Lynis report retrieved successfully, body contains JSON report data</li>
     *         <li><strong>404 NOT FOUND</strong>: agent unreachable, report not available, or generation failed</li>
     *         </ul>
     *
     *  The report format depends on the Lynis configuration and Python agent implementation
     *  Report generation may take time; consider implementing timeout handling
     *  Null responses from the agent are treated as NOT FOUND errors
     *
     */
    @GetMapping("/getLynisReport")
    public ResponseEntity getLynisReport(){
        log.info("start getLynisReport");
        JsonNode data = client.getLynisReport();
        if(data == null){
            log.error("getLynisReport json is null");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(data);
    }



}
