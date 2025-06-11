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
 *                      - password: new user's password
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

            if (creatorUser.getRole() != RoleEnum.SUPERVISOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only supervisors can create users");
            }

            String tmp_name = requestBody.get("user").get("name").asText();

            String tmp_email = requestBody.get("user").get("email").asText();
            if (!Tools.isValidEmail(tmp_email)) {
              log.error("format not valid email "+tmp_email+" during creation user with username ="+tmp_name);
                return ResponseEntity.badRequest().body("format not valid email "+tmp_email+" during creation user with username ="+tmp_name);
            }

            String tmp_password = Tools.hashPassword(requestBody.get("user").get("password").asText());

            log.info("tmp_password="+tmp_password);
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

            if (!localUser.isPresent()) {
                log.error("User not found with email during updateRoleUser made by IP="+request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

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


    /**
     * Deletes a user from the system.
     *
     * This endpoint allows authorized supervisors to delete user accounts from the system.
     * Only users with SUPERVISOR role are permitted to perform this operation.
     *
     * @param requestBody JSON object containing the deletion request with the following required fields:
     *                   - username: The username of the operator performing the deletion (must be a supervisor)
     *                   - email: The email address of the user to be deleted (serves as the unique identifier)
     * @param request HTTP servlet request object used for logging the client's IP address
     *
     * @return ResponseEntity containing:
     *         - 200 OK with "true" if deletion is successful
     *         - 400 Bad Request if required fields (username/email) are missing from the request body
     *         - 401 Unauthorized if the operator does not have SUPERVISOR privileges
     *         - 404 Not Found if the operator username is not found in the system
     *
     * @throws RuntimeException if the user to be deleted does not exist (handled by repository layer)
     *
     * Security considerations:
     * - Validates operator permissions before allowing deletion
     * - Logs all deletion attempts with IP addresses for audit purposes
     * - Logs unauthorized access attempts for security monitoring
     *
     * Usage example:
     * POST /api/users/delete
     * {
     *   "username": "supervisor_user",
     *   "email": "user.to.delete@example.com"
     * }
     */
    @PostMapping("/deleteUser")
    public ResponseEntity deleteUser(@RequestBody JsonNode requestBody, HttpServletRequest request){

            if(!requestBody.hasNonNull("username")){
                log.warn("IP="+request.getRemoteAddr()+" tried to update a user role ");
                return ResponseEntity.badRequest().body("username field missing ");
            }
            if(!requestBody.hasNonNull("email")){
                log.error("IP="+request.getRemoteAddr()+" tried to delete a usere without email field ");
                return ResponseEntity.badRequest().body("email field missing ");
            }
            String operator = requestBody.get("username").asText();
            String userEmail = requestBody.get("email").asText();
        Optional<User> operatorUser = userRepo.findUserByUsername(operator);
        if (operatorUser.isEmpty()) {
            log.error("IP=" + request.getRemoteAddr() + " - Unknown operator: " + operator + " tried to delete user: " + userEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Operator not found");
        }

        RoleEnum roleOperator = operatorUser.get().getRole();
        if (roleOperator != RoleEnum.SUPERVISOR) {
            log.error("User=" + operator + " tried to delete=" + userEmail + " without permissions");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized"); // Fixed: added return
        }
           log.info("operator ="+operator+" delete user ="+userEmail);
           userRepo.deleteById(userEmail);
           return ResponseEntity.ok("true");

    }

    /**
     * Authenticates a user with username and password credentials.
     * Verifies the provided password against the stored Argon2 hash.
     * All authentication attempts are logged with client IP address.
     *
     * @param requestBody JSON object containing user credentials:
     *                    - username (string, required): the user's username
     *                    - password (string, required): the user's plain text password
     * @param request HttpServletRequest used for IP address logging
     * @return ResponseEntity with:
     *         - 200 OK: authentication successful, body contains User object
     *         - 400 BAD REQUEST: missing required fields (username or password)
     *         - 401 UNAUTHORIZED: invalid username or password
     */
    @PostMapping("/authUser")
    public ResponseEntity  authUser(@RequestBody JsonNode requestBody, HttpServletRequest request){
        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+" tried to login to the system");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if(!requestBody.hasNonNull("password")){
            log.error("IP="+request.getRemoteAddr()+"tried to login to the system without password field ");
            return ResponseEntity.badRequest().body("password field missing ");
        }
        String username = requestBody.get("username").asText();
        String password = requestBody.get("password").asText();

        Optional<User> serverUser = userRepo.findUserByUsername(username) ;
        log.info("serverUser="+serverUser.toString());
        if (!serverUser.isPresent()) {
           log.error("Tentative to authenticate with username "+username+" from IP="+request.getRemoteAddr());
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        if(!Tools.isPasswordHashedWith(password, serverUser.get().getPassword())) {
            log.error("Invalid password during login of user ="+username+" from IP="+request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }
        log.info("User ="+username+" successfully authenticated with IP="+request.getRemoteAddr());
        return ResponseEntity.ok(serverUser.get());
    }

    /**
     * Update the password of the user
     * @param requestBody JSON object containing user credentials:
     *                    - username (string, required): the user's username
     *                    - oldPassword (string, required): the user's plain text password
     *                    - newPassword (string, required): the user's plain text password
     * @param request HttpServletRequest used for IP address logging
     * @return ResponseEntity with:
     *         - 200 OK: authentication successful,true
     *         - 400 BAD REQUEST: missing required fields (username or password)
     *         - 401 UNAUTHORIZED: invalid username or password
     *         - 403 FORBIDDEN: if the old password is not correct
     */
    @PostMapping("/updatePassword")
    public ResponseEntity updatePassword(@RequestBody JsonNode requestBody, HttpServletRequest request){

        if(!requestBody.hasNonNull("username")){
            log.warn("IP="+request.getRemoteAddr()+"updatePassword missing username field in json file");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if(!requestBody.hasNonNull("oldPassword")){
            log.error("IP="+request.getRemoteAddr()+"updatePassword missing oldPassword field in json file");
            return ResponseEntity.badRequest().body("oldPassword field missing ");
        }
        if(!requestBody.hasNonNull("newPassword")){
            log.error("IP="+request.getRemoteAddr()+"updatePassword missing newPassword field in json file");
            return ResponseEntity.badRequest().body("newPassword field missing ");
        }

        String username = requestBody.get("username").asText();

        Optional<User> serverUser = userRepo.findUserByUsername(username)  ;
        if (!serverUser.isPresent()) {
            log.error("Tentative to update the password with username "+username+" from IP="+request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        String oldPwd = requestBody.get("oldPassword").asText();
        if (!Tools.isPasswordHashedWith(oldPwd, serverUser.get().getPassword())) {
            log.error("updatePassword : failed to update the password with username "+username+" from IP="+request.getRemoteAddr()+" because the old password is wrong");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid old password");
        }
        String newPwd = Tools.hashPassword(requestBody.get("newPassword").asText()) ;
        serverUser.get().setPassword(newPwd);
        userRepo.save(serverUser.get());
        return ResponseEntity.ok("true");
    }
}
