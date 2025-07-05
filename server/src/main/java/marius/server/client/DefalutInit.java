package marius.server.client;

import marius.server.Tools;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class DefalutInit {
    private final UserRepo userRepo;
    private static final Logger log = LoggerFactory.getLogger(DefalutInit.class);
    public DefalutInit(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void createFirstUser(){
        Long  nrOfUsers = userRepo.count();
        if(nrOfUsers==0){
            //create a tmp user
            String username = "Admin";
            String email = "admin@gmail.com";
            String password = "Admin@123_!";
            String tmp_password = Tools.hashPassword(password);
            User tmpUser = new User(username,email,tmp_password, RoleEnum.SUPERVISOR);
            log.info("tmp_user=" + tmpUser.toString());
            log.info("userRepo= ",password);
            userRepo.save(tmpUser);
        }

    }
}
