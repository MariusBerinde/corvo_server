package marius.server;


import marius.server.controller.AuthController;
import marius.server.data.ApprovedUsers;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;
import marius.server.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApprovedUsersRepo approvedUsersRepo;

    @MockitoBean
    private UserRepo userRepo;

    @MockitoBean
    private AuthService authService;

    /**
     * test dove verifico che il metodo risponde ai ping
     */
    @Test
    void testPing1() throws Exception {
        mockMvc.perform(get("/ping1")).andExpect(status().isOk()).andExpect(content().string("pong"));

    }

    @Test
    void testValidUserOfEnableUserRegistration() throws Exception {
        String username = "admin";
        String bodyRequest = """
                {
                  "email": "utente@example.com",
                  "username": "admin"
                }
                """;

        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(approvedUsersRepo.findByEmail("utente@example.com")).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(approvedUsersRepo, times(1)).save(any(ApprovedUsers.class));
    }

    @Test
    void testValidUserOfEnableUserRegistration2() throws Exception {
        String username = "admin";
        String bodyRequest = """
                {
                  "email": "utente@example.com",
                  "username": "admin"
                }
                """;

        //when(userRepo.existsByUsername(username)).thenReturn(true);
        //when(approvedUsersRepo.findByEmail("utente@example.com")).thenReturn(Optional.empty());
        when(authService.enableUserRegistration("utente@example.com", "admin", "127.0.0.1")).thenReturn("true");
        mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        //verify(approvedUsersRepo, times(1)).save(any(ApprovedUsers.class));
    }

    @Test
    @DisplayName("parity test for enableUserRegistration and enableUserRegistration2 with correct params")
    void parity_validUser_sameStatusAndBody() throws Exception {
        String bodyRequest = """
                {
                  "email": "utente@example.com",
                  "username": "admin"
                }
                """;


        when(userRepo.existsByUsername("admin")).thenReturn(true);
        when(approvedUsersRepo.findByEmail("utente@example.com")).thenReturn(Optional.empty());

        when(authService.enableUserRegistration("utente@example.com", "admin", "127.0.0.1")).thenReturn("true");
        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");
        assertEquals(oldResult.getResponse().getContentAsString(), newResult.getResponse().getContentAsString(), "Body diverso tra le due route");
    }

    @Test
    @DisplayName("parity test for enableUserRegistration and enableUserRegistration2 with missing email field")
    void parity_test_enableUserRegistration_missing_email_field() throws Exception {
        String bodyRequest = """
                {
                  "username": "admin"
                }
                """;

        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");
    }

    @Test
    @DisplayName("parity test for enableUserRegistration and enableUserRegistration2 with missing username field")
    void parity_test_enableUserRegistration_missing_username_field() throws Exception {
        String bodyRequest = """
                {
                  "email": "admin@gmail.com"
                }
                """;

        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");
    }


    @Test
    @DisplayName("parity test for enableUserRegistration and enableUserRegistration2 with  email not valid")
    void parity_test_enableUserRegistration_emailN() throws Exception {
        String bodyRequest = """
                {
                  "email": "admin@gmail.com",
                  "username": "admin"
                }
                """;

        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");
    }


    @Test
    void getApprovedUsers_validUser_returnsEmailList() throws Exception {
        when(userRepo.existsByUsername("admin")).thenReturn(true);
        when(approvedUsersRepo.findAll()).thenReturn(List.of(
                new ApprovedUsers("user1@example.com"),
                new ApprovedUsers("user2@example.com")
        ));

        mockMvc.perform(get("/getApprovedUsers")
                        .header("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("user1@example.com"))
                .andExpect(jsonPath("$[1]").value("user2@example.com"));
    }

    @Test
    @DisplayName("parity test getApprovedUser ok")
    void parity_test_getApprovedUser_ok() throws Exception {
        when(userRepo.existsByUsername("admin")).thenReturn(true);
        when(approvedUsersRepo.findAll()).thenReturn(List.of(
                new ApprovedUsers("user1@example.com"),
                new ApprovedUsers("user2@example.com")
        ));

        when(authService.getApprovedUsers("admin", "127.0.0.1")).thenReturn(List.of(
                "user1@example.com",
                "user2@example.com"
        ));

        MvcResult oldResult = mockMvc.perform(get("/getApprovedUsers")
                        .header("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("user1@example.com"))
                .andExpect(jsonPath("$[1]").value("user2@example.com")).andReturn();

        MvcResult newResult = mockMvc.perform(get("/getApprovedUsers")
                        .header("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("user1@example.com"))
                .andExpect(jsonPath("$[1]").value("user2@example.com")).andReturn();

        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");
    }

    @Test
    void testIsisEmailApprovedOK() throws Exception {
        String email = "test@gmail.com";
        when(userRepo.existsByUsername(email)).thenReturn(true);
        when(authService.isEmaiApproved(email)).thenReturn(true);

        String bodyRequest = """
                {
                  "email": "%s",
                }
                """.formatted(email);

        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/isEmailApproved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/isEmailApproved")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andReturn();

        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");

    }


    @Test
    void testDeleteEnabledUserOkpaired() throws Exception {
        String username = "admin";
        String email = "usertToDelete@gmail.com";

        String bodyRequest = """
                {
                "username":"%s",
                "email":"%s"
                }
                """.formatted(username,email);
        when(authService.deleteEnabledUser(username,email,"127.0.0.1")).thenReturn(1);
        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(approvedUsersRepo.deleteByEmail(email)).thenReturn(1);


        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/deleteEnabledUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();




        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/deleteEnabledUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();


        assertEquals(oldResult.getResponse().getStatus(), newResult.getResponse().getStatus(), "HTTP status diverso tra le due route");
    }

}

