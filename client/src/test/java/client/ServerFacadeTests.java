package client;

import dataaccess.DataAccessException;

import org.junit.jupiter.api.*;
import server.Server;
import service.*;
import static org.junit.jupiter.api.Assertions.*;


//         existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
//         newUser = new UserData("NewUser", "newUserPassword", "nu@gmail.com");

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDataBase() throws DataAccessException {
        facade.clear();
    }

    @Test
    public void registerSuccess() throws DataAccessException {
        RegisterResult registerResult = facade.register("NewUser", "newUserPassword", "nu@gmail.com");

        assertNotNull(registerResult);
        assertEquals("NewUser", registerResult.username());
        assertFalse(registerResult.authToken().isEmpty());
        assertNotNull(registerResult.authToken());
    }

    @Test
    public void registerFail() throws DataAccessException {
        facade.register("NewUser", "newUserPassword", "nu@gmail.com");

        assertThrows(DataAccessException.class, () -> facade.register("NewUser", "newUserPassword", "nu@gmail.com"));
    }

    @Test
    public void loginSuccess() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");

        assertNotNull(loginResult);
        assertEquals("ExistingUser", loginResult.username());
        assertFalse(loginResult.authToken().isEmpty());
        assertNotNull(loginResult.authToken());
    }

    @Test
    public void loginFail() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");

        assertThrows(DataAccessException.class, () -> facade.login("WrongUsername", "existingUserPassword"));
    }

    @Test
    public void logoutSuccess() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");

        assertDoesNotThrow(() -> facade.logout(loginResult.authToken()));
    }

    @Test
    public void logoutFail() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        facade.login("ExistingUser", "existingUserPassword");

        assertThrows(DataAccessException.class, () -> facade.logout("authToken"));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        //
    }

    @Test
    public void listGamesFail() throws DataAccessException {
        //
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        //
    }

    @Test
    public void createGameFail() throws DataAccessException {
        //
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        //
    }

    @Test
    public void joinGameFail() throws DataAccessException {
        //
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        //
    }

    @Test
    public void clearFail() throws DataAccessException {
        //
    }

}
