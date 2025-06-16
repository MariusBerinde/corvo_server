package marius.server.controller;

import marius.server.client.AgentClientPython;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
