package marius.server.controller;

import com.fasterxml.jackson.databind.JsonNode;
import marius.server.client.AgentClientPython;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController

public class AgentController {
    private static final Logger log = LoggerFactory.getLogger(AgentController .class);
    private  final AgentClientPython client;

    public AgentController(AgentClientPython client) {
        this.client = client;
    }

    @GetMapping("/pingAgent")
    public ResponseEntity<String> pingAgent(){
        boolean status = client.pingLocalAgent();
        return status ? ResponseEntity.ok().body("Up"): ResponseEntity.notFound().build();
    }


    @GetMapping("/setUser")
    public ResponseEntity<String> setUser(){
        log.info("start setUser");
        boolean status = client.setActiveUser("java");
        log.info("Set user status: {}", status);
        return status ? ResponseEntity.ok().body("Up"): ResponseEntity.notFound().build();
    }

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
