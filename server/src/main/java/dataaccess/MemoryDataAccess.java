package dataaccess;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<Integer, GameData> games = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();

    public void clear() {
        users.clear();
        games.clear();
        authTokens.clear();
    }

    public void createUser(UserData user) throws DataAccessException {
        if (users.containsKey(user.username())) {
            throw new DataAccessException("Already taken");
        }
        users.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void createGame(GameData game) throws DataAccessException {
        if (games.containsKey(game.gameID())) {
            throw new DataAccessException("Bad request");
        }
        games.put(game.gameID(), game);
    }

    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    public void updateGame(GameData game) throws DataAccessException {
        if (!games.containsKey(game.gameID())) {
            throw new DataAccessException("Bad request");
        }
        games.put(game.gameID(), game);
    }

    public void createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
    }

    public AuthData getAuth(String authToken) {
        return authTokens.get(authToken);
    }

    public void deleteAuth(String authToken) {
        authTokens.remove(authToken);
    }
}
