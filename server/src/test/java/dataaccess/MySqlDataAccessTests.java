package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import service.LoginRequest;
import service.RegisterRequest;
import service.RegisterResult;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlDataAccessTests {

    private MySqlDataAccess dataAccess;
    private UserData existingUser;
    private UserData newUser;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@gmail.com");
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

    @Test
    @DisplayName("Normal User Registration")
    public void registerSuccess() throws DataAccessException {
        dataAccess.createUser(existingUser);

        UserData userResult = dataAccess.getUser(existingUser.username());

        assertNotNull(userResult);
        assertEquals(existingUser.username(), userResult.username());
        assertEquals(existingUser.email(), userResult.email());
    }

    @Test
    @DisplayName("Register Bad Request")
    public void registerBadRequest() throws DataAccessException {
        dataAccess.createUser(existingUser);

        UserData duplicateUser = new UserData(existingUser.username(), existingUser.password(), existingUser.email());

        assertThrows(DataAccessException.class, () -> dataAccess.createUser(duplicateUser));
    }

    @Test
    @DisplayName("Verify User Success")
    public void verifyUserSuccess() throws DataAccessException {
        dataAccess.createUser(existingUser);

        boolean verified = dataAccess.verifyUser(existingUser.username(), existingUser.password());

        assertTrue(verified);
    }

    @Test
    @DisplayName("Verify Bad Password")
    public void loginBadRequest() throws DataAccessException {
        dataAccess.createUser(existingUser);

        boolean verified = dataAccess.verifyUser(existingUser.username(), "badPassword");

        assertFalse(verified);
    }

    @Test
    @DisplayName("Get User Success") // same as create user/register user?
    public void getUserSuccess() throws DataAccessException {
        dataAccess.createUser(existingUser);

        UserData userResult = dataAccess.getUser(existingUser.username());

        assertNotNull(userResult);
        assertEquals(existingUser.username(), userResult.username());
        assertEquals(existingUser.email(), userResult.email());
    }

    @Test
    @DisplayName("Get User Fail")
    public void getUserFail() throws DataAccessException {
        UserData result = dataAccess.getUser("fakeUser");

        assertNull(result);
    }

    // createGame pass

    // createGame fail

    // getGame pass

    // getGame fail

    // listGames pass

    // listGames fail

    // updateGame pass

    // updateGame fail

    // createAuth pass

    // createAuth fail

    // getAuth pass

    // getAuth fail

    // deleteAuth pass

    // deleteAuth fail

}