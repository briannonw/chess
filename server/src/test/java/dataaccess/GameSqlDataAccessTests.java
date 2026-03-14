package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GameSqlDataAccessTests {

    private MySqlDataAccess dataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();
    }

    @Test
    @DisplayName("Valid Creation")
    public void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "TestGame", new ChessGame());

        dataAccess.createGame(game);

        GameData result = dataAccess.getGame(1);

        assertNotNull(result);
        assertEquals(1, result.gameID());
        assertEquals("TestGame", result.gameName());
    }

    @Test
    @DisplayName("Create Game Bad Request")
    public void createGameBadRequest() {
        GameData game = new GameData(1, null, null, null, new ChessGame());

        assertThrows(DataAccessException.class, () -> dataAccess.createGame(game));
    }

    @Test
    @DisplayName("Get Game Success")
    public void getGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        dataAccess.createGame(game);

        GameData result = dataAccess.getGame(1);

        assertNotNull(result);
        assertEquals(1, result.gameID());
        assertEquals("Test Game", result.gameName());
    }

    @Test
    @DisplayName("Get Game Fail")
    public void getGameFail() throws DataAccessException {
        GameData result = dataAccess.getGame(-1);

        assertNull(result);
    }

    @Test
    @DisplayName("List Games Success")
    public void listGamesSuccess() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Test Game 1", new ChessGame());
        dataAccess.createGame(game1);

        GameData game2 = new GameData(2, null, null, "Test Game 2", new ChessGame());
        dataAccess.createGame(game2);

        var games = dataAccess.listGames();

        assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List Games Empty")
    public void listGamesFail() throws DataAccessException {
        var games = dataAccess.listGames();

        assertEquals(0, games.size());
    }

    @Test
    @DisplayName("Update Game Success")
    public void updateGameSuccess() throws DataAccessException {
        GameData game = new GameData(1, null, null, "Test Game", new ChessGame());
        dataAccess.createGame(game);

        GameData updatedGame = new GameData(1, "White", null, "Updated Game", game.game());
        dataAccess.updateGame(updatedGame);

        GameData result = dataAccess.getGame(1);
        assertNotNull(result);
        assertEquals("Updated Game", result.gameName());
        assertEquals("White", result.whiteUsername());
        assertNull(result.blackUsername());
    }

    @Test
    @DisplayName("Update Game Fail")
    public void updateGameFail() throws DataAccessException {
        GameData game = new GameData(-1, null, null, "Test Game", new ChessGame());

        dataAccess.updateGame(game);

        GameData result = dataAccess.getGame(-1);
        assertNull(result);
    }
}
