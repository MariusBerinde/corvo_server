package marius.server.service;

import marius.AppExceptions;
import marius.server.Tools;
import marius.server.data.ApprovedUsers;
import marius.server.data.RoleEnum;
import marius.server.data.User;
import marius.server.data.dto.UpdateRequestRoleDto;
import marius.server.data.dto.UpdateRequestRoleUserDto;
import marius.server.data.dto.UserRegistrationDto;
import marius.server.repo.ApprovedUsersRepo;
import marius.server.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private ApprovedUsersRepo approvedUsersRepo;
    private UserRepo userRepo;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        approvedUsersRepo = mock(ApprovedUsersRepo.class);
        authService = new AuthService(userRepo, approvedUsersRepo);
    }
    @Test
    void enableUserRegistration_ShouldReturnTrue_WhenUsernameExistsAndEmailNotPresent() {

        // Arrange
        String username = "mrossi";
        String email = "mrossi@test.it";
        String ip = "127.0.0.1";

        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(approvedUsersRepo.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        String result = authService.enableUserRegistration(email, username, ip);

        // Assert
        assertEquals("true", result);

        verify(userRepo).existsByUsername(username);
        verify(approvedUsersRepo).findByEmail(email);
        verify(approvedUsersRepo).save(any());
    }

    @Test
    void enableUserRegistration_ShouldThrowUserNotAuthException_WhenUsernameDoesNotExist() {

        // Arrange
        String username = "utenteInesistente";
        String email = "test@test.it";
        String ip = "127.0.0.1";

        when(userRepo.existsByUsername(username)).thenReturn(false);

        // Act & Assert
        assertThrows(
                AppExceptions.UserNotAuthException.class,
                () -> authService.enableUserRegistration(email, username, ip)
        );

        verify(userRepo).existsByUsername(username);

        verify(approvedUsersRepo, never()).findByEmail(anyString());
        verify(approvedUsersRepo, never()).save(any());
    }


    /**
     * Verify that enableUserRegistration returns "true"
     * when the requested email is already present in ApprovedUsers.
     * Expected behaviour:
     * - username is recognized
     * - email is found in ApprovedUsers
     * - no new ApprovedUsers entity is created
     * - save() must never be called
     * - method returns "true"
     */
    @Test
    void enableUserRegistration_ShouldReturnTrue_WhenEmailAlreadyExists() {

        // Arrange
        String username = "supervisor";
        String email = "existing@test.it";
        String ip = "127.0.0.1";

        ApprovedUsers approvedUser = new ApprovedUsers(email);

        when(userRepo.existsByUsername(username))
                .thenReturn(true);

        when(approvedUsersRepo.findByEmail(email))
                .thenReturn(Optional.of(approvedUser));

        // Act
        String result =
                authService.enableUserRegistration(email, username, ip);

        // Assert
        assertEquals("true", result);

        verify(userRepo).existsByUsername(username);
        verify(approvedUsersRepo).findByEmail(email);

        verify(approvedUsersRepo, never())
                .save(any(ApprovedUsers.class));
    }

    /**
     * Verify that getApprovedUsers returns all approved emails
     * when the requesting username is recognized.
     * Expected behaviour:
     * - username exists
     * - approved users are retrieved from repository
     * - returned list contains all approved emails
     */
    @Test
    void getApprovedUsers_ShouldReturnApprovedEmails_WhenUsernameExists() {

        String username = "supervisor";
        String ip = "127.0.0.1";
        ApprovedUsers user1 = new ApprovedUsers("user1@test.it");
        ApprovedUsers user2 = new ApprovedUsers("user2@test.it");
        List<ApprovedUsers> approvedUsers = List.of(user1, user2);
        when(userRepo.existsByUsername(username)) .thenReturn(true);
        when(approvedUsersRepo.findAll()) .thenReturn(approvedUsers);
        // Act
        List<String> result = authService.getApprovedUsers(username, ip);
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.contains("user1@test.it"));
        assertTrue(result.contains("user2@test.it"));

        verify(userRepo) .existsByUsername(username);

        verify(approvedUsersRepo) .findAll();
    }

    /**
     * Verify that getApprovedUsers throws UserNotAuthException
     * when the requesting username is not recognized.
     * Expected behaviour:
     * - username does not exist
     * - operation is denied
     * - approved users are never queried
     */
    @Test
    void getApprovedUsers_ShouldThrowUserNotAuthException_WhenUsernameDoesNotExist() {
        String username = "unknownUser";
        String ip = "127.0.0.1";
        when(userRepo.existsByUsername(username)) .thenReturn(false);

        // Act & Assert
        assertThrows( AppExceptions.UserNotAuthException.class, () -> authService.getApprovedUsers(username, ip) );
        verify(userRepo) .existsByUsername(username);
        verify(approvedUsersRepo, never()) .findAll();
    }

    /**
     * Verify that isEmaiApproved returns true
     * when the specified email exists in ApprovedUsers.
     * Expected behaviour:
     * - repository finds the email
     * - method returns true
     */
    @Test
    void isEmaiApproved_ShouldReturnTrue_WhenEmailExists() {
        // Arrange
        String email = "approved@test.it";
        ApprovedUsers approvedUser = new ApprovedUsers(email);
        when(approvedUsersRepo.findByEmail(email))
                .thenReturn(Optional.of(approvedUser));

        boolean result = authService.isEmaiApproved(email);

        assertTrue(result);
        verify(approvedUsersRepo) .findByEmail(email);
    }
    /**
     * Verify that isEmaiApproved returns false
     * when the specified email does not exist in ApprovedUsers.
     * Expected behaviour:
     * - repository does not find the email
     * - method returns false
     */
    @Test
    void isEmaiApproved_ShouldReturnFalse_WhenEmailDoesNotExist() {

        // Arrange
        String email = "notapproved@test.it";

        when(approvedUsersRepo.findByEmail(email)) .thenReturn(Optional.empty());

        // Act
        boolean result = authService.isEmaiApproved(email);

        // Assert
        assertFalse(result);

        verify(approvedUsersRepo) .findByEmail(email);
    }

    /**
     * test used for verify that deleteEnabledUser delete a user present in the approvedUser repo
     */
    @Test
    void deleteEnabledUserOk() {
        String username = "supervisor";
        String ip = "127.0.0.1";
        String email = "userToDelete@gmail.com";
        Integer nrOfDeletions = 1;
        when(userRepo.existsByUsername(username)).thenReturn(true);
        when(approvedUsersRepo.deleteByEmail(email)).thenReturn(nrOfDeletions);
        Integer ris = authService.deleteEnabledUser(username,  email,ip);
        assertEquals(nrOfDeletions, ris,"nr of deletions should be equal");

    }

    /**
     * test used to verify that deleteEnabledUser launch AppExceptions.UserNotAuthException
     * when username is not present in userRepo
     */
    @Test
    void deleteEnabledUserFail() {

        String username = "supervisor";
        String ip = "127.0.0.1";
        String email = "userToDelete@gmail.com";

        assertThrows( AppExceptions.UserNotAuthException.class, () -> authService.deleteEnabledUser(username,  email,ip) );
    }

    /**
     * Verify that createUser correctly creates a WORKER user
     * and persists it through the repository.
     */
    @Test
    void createUserOk() {

        String username = "test";
        String email = "test@gmail.com";
        String password = "Password1!";
        String ip = "127.0.0.1";

        UserRegistrationDto dto =
                new UserRegistrationDto(username, email, password, 1);

        User savedUser = new User(
                username,
                email,
                Tools.hashPassword(password),
                RoleEnum.WORKER
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        when(userRepo.save(any(User.class)))
                .thenReturn(savedUser);

        User result = authService.createUser(dto, ip);

        verify(userRepo).save(userCaptor.capture());

        User userPassedToRepo = userCaptor.getValue();

        assertAll(
                () -> assertNotNull(result, "saved user should not be null"),
                () -> assertEquals(email, result.getEmail()),
                () -> assertEquals(username, result.getUsername()),
                () -> assertEquals(RoleEnum.WORKER, result.getRole()),

                () -> assertEquals(username, userPassedToRepo.getUsername()),
                () -> assertEquals(email, userPassedToRepo.getEmail()),
                () -> assertEquals(RoleEnum.WORKER, userPassedToRepo.getRole()),

                () -> assertNotEquals(
                        password,
                        userPassedToRepo.getPassword(),
                        "password should be hashed before save"
                )
        );
    }


    /**
     * Verify that a supervisor can change the role of an existing user.
     */
    @Test
    void changeUserRoleOk() {

        String username = "supervisor";
        String email = "test@gmail.com";
        String ip = "127.0.0.1";

        UpdateRequestRoleDto request =
                new UpdateRequestRoleDto(
                        username,
                        new UpdateRequestRoleUserDto(email, 1)
                );

        User supervisor = new User();
        supervisor.setUsername(username);
        supervisor.setRole(RoleEnum.SUPERVISOR);

        User targetUser = new User();
        targetUser.setEmail(email);
        targetUser.setRole(RoleEnum.SUPERVISOR);

        when(userRepo.findUserByUsername(username))
                .thenReturn(Optional.of(supervisor));

        when(userRepo.findUserByEmail(email))
                .thenReturn(Optional.of(targetUser));

        String result = authService.changeUserRole(request, ip);

        assertEquals("true", result);

        assertEquals(
                RoleEnum.WORKER,
                targetUser.getRole(),
                "role should be changed to WORKER"
        );

        verify(userRepo).save(targetUser);
    }

    @Test
    void changeUserRoleFailWhenUserDoesNotExist() {
        String username = "supervisor";
        String email = "test@gmail.com";
        String ip = "127.0.0.1";

        UpdateRequestRoleDto request =
                new UpdateRequestRoleDto(
                        username,
                        new UpdateRequestRoleUserDto(email, 0)
                );

        User supervisor = new User();
        supervisor.setUsername(username);
        supervisor.setRole(RoleEnum.WORKER);

        User targetUser = new User();
        targetUser.setEmail(email);
        targetUser.setRole(RoleEnum.WORKER);

        when(userRepo.findUserByUsername(username)) .thenReturn(Optional.of(supervisor));

        when(userRepo.findUserByEmail(email)) .thenReturn(Optional.of(targetUser));

        assertThrows( AppExceptions.UserNotAuthException.class,()->authService.changeUserRole(request, ip) );

    }


    /**
     * check if the route deleteUser returns true when the paramethers are corrects
     */
    @Test
    void deleteUserOk() {
        String sup = "supervisior";
        String email="userToDelete@mail.com";
        String remoteAddr="127.0.0.1";

        User supervisor = new User();
        supervisor.setUsername(sup);
        supervisor.setRole(RoleEnum.SUPERVISOR);
        User targetUser = new User();
        targetUser.setEmail(email);
        targetUser.setRole(RoleEnum.WORKER);


        when(userRepo.findUserByUsername(sup)) .thenReturn(Optional.of(supervisor));
       // when(userRepo.findUserByEmail(email)) .thenReturn(Optional.of(targetUser));
        String ris = authService.deleteUser(sup,email,remoteAddr);
        assertEquals("true", ris);
    }

    @Test
    void deleteUserFailWhenTargetUserDoesNotExist() {
        String sup = "supervisior";
        String email="userToDelete@mail.com";
        String remoteAddr="127.0.0.1";

        assertThrows(AppExceptions.UserNotFoundException.class,()->authService.deleteUser(sup,email,remoteAddr));
    }

    @Test
    void deleteUserFailWhenRoleTargetWrong(){

        String sup = "supervisior";
        String email="userToDelete@mail.com";
        String remoteAddr="127.0.0.1";

        User supervisor = new User();
        supervisor.setUsername(sup);
        supervisor.setRole(RoleEnum.WORKER);
        User targetUser = new User();
        targetUser.setEmail(email);
        targetUser.setRole(RoleEnum.WORKER);

        when(userRepo.findUserByUsername(sup)) .thenReturn(Optional.of(supervisor));

        assertThrows(AppExceptions.UserNotAuthException.class,()->authService.deleteUser(sup,email,remoteAddr));
    }



    @Test
    void loginUserOk() {
        String email = "test@mail.fr", password = "testPasswrd55", ip = "127.0.0.1";
        User mockUser = new User("test",email,Tools.hashPassword(password),RoleEnum.WORKER);
        when(userRepo.findUserByEmail(email)).thenReturn(Optional.of(mockUser));
        User risLogin = authService.loginUser(email,password,ip );
        assertEquals(email,risLogin.getEmail());
        assertEquals(RoleEnum.WORKER,risLogin.getRole());
    }

    /**
     * Verify that a user can update his password when the old password is correct.
     */
    @Test
    void updateUserPasswordOk() {
        String email = "test@gmail.com";
        String oldPwd = "Password1!";
        String newPwd = "Password2!";
        String ip = "127.0.0.1";
        User user = new User();
        user.setEmail(email);
        user.setPassword(Tools.hashPassword(oldPwd));
        when(userRepo.findUserByEmail(email)) .thenReturn(Optional.of(user));

        String result = authService.updateUserPassword(email, oldPwd, newPwd, ip);
        assertEquals("true", result);
        assertEquals(newPwd, user.getPassword());
    }

    /**
     * Verify that an exception is thrown when the user does not exist.
     */
    @Test
    void updateUserPasswordUserNotFound() {

        String email = "test@gmail.com";

        when(userRepo.findUserByEmail(email)) .thenReturn(Optional.empty());

        assertThrows(
                AppExceptions.UserNotFoundException.class,
                () -> authService.updateUserPassword(
                        email,
                        "Password1!",
                        "Password2!",
                        "127.0.0.1"
                )
        );
    }
    /**
     * Verify that the same password cannot be reused.
     */
    @Test
    void updateUserPasswordSamePassword() {

        String email = "test@gmail.com";
        String password = "Password1!";

        User user = new User();
        user.setEmail(email);
        user.setPassword(Tools.hashPassword(password));

        when(userRepo.findUserByEmail(email))
                .thenReturn(Optional.of(user));

        assertThrows(
                AppExceptions.UserSamePasswordExcemptio.class,
                () -> authService.updateUserPassword(
                        email,
                        password,
                        password,
                        "127.0.0.1"
                )
        );
    }

    /**
     * Verify that an exception is thrown when the old password is wrong.
     */
    @Test
    void updateUserPasswordWrongOldPassword() {

        String email = "test@gmail.com";

        User user = new User();
        user.setEmail(email);
        user.setPassword(Tools.hashPassword("CorrectPassword1!"));

        when(userRepo.findUserByEmail(email))
                .thenReturn(Optional.of(user));

        assertThrows(
                AppExceptions.UserNotAuthException.class,
                () -> authService.updateUserPassword(
                        email,
                        "WrongPassword1!",
                        "NewPassword1!",
                        "127.0.0.1"
                )
        );
    }

    /**
     * Verify that a supervisor can retrieve all users.
     */
    @Test
    void getAllUsersOk() {

        String username = "supervisor";
        String ip = "127.0.0.1";

        User supervisor = new User();
        supervisor.setUsername(username);
        supervisor.setRole(RoleEnum.SUPERVISOR);

        User user1 = new User();
        user1.setUsername("worker1");
        user1.setEmail("worker1@gmail.com");
        user1.setPassword("secret");
        user1.setRole(RoleEnum.WORKER);

        User user2 = new User();
        user2.setUsername("worker2");
        user2.setEmail("worker2@gmail.com");
        user2.setPassword("secret");
        user2.setRole(RoleEnum.WORKER);

        when(userRepo.findUserByUsername(username))
                .thenReturn(Optional.of(supervisor));

        when(userRepo.findAll())
                .thenReturn(List.of(user1, user2));

        List<User> result =
                authService.getAllUsers(username, ip);

        assertEquals(2, result.size());

        assertEquals("worker1", result.get(0).getUsername());
        assertEquals("worker1@gmail.com", result.get(0).getEmail());

        assertNull(result.get(0).getPassword());
        assertNull(result.get(1).getPassword());
    }

    /**
     * Verify that an exception is thrown when the requester does not exist.
     */
    @Test
    void getAllUsersUserNotFound() {

        when(userRepo.findUserByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                AppExceptions.UserNotFoundException.class,
                () -> authService.getAllUsers(
                        "unknown",
                        "127.0.0.1"
                )
        );
    }

    /**
     * Verify that a worker cannot retrieve all users.
     */
    @Test
    void getAllUsersWrongRole() {

        User worker = new User();
        worker.setUsername("worker");
        worker.setRole(RoleEnum.WORKER);

        when(userRepo.findUserByUsername("worker"))
                .thenReturn(Optional.of(worker));

        assertThrows(
                AppExceptions.UserNotAuthException.class,
                () -> authService.getAllUsers(
                        "worker",
                        "127.0.0.1"
                )
        );
    }
}