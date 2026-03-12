package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.SQLException;
import java.util.List;

public class MySqlDataAccess {

    public MySqlDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
    }

    public void clear() throws DataAccessException {
        var clearAuthTokens = "DELETE FROM authTokens";
        var clearGames = "DELETE FROM games";
        var clearUsers = "DELETE FROM users";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedClearAuthTokens = conn.prepareStatement(clearAuthTokens)) {
                preparedClearAuthTokens.executeUpdate();
            }
            try (var preparedClearGames = conn.prepareStatement(clearGames)) {
                preparedClearGames.executeUpdate();
            }
            try (var preparedClearUsers = conn.prepareStatement(clearUsers)) {
                preparedClearUsers.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear", e);
        }
    }

    public void createUser(UserData user) throws DataAccessException {
        var createUser = "INSERT INTO users (username, password, email)" +
                "VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection()) {
            var preparedCreateUser = conn.prepareStatement(createUser);

            preparedCreateUser.setString(1, user.username());
            preparedCreateUser.setString(2, user.password());
            preparedCreateUser.setString(3, user.email());

            preparedCreateUser.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Already taken", e);
        }
    }

    public UserData getUser(String username) {
//        return users.get(username);
    }

    public void createGame(GameData game) throws DataAccessException {
//        if (games.containsKey(game.gameID())) {
//            throw new DataAccessException("Bad request");
//        }
//        games.put(game.gameID(), game);
    }

    public GameData getGame(int gameID) {
//        return games.get(gameID);
    }

    public List<GameData> listGames() {
//        return new ArrayList<>(games.values());
    }

    public void updateGame(GameData game) throws DataAccessException {
//        if (!games.containsKey(game.gameID())) {
//            throw new DataAccessException("Bad request");
//        }
//        games.put(game.gameID(), game);
    }

    public void createAuth(AuthData auth) {
//        authTokens.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) {
//        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) {
//        authTokens.remove(authToken);
    }

}
