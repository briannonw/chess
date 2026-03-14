package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserSqlDataAccessTests {

    private MySqlDataAccess dataAccess;
    private UserData existingUser;

    @BeforeEach
    void setUp() throws DataAccessException {
        dataAccess = new MySqlDataAccess();
        dataAccess.clear();

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@gmail.com");
    }

    @Test
    @DisplayName("Normal User Registration/Get User Success")
    public void registerGetUserSuccess() throws DataAccessException {
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
    @DisplayName("Get User Fail")
    public void getUserFail() throws DataAccessException {
        UserData result = dataAccess.getUser("fakeUser");

        assertNull(result);
    }
}
