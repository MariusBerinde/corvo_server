package marius.server.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import marius.AppExceptions;
import marius.server.Tools;
import marius.server.data.ApprovedUsers;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.data.dto.UpdateRequestRoleDto;
import marius.server.data.dto.UserRegistrationDto;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if(user.isEmpty()){

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
            log.error("IP={} username={} not found" ,ip, username);
            throw new AppExceptions.UserNotAuthException("user not allowed to do delete operation ");
        }
        log.info(" username riconosciuto");
        return approvedUsersRepo.deleteByEmail(email);
    }

    @Transactional
    public User createUser(@Valid UserRegistrationDto user, String remoteAddr) {

        String tmp_name = user.name();

        String tmp_email = user.email();

        String tmp_password = Tools.hashPassword(user.password());
        //String tmp_password = requestBody.get("user").get("password").asText();

        log.info("tmp_password={}" , tmp_password);
        RoleEnum tmp_role = (user.role() == 0) ? RoleEnum.SUPERVISOR : RoleEnum.WORKER;
        log.info("tmp_role={}" , tmp_role);
        User tmp = new User(tmp_name, tmp_email, tmp_password, tmp_role);
        log.info("tmp_user={}", tmp.toString());

        return userRepo.save(tmp);
    }

    @Transactional
    public String changeUserRole( UpdateRequestRoleDto user, String remoteAddr) {

        String operator = user.username();
        User operatorUser = userRepo.findUserByUsername(operator).orElse(null);

        if (operatorUser == null) {
            log.error("operator={} not found", operator);
            throw new AppExceptions.UserNotFoundException("user not allowed to change role");
        }

        if (operatorUser.getRole() != RoleEnum.SUPERVISOR) {
            log.error("Attempt to change role of a user made by IP={} by user with email={}" , remoteAddr, operatorUser.getEmail() );
            throw new AppExceptions.UserNotAuthException("user not allowed to change role");
        }


        String userEmail = user.user().email();

        Optional<User> localUser = userRepo.findUserByEmail(userEmail);
        log.info("localUser=" + localUser.toString());

        if (!localUser.isPresent()) {
            log.error("User not found with email during updateRoleUser made by IP={}" , remoteAddr);
            throw new AppExceptions.UserNotFoundException("user with email ={} not found".formatted(userEmail));
        }
            int roleValue = user.user().role();
            RoleEnum newRole = (roleValue == 0) ? RoleEnum.SUPERVISOR : RoleEnum.WORKER;

            localUser.get().setRole(newRole);
            userRepo.save(localUser.get());

            return "true";
    }

    @Transactional
    public String deleteUser(String supervisor, String email, String remoteAddr) {

        String operator = supervisor;
        String userEmail = email.toLowerCase();
        Optional<User> operatorUser = userRepo.findUserByUsername(operator);
        if (operatorUser.isEmpty()) {
            log.error("IP={} - Unknown operator:{} tried to delete user:{}",remoteAddr,operator,userEmail );
            throw new AppExceptions.UserNotFoundException("user"+operator+" not found to delete operation");
        }
        RoleEnum roleOperator = operatorUser.get().getRole();
        if (roleOperator != RoleEnum.SUPERVISOR) {
            log.error("User={} tried to delete={} without permissions",operator,userEmail);
            throw new AppExceptions.UserNotAuthException("user not allowed to delete");
        }
        log.info("operator ={} delete user ={}",operator,userEmail);
        userRepo.deleteById(userEmail);
       return "true";
    }

    public User loginUser(String email, String password, String ip) {


        Optional<User> serverUser = userRepo.findUserByEmail(email);
        log.info("serverUser={}", serverUser.get());
        if (serverUser.isEmpty()) {
            log.error("Tentative to authenticate with email{} from IP={}", email, ip);
           throw  new AppExceptions.UserNotFoundException("user not found");
        }


        if (!Tools.isPasswordHashedWith(password, serverUser.get().getPassword())) {
            log.error("Invalid password during login of user ={} from IP={}", email, ip);
            log.info("DEBUG HASH ={}", serverUser.get().getPassword());
           throw  new AppExceptions.UserNotAuthException("email of password wrong");
        }

        log.info("User ={} successfully authenticated with IP={}", email, ip);
        serverUser.get().setPassword("");
        return serverUser.get();
    }


    @Transactional
    public String updateUserPassword(String email,String oldPwd,String newPwd,String ip) {
        Optional<User> serverUser = userRepo.findUserByEmail(email);
        if (serverUser.isEmpty()) {
            log.error("Tentative to update the password with email {} from IP={}", email, ip);
            throw new AppExceptions.UserNotFoundException("user not found");
        }
        if(oldPwd.equals(newPwd) ){
            log.info("oldPwd={} newPwd={} for user={}", oldPwd, newPwd, email);
            throw new AppExceptions.UserSamePasswordExcemptio("you can't use your old password");
        }

        if (!Tools.isPasswordHashedWith(oldPwd, serverUser.get().getPassword())) {
            log.error("updatePassword : failed to update the password with email {} from IP={} because the old password is wrong", email, ip);
            throw new AppExceptions.UserNotAuthException("wrong old password");
        }
        log.info("new password ={}", newPwd);
        User userdb=serverUser.get();
        userdb.setPassword(newPwd);
        return "true";
    }

    @Transactional
    public List<User> getAllUsers(String username, String ip) {

        Optional<User> user = userRepo.findUserByUsername(username);
        if (user.isEmpty()) {
            log.info("IP={}getAllUsers user not present ", ip);
            throw new AppExceptions.UserNotFoundException("user not found");
        }
        if (user.get().getRole()==RoleEnum.WORKER) {
            log.info("IP={}getAllUsers user ={} has wrong role", ip, user.get().getUsername());
            throw new AppExceptions.UserNotAuthException("user not allowed to retrieve data of users ");
        }

        return userRepo.findAll()
                .parallelStream()
                .map(tmp -> {
                    User safeUser = new User();
                    safeUser.setUsername(tmp.getUsername());
                    safeUser.setEmail(tmp.getEmail());
                    safeUser.setRole(tmp.getRole());
                    return safeUser;
                })
                .collect(Collectors.toList());
    }
}
