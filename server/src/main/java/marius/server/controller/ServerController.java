package marius.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import marius.server.Tools;
import marius.server.client.AgentClientPython;
import marius.server.client.LocalAgentRegistration;
import marius.server.data.*;
import marius.server.repo.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** This class contains all the REST routes for interract with the agents python
 * @Autorthor Marius Berinde
 */
@RestController
@CrossOrigin
public class ServerController {

    private final UserRepo userRepo;
	private final ServerRepo serverRepo;
    private final ServiceRepo serviceRepo;
    private final RulesRepo rulesRepo;
    private final LynisRepo lynisRepo;
    private final LogRepo logRepo;
    private final LocalAgentRegistration agentRegistration;
    private static final Logger log = LoggerFactory.getLogger(ServerController.class);
    private HashMap<String,Server> servers;
    private  final AgentClientPython client;

    public ServerController(UserRepo userRepo, ServerRepo serverRepo, ServiceRepo serviceRepo,
                            RulesRepo rulesRepo, LynisRepo lynisRepo, LogRepo logRepo,
                            AgentClientPython client,  LocalAgentRegistration agentRegistration) {
        this.userRepo = userRepo;
        this.serverRepo = serverRepo;
        this.serviceRepo = serviceRepo;
        this.rulesRepo = rulesRepo;
        this.lynisRepo = lynisRepo;
        this.logRepo = logRepo;
        this.client = client;
        this.agentRegistration = agentRegistration;
        this.servers = new HashMap<>();
    }

    /**
     *  Add  a server to the list of active nodes
     * @param server the new active node
     * @return if the server is added to the hashmap called server
     */
    public boolean addActiveNode(Server server) { return this.servers.putIfAbsent(server.getIp(), server) == null; }

    /**
     * remove the server wit IP from the hashmap of active nodes
     * @param ip the IP address of the node that will be removed
     * @return true i the node is removed from servers , false otherwise
     */
    public boolean removeActiveNode(String ip){ return this.servers.remove(ip) != null; }

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

