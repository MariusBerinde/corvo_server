package marius.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import marius.server.Tools;
import marius.server.data.ApprovedUsers;
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
			return ResponseEntity.ok(true);

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
           log.error("IP="+request.getRemoteAddr()+"Problema con richiesta") ;
           return ResponseEntity.badRequest().body("missing 'username' or 'email'");
       }
    }


    /**
     * Delete a user from the list of the users that can register to the app
     * @param requestBody
     * @param request is used for get the ip of the sender
     * @return
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
}
