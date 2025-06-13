package marius.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import marius.server.Tools;
import marius.server.data.*;
import marius.server.repo.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * Class used for manage the routes of Server and Services
 */
@RestController
public class ServerController {

    private final UserRepo userRepo;
	private final ServerRepo serverRepo;
    private final ServiceRepo serviceRepo;
    private final RulesRepo rulesRepo;
    private final LynisRepo lynisRepo;
    private static final Logger log = LoggerFactory.getLogger(ServerController.class);

    public ServerController(UserRepo userRepo, ServerRepo serverRepo, ServiceRepo serviceRepo, RulesRepo rulesRepo, LynisRepo lynisRepo) {
        this.userRepo = userRepo;
        this.serverRepo = serverRepo;
        this.serviceRepo = serviceRepo;
        this.rulesRepo = rulesRepo;
        this.lynisRepo = lynisRepo;
    }
    /**
     * Return the information about the server indicate by id
     * @param requestBody JSON object containing user credentials:
     *                    - username (string, required): the user who make the request
     *                    - ip (string, required): the address of the server (must be a valid addr)
     * @param request HttpServletRequest used for IP address logging
     * @return ResponseEntity with:
     *         - 200 OK: authentication successful,true
     *         - 400 BAD REQUEST: missing required fields (username or password)
     *         - 401 UNAUTHORIZED: invalid username or password
     *         - 403 FORBIDDEN: if the old password is not correct
     */

    @GetMapping("/getServerByIp")
   public ResponseEntity getServerByIp(@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        if(!requestBody.hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing ip field ");
            return ResponseEntity.badRequest().body("ip field missing ");
        }
        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getServerByIp : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        String actualIp = requestBody.get("ip").asText();
        if (!Tools.isValidIp(actualIp)){
            log.error("IP="+request.getRemoteAddr()+"failed in getServerByIp : invalid ip ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid ip");
        }

        Optional<Server>  acutalServer = serverRepo.findByIp(actualIp);
        if(!acutalServer.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getServerByIp : ip not found ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ip not found");
        }
        log.info("username="+actualUsername+" getServerByIp  ="+actualIp);
        return ResponseEntity.ok(acutalServer.get());
    }

    /**
     * Retrieves all servers from the database after user authentication.
     *
     * <p>This endpoint validates the user's identity by checking the provided username
     * against the user repository. Only authenticated users can access the server list.</p>
     *
     * @param requestBody JSON object containing the user credentials. Must include:
     *                   <ul>
     *                   <li><strong>username</strong> (String, required): The username of the requesting user</li>
     *                   </ul>
     * @param request HTTP servlet request object used for logging the client's IP address
     *
     * @return ResponseEntity containing:
     *         <ul>
     *         <li><strong>200 OK</strong>: List of all servers if authentication succeeds</li>
     *         <li><strong>400 Bad Request</strong>: If the username field is missing from the request body</li>
     *         <li><strong>401 Unauthorized</strong>: If the provided username is not found in the system</li>
     *         </ul>
     *
     * @throws SecurityException if there are issues with user authentication
     *
     * @apiNote This method logs all authentication attempts including:
     *          <ul>
     *          <li>Warning logs for missing username field</li>
     *          <li>Error logs for unrecognized usernames</li>
     *          <li>Info logs for successful requests</li>
     *          </ul>
     *
     */
    @GetMapping("/getAllServers")
    public ResponseEntity getAllServers(@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getAllServers : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getAllServers  : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        log.info("username="+actualUsername+" getAllServers  ");
        return ResponseEntity.ok(serverRepo.findAll());
    }


    @PostMapping("/addServer")
    public ResponseEntity addServer(@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"problem in addServer : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }

        if(!requestBody.hasNonNull("server")){
            log.warn("IP="+request.getRemoteAddr()+"missing server field in object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing server field  ");
        }

        if (!requestBody.get("server").hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+"missing ip field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing ip field  ");
        }

        String ip = requestBody.get("server").get("ip").asText();

        if (!Tools.isValidIp(ip)){
            log.warn("IP="+request.getRemoteAddr()+"ip not valid");
            return ResponseEntity.badRequest().body("ip not valid");
        }

        if (!requestBody.get("server").hasNonNull("name")){
            log.warn("IP="+request.getRemoteAddr()+"missing name field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing name field  ");
        }
        String name = requestBody.get("server").get("name").asText();

