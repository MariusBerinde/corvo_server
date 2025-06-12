package marius.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import marius.server.Tools;
import marius.server.data.*;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.ServerRepo;
import marius.server.repo.ServiceRepo;
import marius.server.repo.UserRepo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

/**
 * Class used for manage the routes of Server and Services
 */
@RestController
public class ServerController {

    private final UserRepo userRepo;
	private final ServerRepo serverRepo;
    private final ServiceRepo serviceRepo;
    private static final Logger log = LoggerFactory.getLogger(ServerController.class);

    public ServerController(UserRepo userRepo, ServerRepo serverRepo, ServiceRepo serviceRepo) {
        this.userRepo = userRepo;
        this.serverRepo = serverRepo;
        this.serviceRepo = serviceRepo;
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


}
