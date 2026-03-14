package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AuthSqlDataAccessTests {

    private MySqlDataAccess dataAccess;
    private UserData existingUser;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
    }

    @Test
    @DisplayName("Create/Get Auth Success")
    public void createGetAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token", existingUser.username());
        dataAccess.createAuth(auth);

        AuthData authResult = dataAccess.getAuth("token");
        assertNotNull(authResult);
        assertEquals("token", authResult.authToken());
        assertEquals(existingUser.username(), authResult.username());
    }

    @Test
    @DisplayName("Create Auth Fail")
    public void createAuthFail() throws DataAccessException {
        AuthData auth = new AuthData("token", existingUser.username());
        dataAccess.createAuth(auth);

        AuthData duplicateAuth = new AuthData("token", "Duplicate Auth");

        Assertions.assertThrows(DataAccessException.class, () -> {dataAccess.createAuth(duplicateAuth);});
    }

    @Test
    @DisplayName("Get Auth Fail")
    public void getAuthFail() throws DataAccessException {
        AuthData authResult = dataAccess.getAuth("badToken");
        assertNull(authResult);
    }

    @Test
    @DisplayName("Delete Auth Success")
    public void deleteAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token", existingUser.username());
        dataAccess.createAuth(auth);

        dataAccess.deleteAuth("token");

        AuthData authResult = dataAccess.getAuth("token");
        assertNull(authResult);
    }

    @Test
    @DisplayName("Delete Auth Fail")
    public void deleteAuthFail() throws DataAccessException {
        Assertions.assertDoesNotThrow(() -> dataAccess.deleteAuth("noToken"));
    }
}
