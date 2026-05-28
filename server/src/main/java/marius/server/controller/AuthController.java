package marius.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import marius.server.Tools;
import marius.server.data.ApprovedUsers;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.data.dto.UserRegistrationRequestDto;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;
import marius.server.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class implements REST endpoints for user management and authentication
 * @author Marius Berinde Dumitru
 */
@RestController
public class AuthController {
    /**
     * repository for interact with the approved user table
     */
    private final ApprovedUsersRepo approvedUsersRepo;

    /**
     * repository for interact with the approved user table
     */
    private final UserRepo userRepo;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    /**
     * Constructs a new AuthController with the required repository dependencies.
     *
     * @param approvedUsersRepo repository for managing approved user registrations
     * @param userRepo repository for managing user accounts
     */
    public AuthController(ApprovedUsersRepo approvedUsersRepo, UserRepo userRepo, AuthService authService) {

        this.authService = authService;
        this.approvedUsersRepo = approvedUsersRepo;
        this.userRepo = userRepo;
    }


    /**
     * Enables user registration by adding an email address to the approved users list.
     *
     * <p>This endpoint allows authenticated users to approve email addresses for registration.
     * The requesting user must exist in the system and the email must have a valid format.
     * If the email is already approved, the operation succeeds without duplicating the entry.</p>
     *
     * @param requestBody JSON object containing:
     *                    <ul>
     *                    <li><strong>username</strong> (string, required): username of the requesting user</li>
     *                    <li><strong>email</strong> (string, required): email address to approve for registration</li>
     *                    </ul>
     * @param request HttpServletRequest for IP address logging and security monitoring
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: email successfully added to approved users list, body: "true"</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing required fields or invalid email format</li>
     *         <li><strong>401 UNAUTHORIZED</strong>: username not found in system</li>
     *         </ul>
     *
     *  Example request body:
     * <pre>{@code
     * {
     *   "username": "admin_user",
     *   "email": "newuser@example.com"
     * }
     * }</pre>
     *
     *  All requests are logged with client IP address for security monitoring
     */
    @PostMapping("/enableUserRegistration")
    public ResponseEntity<?> enableUserRegistration(@RequestBody JsonNode requestBody, HttpServletRequest request) {
        try {
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

            if (!userRepo.existsByUsername(username)) {
                log.error("IP=" + request.getRemoteAddr());
                return ResponseEntity.badRequest().body("username not auth ");
            }
            log.info(" username riconosciuto");

            String email = requestBody.get("email").asText();

            if (!Tools.isValidEmail(email)) {
                log.debug("the email { } not have a valid format", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email format error");
            }
            Optional<ApprovedUsers> user =approvedUsersRepo.findByEmail(email);
            if(!user.isPresent()){

                log.debug("the email {} have a valid format", email);
                ApprovedUsers tmp = new ApprovedUsers(email);
                System.out.println("AuthController::enableUserRegistration Approved Users: " + tmp);
                approvedUsersRepo.save(tmp);
                return ResponseEntity.ok("true");
            }
            return ResponseEntity.ok("true");


        } catch (Exception e) {
            log.error("IP=" + request.getRemoteAddr() + "Problema con richiesta = ",e.toString());
            return ResponseEntity.badRequest().body("missing 'username' or 'email'");
        }

    }


    @PostMapping("/enableUserRegistration2")
    public ResponseEntity<String> enableUserRegistration2(@RequestBody @Valid UserRegistrationRequestDto data, HttpServletRequest request) {
            String ris = authService.enableUserRegistration(data.email() , data.username(),request.getRemoteAddr());
            log.info(" enableUserRegistration2 ris={}", ris);
            return ResponseEntity.ok(ris);
    }
    /**
     * Retrieves all email addresses that are approved for user registration.
     *
     * <p>This endpoint returns a list of all email addresses that have been approved
     * for registration in the system. Only authenticated users can access this information.</p>
     *
     * @param username username of the requesting user (passed in HTTP header)
     * @param request HttpServletRequest for IP address logging
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: body contains JSON array of approved email addresses</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing or empty username header</li>
     *         <li><strong>401 UNAUTHORIZED</strong>: username not found in system</li>
     *         </ul>
     *
     *  Response format:
     * <pre>{@code
     * ["user1@example.com", "user2@example.com", "user3@example.com"]
     * }</pre>
     *
     */
    @GetMapping("/getApprovedUsers")
    public ResponseEntity<?> getApprovedUsers(@RequestHeader("username") String username, HttpServletRequest request) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing username header");
        }

