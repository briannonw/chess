package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.*;

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
        return new ListGamesResult(dataAccess.listGames());
    }

    public CreateGameResult createGame(CreateGameRequest createGameRequest) throws DataAccessException {
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
        AuthData auth = dataAccess.getAuth(joinGameRequest.authToken());
        if (auth == null) {
            throw new DataAccessException("Unauthorized");
        }
        GameData game = dataAccess.getGame(joinGameRequest.gameID());
        if (game == null) {
            throw new DataAccessException("Game doesn't exist");
        }
        String color = joinGameRequest.playerColor();
        String username = auth.username();
        GameData updatedGame;
        if (color == null) {
            return;
        } else if (color.equals("WHITE")) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("White taken");
            } else {
                updatedGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            }
        } else if (color.equals("BLACK")) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Black taken");
            } else {
                updatedGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            }
        } else {
            throw new DataAccessException("Invalid color");
        }
        dataAccess.updateGame(updatedGame);
    }
}
