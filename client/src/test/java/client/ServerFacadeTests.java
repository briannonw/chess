package client;

import dataaccess.DataAccessException;
import model.UserData;

import org.junit.jupiter.api.*;
import server.Server;
import service.RegisterResult;
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
        assertNotNull(registerResult.authToken());
        assertFalse(registerResult.authToken().isEmpty());
        assertEquals("NewUser", registerResult.username());
    }

    @Test
    public void registerFail() throws DataAccessException {
        facade.register("NewUser", "newUserPassword", "nu@gmail.com");

        assertThrows(DataAccessException.class, () -> {
            facade.register("NewUser", "newUserPassword", "nu@gmail.com");
        });
    }

}
