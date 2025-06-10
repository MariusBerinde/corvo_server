package marius.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import marius.server.Tools;
import marius.server.data.ApprovedUsers;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.repo.ApprovedUsersRepo;
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

import java.util.Optional;

@RestController
public class AuthController {
    private final ApprovedUsersRepo approvedUsersRepo;
    private final UserRepo userRepo;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(ApprovedUsersRepo approvedUsersRepo, UserRepo userRepo) {
        this.approvedUsersRepo = approvedUsersRepo;
        this.userRepo = userRepo;
    }



    /** Route for allow the user with email to register to corvo app
     * @param request the object must contain the username and the email 
     * @return True if the email of the user is added to the table ApprovedUsers , false otherwise
     * @throws Exception handled generically with error message
     */
    @PostMapping("/enableUserRegistration")
    public ResponseEntity enableUserRegistration(@RequestBody JsonNode requestBody, HttpServletRequest request) {
	try{

        if (!requestBody.hasNonNull("username")) {
            log.error("Richiesta malformata: manca 'user' da IP={}", request.getRemoteAddr());
            return ResponseEntity.badRequest().body("missing username");
        }

        // Verifica campo "email"
        if (!requestBody.hasNonNull("email")) {
            log.error("Richiesta malformata: manca 'email' da IP={}", request.getRemoteAddr());
            return ResponseEntity.badRequest().body("missing email");
        }
		String username = requestBody.get("username").asText();

        if (!userRepo.existsByUsername(username)){
            log.error("IP="+request.getRemoteAddr());
            return ResponseEntity.badRequest().body("username not auth ");
        }
            log.info(" username riconosciuto");
			String email = requestBody.get("email").asText();
            if(!Tools.isValidEmail(email)){
                log.debug("the email { } not have a valid format", email);
                return    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email format error");
            }
            log.debug("the email {} have a valid format", email);
			ApprovedUsers tmp = new ApprovedUsers(email);
			System.out.println("AuthController::enableUserRegistration Approved Users: " + tmp);
			approvedUsersRepo.save(tmp);
			return ResponseEntity.ok("true");

	}catch(Exception e){
       log.error("IP="+request.getRemoteAddr()+"Problema con richiesta") ;
        		return ResponseEntity.badRequest().body("missing 'username' or 'email'");
		}

    }


    /**
     *  Route for get the entries of the table ApprovedUsers
     * @param requestBody must be json object with the username of the user that make the request
     * @param request is used for get the ip of the sender
     * @return a json array with all the users that can registrate to the app
     *
     * @throws Exception handled generically with error message
     */
    @GetMapping("/getApprovedUsers")
    public ResponseEntity getApprovedUsers(@RequestBody JsonNode requestBody, HttpServletRequest request){
       try{
           if(!requestBody.hasNonNull("username")){

               log.error("Richiesta malformata: manca 'user' da IP={}", request.getRemoteAddr());
               return ResponseEntity.badRequest().body("missing username");
           }

           String username = requestBody.get("username").asText();
           if (!userRepo.existsByUsername(username)){
               log.error("IP="+request.getRemoteAddr());
               return ResponseEntity.badRequest().body("username not auth ");
           }
           var emails = approvedUsersRepo.findAll();
           return ResponseEntity.ok(emails);
       } catch(Exception e){
           log.error("IP="+request.getRemoteAddr()+"Problema con richiesta ="+e.getMessage()) ;
           return ResponseEntity.badRequest().body("missing 'username' and 'email'");
       }
    }


