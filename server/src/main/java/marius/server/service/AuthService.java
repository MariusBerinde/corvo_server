package marius.server.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import marius.AppExceptions;
import marius.server.Tools;
import marius.server.controller.AuthController;
import marius.server.data.ApprovedUsers;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final ApprovedUsersRepo approvedUsersRepo;

    /**
     * repository for interact with the approved user table
     */
    private final UserRepo userRepo;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepo userRepo, ApprovedUsersRepo approvedUsersRepo) {
        this.approvedUsersRepo = approvedUsersRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public String enableUserRegistration(String email, String username, String  ip) {

        if (!userRepo.existsByUsername(username)) {
            log.error("IP={} username={}", ip, username);
            throw new AppExceptions.UserNotAuthException("user not allowed to allow registration");
        }
        log.info(" username riconosciuto");


        Optional<ApprovedUsers> user =approvedUsersRepo.findByEmail(email);
        if(!user.isPresent()){

            ApprovedUsers tmp = new ApprovedUsers(email);
            log.info(" Approved Users: {}",  tmp);
            approvedUsersRepo.save(tmp);
            return "true";
        }
        return "true";

    }

    @Transactional
    public List<String> getApprovedUsers(String username,String ip) {

        if (!userRepo.existsByUsername(username)) {
            log.error("IP={} username ={} not foud",  ip,username);
            throw new AppExceptions.UserNotAuthException("user not allowed to registration");
        }
        List<ApprovedUsers> listApprovedUsers = approvedUsersRepo.findAll();
        List<String> approvedEmails = new ArrayList<>();
        for (ApprovedUsers approvedUsers : listApprovedUsers) {
            approvedEmails.add(approvedUsers.getEmail());
        }
        return approvedEmails;
    }

    @Transactional
    public boolean isEmaiApproved(String email) {
        return approvedUsersRepo.findByEmail(email).isPresent();
    }

    /**
     *
     * @param username creator's username (must be SUPERVISOR)
     * @param email the email of the user from whom we want to remove permission to register to the app
     * @param ip the ip address of the user that make the request
     * @return the number of user deleted from approved
     */
    @Transactional
    public Integer deleteEnabledUser(String username,  String email,String ip) {

        if (!userRepo.existsByUsername(username)) {
            log.error("IP={} username not found" ,username,ip );
            throw new AppExceptions.UserNotAuthException("user not allowed to do delete operation ");
        }
        log.info(" username riconosciuto");
        return approvedUsersRepo.deleteByEmail(email);
    }
}
