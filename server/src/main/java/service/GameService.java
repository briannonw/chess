package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

import java.util.ArrayList;
import java.util.List;

public class GameService {
    private final DataAccess dataAccess;
    private int lastGameID = 1;


    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(ListGamesRequest listGamesRequest) throws DataAccessException {
        AuthData auth = dataAccess.getAuth(listGamesRequest.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        List<GameData> listOfGames = dataAccess.listGames();
        List<ListGamesData> updatedListOfGames = new ArrayList<>();
        for (GameData game : listOfGames) {
            updatedListOfGames.add(new ListGamesData(
                    game.gameID(),
                    game.whiteUsername(),
                    game.blackUsername(),
                    game.gameName()
            ));
        }
        return new ListGamesResult(updatedListOfGames);
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
        if (createGameRequest.gameName() == null || createGameRequest.gameName().isBlank()) {
            throw new DataAccessException("Bad request");
        }
        AuthData auth = dataAccess.getAuth(createGameRequest.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        int gameID = lastGameID++;
        GameData gameData = new GameData(
                gameID,
                null,
                null,
                createGameRequest.gameName(),
                new ChessGame()
        );
        dataAccess.createGame(gameData);
        return new CreateGameResult(gameID);
    }

    public void joinGame(JoinGameRequest joinGameRequest) throws DataAccessException {
        if (joinGameRequest.playerColor() == null || joinGameRequest.playerColor().isBlank()) {
            throw new DataAccessException("Bad request");
        }

        AuthData auth = dataAccess.getAuth(joinGameRequest.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        GameData game = dataAccess.getGame(joinGameRequest.gameID());
        if (game == null) {
            throw new DataAccessException("Bad request");
        }
        String color = joinGameRequest.playerColor();
        String username = auth.username();
        GameData updatedGame = updateGame(game, color, username);
        dataAccess.updateGame(updatedGame);
    }

    private GameData updateGame(GameData game, String color, String username) throws DataAccessException {
        if (color.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Already taken");
            }
            return new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if (color.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Already taken");
            }
            return new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else {
            throw new DataAccessException("Bad request");
        }
    }
}
