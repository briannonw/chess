package service;


import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.*;


import static org.junit.jupiter.api.Assertions.*;




public class UserServiceTests {
    private MemoryDataAccess dataAccess;
    private UserService userService;
    private UserData existingUser;
    private UserData newUser;


    @BeforeEach
    void setUp() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);


        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@gmail.com");
    }


    @Test
    @DisplayName("Normal User Login")
    public void loginSuccess() throws DataAccessException {
        dataAccess.createUser(existingUser);


        LoginRequest request = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginResult = userService.login(request);


        assertNotNull(loginResult, "Login returned null user");
        assertEquals(existingUser.username(), loginResult.username(), "Response did not give the same username as user");
        assertNotNull(loginResult.authToken(), "Response did not return authentication String");
    }


    @Test
    @DisplayName("Login Bad Request")
    public void loginBadRequest() throws DataAccessException {
        dataAccess.createUser(existingUser);


        LoginRequest noUsername = new LoginRequest(null, existingUser.password());
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(noUsername),
                "Login with null username should throw an exception");


        LoginRequest noPassword = new LoginRequest(existingUser.username(), null);
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(noPassword),
                "Login with null password should throw an exception");
    }


    @Test
    @DisplayName("Login Unauthorized (Multiple Forms)")
    public void loginUnauthorized() throws DataAccessException {
        dataAccess.createUser(existingUser);


        LoginRequest wrongPassword = new LoginRequest(existingUser.username(), "wrongPassword");
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(wrongPassword),
                "Login with wrong password should throw an exception");


        LoginRequest wrongUsername = new LoginRequest("wrongUsername", "password");
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(wrongUsername),
                "Login with wrong username should throw an exception");
    }


    @Test
    @DisplayName("Normal User Registration")
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest(newUser.username(), newUser.password(), newUser.email());
        RegisterResult result = userService.register(request);


        assertNotNull(result, "Register returned null");
        assertEquals(newUser.username(), result.username(), "Response did not have the same username as was registered");
        assertNotNull(result.authToken(), "Response did not contain an authentication string");
    }


    @Test
    @DisplayName("Re-Register User")
    public void registerTwice() throws DataAccessException {
        dataAccess.createUser(existingUser);


        RegisterRequest request = new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.register(request),
                "Registering existing user should throw an exception");
    }


    @Test
    @DisplayName("Register Bad Request")
    public void registerBadRequest() {
        RegisterRequest request = new RegisterRequest(newUser.username(), null, newUser.email());
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.register(request),
                "Registration without a password should throw an exception");
    }


    @Test
    @DisplayName("Normal Logout")
    public void logoutSuccess() throws DataAccessException {
        dataAccess.createUser(existingUser);
        LoginRequest loginRequest = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginResult = userService.login(loginRequest);


        LogoutRequest logoutRequest = new LogoutRequest(loginResult.authToken());
        Assertions.assertDoesNotThrow(() -> userService.logout(logoutRequest), "Logout should not throw an exception");
    }


    @Test
    @DisplayName("Invalid Auth Logout")
    public void logoutTwice() throws DataAccessException{
        dataAccess.createUser(existingUser);
        LoginRequest loginRequest = new LoginRequest(existingUser.username(), existingUser.password());
        LoginResult loginResult = userService.login(loginRequest);


        LogoutRequest firstLogoutRequest = new LogoutRequest(loginResult.authToken());
        Assertions.assertDoesNotThrow(() -> userService.logout(firstLogoutRequest), "First logout should not throw an exception");


        LogoutRequest secondLogoutRequest = new LogoutRequest(loginResult.authToken());
        Assertions.assertThrows(DataAccessException.class, () -> userService.logout(secondLogoutRequest), "Second logout should throw an exception");
    }
}