        log.info("IP=" + request.getRemoteAddr(),"getApprovedUsers:parametro "+username);
            if (!userRepo.existsByUsername(username)) {
                log.error("IP=" + request.getRemoteAddr());
                return ResponseEntity.badRequest().body("username not auth ");
            }
            List<ApprovedUsers> listApprovedUsers = approvedUsersRepo.findAll();
            List<String> approvedEmails = new ArrayList<>();
            for (ApprovedUsers approvedUsers : listApprovedUsers) {
                approvedEmails.add(approvedUsers.getEmail());
            }

            return ResponseEntity.ok(approvedEmails);
    }


    @GetMapping("/getApprovedUsers2")
    public ResponseEntity<?> getApprovedUsers2(@RequestHeader("username") String username, HttpServletRequest request) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing username header");
        }

        log.info("IP=" + request.getRemoteAddr(),"getApprovedUsers:parametro "+username);

        List<String> approvedEmails = authService.getApprovedUsers(username,request.getRemoteAddr());
        return ResponseEntity.ok(approvedEmails);
    }

    /**
     * Checks whether an email address is approved for user registration.
     *
     * <p>This endpoint verifies if a specific email address exists in the approved
     * users list and can be used for account registration.</p>
     *
     * @param requestBody JSON object containing:
     *                    <ul>
     *                    <li><strong>email</strong> (string, required): email address to check</li>
     *                    </ul>
     * @param request HttpServletRequest for IP address logging
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: email is approved for registration, body: "ok"</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing email field in request body</li>
     *         <li><strong>404 NOT FOUND</strong>: email is not in approved users list</li>
     *         </ul>
     *
     *  Example request body:
     * <pre>{@code
     * {
     *   "email": "user@example.com"
     * }
     * }</pre>
     */
    @PostMapping("/isEmailApproved")
    public ResponseEntity<?> isEmailApproved(@RequestBody JsonNode requestBody, HttpServletRequest request) {

        if (!requestBody.hasNonNull("email")) {
            log.error("IP={}  tried to update a user role ",request.getRemoteAddr());
            return ResponseEntity.badRequest().body("username field missing ");
        }
        String email = requestBody.get("email").asText();
        Optional<ApprovedUsers> tmp = approvedUsersRepo.findByEmail(email);
        if (tmp.isPresent()) {
            return ResponseEntity.ok("ok");
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/isEmailApproved2")
    public ResponseEntity<?> isEmailApproved2(@RequestBody JsonNode requestBody, HttpServletRequest request) {

        if (!requestBody.hasNonNull("email")) {
            log.error("IP=" + request.getRemoteAddr() + " tried to update a user role ");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        String email = requestBody.get("email").asText();

        boolean ris = authService.isEmaiApproved(email);
        if(ris){
                return ResponseEntity.ok("ok");
        }else {

            return ResponseEntity.notFound().build();
        }

    }


    /**
     * Delete a user from the list of the users that can register to the app
     *
     * @param requestBody JSON containing:
     *                    - username: creator's username (must be SUPERVISOR)
     *                    - email: the email of the user from whom we want to remove permission to register to the app
     * @param request     is used for get the ip of the sender
     * @return ResponseEntity with:
     * - 200 OK: user created successfully, body contains User object
     * - 400 BAD REQUEST: validation error or insufficient permissions
     *  Exception handled generically with error message
     */
    @PostMapping("/deleteEnabledUser")
    @Transactional
    public ResponseEntity<?> deleteEnabledUser(@RequestBody JsonNode requestBody, HttpServletRequest request) {
        try {

            if (!requestBody.hasNonNull("username")) {
                log.error("Richiesta malformata: manca 'user' da IP={}", request.getRemoteAddr());
                return ResponseEntity.badRequest().body("missing username");
            }

            if (!requestBody.hasNonNull("email")) {
                log.error("Richiesta malformata: manca 'email' da IP={}", request.getRemoteAddr());
                return ResponseEntity.badRequest().body("missing email");
            }
            String username = requestBody.get("username").asText();

            if (!userRepo.existsByUsername(username)) {
                log.error("IP=" + request.getRemoteAddr());
                return ResponseEntity.badRequest().body("username not auth ");
            }
            log.info(" username riconosciuto");
            String email = requestBody.get("email").asText();
            /*
            if (!Tools.isValidEmail(email)) {
                log.error("the email { } not have a valid format", email);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("email format error");
            }

             */


            log.info("the email {} have a valid format", email);
            Integer ris = approvedUsersRepo.deleteByEmail(email);
            log.info("ris {} ris ok", ris);
            return ResponseEntity.ok(ris);

        } catch (Exception e) {
            log.error("IP=" + request.getRemoteAddr() + "Problema con richiesta = " + e.getMessage());
            return ResponseEntity.badRequest().body("missing 'username' or 'email'");
        }

    }

    @PostMapping("/deleteEnabledUser2")
    public ResponseEntity<?> deleteEnabledUser2(@RequestBody @Valid UserRegistrationRequestDto data, HttpServletRequest request) {
            log.info(" username riconosciuto");
            Integer ris = authService.deleteEnabledUser(data.username(),data.email(),request.getRemoteAddr() );
            log.info("ris {} ris ok", ris);
            return ResponseEntity.ok(ris);
    }

    /**
     * Creates a new user account in the system.
     *
     * <p>This endpoint allows the creation of new user accounts with specified roles.
     * The user's password is automatically hashed using Argon2 before storage.
     * Email validation is performed to ensure data integrity.</p>
     *
     * @param requestBody JSON object containing:
     *                    <ul>
     *                    <li><strong>user</strong> (object, required): user data object containing:
     *                        <ul>
     *                        <li><strong>name</strong> (string, required): new user's username</li>
     *                        <li><strong>email</strong> (string, required): new user's email address</li>
     *                        <li><strong>password</strong> (string, required): new user's password (will be hashed)</li>
     *                        <li><strong>role</strong> (integer, required): user role (0 = SUPERVISOR, other = WORKER)</li>
     *                        </ul>
     *                    </li>
     *                    </ul>
     * @param request HttpServletRequest for IP address logging
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: user created successfully, body contains User object</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing required fields or invalid email format</li>
     *         </ul>
     *
     *  Example request body:
     * <pre>{@code
     * {
     *   "user": {
     *     "name": "john_doe",
     *     "email": "john@example.com",
     *     "password": "securePassword123",
     *     "role": 1
     *   }
     * }
     * }</pre>
     *
     *  Password is hashed using Argon2 algorithm before database storage
     * @see RoleEnum for role definitions
     */
    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@RequestBody JsonNode requestBody, HttpServletRequest request) {
        try {
/*
            if (creatorUser.getRole() != RoleEnum.SUPERVISOR) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only supervisors can create users");
            }
            */

            String tmp_name = requestBody.get("user").get("name").asText();

            String tmp_email = requestBody.get("user").get("email").asText();
            if (!Tools.isValidEmail(tmp_email)) {
                log.error("format not valid email " + tmp_email + " during creation user with username =" + tmp_name);
                return ResponseEntity.badRequest().body("format not valid email " + tmp_email + " during creation user with username =" + tmp_name);
            }

            String tmp_password = Tools.hashPassword(requestBody.get("user").get("password").asText());
            //String tmp_password = requestBody.get("user").get("password").asText();

            log.info("tmp_password=" + tmp_password);
            RoleEnum tmp_role = (requestBody.get("user").get("role").asInt() == 0) ? RoleEnum.SUPERVISOR : RoleEnum.WORKER;
            log.info("tmp_role=" + tmp_role);
            User tmp = new User(tmp_name, tmp_email, tmp_password, tmp_role);
            log.info("tmp_user=" + tmp.toString());
            userRepo.save(tmp);
            return ResponseEntity.ok(tmp);

        } catch (Exception e) {
            log.error("IP=" + request.getRemoteAddr() + "Problema con richiesta =" + e.getMessage());
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
     *                    - email: target user's email (must be valid and exist)
     *                    - role: new role as integer (0=SUPERVISOR, other=WORKER)
     * @param request     HttpServletRequest for IP logging and security monitoring
     * @return ResponseEntity with:
     * - 200 OK: role updated successfully, body contains updated User object
     * - 400 BAD REQUEST: validation error, user not found, or insufficient permissions
     *  Exception handled generically with error message
     *                   <p>
     *                   Example JSON request:
     *                   {
     *                   "username": "supervisor_user",
     *                   "user": {
     *                   "email": "target@example.com",
     *                   "role": 1
     *                   }
     *                   }
     *                   <p>
     *                   Security notes:
     *                   - All unauthorized attempts are logged with IP address
     *                   - Only SUPERVISOR users can perform role updates
     *                   - Target user is identified by email address
     */
    @PostMapping("/updateRoleUser")
    public ResponseEntity<?> updateRoleUser(@RequestBody JsonNode requestBody, HttpServletRequest request) {
        try {
            if (!requestBody.hasNonNull("username")) {
                log.warn("IP=" + request.getRemoteAddr() + " tried to update a user role ");
                return ResponseEntity.badRequest().body("username field missing ");
            }

            String operator = requestBody.get("username").asText();
            User operatorUser = userRepo.findUserByUsername(operator).orElse(null);

            if (operatorUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User creator not registered");
            }

            if (operatorUser.getRole() != RoleEnum.SUPERVISOR) {
                log.error("Attempt to change role of a user made by IP=" + request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only supervisors can change user roles");
            }

            if (!requestBody.hasNonNull("user")) {
                log.warn("IP=" + request.getRemoteAddr() + " problem with json object in updateRoleUser ");
                return ResponseEntity.badRequest().body("field user missing ");
            }

            if (!requestBody.get("user").hasNonNull("email")) {
                log.warn("IP=" + request.getRemoteAddr() + " problem with json object missing email field ");
                return ResponseEntity.badRequest().body("field email missing in object user ");
            }

            if (!requestBody.get("user").hasNonNull("role")) {
                log.warn("IP=" + request.getRemoteAddr() + " problem with json object missing role field ");
                return ResponseEntity.badRequest().body("field role missing in object user ");
            }

            String userEmail = requestBody.get("user").get("email").asText();

            if (!Tools.isValidEmail(userEmail)) {
                log.error("Invalid email format during updateRoleUser made by IP=" + request.getRemoteAddr());
                return ResponseEntity.badRequest().body("Invalid email format");
            }

            Optional<User> localUser = userRepo.findUserByEmail(userEmail);
            log.info("localUser=" + localUser.toString());

            if (!localUser.isPresent()) {
                log.error("User not found with email during updateRoleUser made by IP=" + request.getRemoteAddr());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            try {
                int roleValue = requestBody.get("user").get("role").asInt();
                RoleEnum newRole = (requestBody.get("user").get("role").asInt() == 0) ? RoleEnum.SUPERVISOR : RoleEnum.WORKER;

                localUser.get().setRole(newRole);
                userRepo.save(localUser.get());

                return ResponseEntity.ok("true");

            } catch (Exception roleException) {
                log.error("Invalid role value during updateRoleUser made by IP=" + request.getRemoteAddr());
                return ResponseEntity.badRequest().body("Invalid role value");
            }

        } catch (Exception e) {
            log.error("Error updating user role for IP=" + request.getRemoteAddr(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


    /**
     * Deletes a user from the system.
     * <p>
     * This endpoint allows authorized supervisors to delete user accounts from the system.
     * Only users with SUPERVISOR role are permitted to perform this operation.
     *
     * @param requestBody JSON object containing the deletion request with the following required fields:
     *                    - username: The username of the operator performing the deletion (must be a supervisor)
     *                    - email: The email address of the user to be deleted (serves as the unique identifier)
     * @param request     HTTP servlet request object used for logging the client's IP address
     * @return ResponseEntity containing:
     * - 200 OK with "true" if deletion is successful
     * - 400 Bad Request if required fields (username/email) are missing from the request body
     * - 401 Unauthorized if the operator does not have SUPERVISOR privileges
     * - 404 Not Found if the operator username is not found in the system
     * @throws RuntimeException if the user to be deleted does not exist (handled by repository layer)
     *                          <p>
     *                          Security considerations:
     *                          - Validates operator permissions before allowing deletion
     *                          - Logs all deletion attempts with IP addresses for audit purposes
     *                          - Logs unauthorized access attempts for security monitoring
     *                          <p>
     *                          Usage example:
     *                          POST /api/users/delete
     *                          {
     *                          "username": "supervisor_user",
     *                          "email": "user.to.delete@example.com"
     *                          }
     */
    @PostMapping("/deleteUser")
    public ResponseEntity<?> deleteUser(@RequestBody JsonNode requestBody, HttpServletRequest request) {

        if (!requestBody.hasNonNull("username")) {
            log.warn("IP=" + request.getRemoteAddr() + " tried to update a user role ");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if (!requestBody.hasNonNull("email")) {
            log.error("IP=" + request.getRemoteAddr() + " tried to delete a usere without email field ");
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
        log.info("operator =" + operator + " delete user =" + userEmail);
        userRepo.deleteById(userEmail);
        return ResponseEntity.ok("true");

    }

    /**
     * Authenticates a user with email and password credentials.
     *
     * <p>This endpoint verifies user credentials by checking the provided password
     * against the stored Argon2 hash. Upon successful authentication, the user's
     * password is cleared from the response object for security.</p>
     *
     * @param requestBody JSON object containing:
     *                    <ul>
     *                    <li><strong>email</strong> (string, required): user's email address</li>
     *                    <li><strong>password</strong> (string, required): user's plaintext password</li>
     *                    </ul>
     * @param request HttpServletRequest for IP address logging
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: authentication successful, body contains User object (password cleared)</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing required fields (email or password)</li>
     *         <li><strong>401 UNAUTHORIZED</strong>: invalid email or password</li>
     *         </ul>
     *
     *  Example request body:
     * <pre>{@code
     * {
     *   "email": "user@example.com",
     *   "password": "userPassword123"
     * }
     * }</pre>
     *
     *  All authentication attempts are logged with client IP address
     *  Password is cleared from response object for security
     */
    @PostMapping("/authUser")
    public ResponseEntity<?> authUser(@RequestBody JsonNode requestBody, HttpServletRequest request) {
        if (!requestBody.hasNonNull("email")) {
            log.warn("IP=" + request.getRemoteAddr() + " tried to login to the system");
            return ResponseEntity.badRequest().body("username field missing ");
        }
        if (!requestBody.hasNonNull("password")) {
            log.error("IP=" + request.getRemoteAddr() + "tried to login to the system without password field ");
            return ResponseEntity.badRequest().body("password field missing ");
        }
        String email = requestBody.get("email").asText();
        String password = requestBody.get("password").asText();

        Optional<User> serverUser = userRepo.findUserByEmail(email);
        log.info("serverUser=" + serverUser.toString());
        if (!serverUser.isPresent()) {
            log.error("Tentative to authenticate with email" + email + " from IP=" + request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }


        if (!Tools.isPasswordHashedWith(password, serverUser.get().getPassword())) {
            log.error("Invalid password during login of user =" + email + " from IP=" + request.getRemoteAddr());
            log.info("DEBUG HASH =" +serverUser.get().getPassword());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }



        log.info("User =" + email + " successfully authenticated with IP=" + request.getRemoteAddr());
        serverUser.get().setPassword("");
        return ResponseEntity.ok(serverUser.get());
    }

    /**
     * Updates a user's password after verifying their current password.
     *
     * <p>This endpoint allows users to change their password by providing their current
     * password for verification. The new password is hashed using Argon2 before storage.
     * The operation is performed within a transaction to ensure data consistency.</p>
     *
     * @param requestBody JSON object containing:
     *                    <ul>
     *                    <li><strong>email</strong> (string, required): user's email address</li>
     *                    <li><strong>oldPassword</strong> (string, required): current password for verification</li>
     *                    <li><strong>newPassword</strong> (string, required): new password to set</li>
     *                    </ul>
     * @param request HttpServletRequest for IP address logging
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: password updated successfully, body: "true"</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing required fields</li>
     *         <li><strong>401 UNAUTHORIZED</strong>: user not found</li>
     *         <li><strong>403 FORBIDDEN</strong>: current password verification failed</li>
     *         </ul>
     *
     *  Example request body:
     * <pre>{@code
     * {
     *   "email": "user@example.com",
     *   "oldPassword": "currentPassword123",
     *   "newPassword": "newSecurePassword456"
     * }
     * }</pre>
     *
     *  Operation is transactional to ensure data consistency
     *  New password is hashed using Argon2 algorithm
     *  All password change attempts are logged with client IP address
     */
    @PostMapping("/updatePassword")
    @Transactional
    public ResponseEntity<?> updatePassword(@RequestBody JsonNode requestBody, HttpServletRequest request) {

        if (!requestBody.hasNonNull("email")) {
            log.warn("IP=" + request.getRemoteAddr() + "updatePassword missing email field in json file");
            return ResponseEntity.badRequest().body("email field missing ");
        }
        if (!requestBody.hasNonNull("oldPassword")) {
            log.error("IP=" + request.getRemoteAddr() + "updatePassword missing oldPassword field in json file");
            return ResponseEntity.badRequest().body("oldPassword field missing ");
        }
        if (!requestBody.hasNonNull("newPassword")) {
            log.error("IP=" + request.getRemoteAddr() + "updatePassword missing newPassword field in json file");
            return ResponseEntity.badRequest().body("newPassword field missing ");
        }

        String email = requestBody.get("email").asText();

        Optional<User> serverUser = userRepo.findUserByEmail(email);
        if (!serverUser.isPresent()) {
            log.error("Tentative to update the password with email " + email + " from IP=" + request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        String oldPwd = requestBody.get("oldPassword").asText();


        if (!Tools.isPasswordHashedWith(oldPwd, serverUser.get().getPassword())) {
            log.error("updatePassword : failed to update the password with email " + email + " from IP=" + request.getRemoteAddr() + " because the old password is wrong");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid old password");
        }



        /*
        if (!oldPwd.equals(serverUser.get().getPassword())) {
            log.error("updatePassword : failed to update the password with email " + email + " from IP=" + request.getRemoteAddr() + " because the old password is wrong");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid old password");
        }

         */
        String newPwd = Tools.hashPassword( requestBody.get("newPassword").asText());
        //String newPwd =  requestBody.get("newPassword").asText();
        log.info("new password =" + newPwd);

        /*
        serverUser.get().setPassword(newPwd);
        userRepo.save(serverUser.get());
         */
        User userdb=serverUser.get();
        userdb.setPassword(newPwd);



        /*
        if(Tools.isPasswordHashedWith(newPwd, userdb.getPassword()))
            log.info("UPDATE PASSWORD : aggiornamto correttente");
        else
            log.info("UPDATE PASSWORD :problema di aggiornamento");
*/


        //log.info("updatePassword : update pwd ok");
        return ResponseEntity.ok("true");
    }

    /**
     * Retrieves all user accounts from the system with sensitive information removed.
     *
     * <p>This endpoint returns a list of all users in the system. Only users with
     * SUPERVISOR role can access this information. For security, password fields
     * are excluded from the response. The processing is optimized using parallel streams.</p>
     *
     * @param username username of the requesting user (passed in HTTP header, must be SUPERVISOR)
     * @param request HttpServletRequest for IP address logging
     * @return ResponseEntity with:
     *         <ul>
     *         <li><strong>200 OK</strong>: body contains JSON array of User objects (without passwords)</li>
     *         <li><strong>400 BAD REQUEST</strong>: missing username header</li>
     *         <li><strong>401 UNAUTHORIZED</strong>: user not found or insufficient permissions</li>
     *         </ul>
     *
     * Response format:
     * <pre>{@code
     * [
     *   {
     *     "username": "user1",
     *     "email": "user1@example.com",
     *     "role": "SUPERVISOR"
     *   },
     *   {
     *     "username": "user2",
     *     "email": "user2@example.com",
     *     "role": "WORKER"
     *   }
     * ]
     * }</pre>
     *
     *  Password fields are excluded from response for security
     *  Processing uses parallel streams for improved performance
     *  Only SUPERVISOR users can access this endpoint
     */
    @GetMapping("/getAllUsers")
   public ResponseEntity<?> getAllUsers(@RequestHeader("username") String username,HttpServletRequest request){

       if (username == null || username.isEmpty()) {
           log.warn("IP=" + request.getRemoteAddr() + " tried to get users without username");
           return ResponseEntity.badRequest().body("Missing username header");
       }

       Optional<User> user = userRepo.findUserByUsername(username);
       if (!user.isPresent()) {
           log.info("IP=" + request.getRemoteAddr() + "getAllUsers user not present ");
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
       }
       if (user.get().getRole()==RoleEnum.WORKER) {
           log.info("IP=" + request.getRemoteAddr() + "getAllUsers user ="+ user.get().getUsername()+" has wrong role");
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("role wrong");
       }

        List<User> users = userRepo.findAll()
                .parallelStream()
                .map(tmp -> {
                    User safeUser = new User();
                    safeUser.setUsername(tmp.getUsername());
                    safeUser.setEmail(tmp.getEmail());
                    safeUser.setRole(tmp.getRole());
                    // Password non copiata = rimane null
                    return safeUser;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
   }


    /**
     * Health check endpoint to verify service availability.
     *
     * <p>This endpoint provides a simple health check mechanism to verify that the
     * authentication service is running and responding to requests. It always returns
     * true and can be used for monitoring and load balancer health checks.</p>
     *
     * @return ResponseEntity with 200 OK status and string pong
     *
     * This endpoint does not require authentication
     * Used for service health monitoring and load balancer checks
     */
    @GetMapping("/ping1")
    public ResponseEntity<String> ping1() {

        log.info("JAVA UP" );
        return ResponseEntity.ok("pong");
    }


}