    /**
     * Delete a user from the list of the users that can register to the app
     * @param requestBody JSON containing:
     *                    - username: creator's username (must be SUPERVISOR)
     *                    - email: the email of the user from whom we want to remove permission to register to the app
     * @param request is used for get the ip of the sender
     * @return ResponseEntity with:
     *         - 200 OK: user created successfully, body contains User object
     *         - 400 BAD REQUEST: validation error or insufficient permissions
     * @throws Exception handled generically with error message
     */
    @GetMapping("/deleteEnabledUser")
    @Transactional
    public ResponseEntity deleteEnabledUser(@RequestBody JsonNode requestBody, HttpServletRequest request) {
	try{

        if (!requestBody.hasNonNull("username")) {
            log.error("Richiesta malformata: manca 'user' da IP={}", request.getRemoteAddr());
            return ResponseEntity.badRequest().body("missing username");
        }

        if (!requestBody.hasNonNull("email")) {
            log.error("Richiesta malformata: manca 'email' da IP={}", request.getRemoteAddr());
            return ResponseEntity.badRequest().body("missing email");
        }
		String username = requestBody.get("username").asText();

        if (!userRepo.existsByUsername(username)){
            log.error("IP="+request.getRemoteAddr());
            return ResponseEntity.badRequest().body("username not auth ");
        }
            log.info(" username riconosciuto");
			String email = requestBody.get("email").asText();
            if(!Tools.isValidEmail(email)){
                log.debug("the email { } not have a valid format", email);
                return    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email format error");
            }

            log.debug("the email {} have a valid format", email);
			Integer ris = approvedUsersRepo.deleteByEmail(email);
			return ResponseEntity.ok(ris);

	}catch(Exception e){
       log.error("IP="+request.getRemoteAddr()+"Problema con richiesta = "+e.getMessage()) ;
        		return ResponseEntity.badRequest().body("missing 'username' or 'email'");
		}

    }

/**
 * Creates a new user in the system.
 * Only users with SUPERVISOR role can create new users.
 *
 * @param requestBody JSON containing:
 *                    - username: creator's username (must be SUPERVISOR)
 *                    - user: object with new user data
 *                      - name: new user's username
 *                      - email: new user's email (must be valid)
 *                      - password: new user's password (TODO: will be encrypted)
 *                      - role: role as integer (0=SUPERVISOR, other=WORKER)
 * @param request HttpServletRequest for IP logging
 *
 * @return ResponseEntity with:
 *         - 200 OK: user created successfully, body contains User object
 *         - 400 BAD REQUEST: validation error or insufficient permissions
 *
 * @throws Exception handled generically with error message
 */
    @PostMapping("/addUser")
    public ResponseEntity addUser(@RequestBody JsonNode requestBody, HttpServletRequest request){
        try{
            if(!requestBody.hasNonNull("username")){
                log.warn("IP="+request.getRemoteAddr()+"tried to add a new user");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("field username missing ");
            }
            String creator = requestBody.get("username").asText();
            User creatorUser = userRepo.findUserByUsername(creator).orElse(null);
            if (creatorUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User creator not registered");
            }
/*
            if (creatorUser.getRole() != RoleEnum.SUPERVISOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only supervisors can create users");
            }
 */
            String tmp_name = requestBody.get("user").get("name").asText();

            log.info("tmp_name="+tmp_name);
            String tmp_email = requestBody.get("user").get("email").asText();
            if (!Tools.isValidEmail(tmp_email)) {
              log.error("format not valid email "+tmp_email+" during creation user with username ="+tmp_name);
                return ResponseEntity.badRequest().body("format not valid email "+tmp_email+" during creation user with username ="+tmp_name);
            }

            log.info("tmp_email="+tmp_email);
            String tmp_password = requestBody.get("user").get("password").asText();

            log.info("tmp_password="+tmp_password);
            //TODO: criptare password prima di inserimento
            //RoleEnum tmp_role = (requestBody.get("user").get("role").asInt() == 0)?RoleEnum.fromString("Supervisor") : RoleEnum.fromString("Worker");
            RoleEnum tmp_role = (requestBody.get("user").get("role").asInt() == 0)?RoleEnum.SUPERVISOR : RoleEnum.WORKER;
            log.info("tmp_role="+tmp_role);
            User tmp = new User(tmp_name,tmp_email,tmp_password,tmp_role);
            log.info("tmp_user="+tmp.toString());
            userRepo.save(tmp);
            return ResponseEntity.ok(tmp);

        } catch (Exception e) {
            log.error("IP="+request.getRemoteAddr()+"Problema con richiesta ="+e.getMessage()) ;
            return ResponseEntity.badRequest().body("missing 'username' and 'user'");
        }

    }
    /**
     * Updates the role of an existing user in the system.
     * Only users with SUPERVISOR role can update other users' roles.
     *
     * @param requestBody JSON containing:
     *                    - username: operator's username (must be SUPERVISOR)
     *                    - user: object with target user data
     *                      - email: target user's email (must be valid and exist)
     *                      - role: new role as integer (0=SUPERVISOR, other=WORKER)
     * @param request HttpServletRequest for IP logging and security monitoring
     *
     * @return ResponseEntity with:
     *         - 200 OK: role updated successfully, body contains updated User object
     *         - 400 BAD REQUEST: validation error, user not found, or insufficient permissions
     *
     * @throws Exception handled generically with error message
     *
     * Example JSON request:
     * {
     *   "username": "supervisor_user",
     *   "user": {
     *     "email": "target@example.com",
     *     "role": 1
     *   }
     * }
     *
     * Security notes:
     * - All unauthorized attempts are logged with IP address
     * - Only SUPERVISOR users can perform role updates
     * - Target user is identified by email address
     */
    @PostMapping("/updateRoleUser")
    public ResponseEntity updateRoleUser(@RequestBody JsonNode requestBody, HttpServletRequest request){
        try{
            if(!requestBody.hasNonNull("username")){
                log.warn("IP="+request.getRemoteAddr()+" tried to update a user role ");
                return ResponseEntity.badRequest().body("username field missing ");
            }

            String operator = requestBody.get("username").asText();
            User operatorUser = userRepo.findUserByUsername(operator).orElse(null);

            if (operatorUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User creator not registered");
            }

            if (operatorUser.getRole() != RoleEnum.SUPERVISOR) {
                log.error("Attempt to change role of a user made by IP="+request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only supervisors can change user roles");
            }

            if(!requestBody.hasNonNull("user")){
                log.warn("IP="+request.getRemoteAddr()+" problem with json object in updateRoleUser ");
                return ResponseEntity.badRequest().body("field user missing ");
            }

            if(!requestBody.get("user").hasNonNull("email")){
                log.warn("IP="+request.getRemoteAddr()+" problem with json object missing email field ");
                return ResponseEntity.badRequest().body("field email missing in object user ");
            }

            // FIX: Messaggio di log corretto
            if(!requestBody.get("user").hasNonNull("role")){
                log.warn("IP="+request.getRemoteAddr()+" problem with json object missing role field ");
                return ResponseEntity.badRequest().body("field role missing in object user ");
            }

            String userEmail = requestBody.get("user").get("email").asText();

            // FIX: Messaggio di errore meno dettagliato
            if (!Tools.isValidEmail(userEmail)) {
                log.error("Invalid email format during updateRoleUser made by IP="+request.getRemoteAddr());
                return ResponseEntity.badRequest().body("Invalid email format");
            }

            Optional<User> localUser = userRepo.findUserByEmail(userEmail);
            log.info("localUser="+localUser.toString());

            // FIX: Messaggio generico per sicurezza
            if (!localUser.isPresent()) {
                log.error("User not found with email during updateRoleUser made by IP="+request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // FIX: Gestione sicura del parsing del ruolo
            try {
                int roleValue = requestBody.get("user").get("role").asInt();
                RoleEnum newRole = (requestBody.get("user").get("role").asInt() == 0)?RoleEnum.SUPERVISOR : RoleEnum.WORKER;

                localUser.get().setRole(newRole);
                userRepo.save(localUser.get());

                return ResponseEntity.ok("true");

            } catch (Exception roleException) {
                log.error("Invalid role value during updateRoleUser made by IP="+request.getRemoteAddr());
                return ResponseEntity.badRequest().body("Invalid role value");
            }

        } catch(Exception e){
            log.error("Error updating user role for IP="+request.getRemoteAddr(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
   /*
    public ResponseEntity checkCredentials(@RequestBody JsonNode requestBody, HttpServletRequest request){}
    public ResponseEntity updatePassword(@RequestBody JsonNode requestBody, HttpServletRequest request){}
    */
}
