package client;

import chess.ChessGame;
import dataaccess.DataAccessException;

import org.junit.jupiter.api.*;
import server.Server;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNotNull(registerResult.authToken());
        assertFalse(registerResult.authToken().isEmpty());
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
        assertNotNull(loginResult.authToken());
        assertFalse(loginResult.authToken().isEmpty());
    }

    @Test
    public void loginFail() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");

        assertThrows(DataAccessException.class, () -> facade.login("WrongUsername", "existingUserPassword"));
        assertThrows(DataAccessException.class, () -> facade.login("ExistingUser", "WrongPassword"));
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
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");

        assertThrows(DataAccessException.class, () -> facade.logout("authToken"));

        facade.logout(loginResult.authToken());
        assertThrows(DataAccessException.class, () -> facade.logout(loginResult.authToken()));
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");
        CreateGameResult createGameResult = facade.createGame(loginResult.authToken(), "Game1");

        assertNotNull(createGameResult);
        assertTrue(createGameResult.gameID() > 0);
    }

    @Test
    public void createGameFail() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");

        assertThrows(DataAccessException.class, () -> facade.createGame("authToken", "Game1"));

        facade.logout(loginResult.authToken());
        assertThrows(DataAccessException.class, () -> facade.createGame(loginResult.authToken(), "Game1"));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");

        facade.createGame(loginResult.authToken(), "Game1");
        facade.createGame(loginResult.authToken(), "Game2");
        ListGamesResult listGamesResult = facade.listGames(loginResult.authToken());

        assertNotNull(listGamesResult);
        assertEquals(2, listGamesResult.games().size());
    }

    @Test
    public void listGamesFail() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");

        facade.createGame(loginResult.authToken(), "Game1");
        facade.createGame(loginResult.authToken(), "Game2");

        assertThrows(DataAccessException.class, () -> facade.listGames("authToken"));
    }

    @Test
    public void joinGameSuccess() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");
        CreateGameResult createGameResult = facade.createGame(loginResult.authToken(), "Game1");

        facade.joinGame(loginResult.authToken(), ChessGame.TeamColor.WHITE.name(), createGameResult.gameID());
        ListGamesResult listGamesResult = facade.listGames(loginResult.authToken());

        assertNotNull(listGamesResult);
        assertEquals(1, listGamesResult.games().size());
        assertEquals(loginResult.username(), listGamesResult.games().getFirst().whiteUsername());
        assertNull(listGamesResult.games().getFirst().blackUsername());
    }

    @Test
    public void joinGameFail() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");
        facade.createGame(loginResult.authToken(), "Game1");

        assertThrows(DataAccessException.class, () -> facade.joinGame(loginResult.authToken(), ChessGame.TeamColor.WHITE.name(), -1));
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        facade.register("ExistingUser", "existingUserPassword", "eu@gmail.com");
        LoginResult loginResult = facade.login("ExistingUser", "existingUserPassword");
        facade.createGame(loginResult.authToken(), "Game1");

        facade.clear();

        assertThrows(DataAccessException.class, () -> facade.login("ExistingUser", "existingUserPassword"));
        assertThrows(DataAccessException.class, () -> facade.listGames(loginResult.authToken()));

        facade.register("NewUser", "newUserPassword", "nu@gmail.com");
        LoginResult newLoginResult = facade.login("NewUser", "newUserPassword");
        ListGamesResult listGamesResult = facade.listGames(newLoginResult.authToken());

        assertNotNull(listGamesResult);
        assertEquals(0, listGamesResult.games().size());
    }
}
