package marius.server.controller;

import marius.server.data.ApprovedUsers;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final ApprovedUsersRepo approvedUsersRepo;
    private final UserRepo userRepo;

    public AuthController(ApprovedUsersRepo approvedUsersRepo, UserRepo userRepo) {
        this.approvedUsersRepo = approvedUsersRepo;
        this.userRepo = userRepo;
    }



    /**
     * @param email String the email of the user that will be approved for the registration
     * @return True if the email of the user is added to the table ApprovedUsers , false otherwise
     */
    @PostMapping("/enableUserRegistration")
    public ResponseEntity<Boolean> enableUserRegistration(@RequestBody String email){
        //	if (!Tools.isValidEmail(email))
        //		return ResponseEntity.badRequest().build();
        ApprovedUsers tmp = new ApprovedUsers(email);
        System.out.println("AuthController::enableUserRegistration Approved Users: " + tmp);
        approvedUsersRepo.save(tmp);
        return ResponseEntity.ok(true);
    }

    /**
     * @param email String the email of the user
     * @return True if the user can register to the site , false otherwise
     */
    @GetMapping("/isApprovedEmail")
    public ResponseEntity<Boolean> isApprovedEmail(@RequestBody String email){
        Boolean ris = this.approvedUsersRepo.existsByEmail(email);
        return ResponseEntity.ok(ris);
    }
    

    @GetMapping("/getById")
    public ResponseEntity<ApprovedUsers> getById(@RequestBody  Integer id){
      System.out.println("AuthController::getById Approved Users: " + id);
	Optional<ApprovedUsers> data = 	approvedUsersRepo.findById(id);

	return (data.isPresent())?(ResponseEntity.ok(data.get())):(ResponseEntity.notFound().build()) ;
    }


	@GetMapping("/getByMail")
	public  ResponseEntity<ApprovedUsers> getByMail(@RequestBody String email){

        System.out.println("AuthController::getByMail Approved Users: " + email);
		Optional<ApprovedUsers> data = 	approvedUsersRepo.findByEmail(email);
        System.out.println("AuthController::getByMail Approved Users: " + data);

		return (data.isPresent()) ? (ResponseEntity.ok(data.get())):(ResponseEntity.notFound().build()) ;
	}

}