        Optional<Server>  actualServer = serverRepo.findByIp(actualIp);
        if(!actualServer.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getServerByIp : ip not found ");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ip not found");
        }
        log.info("username="+actualUsername+" getServerByIp  ="+actualIp);
        return ResponseEntity.ok(actualServer.get());
    }

    /**
     * This route will return a  List<Server> of all servers added in the database
     * @param email the email of the user who try to get the servers
     * @param request
     * @return
     */
    @GetMapping("/getAllServers")
    public ResponseEntity getAllServers(@RequestHeader("email") String email, HttpServletRequest request){
        if(email == null || email.isEmpty()){
            log.info("IP="+request.getRemoteAddr()+" failed in getAllServers : missing username field");
            return ResponseEntity.badRequest().body("email field missing ");
        }
        log.info(" getAllServers email="+email);

        Optional<User> actualUser = userRepo.findUserByEmail(email);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getAllServers  : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }

        log.info("username="+email+" getAllServers  ");
        List<Server> servers = serverRepo.findAll();
        for (Server server : servers){
            server.setState(this.servers.containsKey(server.getIp()));
        }

        return ResponseEntity.ok(servers);
    }

    @GetMapping("/getAllLogs")
    public ResponseEntity getAllLogs(@RequestHeader("username") String username, HttpServletRequest request){
        if(username == null || username.isEmpty()){
            log.error("IP="+request.getRemoteAddr()+" failed in getAllLogs : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        Optional<User> actualUser = userRepo.findUserByEmail(username);

        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getAllLogs  : unrecognized username ");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        log.info("username="+username+" getAllLogs  ");

        Log log = new Log(actualUser.get().getEmail(),request.getRemoteAddr().toString(),"get all logs ok");
        return ResponseEntity.ok(logRepo.findAll());
    }

    @GetMapping("/getUserLogs")
    public ResponseEntity getUserLogs(@RequestHeader("email") String email, HttpServletRequest request){
        if(email == null || email.isEmpty()){
            log.error("IP="+request.getRemoteAddr()+" failed in getUserLogs : missing email field");
            return ResponseEntity.badRequest().body("email field missing ");
        }

        Optional<User> actualUser = userRepo.findUserByEmail(email);

        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in getAllLogs  : unrecognized email ");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized email");
        }
        log.info("email="+email+" getAllLogs  ");

        Log log = new Log(actualUser.get().getEmail(),request.getRemoteAddr().toString(),"get all logs ok");
        return ResponseEntity.ok(logRepo.findByUserEmail(actualUser.get().getEmail()));
    }


    @PostMapping("/addLog")
    public ResponseEntity addLog(@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.error("IP="+request.getRemoteAddr()+" failed in addLog : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if(!requestBody.hasNonNull("log")){
            log.error("IP="+request.getRemoteAddr()+" failed in addLog : missing log field ");
            return ResponseEntity.badRequest().body("log field missing ");
        }
        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in addLog  : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        if (!requestBody.get("log").hasNonNull("data")) {
            log.error("IP="+request.getRemoteAddr()+" failed in addLog : missing data field");
            return ResponseEntity.badRequest().body("data field missing ");
        }
		String stringLogJson = requestBody.get("log").get("data").asText();

		// Pulisci la stringa rimuovendo la parte tra parentesi
		String cleanedLog = stringLogJson.replaceAll("\\s*\\([^)]*\\)\\s*", "");

		// Crea il formatter 
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);

		// Parsa e crea il Timestamp
		OffsetDateTime offsetDateTime = OffsetDateTime.parse(cleanedLog, formatter);
		Timestamp logTime = Timestamp.valueOf(offsetDateTime.toLocalDateTime());

        if (!requestBody.get("log").hasNonNull("user")) {
            log.error("IP="+request.getRemoteAddr()+" failed in user : missing data field");
            return ResponseEntity.badRequest().body("user field missing in log field ");
        }

        Optional<User> userLog = userRepo.findUserByEmail(requestBody.get("log").get("user").asText());
	if (!userLog.isPresent()){

            log.error("IP="+request.getRemoteAddr()+"failed in addlog: log.user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("log.user not found");
		}
	
        if (!requestBody.get("log").hasNonNull("desc")) {
            log.error("IP="+request.getRemoteAddr()+" failed in addLog : missing log.descr field");
            return ResponseEntity.badRequest().body("log.descr field missing ");
        }
	String descr = requestBody.get("log").get("desc").asText();
	if(requestBody.get("log").hasNonNull("server")){
			String server = requestBody.get("log").get("server").asText();

			if (!Tools.isValidIp(server)){
				log.error("IP="+request.getRemoteAddr()+"server not valid");
				return ResponseEntity.badRequest().body("server not valid");
			}

			Optional<Server>  actualServer = serverRepo.findByIp(server);
			if(!actualServer .isPresent()){
				log.error("IP="+request.getRemoteAddr()+"failed in addLog  : server not found ");
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("server not found");
			}
			Log localLog = new Log(userLog.get().getEmail(),server,descr,logTime);
            logRepo.save(localLog);
            return ResponseEntity.ok(localLog);
		}
    if(requestBody.get("log").hasNonNull("service")){
        Integer service = Integer.valueOf(requestBody.get("log").get("service").asInt());
        Optional<AgentService> localService = serviceRepo.findById(service);
        if(!localService.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in addLog  : service not found");
            return ResponseEntity.badRequest().body("service not found");
        }
        if(!requestBody.get("log").hasNonNull("server")){
            log.error("IP="+request.getRemoteAddr()+"failed in addLog : missing server field in log object ");
            return ResponseEntity.badRequest().body("server field missing in log object ");
        }
        String server = requestBody.get("log").get("server").asText();
        //if (!localService.get().getIp().equals(server)){
        if (!localService.get().getIp().equals(server)){
            log.error("IP="+request.getRemoteAddr()+"error in addLog  : server of json not match with server of service ");
            return ResponseEntity.badRequest().body("server of json not match with server of service ");
        }
        Log localLog = new Log(userLog.get().getEmail(),server,service,descr,logTime);
        logRepo.save(localLog);
        return ResponseEntity.ok(localLog);
    }


        Log locaLog = new Log(userLog.get().getEmail(),descr,logTime);
        logRepo.save(locaLog);
        return ResponseEntity.ok(locaLog);


    }

    /**
     *  Allows to the user to add the json array of array logs
     * @param requestBody  requestBody JSON object containing user credentials:
     *      *                    - username (string, required): the user's username
     *      *                    - logs (string, required): the json array of logs that will be added to the server
     *      *
     * @param request is used for track the ip of unautorizated users
     * @return
     */
    @PostMapping("/addAllLogs")
    public ResponseEntity addAllLogs(@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.error("IP="+request.getRemoteAddr()+" failed in addLogs : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if(!requestBody.hasNonNull("logs")){
            log.error("IP="+request.getRemoteAddr()+" failed in addLogs : missing logs field ");
            return ResponseEntity.badRequest().body("log field missing ");
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"failed in addLogs  : unrecognized username for ",actualUsername);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unrecognized username");
        }
        if (!requestBody.get("logs").isArray() ) {
            log.error("IP="+request.getRemoteAddr()+" failed in addLogs :  logs is not an array");
            return ResponseEntity.badRequest().body("logs is not an array");
        }
       List<Log> logs = new ArrayList<>();
       JsonNode listLogs = requestBody.get("logs");
       for (JsonNode logJson : listLogs) {
           if (!logJson.hasNonNull("data")) {
               log.error("IP="+request.getRemoteAddr()+" failed in addLogs : missing data field");
               return ResponseEntity.badRequest().body("data field missing ");
           }
           String stringLogJson = logJson.get("data").asText();

           // Pulisci la stringa rimuovendo la parte tra parentesi
           String cleanedLog = stringLogJson.replaceAll("\\s*\\([^)]*\\)\\s*", "");

           // Crea il formatter
           DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);

           // Parsa e crea il Timestamp
           OffsetDateTime offsetDateTime = OffsetDateTime.parse(cleanedLog, formatter);
           Timestamp logTime = Timestamp.valueOf(offsetDateTime.toLocalDateTime());

           if(!logJson.hasNonNull("userEmail")){
               log.error("IP="+request.getRemoteAddr()+"failed in addLogs : missing user field in log object ");
               return ResponseEntity.badRequest().body("user field missing in log object of json Array");
           }

           Optional<User> userLog = userRepo.findUserByEmail(logJson.get("userEmail").asText());
           log.info("IP="+request.getRemoteAddr()+" addLogs user email: "+logJson.get("userEmail").asText());

           if (!userLog.isPresent()){

               log.error("IP="+request.getRemoteAddr()+"failed in addlogs: log.user not found = ",logJson.get("userEmail").asText());
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("log.user not found");
           }

           if (!logJson.hasNonNull("descr")) {
               log.error("IP="+request.getRemoteAddr()+" failed in addLog : missing log.descr field");
               return ResponseEntity.badRequest().body("log.descr field missing ");
           }

           String descr = logJson.get("descr").asText();
           if (!logJson.hasNonNull("ip")){
               /*
               log.error("IP="+request.getRemoteAddr()+"failed in addLogs : missing ip field in log object ");
               return ResponseEntity.badRequest().body("server field missing in log object of json Array");
                */
               logs.add(new Log(userLog.get().getEmail(),descr,logTime));
           }else{

               String server = logJson.get("ip").asText();

               logs.add(new Log(userLog.get().getEmail(),server,descr,logTime));
           }
       }
       logRepo.saveAll(logs);
       return ResponseEntity.ok("true");
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
    @PostMapping("/getServiceByIp")
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

        Optional<AgentService>  actualService = serviceRepo.findByName(actualName);
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
            log.warn("IP="+request.getRemoteAddr()+" failed in addService : missing username field");
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
        boolean state = requestBody.get("service").get("state").asBoolean();

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
        AgentService local = new AgentService(ip,name,descr,auto,state);
        serviceRepo.save(local);
        return ResponseEntity.ok(local);
    }

    @PostMapping("/getRulesByIp")
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
            log.warn("IP="+request.getRemoteAddr()+" failed in addRule : missing username field");
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
            Rules local = new Rules("R",descr,status,ip,service);
            rulesRepo.save(local);
            return ResponseEntity.ok(local);
        }
        Rules local = new Rules("R",descr,status,ip);
        rulesRepo.save(local);
        return ResponseEntity.ok(local);
    }

    /**
     * Returns the list of the skipped tests for the IP agent
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping("/getLynisByIp")
    public ResponseEntity<?> getLynisByIp(@RequestBody JsonNode requestBody, HttpServletRequest request){
        /**
         * controls for the integrity of the json objcet
         */
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

        return ResponseEntity.ok(lynisData);
    }



    @PostMapping("/addLynisConfig")
    public  ResponseEntity addLynisConfig(@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in addLynisConfig : missing username field");
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
        // is used to check if we can load now the list of skippable tests or later
        boolean activeServer = this.servers.containsKey(ip);

        if (requestBody.get("lynis").hasNonNull("listIdSkippedTest")){
            String listIdSkippedTestString = null;
            JsonNode listIdSkippedTest = requestBody.get("lynis").get("listIdSkippedTest");
            if(listIdSkippedTest.isArray()){
                List<String> list = new ArrayList<>();
                for (JsonNode listId : listIdSkippedTest){
                    list.add(listId.asText().toString());
                }

                if (activeServer){
                    log.info("addLynisConfig add skipped listIdSkippedTest to local agent");
                    int port = this.servers.get(ip).getPort();
                    log.info("listIdSkippedTest: {}=",list);
                    this.client.setActiveUser(ip,port,auditor);
                    this.client.addLynisRules(ip,port,list);
                }
                if(!list.isEmpty()){
                    listIdSkippedTestString = String.join(",", list);
                    Optional<Lynis>  objDb = lynisRepo.findByIp(ip);
                    if(!objDb.isPresent()){
                        Lynis local = new Lynis(auditor,ip,listIdSkippedTestString,activeServer);
                        lynisRepo.save(local);
                        return ResponseEntity.ok(local);
                    }else{
                       Lynis toUpdate = objDb.get();
                       toUpdate.setListIdSkippedTest(listIdSkippedTestString);
                       toUpdate.setAuditor(auditor);
                       toUpdate.setLoaded(activeServer);
                       lynisRepo.save(toUpdate);
                        return ResponseEntity.ok(toUpdate);
                    }

                }

            }

        }



        Optional<Lynis>  objDb = lynisRepo.findByIp(ip);
        if(!objDb.isPresent()){
            Lynis local = new Lynis(auditor,ip,activeServer);
            lynisRepo.save(local);
            return ResponseEntity.ok(local);
        }
        else {
            Lynis local =  objDb.get();
            local.setAuditor(auditor);
            local.setLoaded(activeServer);
            lynisRepo.save(local);
            return ResponseEntity.ok(local);
        }
    }





    @GetMapping("/getLynisReportByIp")
    public ResponseEntity<String> getLynisReportByIp(@RequestHeader("username") String username,
                                                     @RequestHeader("ip") String ip,
                                                     HttpServletRequest request){
        if(username == null || username.isEmpty()){
            log.info("IP="+request.getRemoteAddr()+" failed in getLynisReportByIp : missing username field");
            return ResponseEntity.badRequest().body("username field missing");
        }
        if(ip == null || ip.isEmpty()){
            log.info("IP="+request.getRemoteAddr()+" failed in getLynisReportByIp : missing ip field"); // Corretto il messaggio
            return ResponseEntity.badRequest().body("ip field missing"); // Corretto il messaggio
        }
        if(!userRepo.existsByUsername(username)){
            log.info("username=" + username + " not found");
            return ResponseEntity.badRequest().body("username not valid");
        }
        if (!this.servers.containsKey(ip)) {
            log.info("IP=" + request.getRemoteAddr() + " ip " + ip + " not running"); // Corretto il log
            return ResponseEntity.badRequest().body("ip format not valid"); // Cambiato da internalServerError
        }
        log.info(" getLynisReportByIp pamams = IP : ",ip ,"username:",username);

        if(!servers.containsKey(ip)){
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("server not found");
        }

        boolean userSet = client.setActiveUser(ip, this.servers.get(ip).getPort(), username);
        if (!userSet) {
            log.error("❌ Impossibile impostare l'utente {} sull'agent {}:5000", username, ip);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Errore: impossibile connettersi all'agent o impostare l'utente");
        }

        // Ottieni il report Lynis
        try {
            String reportContent = client.getLynisReportText(ip, this.servers.get(ip).getPort());

            log.info(" getLynisReportByIp pamams = IP : ",ip ,"username:",username);

            if (reportContent != null && !reportContent.trim().isEmpty()) {
                log.info("✅ Report Lynis ottenuto con successo per utente {} da IP {}", username, ip);

                // Imposta gli headers appropriati per Angular
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.set("Content-Disposition", "inline"); // Per visualizzare nel browser

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(reportContent);
            } else {
                log.warn("⚠️ Report Lynis vuoto o non trovato per utente {} da IP {}", username, ip);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Errore durante il recupero del report Lynis per utente {} da IP {}: {}",
                    username, ip, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Errore interno durante il recupero del report");
        }
    }


    @GetMapping("/startLynisScan")
    public ResponseEntity startLynisScan(@RequestHeader("username") String username,@RequestHeader("ip") String ip, HttpServletRequest request){
        if(username == null || username.isEmpty()){
            log.info("IP="+request.getRemoteAddr()+" failed in StartLyniScan : missing username field");
            return ResponseEntity.badRequest().body("username field missing");
        }
        if(ip == null || ip.isEmpty()){
            log.info("IP="+request.getRemoteAddr()+" failed in StartLyniScan  : missing ip field"); // Corretto il messaggio
            return ResponseEntity.badRequest().body("ip field missing"); // Corretto il messaggio
             }
        if(!this.servers.containsKey(ip)){
            log.info("IP=" + request.getRemoteAddr() + " ip " + ip + " not running");
            return ResponseEntity.badRequest().body("client not running");
        }
        log.info("startLynisScan: ho preso i parametri e tento di inoltro la scan all'agent python");
        try {
            // Imposta l'utente attivo sull'agent target
            if(!servers.containsKey(ip)){
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("server not found");
            }
            boolean userSet = this.client.setActiveUser(ip, this.servers.get(ip).getPort(), username);
            if (!userSet) {
                log.error("IP={} failed in startLynisScan: unable to set active user {} on agent {}",
                        request.getRemoteAddr(), username, ip);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to set active user on agent");
            }

            // Avvia la scansione Lynis
            boolean scanStarted = this.client.startLynisScan(ip, this.servers.get(ip).getPort());

            if (scanStarted) {
                log.info("IP={} successfully started Lynis scan for user {} on agent {}",
                        request.getRemoteAddr(), username, ip);
                return ResponseEntity.ok().body("Lynis scan started successfully");
            } else {
                log.warn("IP={} failed to start Lynis scan for user {} on agent {} - scan may already be running or user not authorized",  username, ip);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Failed to start Lynis scan - scan may already be running or user not authorized");
            }

        } catch (Exception e) {
            log.error("IP={} error in startLynisScan for user {} on agent {}: {}",
                    request.getRemoteAddr(), username, ip, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error occurred while starting Lynis scan");
        }

    }


    /**
     *  this method update name and or description of the server
     * @param requestBody  must contain the usename of the user who change the data
     *                     ip : the IP address of the server
     *                     name : the new name of the server
     *                     descr : the new description
     * @param request
     * @return true if the data are update in the server , false othervise
     */
    @PostMapping("updateDetailServer")
    ResponseEntity<Boolean> updateDetailServer(@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing username field");
            return ResponseEntity.badRequest().build();
        }

        String actualUsername = requestBody.get("username").asText();
        Optional<User> actualUser = userRepo.findUserByUsername(actualUsername);
        if(!actualUser.isPresent()){
            log.error("IP="+request.getRemoteAddr()+"problem in updateDetailServer : unrecognized username ");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if(!requestBody.hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getServerByIp : missing ip field");
            return ResponseEntity.badRequest().build();
        }


        String ip = requestBody.get("ip").asText();
        Optional<Server> actualServer = serverRepo.findByIp(ip);
        if(actualServer.isEmpty()){
            log.error("IP="+request.getRemoteAddr()+"ip not found");
            return ResponseEntity.badRequest().build();
        }

        String newName = null;
        String newDesc = null;
        if(requestBody.hasNonNull("name")){
            newName = requestBody.get("name").asText();
        }
       if(requestBody.hasNonNull("descr")){
           newDesc = requestBody.get("descr").asText();
       }
       if((newName == null || newName.isEmpty()) &&  (newDesc == null || newDesc.isEmpty()) ){
           return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
       }
       if(newName!=null && newName.length()>0){
           actualServer.get().setName(newName);
       }
       if(newDesc!=null && newDesc.length()>0){
           actualServer.get().setDescr(newDesc);
       }
       serverRepo.save(actualServer.get());
       return ResponseEntity.ok().build();
    }

    /**
     * Add the agent python to the server
     * @param requestBody a json file with
     *                    - ip : the ip of the agent
     *                    - name: the name of the agent
     *                    - descr : the description of the agent
     *                    - port : the port where connct
     * @return
     */
    @PostMapping("/addAgent")
    ResponseEntity addServer(@RequestBody JsonNode requestBody,HttpServletRequest request){
        if(!requestBody.hasNonNull("ip")){
            log.info("ip field missing in json object in AddServer");
            return ResponseEntity.badRequest().build();
        }
        String ip = requestBody.get("ip").asText();
        int port;
        if(!requestBody.hasNonNull("port")){
            log.info("port field missing in json object in AddServer");
            port = 5000;
        }
        else
            port = requestBody.get("port").asInt();
        String name ;
        if (!requestBody.hasNonNull("name")) {
            log.info("name field missing in json object in AddServer");
            name = "todo";
        }
        else
            name = requestBody.get("name").asText();
        String desc ;
        if (!requestBody.hasNonNull("descr")) {
            log.info("descr field missing in json object in AddServer");
            desc = "todo";
        }
        else
            desc = requestBody.get("descr").asText();
       Server local = new Server(ip,true,name,desc,port);
       if ( !servers.containsKey(ip)){
           this.servers.put(ip,local);
       }
        boolean registrationStarted = agentRegistration.registerAgent(ip, port);

        if (registrationStarted) {
            log.info("Processo di registrazione avviato per agente {}:{}", ip, port);
        } else {
            log.error("Impossibile avviare registrazione per agente {}:{}", ip, port);
        }      // Avvia nuovo thread per gestion comunicazione con questo server


      return  ResponseEntity.ok().body(local);

    }


    @PostMapping("/getStatusServer")
    ResponseEntity getStatusServices(@RequestBody JsonNode requestBody,HttpServletRequest request){
        log.info("getStatusServices(requestBody:{})", requestBody);

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getStatusServer : missing username field");
            return ResponseEntity.badRequest().body("username field missing ");
        }

        if(!requestBody.hasNonNull("ip")){
            log.warn("IP="+request.getRemoteAddr()+" failed in getStaustServer : missing ip field ");
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
            log.error("IP="+request.getRemoteAddr()+"failed in getStatusServer : invalid ip ");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid ip");
        }

        if(this.servers.containsKey(actualIp)){
            log.info(" getStatusServices , server running so get data diretly form datasource");
            Server server = this.servers.get(actualIp);
            List<AgentService> services = client.getServiceStatus(server.getIp(),server.getPort());
            return ResponseEntity.ok(services);
        }
        else{
            log.info(" getStatusServices , server down data from db");
            List<AgentService> services = serviceRepo.findByIp(actualIp);
            return ResponseEntity.ok(services);
        }


    }



}
