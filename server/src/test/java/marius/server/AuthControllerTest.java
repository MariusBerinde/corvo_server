package marius.server;

import marius.server.controller.AuthController;
import marius.server.data.ApprovedUsers;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.data.dto.UpdateRequestRoleDto;
import marius.server.data.dto.UpdateRequestRoleUserDto;
import marius.server.data.dto.UserRegistrationDto;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;
import marius.server.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
        String bodyRequest = """
                {
                  "email": "utente@example.com",
                  "username": "admin"
                }
                """;

        when(authService.enableUserRegistration("utente@example.com", "admin", "127.0.0.1")).thenReturn("true");
        mockMvc.perform(MockMvcRequestBuilders.post("/enableUserRegistration2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

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
                  "email": "m,,,",
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

    /*
    *   test used for check if addUser and addUser respond in the same way if have the sase body
     */
    @Test
    void pairAddOk() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "john@example.com",
                "password": "SecurePass1!",
                "role": 1
              }
            }
            """;

        User fakeUser = new User("john_doe", "john@example.com", "HASHED_PWD", RoleEnum.WORKER);

            when(userRepo.save(any(User.class))).thenReturn(fakeUser);
            when(authService.createUser(any(UserRegistrationDto.class), anyString()))
                    .thenReturn(fakeUser);


            MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/addUser")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyRequest)).andExpect(status().isOk())
                    .andReturn();

        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();
        assertEquals(
            oldResult.getResponse().getStatus(),
            newResult.getResponse().getStatus(),
            "HTTP status diverso tra le due route"
        );


    }

    @ParameterizedTest
    @ValueSource(strings = { "password1!",     "PASSWORD1!",     "Password!",      "Password1",      "Ab1!"            })
    void addUserTestPasswordFormat(String password)throws Exception {

        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "john@example.com",
                "password": "%s",
                "role": 1
              }
            }
            """.formatted(password);


        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isBadRequest()).andReturn();
        log.info("http status ok in test of wrong format , body message ={}", newResult.getResponse().getContentAsString());
    }

    @Test
    void addUserTestMissingUsername()throws Exception {

        String bodyRequest = """
            {
              "user": {
                "email": "john@example.com",
                "password": "%s",
                "role": 1
              }
            }
            """;


        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRequest)).andExpect(status().isBadRequest()).andReturn();
        log.info("http status ok in test of wrong format , body message ={}", newResult.getResponse().getContentAsString());
    }
    @Test
    void addUser2_missingName() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "email": "john@example.com",
                "password": "SecurePass1!",
                "role": 1
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("missingName body={}", result.getResponse().getContentAsString());
    }

    @Test
    void addUser2_missingEmail() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "password": "SecurePass1!",
                "role": 1
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("missingEmail body={}", result.getResponse().getContentAsString());
    }

    @Test
    void addUser2_missingPassword() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "john@example.com",
                "role": 1
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("missingPassword body={}", result.getResponse().getContentAsString());
    }

    @Test
    void addUser2_missingRole() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "john@example.com",
                "password": "SecurePass1!"
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("missingRole body={}", result.getResponse().getContentAsString());
    }

    @Test
    void addUser2_missingUserObject() throws Exception {
        String bodyRequest = """
            {}
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("missingUserObject body={}", result.getResponse().getContentAsString());
    }

    @Test
    void addUser2_invalidEmailFormat() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "not-an-email",
                "password": "SecurePass1!",
                "role": 1
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("invalidEmail body={}", result.getResponse().getContentAsString());
    }


    @Test
    void addUser2_roleOutOfRange() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "john@example.com",
                "password": "SecurePass1!",
                "role": 5
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("roleOutOfRange body={}", result.getResponse().getContentAsString());
    }

    @Test
    void addUser2_roleNegative() throws Exception {
        String bodyRequest = """
            {
              "user": {
                "name": "john_doe",
                "email": "john@example.com",
                "password": "SecurePass1!",
                "role": -1
              }
            }
            """;
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/addUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        log.info("roleNegative body={}", result.getResponse().getContentAsString());
    }


    @Test
    void updateRoleUserOk() throws Exception {
        String bodyRequest = """
                        {
                        "username": "supervisor_user",
                        "user": {
                        "email": "target@example.com",
                        "role": 0
                        }
                        }
        """;

        User fakeSupervisor = new User("supervisor_user", "john@example.com", "HASHED_PWD", RoleEnum.SUPERVISOR);
        User fakeUser = new User("target", "target@example.com", "HASHED_PWD", RoleEnum.WORKER);
        UpdateRequestRoleDto fakeR = new UpdateRequestRoleDto(
                "supervisor_user",
                new UpdateRequestRoleUserDto(
                        "target@example.com",
                        0
                )
        );
        when(userRepo.findUserByUsername("supervisor_user")).thenReturn(Optional.of(fakeSupervisor)) ;
        when(userRepo.findUserByEmail("target@example.com")).thenReturn(Optional.of(fakeUser));
        when(authService.changeUserRole(fakeR,"127.0.0.1")).thenReturn("true");


        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/updateRoleUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();


        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/updateRoleUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();
        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus(),
                "HTTP status diverso tra le due route"
        );


    }


    @Test
    void updateRoleUser_missingUsername_shouldReturnBadRequest() throws Exception {

        String bodyRequest = """
            {
              "user": {
                "email": "target@example.com",
                "role": 0
              }
            }
            """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus(),
                "HTTP status diverso tra le due route"
        );
    }
    @Test
    void updateRoleUser_missingUserObject_shouldReturnBadRequest() throws Exception {

        String bodyRequest = """
            {
              "username": "supervisor_user"
            }
            """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }

    @Test
    void updateRoleUser_missingEmail_shouldReturnBadRequest() throws Exception {

        String bodyRequest = """
            {
              "username": "supervisor_user",
              "user": {
                "role": 0
              }
            }
            """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }

    @Test
    void updateRoleUser_missingRole_shouldReturnBadRequest() throws Exception {

        String bodyRequest = """
            {
              "username": "supervisor_user",
              "user": {
                "email": "target@example.com"
              }
            }
            """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updateRoleUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }

    @Test
    void testDeleteUserOk() throws Exception {
        String bodyRequest = """
       
                              {
                              "username": "supervisor_user",
                              "email": "user.to.delete@example.com"
                              }
        """;

        User fakeSupervisor = new User("supervisor_user", "john@example.com", "HASHED_PWD", RoleEnum.SUPERVISOR);
        User userToDelete = new User("userToDelete", "user.to.delete@example.com", "HASHED_PWD", RoleEnum.SUPERVISOR);
        when(userRepo.findUserByUsername("supervisor_user")).thenReturn(Optional.of(fakeSupervisor)) ;
        when(userRepo.findUserByEmail("user.to.delete@example.com")).thenReturn(Optional.of(userToDelete));

        MvcResult oldResult = mockMvc.perform(MockMvcRequestBuilders.post("/deleteUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();


        MvcResult newResult = mockMvc.perform(MockMvcRequestBuilders.post("/deleteUser2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyRequest)).andExpect(status().isOk())
                .andReturn();
        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus(),
                "HTTP status diverso tra le due route"
        );
    }
    @Test
    void testDeleteUser_missingUsername_shouldReturnBadRequest() throws Exception {

        String bodyRequest = """
            {
              "email": "user.to.delete@example.com"
            }
            """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/deleteUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/deleteUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus(),
                "HTTP status diverso tra le due route"
        );
    }

    @Test
    void testDeleteUser_missingEmail_shouldReturnBadRequest() throws Exception {

        String bodyRequest = """
            {
              "username": "supervisor_user"
            }
            """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/deleteUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/deleteUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus(),
                "HTTP status diverso tra le due route"
        );
    }

    @Test
    void testLoginOk() throws Exception {
       String bodyRequest = """
      {
        "email": "user@example.com",
        "password": "userPassword123!"
      }
       """ ;
        User fakeUser = new User("user", "user@example.com", Tools.hashPassword("userPassword123!"), RoleEnum.WORKER);
        when(userRepo.findUserByEmail("user@example.com")).thenReturn(Optional.of(fakeUser));
        when(authService.loginUser("user@example.com","userPassword123","127.0.0.1")).thenReturn(fakeUser);
        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isOk())
                .andReturn();


        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }

    @Test
    void testLoginMissingEmail()throws Exception{
        String bodyRequest = """
      {
        "password": "userPassword123!"
      }
       """ ;
        User fakeUser = new User("user", "user@example.com", Tools.hashPassword("userPassword123!"), RoleEnum.WORKER);
        when(userRepo.findUserByEmail("user@example.com")).thenReturn(Optional.of(fakeUser));
        when(authService.loginUser("user@example.com","userPassword123","127.0.0.1")).thenReturn(fakeUser);
        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();


        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }
    void testLoginMissingPassword()throws Exception{
        String bodyRequest = """
      {
        "email": "test@libero.com"
      }
       """ ;
        User fakeUser = new User("user", "user@example.com", Tools.hashPassword("userPassword123!"), RoleEnum.WORKER);
        when(userRepo.findUserByEmail("user@example.com")).thenReturn(Optional.of(fakeUser));
        when(authService.loginUser("user@example.com","userPassword123","127.0.0.1")).thenReturn(fakeUser);
        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();


        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals( oldResult.getResponse().getStatus(), newResult.getResponse().getStatus());

    }

   @ParameterizedTest
   @ValueSource(strings = { "",     " ", "mail",  "melior@.com", "melior@ferd" })
    void loginEmailWrongFormat(String email)throws Exception{
       String bodyRequest = """
           {
           "email": "%s",
           "password":"testPassword"
           }
       """.formatted(email);
        User fakeUser = new User("user", "user@example.com", Tools.hashPassword("userPassword123!"), RoleEnum.WORKER);
        when(userRepo.findUserByEmail("user@example.com")).thenReturn(Optional.of(fakeUser));
        when(authService.loginUser("user@example.com","userPassword123","127.0.0.1")).thenReturn(fakeUser);
        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();


        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/authUser2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals( oldResult.getResponse().getStatus(), newResult.getResponse().getStatus());
   }

    @Test
    void  updatePasswordTestParamOk() throws Exception{
        String bodyRequest = """
        {
            "email": "test@libero.com",
            "oldPassword": "userPassword123!",
            "newPassword": "userPassword124!",
            }
        """;
       when(authService.updateUserPassword("test@libero.com","userPassword123!","userPassword124!","127.0.0.1")).thenReturn("true");
       User fakeOld = new User("test", "test@libero.com", Tools.hashPassword("userPassword123!"), RoleEnum.WORKER);
       when(userRepo.findUserByEmail("test@libero.com") ).thenReturn(Optional.of(fakeOld));

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isOk())
                .andReturn();


        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }
    @Test
    void  updatePasswordTestMissingEmail() throws Exception{
        String bodyRequest = """
        {
            "oldPassword": "userPassword123!",
            "newPassword": "userPassword124!",
            }
        """;
        when(authService.updateUserPassword("test@libero.com","userPassword123!","userPassword124!","127.0.0.1")).thenReturn("true");
        User fakeOld = new User("test", "test@libero.com", Tools.hashPassword("userPassword123!"), RoleEnum.WORKER);
        when(userRepo.findUserByEmail("test@libero.com") ).thenReturn(Optional.of(fakeOld));

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();


        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }

    @Test
    void updatePasswordTestMissingOldPassword() throws Exception {

        String bodyRequest = """
    {
        "email": "test@libero.com",
        "newPassword": "userPassword124!"
    }
    """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }

    @Test
    void updatePasswordTestMissingNewPassword() throws Exception {

        String bodyRequest = """
    {
        "email": "test@libero.com",
        "oldPassword": "userPassword123!"
    }
    """;

        MvcResult oldResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        MvcResult newResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/updatePassword2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyRequest))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertEquals(
                oldResult.getResponse().getStatus(),
                newResult.getResponse().getStatus()
        );
    }
}


