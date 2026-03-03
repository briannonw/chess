package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private UserService userService;
    private GameService gameService;
    private UserData existingUser;
    private UserData newUser;
    private String existingAuth;

    @BeforeEach
    void setUp() throws DataAccessException {
        MemoryDataAccess dataAccess = new MemoryDataAccess();

        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@gmail.com");

        RegisterRequest registerRequest = new RegisterRequest(existingUser.username(), existingUser.password(), existingUser.email());
        RegisterResult registerResult = userService.register(registerRequest);
        existingAuth = registerResult.authToken();
    }

    @Test
    @DisplayName("Valid Creation")
    public void createGameSuccess() throws DataAccessException {
        CreateGameRequest request = new CreateGameRequest(existingAuth, "Test Game");
        CreateGameResult result = gameService.createGame(request);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.gameID() > 0, "Game ID should be positive");
    }

    @Test
    @DisplayName("Create with Bad Authentication")
    public void createGameUnauthorized() {
        CreateGameRequest request = new CreateGameRequest("badAuthentication", "Test Game");

        assertThrows(DataAccessException.class, () -> gameService.createGame(request), "Bad authentication should throw exception");
    }

    @Test
    @DisplayName("Create Bad Request")
    public void createGameBadRequest() {
        CreateGameRequest request = new CreateGameRequest(existingAuth, null);

        assertThrows(DataAccessException.class, () -> gameService.createGame(request), "Game name should not be null");
    }

    @Test
    @DisplayName("Join Created Game")
    public void joinGameSuccess() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame(new CreateGameRequest(existingAuth, "Test Game"));

        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, ChessGame.TeamColor.WHITE.name(), createResult.gameID());

        assertDoesNotThrow(() -> gameService.joinGame(joinRequest));

        ListGamesResult listResult = gameService.listGames(new ListGamesRequest(existingAuth));

        assertNotNull(listResult.games());
        assertEquals(1, listResult.games().size());
        assertEquals(existingUser.username(), listResult.games().getFirst().whiteUsername());
        assertNull(listResult.games().getFirst().blackUsername());
    }

    @Test
    @DisplayName("Join Bad Authentication")
    public void joinGameUnauthorized() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame(new CreateGameRequest(existingAuth, "Test Game"));

        JoinGameRequest joinRequest = new JoinGameRequest("badAuthentication", ChessGame.TeamColor.WHITE.name(), createResult.gameID());

        assertThrows(DataAccessException.class, () -> gameService.joinGame(joinRequest));
    }

    @Test
    @DisplayName("Join Bad Team Color")
    public void joinGameBadColor() throws DataAccessException {
        CreateGameResult createResult = gameService.createGame(new CreateGameRequest(existingAuth, "Test Game"));

        for (String color : new String[]{null, "", "GREEN"}) {
            JoinGameRequest request = new JoinGameRequest(existingAuth, color, createResult.gameID());

            assertThrows(DataAccessException.class, () -> gameService.joinGame(request));
        }
    }

    @Test
    @DisplayName("Join Steal Team Color")
    public void joinGameStealColor() throws DataAccessException {

        CreateGameResult createResult = gameService.createGame(new CreateGameRequest(existingAuth, "Test Game"));

        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, ChessGame.TeamColor.BLACK.name(), createResult.gameID());
        gameService.joinGame(joinRequest);

        RegisterResult registerResult = userService.register(new RegisterRequest(newUser.username(), newUser.password(), newUser.email()));

        JoinGameRequest secondJoinRequest = new JoinGameRequest(registerResult.authToken(), ChessGame.TeamColor.BLACK.name(), createResult.gameID());

        assertThrows(DataAccessException.class, () -> gameService.joinGame(secondJoinRequest));
    }

    @Test
    @DisplayName("Join Bad Game ID")
    public void joinGameBadGameId() {
        JoinGameRequest joinRequest = new JoinGameRequest(existingAuth, ChessGame.TeamColor.WHITE.name(), -1);

        assertThrows(DataAccessException.class, () -> gameService.joinGame(joinRequest));
    }

    @Test
    @DisplayName("List No Games")
    public void listGamesEmpty() throws DataAccessException {
        ListGamesResult result = gameService.listGames(new ListGamesRequest(existingAuth));

        assertNotNull(result.games());
        assertEquals(0, result.games().size());
    }

    @Test
    @DisplayName("List Multiple Games")
    public void listGamesSuccess() throws DataAccessException {

        RegisterResult userA = userService.register(new RegisterRequest("a", "A", "a@mail.com"));
        RegisterResult userB = userService.register(new RegisterRequest("b", "B", "b@mail.com"));
        RegisterResult userC = userService.register(new RegisterRequest("c", "C", "c@mail.com"));

        CreateGameResult game1 = gameService.createGame(new CreateGameRequest(userA.authToken(), "Game1"));
        gameService.joinGame(new JoinGameRequest(userA.authToken(), ChessGame.TeamColor.BLACK.name(), game1.gameID()));

        CreateGameResult game2 = gameService.createGame(new CreateGameRequest(userB.authToken(), "Game2"));
        gameService.joinGame(new JoinGameRequest(userB.authToken(), ChessGame.TeamColor.WHITE.name(), game2.gameID()));

        CreateGameResult game3 = gameService.createGame(new CreateGameRequest(userC.authToken(), "Game3"));
        gameService.joinGame(new JoinGameRequest(userC.authToken(), ChessGame.TeamColor.WHITE.name(), game3.gameID()));
        gameService.joinGame(new JoinGameRequest(userA.authToken(), ChessGame.TeamColor.BLACK.name(), game3.gameID()));

        CreateGameResult game4 = gameService.createGame(new CreateGameRequest(userC.authToken(), "Game4"));
        gameService.joinGame(new JoinGameRequest(userC.authToken(), ChessGame.TeamColor.WHITE.name(), game4.gameID()));
        gameService.joinGame(new JoinGameRequest(userC.authToken(), ChessGame.TeamColor.BLACK.name(), game4.gameID()));

        ListGamesResult listResult = gameService.listGames(new ListGamesRequest(existingAuth));

        assertNotNull(listResult.games());
        assertEquals(4, listResult.games().size());
    }
}