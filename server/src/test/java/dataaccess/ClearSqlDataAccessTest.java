package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearSqlDataAccessTest {

    private MySqlDataAccess dataAccess;
    private UserData existingUser;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
    }

    @Test
    @DisplayName("Clear Test")
    public void clearData() throws DataAccessException {
        dataAccess.createUser(existingUser);

        AuthData auth = new AuthData("token", existingUser.username());
        dataAccess.createAuth(auth);

        GameData game = new GameData(1, null, null, "testGame", new ChessGame());
        dataAccess.createGame(game);

        dataAccess.clear();

        UserData userResult = dataAccess.getUser(existingUser.username());
        assertNull(userResult);

        AuthData authResult = dataAccess.getAuth("token");
        assertNull(authResult);

        GameData gameResult = dataAccess.getGame(1);
        assertNull(gameResult);
    }
}
