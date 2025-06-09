package marius.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import marius.server.controller.AuthController;
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
        mockMvc.perform(post("/enableUserRegistration")
                        .contentType("application/json")
                        .content(json.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        //  Verifica che `save` sia stato chiamato
        verify(approvedUsersRepo, times(1)).save(any(ApprovedUsers.class));
    }

    @Test
    void testInvalidFormatEnableUserRegistration() throws Exception {
        String email = "utentemplecom";
        String username = "admin";
        ObjectNode json = objectMapper.createObjectNode();
        json.put("user", username);
        json.put("email", email);
        MvcResult ris=mockMvc.perform(post("/enableUserRegistration")
                        .contentType("application/json")
                        .content(json.toString()))
                .andExpect(status().isBadRequest() )
                .andExpect(content().string("email format error")).andReturn();
        int status = ris.getResponse().getStatus();
        String body = ris.getResponse().getContentAsString();
        log.info("[testInvalidFormatEnableUserRegistration]\tstauts ="+status+"\tbody ="+body);
    }

    @Test
    void testGetApprovedUsers() throws Exception {
        ObjectNode json = objectMapper.createObjectNode();
    }
}
