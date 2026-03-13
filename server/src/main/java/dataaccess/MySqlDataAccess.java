package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
    }

    public void clear() throws DataAccessException {
        var clearAuthTokens = "DELETE FROM authTokens";
        var clearGames = "DELETE FROM games";
        var clearUsers = "DELETE FROM users";
        var clearGameID = "ALTER TABLE games AUTO_INCREMENT = 1";

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
            try (var preparedClearGameID = conn.prepareStatement(clearGameID)) {
                preparedClearGameID.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear", e);
        }
    }

    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        var createUser = "INSERT INTO users (username, password, email)" +
                "VALUES (?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedCreateUser = conn.prepareStatement(createUser);

            preparedCreateUser.setString(1, user.username());
            preparedCreateUser.setString(2, hashedPassword);
            preparedCreateUser.setString(3, user.email());

            preparedCreateUser.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Already taken", e);
        }
    }

    public boolean verifyUser(String username, String providedPassword) throws DataAccessException {
        var verifyUser = "SELECT password " +
                "FROM users " +
                "WHERE username = ?";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedVerifyUser = conn.prepareStatement(verifyUser);
            preparedVerifyUser.setString(1, username);
            var rs = preparedVerifyUser.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                return BCrypt.checkpw(providedPassword, hashedPassword);
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to verify user", e);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        var getUser = "SELECT username, password, email " +
                "FROM users " +
                "WHERE username = ?";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedGetUser = conn.prepareStatement(getUser);
            preparedGetUser.setString(1, username);
            var rs = preparedGetUser.executeQuery();

            if (rs.next()) {
                var user = rs.getString("username");
                var password = rs.getString("password");
                var email = rs.getString("email");

                return new UserData(user, password, email);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get user", e);
        }
    }

    public void createGame(GameData game) throws DataAccessException {
        var createGame = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName, game)" +
                "VALUES (?, ?, ?, ?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedCreateGame = conn.prepareStatement(createGame);

            var gameGson = new Gson();
            String gameJson = gameGson.toJson(game.game());

            preparedCreateGame.setInt(1, game.gameID());
            preparedCreateGame.setString(2, game.whiteUsername());
            preparedCreateGame.setString(3, game.blackUsername());
            preparedCreateGame.setString(4, game.gameName());
            preparedCreateGame.setString(5, gameJson);

            preparedCreateGame.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    public GameData getGame(int gameID) throws DataAccessException {
        var getGame = "SELECT * " +
                "FROM games " +
                "WHERE gameID = ? ";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedGetGame = conn.prepareStatement(getGame);

            preparedGetGame.setInt(1, gameID);

            var rs = preparedGetGame.executeQuery();

            if (rs.next()) {
                var gameID1 = rs.getInt("gameID");
                var whiteUsername = rs.getString("whiteUsername");
                var blackUsername = rs.getString("blackUsername");
                var gameName = rs.getString("gameName");

                var gameGson = new Gson();
                ChessGame chessGame = gameGson.fromJson(rs.getString("game"), ChessGame.class);

                return new GameData(gameID1, whiteUsername, blackUsername, gameName, chessGame);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get game", e);
        }
    }

    public List<GameData> listGames() throws DataAccessException {
        var newList = new ArrayList<GameData>();
        var listGames = "SELECT * FROM games";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedListGames = conn.prepareStatement(listGames);
            var rs = preparedListGames.executeQuery();
            var gameGson = new Gson();

            while (rs.next()) {
                var gameID = rs.getInt("gameID");
                var whiteUsername = rs.getString("whiteUsername");
                var blackUsername = rs.getString("blackUsername");
                var gameName = rs.getString("gameName");

                ChessGame chessGame = gameGson.fromJson(rs.getString("game"), ChessGame.class);

                newList.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
            }
            return newList;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list games", e);
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        var updateGame = "UPDATE games " +
                "SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? " +
                "WHERE gameID = ?";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedUpdateGame = conn.prepareStatement(updateGame);

            var gameGson = new Gson();
            String gameJson = gameGson.toJson(game.game());

            preparedUpdateGame.setString(1, game.whiteUsername());
            preparedUpdateGame.setString(2, game.blackUsername());
            preparedUpdateGame.setString(3, game.gameName());
            preparedUpdateGame.setString(4, gameJson);
            preparedUpdateGame.setInt(5, game.gameID());

            preparedUpdateGame.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }

    public void createAuth(AuthData auth) throws DataAccessException {
        var createAuth = "INSERT INTO authTokens (authToken, username)" +
                "VALUES (?, ?)";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedCreateAuth = conn.prepareStatement(createAuth);

            preparedCreateAuth.setString(1, auth.authToken());
            preparedCreateAuth.setString(2, auth.username());

            preparedCreateAuth.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Failed to create auth", e);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        var getAuth = "SELECT authToken, username " +
                "FROM authTokens " +
                "WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedGetAuth = conn.prepareStatement(getAuth);

            preparedGetAuth.setString(1, authToken);

            var rs = preparedGetAuth.executeQuery();

            if (rs.next()) {
                var authToken1 = rs.getString("authToken");
                var username = rs.getString("username");

                return new AuthData(authToken1, username);
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth", e);
        }
    }

    public void deleteAuth(String authToken) throws DataAccessException{
        var deleteAuth = "DELETE FROM authTokens " +
                "WHERE authToken = ?";

        try (var conn = DatabaseManager.getConnection()) {
            var preparedDeleteAuth = conn.prepareStatement(deleteAuth);

            preparedDeleteAuth.setString(1, authToken);

            preparedDeleteAuth.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth", e);
        }
    }
}