        if (!requestBody.get("server").hasNonNull("state")){
            log.warn("IP="+request.getRemoteAddr()+"missing state field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing state field  ");
        }
        boolean state = requestBody.get("server").get("state").asBoolean();

        if (!requestBody.get("server").hasNonNull("descr")){
            log.warn("IP="+request.getRemoteAddr()+"missing descr field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing descr field  ");
        }

        String descr = requestBody.get("server").get("descr").asText();
        Server local = new Server(ip, state, name, descr);
        serverRepo.save(local);
        return ResponseEntity.ok(local);
    }


    /**
     * Retrieves all services associated with a specific IP address.
     *
     * This endpoint allows authenticated users to obtain the complete list
     * of services that are configured for a specific server identified by its IP.
     *
     * @param requestBody JSON object containing the request parameters:
     *                   - username (String, required): Username of the requester
     *                   - ip (String, required): IP address of the server to get services from
     * @param request HttpServletRequest object used for logging the client's IP address
     *
     * @return ResponseEntity containing:
     *         - 200 OK: List of services associated with the specified IP
     *         - 400 Bad Request: If username field, ip field is missing or if IP is invalid
     *         - 401 Unauthorized: If the username is not recognized in the system
     *         - 404 Not Found: If the specified IP does not correspond to any registered server
     *
     * @apiNote The method performs the following validations in sequence:
     *          1. Presence of username field in the request body
     *          2. Presence of ip field in the request body
     *          3. User authentication via username
     *          4. IP address format validation
     *          5. Verification of server existence with the specified IP
     *
     */
    @GetMapping("/getServiceByIp")
    public ResponseEntity getServiceByIp (@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServiceByIp  : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        if(!requestBody.hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServiceByIp  : missing ip field ");
            return ResponseEntity.badRequest().body("ip field missing ");
        }
        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getServiceByIp   : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        String actualIp = requestBody.get("ip").asText();
        if (!Tools.isValidIp(actualIp)){
            log.error("IP="+request.getRemoteAddr()+"failed in getServiceByIp : invalid ip ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid ip");
        }


        Optional<Server>  actualServer = serverRepo.findByIp(actualIp);
        if(!actualServer .isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getServiceByIp  : ip not found ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ip not found");
        }
        log.info("username="+actualUsername+" get all services with ip=  ="+actualIp);
        return ResponseEntity.ok(serviceRepo.findAllByIp(actualIp));
    }


    /**
     * Retrieves all services from the database.
     *
     * This endpoint allows users to obtain the complete list of all services
     * registered in the system regardless of their associated server or IP address.
     *
     * @param requestBody JSON object containing the request parameters:
     *                   - username (String, required): Username of the requester
     * @param request HttpServletRequest object used for logging the client's IP address
     *
     * @return ResponseEntity containing:
     *         - 200 OK: Complete list of all services in the system
     *         - 400 Bad Request: If the username field is missing from the request body
     *
     * @apiNote The method performs the following validations:
     *          1. Presence of username field in the request body
     *          2. User lookup in the repository (though authentication result is not currently validated)
     *          Note: The method currently returns all services regardless of user authentication status
     */
    @GetMapping("/getAllServices")
    public ResponseEntity getAllServices (@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getAllServices : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);

        return ResponseEntity.ok(serviceRepo.findAll());
    }

    /**
     * Retrieves a specific service by its name.
     *
     * This endpoint allows authenticated users to obtain detailed information
     * about a specific service identified by its name.
     *
     * @param requestBody JSON object containing the request parameters:
     *                   - username (String, required): Username of the requester
     *                   - name (String, required): Name of the service to retrieve
     * @param request HttpServletRequest object used for logging the client's IP address
     *
     * @return ResponseEntity containing:
     *         - 200 OK: Service object with the specified name
     *         - 400 Bad Request: If username field or name field is missing
     *         - 401 Unauthorized: If the username is not recognized in the system
     *         - 404 Not Found: If no service with the specified name exists
     *
     * @apiNote The method performs the following validations in sequence:
     *          1. Presence of username field in the request body
     *          2. Presence of name field in the request body
     *          3. User authentication via username
     *          4. Verification of service existence with the specified name
     */
    @GetMapping("/getServiceByName")
    public ResponseEntity getServiceByName (@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServiceByName : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        if(!requestBody.hasNonNull("name")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServiceByName : missing ip field ");
            return ResponseEntity.badRequest().body("name field missing ");
        }
        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getServiceByIp   : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        String actualName = requestBody.get("name").asText();

        Optional<Service>  actualService = serviceRepo.findByName(actualName);
        if(!actualService.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in ResponseEntity : service not found ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("service not found");
        }
        log.info("username="+actualUsername+" get all services with name=  ="+actualName);
        return ResponseEntity.ok(actualService.get());
    }



    @PostMapping("/addService")
    public ResponseEntity addService (@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        log.info("username="+request.getRemoteAddr()+" add service  ="+requestBody.get("name"));

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"problem in addr : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }

        if(!requestBody.hasNonNull("service")){
            log.warn("IP="+request.getRemoteAddr()+"missing server field in object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing server field  ");
        }

        if (!requestBody.get("service").hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+"missing ip field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing ip field  ");
        }

        String ip = requestBody.get("service").get("ip").asText();

        if (!Tools.isValidIp(ip)){
            log.warn("IP="+request.getRemoteAddr()+"ip format  not valid");
            return ResponseEntity.badRequest().body("ip format not valid");
        }
        if(!serverRepo.findByIp(ip).isPresent()){
            log.warn("IP="+request.getRemoteAddr()+"ip value  not valid");
            return ResponseEntity.badRequest().body("ip value not valid");
        }

        if (!requestBody.get("service").hasNonNull("name")){
            log.warn("IP="+request.getRemoteAddr()+"missing name field in service object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing name field  ");
        }

        String name = requestBody.get("service").get("name").asText();

        if (!requestBody.get("service").hasNonNull("state")){
            log.warn("IP="+request.getRemoteAddr()+"missing state field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing state field  ");
        }
        boolean state = requestBody.get("servic").get("state").asBoolean();

        if (!requestBody.get("service").hasNonNull("auto")){
            log.warn("IP="+request.getRemoteAddr()+"missing auto field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing auto field  ");
        }
        boolean auto = requestBody.get("service").get("auto").asBoolean();

        if (!requestBody.get("service").hasNonNull("descr")){
            log.warn("IP="+request.getRemoteAddr()+"missing descr field in server object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing descr field  ");
        }

        String descr = requestBody.get("service").get("descr").asText();
        Service local = new Service(ip,name,descr,auto,state);
        serviceRepo.save(local);
        return ResponseEntity.ok(local);
    }

    @GetMapping("/getRulesByIp")
    public ResponseEntity getRulesByIp (@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getRulesByIp  : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        if(!requestBody.hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getRulesByIp  : missing ip field ");
            return ResponseEntity.badRequest().body("ip field missing ");
        }
        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getRulesByIp   : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        String actualIp = requestBody.get("ip").asText();
        if (!Tools.isValidIp(actualIp)){
            log.error("IP="+request.getRemoteAddr()+"failed in getRulesByIp : invalid ip ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid ip");
        }


        Optional<Server>  actualServer = serverRepo.findByIp(actualIp);
        if(!actualServer .isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getRulesByIp  : ip not found ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ip not found");
        }
        log.info("username="+actualUsername+" get all rules with ip=  ="+actualIp);
        return ResponseEntity.ok(rulesRepo.findByIp(actualIp));
    }

    @PostMapping("/addRule")
    public  ResponseEntity addRule(@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"problem in addServer : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }

        if(!requestBody.hasNonNull("rule")){
            log.warn("IP="+request.getRemoteAddr()+"missing rule field in object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing rule field  ");
        }

        if (!requestBody.get("rule").hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+"missing ip field in rule object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing ip field  ");
        }

        String ip = requestBody.get("rule").get("ip").asText();

        if (!Tools.isValidIp(ip)){
            log.warn("IP="+request.getRemoteAddr()+"ip not valid");
            return ResponseEntity.badRequest().body("ip format not valid");
        }
        if(!serverRepo.findByIp(ip).isPresent()){
            log.error("IP="+request.getRemoteAddr()+"ip not found");
            return ResponseEntity.badRequest().body("ip not used ");
        }


        if (!requestBody.get("rule").hasNonNull("status")){
            log.warn("IP="+request.getRemoteAddr()+"missing state field in rule object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing state field  ");
        }
        boolean status = requestBody.get("rule").get("status").asBoolean();

        if (!requestBody.get("rule").hasNonNull("descr")){
            log.warn("IP="+request.getRemoteAddr()+"missing descr field in rule object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing descr field  ");
        }

        String descr = requestBody.get("rule").get("descr").asText();

        if(requestBody.get("rule").hasNonNull("service")){
            Integer service = requestBody.get("rule").get("service").asInt();
            Rules local = new Rules(descr,status,ip,service);
            rulesRepo.save(local);
            return ResponseEntity.ok(local);
        }
        Rules local = new Rules(descr,status,ip);
        rulesRepo.save(local);
        return ResponseEntity.ok(local);
    }

    @GetMapping("/getLynisByIp")
    public ResponseEntity<?> getLynisByIp(@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getLynisByIp  : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if(!requestBody.hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getLynisByIp  : missing ip field ");
            return ResponseEntity.badRequest().body("ip field missing ");
        }
        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getLynisByIp   : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        String actualIp = requestBody.get("ip").asText();
        if (!Tools.isValidIp(actualIp)){
            log.error("IP="+request.getRemoteAddr()+"failed in getLynisByIp : invalid ip ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid ip");
        }
        Optional<Server> actualServer = serverRepo.findByIp(actualIp);
        if(!actualServer.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getLynisByIp  : ip not found ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ip not found");
        }

        Optional<Lynis> lynisOptional = lynisRepo.findByIp(actualIp);
        log.info("username="+actualUsername+" get all rules with ip="+actualIp);

        if (!lynisOptional.isPresent()) {
            log.error("IP="+request.getRemoteAddr()+" failed in getLynisByIp: no lynis data found for ip ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("no lynis data found for ip");
        }

        Lynis lynis = lynisOptional.get();

        // Costruzione della risposta nel formato richiesto
        Map<String, Object> response = new HashMap<>();
        response.put("username", actualUsername);

        Map<String, Object> lynisData = new HashMap<>();
        lynisData.put("ip", lynis.getIp());
        lynisData.put("auditor", lynis.getAuditor());

        // Gestione del campo listIdSkippedTest
        if (lynis.getListIdSkippedTest() != null && !lynis.getListIdSkippedTest().trim().isEmpty()) {
            // Assumendo che gli ID siano separati da virgole
            String[] testIds = lynis.getListIdSkippedTest().split(",");
            List<String> testIdsList = new ArrayList<>();
            for (String testId : testIds) {
                testIdsList.add(testId.trim());
            }
            lynisData.put("listIdSkippedTest", testIdsList);
        } else {
            lynisData.put("listIdSkippedTest", new ArrayList<String>());
        }

        response.put("lynis", lynisData);

        return ResponseEntity.ok(response);
    }



    @PostMapping("/addLynisConfig")
    public  ResponseEntity addLynisConfig(@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"problem in addServer : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }

        if(!requestBody.hasNonNull("lynis")){
            log.warn("IP="+request.getRemoteAddr()+"missing lynis field in object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing lynis field  ");
        }

        if (!requestBody.get("lynis").hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+"missing ip field in lynis object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing ip field  ");
        }

        String ip = requestBody.get("lynis").get("ip").asText();

        if (!Tools.isValidIp(ip)){
            log.warn("IP="+request.getRemoteAddr()+"ip not valid");
            return ResponseEntity.badRequest().body("ip format not valid");
        }
        if(!serverRepo.findByIp(ip).isPresent()){
            log.error("IP="+request.getRemoteAddr()+"ip not found");
            return ResponseEntity.badRequest().body("ip not used ");
        }


        if (!requestBody.get("lynis").hasNonNull("auditor")){
            log.warn("IP="+request.getRemoteAddr()+"missing auditor field in lynis object in updateRoleUser ");
            return ResponseEntity.badRequest().body("missing auditor field  ");
        }
        String auditor = requestBody.get("lynis").get("auditor").textValue();

        if (requestBody.get("lynis").hasNonNull("listIdSkippedTest")){
            String listIdSkippedTestString = null;
            JsonNode listIdSkippedTest = requestBody.get("lynis").get("listIdSkippedTest");
            if(listIdSkippedTest.isArray()){
                List<String> list = new ArrayList<>();
                for (JsonNode listId : listIdSkippedTest){
                    list.add(listId.asText());
                }
                if(!list.isEmpty()){
                    listIdSkippedTestString = String.join(",", list);

                    Lynis local = new Lynis(auditor,ip,listIdSkippedTestString);
                    lynisRepo.save(local);
                    return ResponseEntity.ok(local);
                }

            }


        }


        Lynis local = new Lynis(auditor,ip);
        lynisRepo.save(local);
        return ResponseEntity.ok(local);
    }

    /*
    @GetMapping("/getLynisReportByIp")
    public ResponseEntity getLynisReportByIp(String ip){}
     */


}
