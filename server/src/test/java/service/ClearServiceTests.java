package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTests {

    private UserService userService;
    private ClearService clearService;
    private GameService gameService;
    private UserData existingUser;

    @BeforeEach
    void setUp() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        clearService = new ClearService(dataAccess);
        gameService = new GameService(dataAccess);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
        dataAccess.createUser(existingUser);
    }

    @Test
    @DisplayName("Clear Test")
    public void clearData() throws DataAccessException {
        UserData newUser = new UserData("ClearMe", "cleared", "clear@gmail.com");
        RegisterRequest registerRequest = new RegisterRequest(newUser.username(), newUser.password(), newUser.email());
        RegisterResult registerResult = userService.register(registerRequest);
        String newUserAuth = registerResult.authToken();
        assertNotNull(registerResult.authToken(), "Auth token is not set yet");

        CreateGameResult game = gameService.createGame(new CreateGameRequest(newUserAuth, "Clear game"));
        JoinGameRequest joinRequest = new JoinGameRequest(newUserAuth, ChessGame.TeamColor.WHITE.name(), game.gameID());
        gameService.joinGame(joinRequest);

        assertDoesNotThrow(() -> clearService.clear(), "Exception should not be thrown by clearing service");

        LoginRequest oldUserLogin = new LoginRequest(existingUser.username(), existingUser.password());
        assertThrows(DataAccessException.class, () -> userService.login(oldUserLogin), "Old user should not be able to log in after clear");

        LoginRequest newUserLogin = new LoginRequest(newUser.username(), newUser.password());
        assertThrows(DataAccessException.class, () -> userService.login(newUserLogin), "New user should not be able to log in after clear");

        assertThrows(DataAccessException.class, () -> gameService.listGames(new ListGamesRequest(newUserAuth)), "Old auth token should throw an exception");

        registerResult = userService.register(registerRequest);
        String newAuth = registerResult.authToken();
        assertNotNull(newAuth, "Re-registration should set new authToken");

        ListGamesResult listResult = gameService.listGames(new ListGamesRequest(newAuth));
        assertNotNull(listResult.games(), "List should not be null");
        assertEquals(0,listResult.games().size(), "List should be empty");

        LoginResult loginResult = userService.login(newUserLogin);
        assertNotNull(loginResult, "Login should succeed after re-registration");
    }

    @Test
    @DisplayName("Multiple Clears")
    public void clearMultipleTimes() {
        assertDoesNotThrow(() -> {
            clearService.clear();
            clearService.clear();
            clearService.clear();
        }, "Calling clear multiple times should not throw an exception");
    }
}
