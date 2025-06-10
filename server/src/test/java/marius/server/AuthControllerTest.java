package marius.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marius.server.controller.AuthController;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import marius.server.data.ApprovedUsers;
import marius.server.repo.ApprovedUsersRepo;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovedUsersRepo approvedUsersRepo;

    @MockBean
    private UserRepo userRepo;

    @Test
    void testValidUserOfEnableUserRegistration() throws Exception {
        String email = "utente@example.com";
        String username = "admin";
        ObjectNode json = objectMapper.createObjectNode();
        json.put("username", username);
        json.put("email", email);
        //  Esegui POST simulato
        User fakeUser = new User("admin", "admin@gmail.com","lol", RoleEnum.SUPERVISOR);

        when(userRepo.existsByUsername(username)).thenReturn(true);
        mockMvc.perform(post("/enableUserRegistration")
                        .contentType("application/json")
                        .content(json.toString()))
                .andExpect(status().isOk());
           //     .andExpect(content().string("true"));

        //  Verifica che `save` sia stato chiamato
        verify(approvedUsersRepo, times(1)).save(any(ApprovedUsers.class));
    }

    @Test
    void testInvalidFormatEnableUserRegistration() throws Exception {
        String email = "utentemplecom";
        String username = "admin";
        ObjectNode json = objectMapper.createObjectNode();
        json.put("username", username);
        //json.put("email", email);
        MvcResult ris = mockMvc.perform(post("/enableUserRegistration")
                        .contentType("application/json")
                        .content(json.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("missing email")).andReturn();
        int status = ris.getResponse().getStatus();
        String body = ris.getResponse().getContentAsString();
        log.info("[testInvalidFormatEnableUserRegistration]\tstauts ="+status+"\tbody ="+body);
    }

    @Test
    void testInvalidFormatParamUpdateRoleUser() throws Exception {
        String user = "user";
        String email = "t1@gmail.com";

        ObjectNode mainJson = objectMapper.createObjectNode();
        ObjectNode userJson = objectMapper.createObjectNode();
        userJson.put("email", email);
        userJson.put("role", 1);
        //mainJson.put("username", user);
        mainJson.put("user", userJson);
        log.info("testInvalidFormatParamUpdateRoleUser creazione oggetto annidato "+mainJson.toString());

      ObjectNode missUsername = objectMapper.createObjectNode();

        missUsername.put("user", userJson);

        mockMvc.perform(post("/updateRoleUser")
                        .contentType("application/json")
                        .content(missUsername.toString()))
                .andExpect(status().isBadRequest() )
                .andExpect(content().string("username field missing "));

    }

    @Test
    void testUpdateRoleUser() throws Exception {
        String user = "user";
        String username = "admin";
        String email = "admin@gmail.com";
        ObjectNode mainJson = objectMapper.createObjectNode();
        ObjectNode userJson = objectMapper.createObjectNode();
        userJson.put("username", username);
        userJson.put("email", email);
        userJson.put("role", 0);
        mainJson.put("username", username);
        mainJson.put("user", userJson);

        User tmp = new User("lol","lol@gmail.com","afs", RoleEnum.SUPERVISOR);
        when(userRepo.findUserByUsername("")).thenReturn(tmp);

        mockMvc.perform(post("/updateRoleUser")
                        .contentType("application/json")
                        .content(mainJson.toString()))
                .andExpect(status().isOk()  );

    }

}
